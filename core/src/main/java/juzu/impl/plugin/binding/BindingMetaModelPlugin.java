/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.plugin.binding;

import juzu.impl.common.Name;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.MessageCode;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.common.JSON;
import juzu.inject.ProviderFactory;
import juzu.plugin.binding.Bindings;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BindingMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  public static final MessageCode IMPLEMENTATION_NOT_ASSIGNABLE =
    new MessageCode(
      "BINDING_IMPLEMENTATION_NOT_ASSIGNABLE",
      "The binding implementation type %1$s does not extend or implement the %2$s type");

  /** . */
  public static final MessageCode PROVIDER_NOT_ASSIGNABLE =
    new MessageCode(
      "BINDING_PROVIDER_NOT_ASSIGNABLE",
      "The binding implementation type %1$s must provides a type %2$s that extends the %3$s type");

  /** . */
  public static final MessageCode IMPLEMENTATION_NOT_ABSTRACT =
    new MessageCode(
      "BINDING_PROVIDER_FACTORY_NOT_ABSTRACT",
      "The binding implementation provider factory %1$s must not be abstract");

  /** . */
  public static final MessageCode PROVIDER_FACTORY_NOT_PUBLIC =
    new MessageCode(
      "BINDING_IMPLEMENTATION_NOT_PUBLIC",
      "The binding implementation provider factory %1$s must be public");

  /** . */
  public static final MessageCode IMPLEMENTATION_INVALID_TYPE =
    new MessageCode(
      "BINDING_IMPLEMENTATION_INVALID_TYPE",
      "The binding implementation provider factory %1$s must be a class");

  /** . */
  public static final MessageCode PROVIDER_FACTORY_NO_ZERO_ARG_CTOR =
    new MessageCode(
      "BINDING_PROVIDER_FACTORY_NO_ZERO_ARG_CTOR",
      "The binding implementation provider factory %1$s must provides a public zero argument constructor");

  /** . */
  public static final MessageCode PROVIDER_FACTORY_NO_PUBLIC_CTOR =
    new MessageCode(
      "BINDING_PROVIDER_FACTORY_NO_PUBLIC_CTOR",
      "The binding implementation provider factory %1$s must provides a public constructor");

  /** . */
  private static final Name BINDINGS = Name.create(Bindings.class);

  /** . */
  private Map<ElementHandle.Package, JSON> state = new HashMap<ElementHandle.Package, JSON>();

  public BindingMetaModelPlugin() {
    super("binding");
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends java.lang.annotation.Annotation>>singleton(Bindings.class);
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    ProcessingContext env = metaModel.model.processingContext;

    //
    TypeMirror providerFactoryTM = env.getTypeElement(ProviderFactory.class.getName()).asType();
    TypeElement providerElt = env.getTypeElement("javax.inject.Provider");
    DeclaredType providerTM = (DeclaredType)providerElt.asType();
    TypeMirror rawProviderTM = env.erasure(providerTM);

    //
    List<Map<String, Object>> bindings = (List<Map<String, Object>>)added.get("value");
    ArrayList<JSON> list = new ArrayList<JSON>();
    if (bindings != null) {
      for (Map<String, Object> binding : bindings) {
        ElementHandle.Class bindingValue = (ElementHandle.Class)binding.get("value");
        ElementHandle.Class bindingImplementation = (ElementHandle.Class)binding.get("implementation");
        String scope = (String)binding.get("scope");

        //
        JSON bindingJSON = new JSON().set("value", bindingValue.getFQN().toString());

        //
        TypeElement valueElt = env.get(bindingValue);
        TypeMirror valueTM = valueElt.asType();

        //
        if (bindingImplementation != null) {
          TypeElement implementationElt = env.get(bindingImplementation);
          DeclaredType implementationTM = (DeclaredType)implementationElt.asType();

          // Check class
          if (implementationElt.getKind() != ElementKind.CLASS) {
            throw IMPLEMENTATION_INVALID_TYPE.failure(env.get(key.getElement()), providerElt.getQualifiedName());
          }

          //
          Set<Modifier> modifiers = implementationElt.getModifiers();

          // Check not abstract
          if (modifiers.contains(Modifier.ABSTRACT)) {
            throw IMPLEMENTATION_NOT_ABSTRACT.failure(env.get(key.getElement()), implementationElt.getQualifiedName());
          }

          //
          if (env.isAssignable(implementationTM, providerFactoryTM)) {
            // Check public
            if (!modifiers.contains(Modifier.PUBLIC)) {
              throw PROVIDER_FACTORY_NOT_PUBLIC.failure(env.get(key.getElement()), implementationElt.getQualifiedName());
            }

            // Find zero arg constructor
            ExecutableElement emptyCtor = null;
            for (ExecutableElement ctorElt : ElementFilter.constructorsIn(env.getAllMembers(implementationElt))) {
              if (ctorElt.getParameters().isEmpty()) {
                emptyCtor = ctorElt;
                break;
              }
            }

            // Validate constructor
            if (emptyCtor == null) {
              throw PROVIDER_FACTORY_NO_ZERO_ARG_CTOR.failure(env.get(key.getElement()), implementationElt.getQualifiedName());
            }
            if (!emptyCtor.getModifiers().contains(Modifier.PUBLIC)) {
              throw PROVIDER_FACTORY_NO_PUBLIC_CTOR.failure(env.get(key.getElement()), implementationElt.getQualifiedName());
            }
          }
          else if (env.isAssignable(implementationTM, rawProviderTM)) {
            TypeVariable T = (TypeVariable)providerTM.getTypeArguments().get(0);
            TypeMirror resolved = env.asMemberOf(implementationTM, T.asElement());
            if (env.isAssignable(resolved, valueTM)) {
              // OK
            }
            else {
              throw PROVIDER_NOT_ASSIGNABLE.failure(
                  env.get(key.getElement()),
                implementationElt.getQualifiedName(),
                resolved,
                valueElt.getQualifiedName());
            }
          }
          else if (env.isAssignable(implementationTM, valueTM)) {
            // OK
          }
          else {
            throw IMPLEMENTATION_NOT_ASSIGNABLE.failure(
                env.get(key.getElement()),
              implementationElt.getQualifiedName(),
              valueElt.getQualifiedName());
          }

          //
          bindingJSON.set("implementation", bindingImplementation.getFQN().toString());
        }

        // Add the declared scope if any
        if (scope != null) {
          bindingJSON.set("scope", scope);
        }

        //
        list.add(bindingJSON);
      }
    }

    //
    state.put(metaModel.getHandle(), new JSON().set("bindings", list));
  }

  @Override
  public void destroy(ApplicationMetaModel application) {
    state.remove(application.getHandle());
  }

  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {
    return state.get(application.getHandle());
  }
}
