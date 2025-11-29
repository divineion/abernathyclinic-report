package com.abernathyclinic.report.services;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

import org.springframework.stereotype.Service;

import com.abernathyclinic.report.dto.PatientProfileDto;

@Service
public class TimeService {
	private final Clock clock;

	public TimeService(Clock clock) {
		this.clock = clock;
	}
	
	public TimeService() {
		this(Clock.systemDefaultZone());
	}
	
	/**
	 * Calculate the age from a birthdate with format "yyyy-MM-dd".
	 * @param patient a {@link PatientProfileDto}
	 * @return int the patient age in years
	 */
	public int calculateAge(String birthDate) {
		LocalDate now = LocalDate.now(clock);
		LocalDate birthdate = LocalDate.parse(birthDate);

		return Period.between(birthdate, now).getYears();
	}
}
