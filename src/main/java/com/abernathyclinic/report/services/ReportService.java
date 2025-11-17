package com.abernathyclinic.report.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import com.abernathyclinic.report.constants.RiskKeywords;
import com.abernathyclinic.report.constants.RiskLevel;
import com.abernathyclinic.report.dto.NoteContentDto;
import com.abernathyclinic.report.dto.PatientProfileDto;
import com.abernathyclinic.report.dto.ReportDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ReportService {
	
	private final TimeService timeService;
	
	public ReportService(TimeService timeService) {
		this.timeService = timeService;
	}

	// compter le nombre de mots-clés par note
	private Integer countKeyWords(NoteContentDto note) {

		// TODO reactive
		int keyWordsCount = 0;
		List<String> words = new ArrayList<>();

		for (String keyword : RiskKeywords.KEYWORDS) {
			if (note.content().toLowerCase().trim().contains(keyword.toLowerCase().trim())) {
				if (!words.contains(keyword.toLowerCase())) {
					words.add(keyword);
					keyWordsCount++;
				}
			}
		}

		return keyWordsCount;
	}

	private RiskLevel computeRiskLevel(Integer totalCount, PatientProfileDto patient) {
				
		// EARLY ONSET 
		// dépend de l'âge et du sexe
		// Si le patient a plus de 30 ans, alors il en faudra huit ou plus
		if (totalCount >= 8 && timeService.calculateAge(patient) >= 30) {
			return RiskLevel.EARLY_ONSET;
		}

		//// Si le patient est une femme et a moins de 30 ans, il faudra au moins
//		sept termes déclencheurs. 		
		if (patient.gender().equalsIgnoreCase("F") && totalCount >= 7 && timeService.calculateAge(patient) < 30) {
			return RiskLevel.EARLY_ONSET;
		}

//		le patient est un homme de moins de 30 ans, alors au moins cinq termes déclencheurs
//		sont nécessaires.
		if (patient.gender().equalsIgnoreCase("M") && timeService.calculateAge(patient) < 30 && totalCount >= 5) {
			return RiskLevel.EARLY_ONSET;
		}

		// IN DANGER
		// Si le patient est une femme et a moins de 30 ans, il faudra quatre termes déclencheurs.
		// Si le patient a plus de 30 ans, alors il en faudra six ou sept 
		if (patient.gender().equalsIgnoreCase("F") && 
				((timeService.calculateAge(patient) < 30 &&totalCount >= 4) 
				|| (timeService.calculateAge(patient) >= 30 && totalCount >= 7 ))) {
			return RiskLevel.IN_DANGER;
		}

		if (patient.gender().equalsIgnoreCase("M") && 
				((timeService.calculateAge(patient) < 30 && totalCount >= 3)
				|| (timeService.calculateAge(patient) >= 30 && totalCount >= 6))) {
			return RiskLevel.IN_DANGER;
		}
		
		// BORDERLINE
		if (totalCount >= 2 && totalCount <= 5 && timeService.calculateAge(patient) > 30) {
			return RiskLevel.BORDERLINE;
		}

		// Le dossier du patient ne contient aucune note du médecin ou une seule note
		// contenant les déclencheurs (terminologie)
		return RiskLevel.NONE;
	}

	public Mono<ReportDto> generateReport(Mono<PatientProfileDto> patient, Flux<NoteContentDto> notes) {
		// transformer chaque note en un nombre de mots-clés détectés
		// faire la somme des mots clés détectés

		return patient
			.flatMap(p -> 
				notes
					.map(note -> countKeyWords(note)) // chque Flux de note devient un Flux<Integer>
					.reduce(0, Integer::sum) // convertir en Mono<Integer> en appliquant la somme
					.map(totalCount -> computeRiskLevel(totalCount, p)) // convertir en RiskLevel
					.map(riskLevel -> new ReportDto(riskLevel.name()))// construire le dto Mono<RiskLevel>
			);
	}
}
