package br.wanderley.com.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class UserAgentGatewayFilterFactory extends AbstractGatewayFilterFactory<UserAgentGatewayFilterFactory.Config> {

    public UserAgentGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String userAgent = request.getHeaders().getFirst("User-Agent");

            if (userAgent == null || !config.shouldRedirect()) return chain.filter(exchange);

            if (userAgent.contains("PostmanRuntime")) {
                exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                exchange.getResponse().getHeaders().setLocation(URI.create(config.postmanUrl()));
                return exchange.getResponse().setComplete();
            } else if (userAgent.contains("Chrome")) {
                exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                exchange.getResponse().getHeaders().setLocation(URI.create(config.browserUrl()));
                return exchange.getResponse().setComplete();
            }

            throw new IllegalArgumentException("agent not supported");
        };
    }

    public record Config(boolean shouldRedirect, String postmanUrl, String browserUrl){}
}
