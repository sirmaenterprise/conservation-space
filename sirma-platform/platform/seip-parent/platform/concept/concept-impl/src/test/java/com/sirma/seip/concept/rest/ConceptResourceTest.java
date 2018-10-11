package com.sirma.seip.concept.rest;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.seip.concept.ConceptService;

/**
 * Tests {@link ConceptResource}.
 * 
 * @author Vilizar Tsonev
 */
public class ConceptResourceTest {

	@InjectMocks
	private ConceptResource conceptResource;
	
	@Mock
	private ConceptService conceptService;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_Retrieve_By_Broader_If_Provided() {
		conceptResource.getConceptHierarchy("test-Scheme", "sample-Concept");
		verify(conceptService).getConceptsByBroader(eq("sample-Concept"));
		verify(conceptService, never()).getConceptsByScheme(anyString());
	}

	@Test
	public void should_Retrieve_By_Scheme_If_Broader_Not_Provided() {
		conceptResource.getConceptHierarchy("test-Scheme", null);
		verify(conceptService).getConceptsByScheme(eq("test-Scheme"));
		verify(conceptService, never()).getConceptsByBroader(anyString());
	}
}
