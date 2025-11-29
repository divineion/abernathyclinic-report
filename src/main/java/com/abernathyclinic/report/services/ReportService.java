package com.abernathyclinic.report.services;

import org.springframework.stereotype.Service;
import com.abernathyclinic.report.dto.NoteContentDto;
import com.abernathyclinic.report.dto.PatientProfileDto;
import com.abernathyclinic.report.dto.ReportDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ReportService {	
	private final RiskCalculator riskCalculator;
	
	private final KeyWordsCounter keywordsCounter;
	
	public ReportService(RiskCalculator riskCalculator, KeyWordsCounter keywordsCounter) {
		this.riskCalculator = riskCalculator;
		this.keywordsCounter = keywordsCounter;
	}

	public Mono<ReportDto> generateReport(Mono<PatientProfileDto> patient, Flux<NoteContentDto> notes) {

		return patient

			.flatMap(profile -> 
				notes
					.flatMap(note -> keywordsCounter.countKeyWords(note))
					.reduce(0, Integer::sum)
					.map(totalCount -> riskCalculator.computeRiskLevel(totalCount, profile))
					.map(riskLevel -> new ReportDto(riskLevel.name()))
			);
	}
}
