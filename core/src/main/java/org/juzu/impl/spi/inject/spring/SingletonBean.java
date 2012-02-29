package org.juzu.impl.spi.inject.spring;

import org.springframework.beans.factory.support.AutowireCandidateQualifier;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class SingletonBean
{

   final Object instance;

   final List<AutowireCandidateQualifier> qualifiers;

   SingletonBean(Object instance, List<AutowireCandidateQualifier> qualifiers)
   {
      this.instance = instance;
      this.qualifiers = qualifiers;
   }
}
