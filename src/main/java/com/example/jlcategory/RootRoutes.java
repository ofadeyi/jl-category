package com.example.jlcategory;

import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

public final class RootRoutes {

    public static RouterFunction<ServerResponse> routes(Stream<ResourceRoutes> routeStream) {
        return routeStream
                .map(handler -> handler.routes())
                .reduce(route(GET("/"), RootRoutes::initRoute), RootRoutes::addSubRoute);
    }

    private static Mono<ServerResponse> initRoute(ServerRequest serverRequest) {
        return ok().build();
    }

    private static RouterFunction<ServerResponse> addSubRoute(
            RouterFunction<ServerResponse> accumulatorRoute,
            RouterFunction<ServerResponse> subRoutes) {
        return accumulatorRoute
                .and(subRoutes);
    }
}
