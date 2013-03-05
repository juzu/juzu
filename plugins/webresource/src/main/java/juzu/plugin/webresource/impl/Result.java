/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package juzu.plugin.webresource.impl;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 2/25/13
 */
public abstract class Result<T extends CharSequence> {

    public String name;

    public ResultType type;

    public Result(String name, ResultType type) {
        this.name = name;
        this.type = type;
    }

    public abstract T getContent();

    public static class MergedResult extends Result<StringBuilder> {

        private StringBuilder mergedContent;

        public MergedResult(String name, ResultType type, StringBuilder mergedContent) {
            super(name, type);
            this.mergedContent = mergedContent;
        }

        @Override
        public StringBuilder getContent() {
            return mergedContent;
        }
    }

    public static class ErrorResult extends Result<String> {

        private String message;

        private Throwable cause;

        private String error;

        public ErrorResult(String name, String message) {
            this(name, message, null);
        }

        public ErrorResult(String name, String message, Throwable cause) {
            super(name, ResultType.ERROR);
            this.message = message;
            this.cause = cause;
        }

        @Override
        public String getContent() {
            if (error == null) {
                if (cause == null) {
                    error = message;
                } else {
                    StringWriter w = new StringWriter();
                    w.append(message).append('\n');

                    PrintWriter printWriter = new PrintWriter(w);
                    try {
                        cause.printStackTrace(printWriter);
                        error = w.getBuffer().toString();
                    } finally {
                        printWriter.close();
                    }
                }
            }
            return error;
        }
    }
}
