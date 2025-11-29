package com.abernathyclinic.report.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;


public class TimeServiceTest {
	TimeService timeService;
		
	@Test
	void testCalculateAge_atSpecificDate() {
		// Arrange
		String birthDate = "1966-12-31";
		
	    Clock fixedClock = Clock.fixed(
	        Instant.parse("2025-11-17T00:00:00Z"), ZoneId.of("UTC"));
	    
	    TimeService timeService = new TimeService(fixedClock); 

	    // Act
	    int age = timeService.calculateAge(birthDate);

	    // Assert
	    assertEquals(58, age); 
	}
}
