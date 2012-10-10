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

package juzu.plugin.upload.impl;

import juzu.impl.common.JSON;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.application.ApplicationException;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.request.ContextualParameter;
import juzu.impl.request.Parameter;
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
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class UploadPlugin extends ApplicationPlugin implements RequestFilter {

  public UploadPlugin() {
    super("upload");
  }

  @Override
  public Descriptor init(ApplicationDescriptor application, JSON config) throws Exception {
    // We always load the plugin
    return new Descriptor();
  }

  public void invoke(Request request) throws ApplicationException {

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
              if (parameter instanceof ContextualParameter) {
                if (FileItem.class.isAssignableFrom(parameter.getType())) {
                  request.setArgument(parameter, file);
                }
              }
            }
          }
          catch (FileUploadException e) {
            throw new ApplicationException(e);
          }
        }
      }
    }

    //
    request.invoke();
  }
}
