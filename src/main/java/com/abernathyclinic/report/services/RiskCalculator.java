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
		if ((totalCount >= 8 && timeService.calculateAge(patient.birthDate()) >= 30)
			|| (patient.gender().equalsIgnoreCase("F") && totalCount >= 7 && timeService.calculateAge(patient.birthDate()) < 30)		
			|| (patient.gender().equalsIgnoreCase("M") && timeService.calculateAge(patient.birthDate()) < 30 && totalCount >= 5)
				) {
			return RiskLevel.EARLY_ONSET;
		}

		if (patient.gender().equalsIgnoreCase("F") && 
				((timeService.calculateAge(patient.birthDate()) < 30 &&totalCount >= 4) 
				|| (timeService.calculateAge(patient.birthDate()) >= 30 && totalCount >= 7 ))
				|| (patient.gender().equalsIgnoreCase("M") 
						&& ((timeService.calculateAge(patient.birthDate()) < 30 && totalCount >= 3)	
								|| (timeService.calculateAge(patient.birthDate()) >= 30 && totalCount >= 6)))) {
			return RiskLevel.IN_DANGER;
		}


		if (totalCount >= 2 && totalCount <= 5 && timeService.calculateAge(patient.birthDate()) > 30) {
			return RiskLevel.BORDERLINE;
		}

		return RiskLevel.NONE;
	}
}
