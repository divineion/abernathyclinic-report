package com.abernathyclinic.report.it;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.abernathyclinic.report.constants.ApiMessages;
import com.abernathyclinic.report.controllers.ReportController;
import com.abernathyclinic.report.dto.ReportDto;
import com.abernathyclinic.report.services.ReportService;
import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.util.UUID;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) //démarre un serveur web réel sur un port aléatoire
public class ReportControllerIT {
// vérifier que le contrôleur appelle bien le microservice Patient
// vérifier que le contrôleur appelle bien le microservice Notes

// prend l'objet Java
// le transforme en JSON
// envoie une requête HTTP
// Spring Webflux reçoit le JSON et le transforme en Publisher
// le contrôleur s'abonne à ce flux, récupère l'objet et appelle le service
// puis le contrôleur retourne un Mono<ReportDto>
	@Autowired
	WebTestClient webTestClient; //WebTestClient injecté automatiquement est lié au serveur web démarré par spring
	
	@Autowired
	ReportController reportController;
	
	@Autowired
	ReportService reportService;
	
	@Value("${patient.service.url}")
	private String patientServiceUrl;
	
	@Value("${notes.service.url}")
	private String notesServiceUrl;
	
	private WireMockServer patientMock;
	private WireMockServer notesMock;
	
	// faire 2 serveurs à la main pour ne pas planter un single serveur avec 2 appels
	@BeforeEach
	void setUp() {
	patientMock = new WireMockServer(8090);
	notesMock = new WireMockServer(8091);
	
	patientMock.start();
	notesMock.start();
	}
	
	@AfterEach
	void tearDown() {
	patientMock.stop();
	notesMock.stop();
	}
	
	// [https://www.baeldung.com/spring-boot-wiremock](https://www.baeldung.com/spring-boot-wiremock)
	
	
	@Test
	void testGetPatientReport_shouldReturnBorderline() {
		String patientUuid = "d6d4aa48-0fba-4845-9659-4b3a336d988c";
		
		ReportDto expectedReport = new ReportDto("BORDERLINE");
		
		String mockPatientProfile = "{\"gender\": \"M\", \"birthDate\": \"1945-06-24\"}";
		
		String mockNotesJson = "["
			    + "{ \"content\": \"Le patient déclare avoir fait une réaction aux médicaments au cours des 3 derniers mois Il remarque également que son audition continue d'être anormale\" },"
			    + "{ \"content\": \"Le patient déclare qu'il ressent beaucoup de stress au travail Il se plaint également que son audition est anormale dernièrement\" }"
			    + "]";
		
		patientMock.stubFor(get("/patient/" + patientUuid + "/report-info").willReturn(okJson(mockPatientProfile)));
		
		notesMock.stubFor(get("/notes/" + patientUuid + "/report-info").willReturn(okJson(mockNotesJson)));
		
		webTestClient.get()
			.uri("/api/report/{patientUuid}", patientUuid) // rutilise rune uri relative (port random) - spring utilise automatiquelent le servue rlancé
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", "basictoken")
			.exchange()
			.expectStatus().isOk()
			.expectBody(ReportDto.class).isEqualTo(expectedReport);
	}
	
	@Test
	void testGetPatientReport_shouldReturnNone() {
		String patientUuid = "d6d4aa48-0fba-4845-9659-4b3a336d988c";
		
		ReportDto expectedReport = new ReportDto("NONE");
		
		String mockPatientProfile = "{\"gender\": \"M\", \"birthDate\": \"1966-12-31\"}";
		
		String mockNotesJson = "["
			    + "{ \"content\": \"Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé\" }"
			    + "]";
		
		patientMock.stubFor(get("/patient/" + patientUuid + "/report-info").willReturn(okJson(mockPatientProfile)));
		
		notesMock.stubFor(get("/notes/" + patientUuid + "/report-info").willReturn(okJson(mockNotesJson)));
		
		webTestClient.get()
			.uri("/api/report/{patientUuid}", patientUuid) 
			.accept(MediaType.APPLICATION_JSON)
			.header("Authorization", "basictoken")
			.exchange()
			.expectStatus().isOk()
			.expectBody(ReportDto.class).isEqualTo(expectedReport);
	}
	
	@Test
	void testGetPatientReport_shouldReturnInDanger() {
		String patientUuid = "d6d4aa48-0fba-4845-9659-4b3a336d988c";
		
		ReportDto expectedReport = new ReportDto("IN_DANGER");
		
		String mockPatientProfile = "{\"gender\": \"M\", \"birthDate\": \"2004-06-18\"}";
		
		String mockNotesJson = "["
				+ "{ \"content\": \"Le patient déclare qu'il fume depuis peu\" },"
				+ "{ \"content\": \"Le patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année dernière Il se plaint également de crises d’apnée respiratoire anormales Tests de laboratoire indiquant un taux de cholestérol LDL élevé\" }"
				+ "]";
		
		patientMock.stubFor(get("/patient/" + patientUuid + "/report-info").willReturn(okJson(mockPatientProfile)));
		
		notesMock.stubFor(get("/notes/" + patientUuid + "/report-info").willReturn(okJson(mockNotesJson)));
		
		webTestClient.get()
		.uri("/api/report/{patientUuid}", patientUuid) 
		.accept(MediaType.APPLICATION_JSON)
		.header("Authorization", "basictoken")
		.exchange()
		.expectStatus().isOk()
		.expectBody(ReportDto.class).isEqualTo(expectedReport);
	}

	@Test
	void testGetPatientReport_shouldReturnEarlyOnset() {
		String patientUuid = "d6d4aa48-0fba-4845-9659-4b3a336d988c";
		
		ReportDto expectedReport = new ReportDto("EARLY_ONSET");
		
		String mockPatientProfile = "{\"gender\": \"M\", \"birthDate\": \"2002-06-28\"}";
		
		String mockNotesJson = "["
				+ "{ \"content\": \"Le patient déclare qu'il lui est devenu difficile de monter les escaliers Il se plaint également d’être essoufflé Tests de laboratoire indiquant que les anticorps sont élevés Réaction aux médicaments\" },"
				+ "{ \"content\": \"Le patient déclare qu'il a mal au dos lorsqu'il reste assis pendant longtemps\" },"
				+ "{ \"content\": \"Le patient déclare avoir commencé à fumer depuis peu Hémoglobine A1C supérieure au niveau recommandé\" },"
				+ "{ \"content\": \"Taille, Poids, Cholestérol, Vertige et Réaction\" }"
				+ "]";
		
		patientMock.stubFor(get("/patient/" + patientUuid + "/report-info").willReturn(okJson(mockPatientProfile)));
		
		notesMock.stubFor(get("/notes/" + patientUuid + "/report-info").willReturn(okJson(mockNotesJson)));
		
		webTestClient.get()
		.uri("/api/report/{patientUuid}", patientUuid) 
		.accept(MediaType.APPLICATION_JSON)
		.header("Authorization", "basictoken")
		.exchange()
		.expectStatus().isOk()
		.expectBody(ReportDto.class).isEqualTo(expectedReport);
	}

	@Test
	void testPatientNotFound() {
		UUID patientUuid = UUID.randomUUID();
				
		patientMock.stubFor(get("/patient/" + patientUuid + "/report-info").willReturn(notFound()));
				
		webTestClient.get()
		.uri("/api/report/{patientUuid}", patientUuid) 
		.header("Authorization", "basictoken")
		.exchange()
		.expectStatus().isNotFound()
		.expectBody()
		.jsonPath("$.message").isEqualTo(ApiMessages.PATIENT_NOT_FOUND);
	}
	
	@Test
	void testForbiddenUser() {
		String patientUuid = "d6d4aa48-0fba-4845-9659-4b3a336d988c";
		
		patientMock.stubFor(get("/patient/" + patientUuid + "/report-info").willReturn(forbidden()));
		
		webTestClient.get()
		.uri("/api/report/{patientUuid}", patientUuid) 
		.header("Authorization", "basictoken")
		.exchange()
		.expectStatus().isForbidden()
		.expectBody()
		.jsonPath("$.message").isEqualTo(ApiMessages.FORBIDDEN_ACCESS);
	}
}