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
package examples.plugin.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import juzu.Response;
import juzu.Scope;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.plugin.ServiceContext;
import juzu.impl.plugin.ServiceDescriptor;
import juzu.impl.plugin.application.ApplicationService;
import juzu.impl.request.RequestFilter;
import juzu.impl.request.Stage;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Julien Viet
 */
public class MetricsService extends ApplicationService implements RequestFilter<Stage.Invoke> {

  /** . */
  private final MetricRegistry registry;

  /** . */
  private final Timer responses;

  // tag::constructor[]
  public MetricsService() {
    super("metrics");
    registry = new MetricRegistry();
    responses = registry.timer("juzu.responses");
  }
  // end::constructor[]

  // tag::init[]
  @Override
  public ServiceDescriptor init(ServiceContext context) throws Exception {
    Provider<MetricRegistry> registryProvider = new Provider<MetricRegistry>() {
      @Override
      public MetricRegistry get() {
        return registry;
      }
    }; //<1>
    BeanDescriptor registryDescriptor = BeanDescriptor.createFromProvider(  //<2>
        MetricRegistry.class,
        Scope.SINGLETON,
        Collections.<Annotation>emptyList(),
        registryProvider);
    return new ServiceDescriptor(Arrays.asList(registryDescriptor));        //<3>
  }
  // end::init[]

  // tag::start[]
  @PostConstruct
  public void start() {
    SharedMetricRegistries.add(application.getPackageName(), registry);
  }
  // end::start[]

  @PreDestroy
  public void stop() {
    SharedMetricRegistries.remove(application.getPackageName());
  }

  @Override
  public Class<Stage.Invoke> getStageType() {
    return Stage.Invoke.class;
  }

  // tag::handle[]
  @Override
  public Response handle(Stage.Invoke argument) {
    final Timer.Context context = responses.time();
    try {
      return argument.invoke();
    } finally {
      context.stop();
    }
  }
  // end::handle[]
}
