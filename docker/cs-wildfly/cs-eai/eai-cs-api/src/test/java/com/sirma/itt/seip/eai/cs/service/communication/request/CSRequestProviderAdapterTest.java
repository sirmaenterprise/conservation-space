package com.sirma.itt.seip.eai.cs.service.communication.request;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.mockito.Mockito;

import com.sirma.itt.seip.eai.cs.model.request.CSRetrieveRequest;
import com.sirma.itt.seip.eai.cs.model.request.CSSearchRequest;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.service.communication.BaseEAIServices;
import com.sirma.itt.seip.eai.service.communication.EAIServiceIdentifier;

/**
 * {@link CSRequestProviderAdapter}
 * 
 * @author gshevkedov
 */
public class CSRequestProviderAdapterTest {

	@Test
	public void testBuildRequestSearch() throws EAIException {
		CSRequestProviderAdapter adapter = Mockito.mock(CSRequestProviderAdapter.class);
		EAIServiceIdentifier identifier = BaseEAIServices.SEARCH;
		Object sourceArgument = Mockito.mock(Object.class);
		CSSearchRequest searchRequest = Mockito.mock(CSSearchRequest.class);
		Mockito.when(adapter.buildSearchRequest(sourceArgument)).thenReturn(searchRequest);
		Mockito.doCallRealMethod().when(adapter).buildRequest(identifier, sourceArgument);
		assertNotNull(adapter.buildRequest(identifier, sourceArgument));
	}

	@Test
	public void testBuildRequestRetrieve() throws EAIException {
		CSRequestProviderAdapter adapter = Mockito.mock(CSRequestProviderAdapter.class);
		EAIServiceIdentifier identifier = BaseEAIServices.RETRIEVE;
		Object sourceArgument = Mockito.mock(Object.class);
		CSRetrieveRequest retrieveRequest = Mockito.mock(CSRetrieveRequest.class);
		Mockito.when(adapter.buildRetrieveRequest(sourceArgument)).thenReturn(retrieveRequest);
		Mockito.doCallRealMethod().when(adapter).buildRequest(identifier, sourceArgument);
		assertNotNull(adapter.buildRequest(identifier, sourceArgument));
	}

	@Test(expected = EAIException.class)
	public void testBuildRequestNotImplemented() throws EAIException {
		CSRequestProviderAdapter adapter = Mockito.mock(CSRequestProviderAdapter.class);
		EAIServiceIdentifier identifier = BaseEAIServices.DIRECT;
		Object sourceArgument = Mockito.mock(Object.class);
		Mockito.doCallRealMethod().when(adapter).buildRequest(identifier, sourceArgument);
		adapter.buildRequest(identifier, sourceArgument);
	}

	@Test(expected = EAIRuntimeException.class)
	public void testBuildSearchRequestInvalid() throws EAIException {
		CSRequestProviderAdapter adapter = Mockito.mock(CSRequestProviderAdapter.class);
		Object sourceArgument = Mockito.mock(Object.class);
		Mockito.doCallRealMethod().when(adapter).buildSearchRequest(sourceArgument);
		adapter.buildSearchRequest(sourceArgument);
	}

	@Test(expected = EAIRuntimeException.class)
	public void testBuildSearchRequestNull() throws EAIException {
		CSRequestProviderAdapter adapter = Mockito.mock(CSRequestProviderAdapter.class);
		Mockito.doCallRealMethod().when(adapter).buildSearchRequest(null);
		adapter.buildSearchRequest(null);
	}
}
