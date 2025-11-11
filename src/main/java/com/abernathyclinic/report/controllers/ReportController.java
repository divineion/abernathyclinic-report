package com.abernathyclinic.report.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.abernathyclinic.report.constants.RiskLevel;
import com.abernathyclinic.report.dto.ReportDto;

import reactor.core.publisher.Mono;

@RestController
public class ReportController {
	
	@GetMapping("/api/report/{uuid}")
	public Mono<ReportDto> getTestReport(@PathVariable String patientUuid) {
		return Mono.just(new ReportDto(patientUuid, RiskLevel.NONE.toString()));
	}
}
