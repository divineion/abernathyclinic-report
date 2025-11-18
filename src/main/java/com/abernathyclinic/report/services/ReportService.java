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
		// transformer chaque note en un nombre de mots-clés détectés
		// faire la somme des mots clés détectés
		return patient
				// transformer chaque élément en un autre Flux
				// là on a un Mono de patientProfile et flatMap() va créer un flux de Note
			.flatMap(profile -> 
				notes
					.flatMap(note -> keywordsCounter.countKeyWords(note)) // transforme le Flux de notes qui devient un Flux<Integer>
					.reduce(0, Integer::sum) // convertir en Mono<Integer> en faisant la somme des décomptes
					.map(totalCount -> riskCalculator.computeRiskLevel(totalCount, profile)) // convertir en RiskLevel en appliquant le calcul
					.map(riskLevel -> new ReportDto(riskLevel.name()))// construire le dto Mono<RiskLevel>
			);
	}
}
