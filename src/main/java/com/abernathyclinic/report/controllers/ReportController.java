package com.abernathyclinic.report.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.abernathyclinic.report.constants.ApiMessages;
import com.abernathyclinic.report.dto.NoteContentDto;
import com.abernathyclinic.report.dto.PatientProfileDto;
import com.abernathyclinic.report.dto.ReportDto;
import com.abernathyclinic.report.exception.ForbiddenAccessException;
import com.abernathyclinic.report.exception.PatientNotFoundException;
import com.abernathyclinic.report.services.ReportService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ReportController {

    private final WebClient webClient;
    
    private final ReportService noteService;

    public ReportController(WebClient webClient, ReportService noteService) {
        this.webClient = webClient;
        this.noteService = noteService;
    }
    
    @Value("${patient.service.url}")
    private String patientServiceUrl;
    
    @Value("${notes.service.url}")
    private String notesServiceUrl;
    
	@GetMapping("/api/report/{uuid}")
	public Mono<ReportDto> getPatientReport(
		@RequestHeader("Authorization") String authorization, 
		@PathVariable("uuid") String patientUuid) {
				
		// https://medium.com/@AlexanderObregon/the-technical-side-of-webclient-error-handling-in-spring-boot-6213b06ba6a0
		Mono<PatientProfileDto> patient = webClient.get()
				.uri(patientServiceUrl + patientUuid + "/report-info")
				.header("Authorization", authorization)
				.retrieve()
				.onStatus(status -> status.is4xxClientError(), response -> {
					if (response.statusCode() == HttpStatus.NOT_FOUND) {
		                return Mono.error(new PatientNotFoundException(ApiMessages.PATIENT_NOT_FOUND));
		            } else if (response.statusCode() == HttpStatus.FORBIDDEN) {
		                return Mono.error(new ForbiddenAccessException(ApiMessages.FORBIDDEN_ACCESS));
		            }
		            return response.createException();
				})
				.bodyToMono(PatientProfileDto.class);
		
		Flux<NoteContentDto> notes = webClient.get()
				.uri(notesServiceUrl + patientUuid + "/report-info")
				.header("Authorization", authorization)
				.retrieve()
				.bodyToFlux(NoteContentDto.class);
		
		 return noteService.generateReport(patient, notes);
		 
	}
}
