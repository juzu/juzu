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
package juzu.plugin.webresource.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 2/19/13
 */
@Retention(RetentionPolicy.SOURCE)
@Target({})
public @interface Group {

    /**
     * Name of group, it is used to name merged files
     *
     * @return
     */
    String name();

    /**
     * Array of JavaScript resources bundled in current group
     *
     * @return
     */
    JavaScript[] javascripts() default {};

    /**
     * Array of Css resources bundled in current group
     *
     * @return
     */
    Css[] stylesheets() default {};

    /**
     * Array of Less resources bundled in current group
     *
     * @return
     */
    Less[] lesses() default {};

    /**
     * Flag indicating if pre-processing of resource files is done by multiple thread
     *
     * @return
     */
    boolean parallelProcess() default false;

}
