package br.wanderley.com.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class UserAgentGatewayFilterFactory extends AbstractGatewayFilterFactory<UserAgentGatewayFilterFactory.Config> {

    private final WebClient webClient;

    public UserAgentGatewayFilterFactory(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClient = webClientBuilder.build();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String userAgent = request.getHeaders().getFirst(HttpHeaders.USER_AGENT);

            if (userAgent == null || !config.shouldRedirect()) return chain.filter(exchange);

            String url;
            if (userAgent.contains("PostmanRuntime")) {
                url = config.postmanUrl();
            } else if (userAgent.contains("Chrome")) {
                url = config.browserUrl();
            } else {
                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                return exchange.getResponse().setComplete();
            }

            return webClient.get()
                    .uri(url)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)))
                    .exchangeToMono(response -> copyResponse(exchange, response));
        };
    }

    private Mono<Void> copyResponse(ServerWebExchange exchange, ClientResponse response) {
        exchange.getResponse().setStatusCode(response.statusCode());
        response.headers().asHttpHeaders().forEach((key, values) -> {
            if (!key.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) {
                exchange.getResponse().getHeaders().addAll(key, values);
            }
        });
        return exchange.getResponse().writeWith(response.bodyToMono(DataBuffer.class));
    }

    public record Config(boolean shouldRedirect, String postmanUrl, String browserUrl){}
}
