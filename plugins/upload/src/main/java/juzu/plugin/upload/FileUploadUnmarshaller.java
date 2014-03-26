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
package juzu.plugin.upload;

import juzu.impl.request.ContextualParameter;
import juzu.impl.request.EntityUnmarshaller;
import juzu.request.ClientContext;
import juzu.request.RequestParameter;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Julien Viet
 */
public class FileUploadUnmarshaller extends EntityUnmarshaller {

  @Override
  public boolean accept(String mediaType) {
    return mediaType.startsWith("multipart/");
  }

  @Override
  public void unmarshall(
      String mediaType,
      final ClientContext context,
      Iterable<Map.Entry<ContextualParameter, Object>> contextualArguments,
      Map<String, RequestParameter> parameterArguments) throws IOException {

    org.apache.commons.fileupload.RequestContext ctx = new org.apache.commons.fileupload.RequestContext() {
      public String getCharacterEncoding() {
        return context.getCharacterEncoding();
      }
      public String getContentType() {
        return context.getContentType();
      }
      public int getContentLength() {
        return context.getContentLenth();
      }
      public InputStream getInputStream() throws IOException {
        return context.getInputStream();
      }
    };

    //
    FileUpload upload = new FileUpload(new DiskFileItemFactory());
    try {
      List<FileItem> list = (List<FileItem>)upload.parseRequest(ctx);
      HashMap<String, FileItem> files = new HashMap<String, FileItem>();
      for (FileItem file : list) {
        String name = file.getFieldName();
        if (file.isFormField()) {
          RequestParameter parameterArg = parameterArguments.get(name);
          if (parameterArg == null) {
            parameterArguments.put(name, RequestParameter.create(name, file.getString()));
          } else {
            parameterArguments.put(name, parameterArg.append(new String[]{file.getString()}));
          }
        } else {
          files.put(name, file);
        }
      }
      for (Map.Entry<ContextualParameter, Object> argument : contextualArguments) {
        ContextualParameter contextualParam = argument.getKey();
        FileItem file = files.get(contextualParam.getName());
        if (file != null && FileItem.class.isAssignableFrom(contextualParam.getType())) {
          argument.setValue(file);
        }
      }
    }
    catch (FileUploadException e) {
      throw new IOException(e);
    }
  }
}
