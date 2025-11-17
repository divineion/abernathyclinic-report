package com.abernathyclinic.report.services;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abernathyclinic.report.dto.NoteContentDto;
import com.abernathyclinic.report.dto.PatientProfileDto;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {
// https://www.baeldung.com/java-clock
	
	@InjectMocks
	ReportService service;
		
	@Mock 
	TimeService timeService;
	
	PatientProfileDto patient;
	
	Flux<NoteContentDto> notes;
	
	//
	@Test
	void testGenerateReport_shouldReturnNone() {
		// Arrange		
		Mono<PatientProfileDto> patientInfos = Mono.just(new PatientProfileDto("F", "1966-12-31"));
		Flux<NoteContentDto> note = Flux.just(new NoteContentDto(
				"Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé"));
		
		when(timeService.calculateAge(any(String.class))).thenReturn(58);
		
		// Act
		Mono<String> report = service.generateReport(patientInfos, note)
				.map(rep -> rep.riskLevel());
		
		// Assert
		StepVerifier.create(report)
			.expectNext("NONE")
			.verifyComplete();
	}
	
//	@Test
//	void testGenerateReport_shouldReturn_Borderline() {
//		
//	}
//	
//	@Test
//	void testGenerateReport_shouldReturn_InDanger() {
//		
//	}
//	
//	@Test
//	void testGenerateReport_shouldReturn_EarlyOnset() {
//		
//	}	
}
