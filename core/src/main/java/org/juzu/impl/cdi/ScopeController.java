/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.juzu.impl.cdi;

import org.juzu.ActionScoped;
import org.juzu.FlashScoped;
import org.juzu.MimeScoped;
import org.juzu.RenderScoped;
import org.juzu.ResourceScoped;
import org.juzu.impl.request.Request;
import org.juzu.impl.request.Scope;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScopeController
{

   /** . */
   static final ScopeController INSTANCE = new ScopeController();

   /** . */
   final ContextImpl flashContext = new ContextImpl(this, Scope.FLASH, FlashScoped.class);

   /** . */
   final ContextImpl requestContext = new ContextImpl(this, Scope.REQUEST, RequestScoped.class);

   /** . */
   final ContextImpl actionContext = new ContextImpl(this, Scope.ACTION, ActionScoped.class);

   /** . */
   final ContextImpl renderContext = new ContextImpl(this, Scope.RENDER, RenderScoped.class);

   /** . */
   final ContextImpl resourceContext = new ContextImpl(this, Scope.RESOURCE, ResourceScoped.class);

   /** . */
   final ContextImpl mimeContext = new ContextImpl(this, Scope.MIME, MimeScoped.class);

   /** . */
   final ContextImpl sessionContext = new ContextImpl(this, Scope.SESSION, SessionScoped.class);

   /** . */
   final ThreadLocal<Request> currentRequest = new ThreadLocal<Request>();

   public static void begin(Request context) throws IllegalStateException
   {
      if (context == null)
      {
         throw new NullPointerException();
      }
      if (INSTANCE.currentRequest.get() != null)
      {
         throw new IllegalStateException("Already started");
      }
      INSTANCE.currentRequest.set(context);
   }

   public static void end()
   {
      INSTANCE.currentRequest.set(null);
   }

   public <T> T get(Scope scope, Contextual<T> contextual, CreationalContext<T> creationalContext)
   {
      Request req = currentRequest.get();
      if (req == null)
      {
         throw new ContextNotActiveException();
      }
      if (!scope.isActive(req))
      {
         throw new ContextNotActiveException();
      }
      Object o = req.getContextualValue(scope, contextual);
      if (o == null)
      {
         if (creationalContext != null)
         {
            o = contextual.create(creationalContext);
            req.setContextualValue(scope, contextual, o);
         }
      }
      return (T)o;
   }

   public boolean isActive(Scope scope)
   {
      Request req = currentRequest.get();
      if (req == null)
      {
         throw new ContextNotActiveException();
      }
      return scope.isActive(req);
   }
}
