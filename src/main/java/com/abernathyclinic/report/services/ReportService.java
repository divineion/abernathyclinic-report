package com.abernathyclinic.report.services;

import org.springframework.stereotype.Service;
import com.abernathyclinic.report.constants.RiskLevel;
import com.abernathyclinic.report.dto.NoteContentDto;
import com.abernathyclinic.report.dto.PatientProfileDto;
import com.abernathyclinic.report.dto.ReportDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ReportService {
	
	private final TimeService timeService;

	private final KeyWordsReader keywordsReader;
	
	public ReportService(KeyWordsReader keywordsReader, TimeService timeService) {
		this.timeService = timeService;
		this.keywordsReader = keywordsReader;
	}

	// compter le nombre de mots-clés par note		
	private Mono<Integer> countKeyWords(NoteContentDto note) {
		return keywordsReader.keyWords()
	    	.filter(keyword -> note.content().toLowerCase().contains(keyword.toLowerCase()))
	    	.count()
	    	.map(count -> Math.toIntExact(count));
    }

	private RiskLevel computeRiskLevel(Integer totalCount, PatientProfileDto patient) {
				
		// EARLY ONSET 
		// dépend de l'âge et du sexe
		// Si le patient a plus de 30 ans, alors il en faudra huit ou plus
		if (totalCount >= 8 && timeService.calculateAge(patient.birthDate()) >= 30) {
			return RiskLevel.EARLY_ONSET;
		}

		//// Si le patient est une femme et a moins de 30 ans, il faudra au moins
//		sept termes déclencheurs. 		
		if (patient.gender().equalsIgnoreCase("F") && totalCount >= 7 && timeService.calculateAge(patient.birthDate()) < 30) {
			return RiskLevel.EARLY_ONSET;
		}

//		le patient est un homme de moins de 30 ans, alors au moins cinq termes déclencheurs
//		sont nécessaires.
		if (patient.gender().equalsIgnoreCase("M") && timeService.calculateAge(patient.birthDate()) < 30 && totalCount >= 5) {
			return RiskLevel.EARLY_ONSET;
		}

		// IN DANGER
		// Si le patient est une femme et a moins de 30 ans, il faudra quatre termes déclencheurs.
		// Si le patient a plus de 30 ans, alors il en faudra six ou sept 
		if (patient.gender().equalsIgnoreCase("F") && 
				((timeService.calculateAge(patient.birthDate()) < 30 &&totalCount >= 4) 
				|| (timeService.calculateAge(patient.birthDate()) >= 30 && totalCount >= 7 ))) {
			return RiskLevel.IN_DANGER;
		}

		if (patient.gender().equalsIgnoreCase("M") && 
				((timeService.calculateAge(patient.birthDate()) < 30 && totalCount >= 3)
				|| (timeService.calculateAge(patient.birthDate()) >= 30 && totalCount >= 6))) {
			return RiskLevel.IN_DANGER;
		}
		
		// BORDERLINE
		if (totalCount >= 2 && totalCount <= 5 && timeService.calculateAge(patient.birthDate()) > 30) {
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
				// transformer chaque élément en un autre Flux
				// là on a un Mono de patientProfile et flatMap() va créer un flux de Note
			.flatMap(profile -> 
				notes
					.flatMap(note -> countKeyWords(note)) // transforme le Flux de notes qui devient un Flux<Integer>
					.reduce(0, Integer::sum) // convertir en Mono<Integer> en faisant la somme des décomptes
					.map(totalCount -> computeRiskLevel(totalCount, profile)) // convertir en RiskLevel en appliquant le calcul
					.map(riskLevel -> new ReportDto(riskLevel.name()))// construire le dto Mono<RiskLevel>
			);
	}
}
