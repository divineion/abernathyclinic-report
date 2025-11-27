package com.abernathyclinic.report.exception;

import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

/**
 * Handles error details for the application.
 * Overrides default error attributes to customize error response content.
 * Sets status to 403 if access is forbidden.
 * Sets status to 404 if patient not found.
 * Always includes error message in response.
 */
@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {	
	@Override
	public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
		Map<String, Object> errorPropertiesMap = super.getErrorAttributes(request, options);
		
		Throwable error = getError(request);
		
		int status;
		
		if (isForbidden(error)) {
			status = 403;
			errorPropertiesMap.put("status", status);
			errorPropertiesMap.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
		} else if (isNotFound(error)) {
			status = 404;
			errorPropertiesMap.put("status", status);
			errorPropertiesMap.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
		}
				
		errorPropertiesMap.put("message", error.getMessage());
		
		return errorPropertiesMap;
	}
	
	boolean isForbidden(Throwable error) {
		return error instanceof ForbiddenAccessException;
	}
	
	boolean isNotFound(Throwable error) {
		return error instanceof PatientNotFoundException;
	}
}
