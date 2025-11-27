package com.abernathyclinic.report.exception;

import java.util.Map;

import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

/**
 * Global exception handler for the Report microservice.
 * Handles all errors and returns JSON responses with status code and error details.
 * Works with Spring WebFlux reactive stack.
 * 
 * <p>Priority -2 ensures this handler runs before default Spring Boot handlers.</p>
 */
@Component
@Order(-2)
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {	
	/**
     * Constructor. Sets up error handling with Spring Boot resources and codec.
     * 
     * @param errorAttributes Spring error attributes to read error info
     * @param resources Web properties for error pages (not used here)
     * @param applicationContext Spring application context
     * @param configurer Server codec configurer for JSON writing
     */
	public GlobalExceptionHandler(
			ErrorAttributes errorAttributes, 
			Resources resources,
			ApplicationContext applicationContext,
			ServerCodecConfigurer configurer

	) {
		super(errorAttributes, resources, applicationContext);
		this.setMessageWriters(configurer.getWriters());
	 	}

	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
		return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
	}
	
	private Mono<ServerResponse> renderErrorResponse (ServerRequest request) {
		Map<String, Object> errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults());
		
		int status = (int) errorPropertiesMap.get("status");

		return ServerResponse.status(status)
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(errorPropertiesMap));
	}
}
