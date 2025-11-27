package com.abernathyclinic.report.exception;

@SuppressWarnings("serial")
public class PatientNotFoundException extends Exception {
	public PatientNotFoundException(String message) {
		super(message);
	}
}
