package com.abernathyclinic.report.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.abernathyclinic.report.dto.NoteContentDto;
import com.abernathyclinic.report.dto.PatientProfile;
import com.abernathyclinic.report.dto.ReportDto;
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
    
	@GetMapping("/api/report/{uuid}")
	public Mono<ReportDto> getPatientReport(
		@RequestHeader("Authorization") String authorization, 
		@PathVariable("uuid") String patientUuid) {
					
		Mono<PatientProfile> patient = webClient.get()
				.uri("http://localhost:8080/patient/"+patientUuid + "/report-info")
				.header("Authorization", authorization)
				.retrieve()
				.bodyToMono(PatientProfile.class);
		
		Flux<NoteContentDto> notes = webClient.get()
				.uri("http://localhost:8080/notes/" + patientUuid + "/report-info")
				.header("Authorization", authorization)
				.retrieve()
				.bodyToFlux(NoteContentDto.class);
		
		 return noteService.generateReport(patient, notes);
		 
	}
}
