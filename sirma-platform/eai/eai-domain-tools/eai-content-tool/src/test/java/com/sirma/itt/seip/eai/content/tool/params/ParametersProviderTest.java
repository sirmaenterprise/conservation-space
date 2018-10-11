package com.sirma.itt.seip.eai.content.tool.params;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import com.sirma.itt.seip.eai.content.tool.exception.EAIRuntimeException;

import javafx.application.Application.Parameters;

public class ParametersProviderTest {
	@Test
	public void testGetAndSetParameters() {
		Parameters parameters = Mockito.mock(Parameters.class);
		Map<String, String> paramsNamed = new HashMap<>();
		paramsNamed.put(ParametersProvider.PARAM_API_URL, "http://0.0.0.0:11999");
		paramsNamed.put(ParametersProvider.PARAM_AUTHORIZATION, "header");
		paramsNamed.put(ParametersProvider.PARAM_CONTENT_URI, "emf:uri");
		Mockito.when(parameters.getNamed()).thenReturn(paramsNamed);
		ParametersProvider.setParameters(parameters);
		assertNotNull(ParametersProvider.get(ParametersProvider.PARAM_API_URL));
	}

	@Test(expected = EAIRuntimeException.class)
	public void testGetAndSetParametersWithMissingParams() {
		Parameters parameters = Mockito.mock(Parameters.class);
		Map<String, String> paramsNamed = new HashMap<>();
		paramsNamed.put(ParametersProvider.PARAM_AUTHORIZATION, "header");
		paramsNamed.put(ParametersProvider.PARAM_CONTENT_URI, "emf:uri");
		Mockito.when(parameters.getNamed()).thenReturn(paramsNamed);
		ParametersProvider.setParameters(parameters);
	}

}
