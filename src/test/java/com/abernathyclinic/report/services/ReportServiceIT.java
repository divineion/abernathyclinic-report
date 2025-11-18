package com.abernathyclinic.report.services;


import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
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

// entrée : un patient + série de notes
// sortie : un Mono de reportDTO qui contient une string pour le nv de risk
// pas d'annotation @SpringBootTest puisqu'il n'y a plus d'injectiona utomatique, tout est créé dans la nouvelle mtehode @BeforeEach
@ExtendWith(MockitoExtension.class)
public class ReportServiceIT {
	@InjectMocks
	private ReportService service;
	
	@Mock
    private KeyWordsReader keyWordsReader;
	
	TimeService timeService;
	
	RiskCalculator riskCalculator;
	
	KeyWordsCounter keyWordsCounter;
	
	PatientProfileDto patient;
	
	Flux<NoteContentDto> notes;
	
	@BeforeEach
    void setUp() {
        // créer le KeyWordsCounter avec le mock
        keyWordsCounter = new KeyWordsCounter(keyWordsReader);
        timeService = new TimeService(Clock.fixed(Instant.parse("2025-11-17T00:00:00Z"), ZoneId.of("UTC")));
        riskCalculator = new RiskCalculator(timeService);

        // puis leservice avec le KeyWordsCounter et le RiskCalculator mock
        service = new ReportService(riskCalculator, keyWordsCounter);
    }
	
	// 0 - 1 mots
	@Test
	void testGenerateReport_shouldReturnNone_whenPatientHasLessThanTwoKeyWords() {
		// Arrange		
		Mono<PatientProfileDto> patientInfos = Mono.just(new PatientProfileDto("F", "1966-12-31"));
		Flux<NoteContentDto> note = Flux.just(new NoteContentDto(
				"Tout est normal, absence de douleur"));
		
		// mock du KeyWordsReader -> retourne les mots-clés quon veut
        when(keyWordsReader.keyWords()).thenReturn(Flux.just("douleur", "diabète", "anormal"));		

		// Act
		Mono<String> report = service.generateReport(patientInfos, note)
				.map(rep -> rep.riskLevel());
		
		// Assert
		StepVerifier.create(report)
			.expectNext("NONE")
			.verifyComplete();
	}
	
	// entre 2 et 5 termes âge > 30 ans
	@Test
	void testGenerateReport_shouldReturn_BorderlinewhenTwoToFiveKeywordsAndAgeOver30() {
		Mono<PatientProfileDto> patientInfos = Mono.just(new PatientProfileDto("M", "1945-06-24"));
		Flux<NoteContentDto> note = Flux.just(new NoteContentDto(
				"Patient ressent des douleurs liées à son diabète"));
		
        when(keyWordsReader.keyWords()).thenReturn(Flux.just("douleur", "diabète", "anormal"));		
        
        Mono<String> report = service.generateReport(patientInfos, note)
				.map(rep -> rep.riskLevel());
		
		// Assert
		StepVerifier.create(report)
			.expectNext("BORDERLINE")
			.verifyComplete();

	}
	
	
	// h <= 30 : 3 termes ; f <= 30 ans : 4 termes
	// h > 30 : 6 termes ; f > 30 : 7 terles
	@Test
	void testGenerateReport_shouldReturn_InDanger_whenMaleUnder30WithThreeKeywords() {
		Mono<PatientProfileDto> patientInfos = Mono.just(new PatientProfileDto("M", "2004-06-18"));
		
		Flux<NoteContentDto> note = Flux.just(new NoteContentDto(
				"Le patient déclare qu'il fume depuis peu Le patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année dernière"
				+ " Il se plaint également de crises d’apnée respiratoire anormales Tests de laboratoire indiquant un taux de cholestérol "
				+ "LDL élevé"));
		
        when(keyWordsReader.keyWords()).thenReturn(Flux.just("cholestérol", "respiratoire", "fumer"));		
        
        Mono<String> report = service.generateReport(patientInfos, note)
				.map(rep -> rep.riskLevel());
		
		// Assert
		StepVerifier.create(report)
			.expectNext("IN_DANGER")
			.verifyComplete();

	}
	
	// femme <= 30 ans, 4 mots 
    @Test
    void testGenerateReport_shouldReturnInDanger_whenFemaleUnder30WithFourKeywords() {
        Mono<PatientProfileDto> patient = Mono.just(new PatientProfileDto("F", "2000-03-12"));
        Flux<NoteContentDto> notes = Flux.just(
                new NoteContentDto("Douleur, diabète, fumer, cholestérol"));

        when(keyWordsReader.keyWords()).thenReturn(Flux.just("douleur", "diabète", "fumer", "cholestérol"));

        Mono<String> report = service.generateReport(patient, notes)
                                     .map(r -> r.riskLevel());

        StepVerifier.create(report)
                    .expectNext("IN_DANGER")
                    .verifyComplete();
    }
	
	// h <= 30 : 5 termes ; f <= 30 ans : 7 termes
	//  > 30 : au moins8 termes
	
	@Test
	void testGenerateReport_shouldReturnEarlyOnset_whenMaleOver30WithEightOrMoreKeywords() {
Mono<PatientProfileDto> patientInfos = Mono.just(new PatientProfileDto("M", "1984-06-18"));
		
		Flux<NoteContentDto> note = Flux.just(new NoteContentDto(
				"Le patient déclare qu'il fume depuis peu Le patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année dernière"
				+ " Il se plaint également de crises d’apnée respiratoire anormales Tests de laboratoire indiquant un taux de cholestérol "
				+ "LDL élevé Taille, Poids, Cholestérol, Vertige et Réaction"));
		
        when(keyWordsReader.keyWords()).thenReturn(Flux.just("cholestérol", "taille", "fumer", "poids", "vertige", "fumeur", "fumeuse", "réaction", "anormal"));		
        
        Mono<String> report = service.generateReport(patientInfos, note)
				.map(rep -> rep.riskLevel());
		
		// Assert
		StepVerifier.create(report)
			.expectNext("EARLY_ONSET")
			.verifyComplete();

	}	
}
