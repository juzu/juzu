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

package juzu.plugin.upload.impl;

import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.PluginContext;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.request.ContextualParameter;
import juzu.impl.request.Parameter;
import juzu.impl.request.PhaseParameter;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.request.ActionContext;
import juzu.request.ClientContext;
import juzu.request.RequestContext;
import juzu.request.ResourceContext;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class UploadPlugin extends ApplicationPlugin implements RequestFilter {

  public UploadPlugin() {
    super("upload");
  }

  @Override
  public Descriptor init(PluginContext context) throws Exception {
    // We always load the plugin
    return new Descriptor();
  }

  public void invoke(Request request) {

    //
    RequestContext context = request.getContext();

    //
    final ClientContext clientContext;
    if (context instanceof ActionContext) {
      clientContext = ((ActionContext)context).getClientContext();
    } else if (context instanceof ResourceContext) {
      clientContext = ((ResourceContext)context).getClientContext();
    } else {
      clientContext = null;
    }

    //
    if (clientContext != null) {
      String contentType = clientContext.getContentType();
      if (contentType != null) {
        if (contentType.startsWith("multipart/")) {

          //
          org.apache.commons.fileupload.RequestContext ctx = new org.apache.commons.fileupload.RequestContext() {
            public String getCharacterEncoding() {
              return clientContext.getCharacterEncoding();
            }
            public String getContentType() {
              return clientContext.getContentType();
            }
            public int getContentLength() {
              return clientContext.getContentLenth();
            }
            public InputStream getInputStream() throws IOException {
              return clientContext.getInputStream();
            }
          };

          //
          FileUpload upload = new FileUpload(new DiskFileItemFactory());

          //
          try {
            List<FileItem> list = (List<FileItem>)upload.parseRequest(ctx);
            for (FileItem file : list) {
              String name = file.getFieldName();
              Parameter parameter = request.getContext().getMethod().getParameter(name);
              if (file.isFormField()) {
                if (parameter instanceof PhaseParameter) {
                  request.setArgument(parameter, file.getString());
                }
              } else {
                if (parameter instanceof ContextualParameter && FileItem.class.isAssignableFrom(parameter.getType())) {
                  request.setArgument(parameter, file);
                }
              }
            }
          }
          catch (FileUploadException e) {
            throw new UndeclaredThrowableException(e);
          }
        }
      }
    }

    //
    request.invoke();
  }
}
