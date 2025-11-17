package com.abernathyclinic.report.services;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

import org.springframework.stereotype.Service;

import com.abernathyclinic.report.dto.PatientProfileDto;

@Service
public class TimeService {
	private final Clock clock;
	// https://www.geeksforgeeks.org/java/java-8-clock-fixed-method-with-examples/

	// soit je lui passe une date (tests)
	public TimeService(Clock clock) {
		this.clock = clock;
	}
	
	// soit j'utilise le constructeur par défaut qui utilise l'horloge système
	public TimeService() {
		this(Clock.systemDefaultZone());
	}
	
	/**
	 * Calculate the age from a birthdate with format "yyyy-MM-dd".
	 * @param patient a {@link PatientProfileDto}
	 * @return int the patient age in years
	 */
	public int calculateAge(PatientProfileDto patient) {
		LocalDate now = LocalDate.now(clock);
		LocalDate birthdate = LocalDate.parse(patient.birthDate());

		return Period.between(birthdate, now).getYears();
	}
}
