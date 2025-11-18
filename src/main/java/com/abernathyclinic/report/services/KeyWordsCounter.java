package com.abernathyclinic.report.services;

import org.springframework.stereotype.Service;

import com.abernathyclinic.report.dto.NoteContentDto;

import reactor.core.publisher.Mono;

@Service
public class KeyWordsCounter {
	private final KeyWordsReader keyWordsReader;
	
	public KeyWordsCounter(KeyWordsReader keyWordsReader) {
		this.keyWordsReader = keyWordsReader;
	}
	
	// compter le nombre de mots-clés par note		
		Mono<Integer> countKeyWords(NoteContentDto note) {
			return keyWordsReader.keyWords()
		    	.filter(keyword -> note.content().toLowerCase().contains(keyword.toLowerCase()))
		    	.count()
		    	.map(count -> Math.toIntExact(count));
	    }
}
