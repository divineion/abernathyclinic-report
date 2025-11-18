package com.abernathyclinic.report.services;

import org.springframework.stereotype.Service;

import com.abernathyclinic.report.constants.RiskLevel;
import com.abernathyclinic.report.dto.PatientProfileDto;

@Service
public class RiskCalculator {

    private final TimeService timeService;
    
	public RiskCalculator(TimeService timeService) {
		this.timeService = timeService;
	}
	
	public RiskLevel computeRiskLevel(Integer totalCount, PatientProfileDto patient) {
		
		// EARLY ONSET 
		// dépend de l'âge et du sexe
		// Si le patient a plus de 30 ans, alors il en faudra huit ou plus
		if ((totalCount >= 8 && timeService.calculateAge(patient.birthDate()) >= 30)
			// Si le patient est une femme et a moins de 30 ans, il faudra au moins 7 termes
			|| (patient.gender().equalsIgnoreCase("F") && totalCount >= 7 && timeService.calculateAge(patient.birthDate()) < 30)
				
			// le patient est un homme de moins de 30 ans, alors au moins cinq termes déclencheurs
			|| (patient.gender().equalsIgnoreCase("M") && timeService.calculateAge(patient.birthDate()) < 30 && totalCount >= 5)
				) {
			return RiskLevel.EARLY_ONSET;
		}

		// IN DANGER
		// Si le patient est une femme et a moins de 30 ans, il faudra quatre termes déclencheurs.
		if (patient.gender().equalsIgnoreCase("F") && 
				((timeService.calculateAge(patient.birthDate()) < 30 &&totalCount >= 4) 
				|| (timeService.calculateAge(patient.birthDate()) >= 30 && totalCount >= 7 ))

				// Si le patient a plus de 30 ans, alors il en faudra six ou sept 
				|| (patient.gender().equalsIgnoreCase("M") 
						&& ((timeService.calculateAge(patient.birthDate()) < 30 && totalCount >= 3)	
								|| (timeService.calculateAge(patient.birthDate()) >= 30 && totalCount >= 6)))) {
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

}
