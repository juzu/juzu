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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 2/19/13
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PACKAGE)
public @interface Groups {


    /**
     * Relative path to target directory containing merged web resource files
     *
     * @return
     */
    String targetDir() default "wro4j";

    /**
     * Array of web resource groups defined in current application
     *
     * @return
     */
    Group[] groups() default {};

    /**
     * Array of ResourcePreProcessor which are applied to resource files belonging
     * to the same Group before merging them.
     *
     * @return
     */
    Class<? extends ResourcePreProcessor>[] preProcessors() default {};

    /**
     * Array of ResourcePostProcessor which are applied to merged resource files
     *
     * @return
     */
    Class<? extends ResourcePostProcessor>[] postProcessors() default {};
}
