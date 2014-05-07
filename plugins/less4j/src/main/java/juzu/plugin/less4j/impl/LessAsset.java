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
package juzu.plugin.less4j.impl;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ThreadUnsafeLessCompiler;
import juzu.asset.AssetLocation;
import juzu.impl.compiler.Message;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.plugin.asset.Asset;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Julien Viet
 */
public class LessAsset extends Asset {

  private static String cssValue(String lessValue) {
    int pos = lessValue.lastIndexOf('.');
    if (pos == -1) {
      return lessValue + ".css";
    } else {
      return lessValue.substring(0, pos) + ".css";
    }
  }

  /** . */
  private final String lessValue;

  public LessAsset(
      String id,
      String lessValue,
      List<String> depends,
      Integer maxAge) {
    super(id, "stylesheet", cssValue(lessValue), null, depends, AssetLocation.APPLICATION, maxAge, null);

    //
    this.lessValue = lessValue;
  }

  @Override
  protected String getSource() {
    return lessValue;
  }

  @Override
  public InputStream open(String s, URLConnection resource) throws IOException {
    LessCompiler compiler = new ThreadUnsafeLessCompiler();
    try {
      LessCompiler.CompilationResult result = compiler.compile(resource.getURL());
      return new ByteArrayInputStream(result.getCss().getBytes());
    }
    catch (Less4jException e) {
      List<LessCompiler.Problem> errors = e.getErrors();
      ArrayList<Message> messages = new ArrayList<Message>(errors.size());
      for (LessCompiler.Problem error : errors) {
        String text = error.getMessage() != null ? error.getMessage() : "There is an error in your .less file";
        String errorName = error.getType().name();
        LessSource source = error.getSource();
        Message msg;
        if (source != null) {
          msg = new Message(MetaModelPluginImpl.COMPILATION_ERROR, errorName, source.getName(), error.getLine(), text);
        } else {
          msg = new Message(MetaModelPluginImpl.GENERAL_PROBLEM, errorName, text);
        }
        MetaModelPluginImpl.log.info(msg.toDisplayString());
        messages.add(msg);
      }
      throw new ProcessingException(messages);
    }
  }
}
