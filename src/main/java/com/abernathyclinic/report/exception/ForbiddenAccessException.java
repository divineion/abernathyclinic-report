package com.abernathyclinic.report.exception;

@SuppressWarnings("serial")
public class ForbiddenAccessException extends RuntimeException {
	public ForbiddenAccessException(String message) {
		super(message);
	}
}

