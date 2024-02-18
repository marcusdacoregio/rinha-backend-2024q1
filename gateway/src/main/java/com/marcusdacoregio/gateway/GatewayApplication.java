package com.marcusdacoregio.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.gateway.server.mvc.GatewayServerMvcAutoConfiguration;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication(exclude = GatewayServerMvcAutoConfiguration.class)
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Bean
	ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> supplier) {
		return new RoundRobinLoadBalancer(supplier, "api-service");
	}

	@Bean
	@Primary
	ServiceInstanceListSupplier serviceInstanceListSupplier(@Value("${gateway.api-uris}") Set<String> apiUris) {
		return new ApiServiceInstanceListSuppler("api-service", apiUris);
	}

	static class ApiServiceInstanceListSuppler implements ServiceInstanceListSupplier {

		static Logger logger = LoggerFactory.getLogger(ApiServiceInstanceListSuppler.class);

		private final String serviceId;

		private final Set<String> uris;

		ApiServiceInstanceListSuppler(String serviceId, Set<String> uris) {
			this.serviceId = serviceId;
			this.uris = uris;
		}

		@Override
		public String getServiceId() {
			return serviceId;
		}

		@Override
		public Flux<List<ServiceInstance>> get() {
			AtomicInteger instanceCount = new AtomicInteger(1);
			return Flux.fromIterable(this.uris)
					.map((uri) -> {
						UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(uri).build();
						ServiceInstance si = new DefaultServiceInstance(this.serviceId + instanceCount.getAndIncrement(), this.serviceId,
								uriComponents.getHost(), uriComponents.getPort(), "https".equals(uriComponents.getScheme()));
						return si;
					})
					.buffer();
		}
	}

}
