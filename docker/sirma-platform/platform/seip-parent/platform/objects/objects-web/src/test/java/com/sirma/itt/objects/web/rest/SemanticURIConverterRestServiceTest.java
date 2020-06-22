package com.sirma.itt.objects.web.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.testutil.EmfTest;

public class SemanticURIConverterRestServiceTest extends EmfTest {

	@InjectMocks
	private SemanticURIConverterRestService rest;

	@Mock
	private TypeConverter typeConverter;

	@Override
	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void convertToShortURIsTest() throws JSONException {
		mockStringToURIConverter();
		mockURIToStringConverter();
		List<String> fullUris = new ArrayList<String>(2);
		fullUris.add("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project");
		fullUris.add("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case");
		Map<String, String> convertedURIs = rest.convertToShortURIs(fullUris);
		Assert.assertEquals(2, convertedURIs.size());
		Assert.assertEquals("emf:Project", convertedURIs.get("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project"));
		Assert.assertEquals("emf:Case", convertedURIs.get("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case"));
	}

	@Test
	public void convertToFullURIsTest() throws JSONException {
		mockStringToURIConverter();
		List<String> shortURIs = new ArrayList<String>(2);
		shortURIs.add("emf:Project");
		shortURIs.add("emf:Case");
		Map<String, String> convertedURIs = rest.convertToFullURIs(shortURIs);
		Assert.assertEquals(2, convertedURIs.size());
		Assert.assertEquals("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project", convertedURIs.get("emf:Project"));
		Assert.assertEquals("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case", convertedURIs.get("emf:Case"));
	}

	private void mockStringToURIConverter() {
		Mockito.when(typeConverter.convert(Matchers.eq(IRI.class), Matchers.any(String.class))).thenAnswer(invocation -> {
			String fullURI = invocation.getArgumentAt(1, String.class);
			fullURI = fullURI.replaceAll("emf:", "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#");
			return SimpleValueFactory.getInstance().createIRI(fullURI);
		});
	}

	private void mockURIToStringConverter() {
		Mockito.when(typeConverter.convert(Matchers.eq(String.class), Matchers.any(IRI.class))).thenAnswer(invocation -> {
			IRI fullURI = invocation.getArgumentAt(1, IRI.class);
			return fullURI.stringValue().replaceAll("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#", "emf:");
		});
	}
}
