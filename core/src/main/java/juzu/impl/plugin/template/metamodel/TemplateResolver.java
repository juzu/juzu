/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu.impl.plugin.template.metamodel;

import juzu.impl.common.Name;
import juzu.impl.common.FileKey;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.compiler.BaseProcessor;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.template.spi.EmitContext;
import juzu.impl.template.spi.TemplateProvider;
import juzu.impl.template.spi.Template;
import juzu.impl.plugin.template.TemplatePlugin;
import juzu.impl.plugin.template.metadata.TemplateDescriptor;
import juzu.impl.common.Logger;
import juzu.impl.common.Path;
import juzu.impl.common.Tools;

import javax.annotation.Generated;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * The template repository.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TemplateResolver implements Serializable {

  /** . */
  private static final Logger log = BaseProcessor.getLogger(TemplateResolver.class);

  /** . */
  private final ApplicationMetaModel application;

  /** . */
  private Map<Path, Template<?>> templates;

  /** . */
  private Set<Path> emitted;

  /** . */
  private Map<FileKey, FileObject> stubCache;

  /** . */
  private Map<FileKey, FileObject> classCache;

  public TemplateResolver(ApplicationMetaModel application) {
    if (application == null) {
      throw new NullPointerException();
    }

    //
    this.application = application;
    this.templates = new HashMap<Path, Template<?>>();
    this.emitted = new HashSet<Path>();
    this.stubCache = new HashMap<FileKey, FileObject>();
    this.classCache = new HashMap<FileKey, FileObject>();
  }

  public Collection<Template<?>> getTemplates() {
    return templates.values();
  }

  public void removeTemplate(Path path) {
    // Shall we do something else ?
    templates.remove(path);
  }

  public void prePassivate() {
    log.log("Evicting cache " + emitted);
    emitted.clear();
    stubCache.clear();
    classCache.clear();
  }

  public void process(TemplateMetaModelPlugin plugin, ProcessingContext context) throws ProcessingException {

    //
    TemplatesMetaModel metaModel = application.getChild(TemplatesMetaModel.KEY);

    // Evict templates that are out of date
    log.log("Synchronizing existing templates " + templates.keySet());
    for (Iterator<Template<?>> i = templates.values().iterator();i.hasNext();) {
      Template<?> template = i.next();
      FileKey absolute = metaModel.resolve(template.getPath());
      FileObject resource = context.resolveResource(application.getHandle(), absolute);
      if (resource == null) {
        // That will generate a template not found error
        i.remove();
        log.log("Detected template removal " + template.getPath());
      }
      else if (resource.getLastModified() > template.getLastModified()) {
        // That will force the regeneration of the template
        i.remove();
        log.log("Detected stale template " + template.getPath());
      }
      else {
        log.log("Template " + template.getPath() + " is valid");
      }
    }

    // Build missing templates
    log.log("Building missing templates");
    Map<Path, Template<?>> copy = new HashMap<Path, Template<?>>(templates);
    for (TemplateMetaModel templateMeta : metaModel) {
      Template<?> template = copy.get(templateMeta.getPath());
      if (template == null) {
        log.log("Compiling template " + templateMeta.getPath());
        ModelTemplateProcessContext compiler = new ModelTemplateProcessContext(templateMeta, new HashMap<Path, Template<?>>(copy), context);
        Collection<Template<?>> resolved = compiler.resolve(templateMeta);
        for (Template<?> added : resolved) {
          copy.put(added.getPath(), added);
        }
      }
    }
    templates = copy;

    // Generate missing files from template
    for (Template<?> template : templates.values()) {
      //
      Path originPath = template.getOriginPath();
      TemplateMetaModel templateMeta = metaModel.get(originPath);

      //
      // We compute the class elements from the field elements (as eclipse will make the relationship)
      Set<Name> types = new LinkedHashSet<Name>();
      for (TemplateRefMetaModel ref : templateMeta.getRefs()) {
        ElementHandle.Field handle = ref.getHandle();
        types.add(handle.getFQN());
      }
      final Element[] elements = new Element[types.size()];
      int index = 0;
      for (Name type : types) {
        elements[index++] = context.getTypeElement(type.getIdentifier());
      }

      // Resolve the stub
      resolveStub(template, plugin, context, elements);

      // Resolve the qualified class
      resolvedQualified(template, context, elements);

      //
      resolveScript(template, plugin, context, elements);
    }
  }

  private <M extends Serializable> void resolveScript(final Template<M> template, final TemplateMetaModelPlugin plugin, final ProcessingContext context, final Element[] elements) {
    context.executeWithin(elements[0], new Callable<Void>() {
      public Void call() throws Exception {

        //
        TemplatesMetaModel metaModel = application.getChild(TemplatesMetaModel.KEY);

        // If CCE that would mean there is an internal bug
        TemplateProvider<M> provider = (TemplateProvider<M>)plugin.providers.get(template.getPath().getExt());

        // If it's the cache we do nothing
        if (!emitted.contains(template.getPath())) {
          //
          try {
            M model = template.getModel();
            EmitContext emitCtx = new EmitContext();

            //
            CharSequence res = provider.emit(emitCtx, model);

            //
            if (res != null) {
              //
              FileKey absolute = metaModel.resolve(template.getPath().as(provider.getTargetExtension()));
              FileObject scriptFile = context.createResource(StandardLocation.CLASS_OUTPUT, absolute, elements);
              Writer writer = null;
              try {
                writer = scriptFile.openWriter();
                writer.write(res.toString());
              }
              finally {
                Tools.safeClose(writer);
              }

              //
              log.log("Generated template script " + template.getPath() + " as " + scriptFile.toUri() +
                " with originating elements " + Arrays.asList(elements));
            }

            // Put it in cache
            emitted.add(template.getPath());
          }
          catch (IOException e) {
            throw TemplateMetaModel.CANNOT_WRITE_TEMPLATE_SCRIPT.failure(e, template.getPath());
          }
        }
        else {
          log.log("Template " + template.getPath() + " was found in cache");
        }

        //
        return null;
      }
    });
  }

  private <M extends Serializable> void resolvedQualified(Template<M> template, ProcessingContext context, Element[] elements) {

    //
    TemplatesMetaModel metaModel = application.getChild(TemplatesMetaModel.KEY);

    //
    Path path = template.getPath();
    FileKey absolute = metaModel.resolve(path);
    if (classCache.containsKey(absolute)) {
      log.log("Template class " + path + " was found in cache");
      return;
    }

    //
    Writer writer = null;
    try {
      // Template qualified class
      FileObject classFile = context.createSourceFile(absolute.fqn, elements);
      writer = classFile.openWriter();
      writer.append("package ").append(absolute.packageFQN).append(";\n");
      writer.append("import ").append(TemplateDescriptor.class.getCanonicalName()).append(";\n");
      writer.append("import ").append(TemplatePlugin.class.getCanonicalName()).append(";\n");
      writer.append("@").append(Generated.class.getName()).append("({})\n");
      writer.append("@").append(juzu.Path.class.getName()).append("(\"").append(path.getValue()).append("\")\n");
      writer.append("public class ").append(path.getRawName()).append(" extends ").append(juzu.template.Template.class.getName()).append("\n");
      writer.append("{\n");
      writer.append("@javax.inject.Inject\n");
      writer.append("public ").append(path.getRawName()).append("(").
        append(TemplatePlugin.class.getSimpleName()).append(" templatePlugin").
        append(")\n");
      writer.append("{\n");
      writer.append("super(templatePlugin, \"").append(path.getValue()).append("\");\n");
      writer.append("}\n");

      //
      writer.
          append("public static final ").
          append(TemplateDescriptor.class.getName()).
          append(" DESCRIPTOR = new ").
          append(TemplateDescriptor.class.getName()).
          append("(").append(absolute.fqn).append(".class);\n");

      //
      String baseBuilderName = juzu.template.Template.Builder.class.getCanonicalName();
      if (template.getParameters() != null) {
        // Implement abstract method with this class Builder covariant return type
        writer.append("public Builder with() {\n");
        writer.append("return new Builder();\n");
        writer.append("}\n");

        // Setters on builders
        writer.append("public class Builder extends ").append(baseBuilderName).append("\n");
        writer.append("{\n");
        for (String paramName : template.getParameters()) {
          writer.append("public Builder ").append(paramName).append("(Object ").append(paramName).append(") {\n");
          writer.append("set(\"").append(paramName).append("\",").append(paramName).append(");\n");
          writer.append("return this;\n");
          writer.append(("}\n"));
        }
        writer.append("}\n");
      }
      else {
        // Implement abstract method
        writer.append("public ").append(baseBuilderName).append(" with() {\n");
        writer.append("return new ").append(baseBuilderName).append("();\n");
        writer.append("}\n");
      }

      // Close class
      writer.append("}\n");

      //
      classCache.put(absolute, classFile);

      //
      log.log("Generated template class " + path + " as " + classFile.toUri() +
        " with originating elements " + Arrays.asList(elements));
    }
    catch (IOException e) {
      throw TemplateMetaModel.CANNOT_WRITE_TEMPLATE_CLASS.failure(e, elements[0], path);
    }
    finally {
      Tools.safeClose(writer);
    }
  }

  private void resolveStub(Template<?> template, TemplateMetaModelPlugin plugin, ProcessingContext context, Element[] elements) {

    //
    TemplatesMetaModel metaModel = application.getChild(TemplatesMetaModel.KEY);
    FileKey absolute = metaModel.resolve(template.getPath());

    //
    if (stubCache.containsKey(absolute)) {
      log.log("Template strub " + template.getPath() + " was found in cache");
      return;
    }

    //
    Name stubFQN = Name.parse(absolute.fqn + "_");
    TemplateProvider provider = plugin.providers.get(template.getPath().getExt());
    Writer writer = null;
    try {
      // Template stub
      JavaFileObject stubFile = context.createSourceFile(stubFQN, elements);
      writer = stubFile.openWriter();
      writer.append("package ").append(stubFQN.getParent()).append(";\n");
      writer.append("import ").append(Generated.class.getCanonicalName()).append(";\n");
      writer.append("@Generated({\"").append(stubFQN).append("\"})\n");
      writer.append("public class ").append(stubFQN.getIdentifier()).append(" extends ").append(provider.getTemplateStubType().getName()).append(" {\n");
      writer.append("}");

      //
      stubCache.put(absolute, stubFile);

      //
      log.log("Generating template stub " + stubFQN + " as " + stubFile.toUri() +
        " with originating elements " + Arrays.asList(elements));
    }
    catch (IOException e) {
      throw TemplateMetaModel.CANNOT_WRITE_TEMPLATE_STUB.failure(e, elements[0], template.getPath());
    }
    finally {
      Tools.safeClose(writer);
    }
  }
}
