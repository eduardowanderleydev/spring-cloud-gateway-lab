package br.wanderley.com.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;


@SpringBootApplication
public class GatewayApplication {

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	WebFilter writeableHeaders() {
		return (exchange, chain) -> {
			HttpHeaders writeableHeaders = HttpHeaders.writableHttpHeaders(
					exchange.getRequest().getHeaders());

			ServerHttpRequestDecorator writeableRequest = new ServerHttpRequestDecorator(
					exchange.getRequest()) {
				@Override
				public HttpHeaders getHeaders() {
					return writeableHeaders;
				}
			};
			ServerWebExchange writeableExchange = exchange.mutate()
					.request(writeableRequest)
					.build();
			return chain.filter(writeableExchange);
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}
}
