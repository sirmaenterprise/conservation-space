package com.sirma.itt.seip.eai.cs.service.communication;

import static org.junit.Assert.assertNotNull;

import java.io.Serializable;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.eai.cs.model.response.CSItemsSetResponse;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.error.InformationSeverity;
import com.sirma.itt.seip.eai.model.error.LoggingDTO;
import com.sirma.itt.seip.eai.model.response.SimpleHttpResponse;
import com.sirma.itt.seip.eai.model.response.StreamingResponse;
import com.sirma.itt.seip.eai.service.communication.BaseEAIServices;
import com.sirma.itt.seip.eai.service.communication.EAIServiceIdentifier;

/**
 * Test {@link CSClientCommunicationAdapter}
 * 
 * @author gshevkedov
 */
public class CSClientCommunicationAdapterTest {

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = EAIException.class)
	public void testInvokeSearchEx() throws EAIException {
		CSClientCommunicationAdapter adapter = Mockito.mock(CSClientCommunicationAdapter.class,
				Mockito.CALLS_REAL_METHODS);
		RequestInfo requestInfo = Mockito.mock(RequestInfo.class);
		adapter.invoke(requestInfo);
	}

	@Test
	public void testInvokeSearch() throws EAIException {
		CSClientCommunicationAdapter adapter = Mockito.mock(CSClientCommunicationAdapter.class);
		RequestInfo requestInfo = Mockito.mock(RequestInfo.class);
		CSItemsSetResponse serviceResponse = new CSItemsSetResponse();
		Mockito.when(requestInfo.getServiceId()).thenReturn(BaseEAIServices.SEARCH);
		Mockito.when(adapter.invokeSearch(requestInfo)).thenReturn(serviceResponse);
		Mockito.doCallRealMethod().when(adapter).invoke(requestInfo);
		assertNotNull(adapter.invoke(requestInfo));
	}

	@Test
	public void testInvokeRetrieve() throws EAIException {
		CSClientCommunicationAdapter adapter = Mockito.mock(CSClientCommunicationAdapter.class);
		RequestInfo requestInfo = Mockito.mock(RequestInfo.class);
		CSItemsSetResponse serviceResponse = new CSItemsSetResponse();
		Mockito.when(requestInfo.getServiceId()).thenReturn(BaseEAIServices.RETRIEVE);
		Mockito.when(adapter.invokeRetrieve(requestInfo)).thenReturn(serviceResponse);
		Mockito.doCallRealMethod().when(adapter).invoke(requestInfo);
		assertNotNull(adapter.invoke(requestInfo));
	}

	@Test
	public void testInvokeLogging() throws EAIException {
		CSClientCommunicationAdapter adapter = Mockito.mock(CSClientCommunicationAdapter.class);
		RequestInfo requestInfo = Mockito.mock(RequestInfo.class);
		CSItemsSetResponse serviceResponse = new CSItemsSetResponse();
		Mockito.when(requestInfo.getServiceId()).thenReturn(BaseEAIServices.LOGGING);
		Mockito.when(adapter.invokeLogging(requestInfo)).thenReturn(serviceResponse);
		Mockito.doCallRealMethod().when(adapter).invoke(requestInfo);
		assertNotNull(adapter.invoke(requestInfo));
	}

	@Test
	public void testInvokeDirect() throws EAIException {
		CSClientCommunicationAdapter adapter = Mockito.mock(CSClientCommunicationAdapter.class);
		RequestInfo requestInfo = Mockito.mock(RequestInfo.class);
		StreamingResponse serviceResponse = Mockito.mock(StreamingResponse.class);
		Mockito.when(requestInfo.getServiceId()).thenReturn(BaseEAIServices.DIRECT);
		Mockito.when(adapter.invokeByURI(requestInfo)).thenReturn(serviceResponse);
		Mockito.doCallRealMethod().when(adapter).invoke(requestInfo);
		assertNotNull(adapter.invoke(requestInfo));
	}

	@Test(expected = EAIException.class)
	public void testInvokeSearchMethod() throws EAIException {
		CSClientCommunicationAdapter adapter = Mockito.mock(CSClientCommunicationAdapter.class);
		SimpleHttpResponse httpResponse = Mockito.mock(SimpleHttpResponse.class);
		RequestInfo request = Mockito.mock(RequestInfo.class);
		HttpPost errorLog = Mockito.mock(HttpPost.class);
		LoggingDTO logInfoRequest = Mockito.mock(LoggingDTO.class);
		Mockito.when(request.getRequest()).thenReturn(logInfoRequest);
		Mockito.when(logInfoRequest.getSeverity()).thenReturn(InformationSeverity.INFO);
		Mockito.when(logInfoRequest.getSummary()).thenReturn("loren ipsum random text");
		Mockito.when(logInfoRequest.getDetails()).thenReturn("details");
		Serializable origin = Mockito.mock(Serializable.class);
		Mockito.when(logInfoRequest.getOrigin()).thenReturn(origin);
		EAIServiceIdentifier identifier = Mockito.mock(EAIServiceIdentifier.class);
		Mockito.when(request.getServiceId()).thenReturn(identifier);
		HttpEntity entity = Mockito.mock(HttpEntity.class);
		MultipartEntityBuilder entityBuilder = Mockito.mock(MultipartEntityBuilder.class);
		Mockito.when(entityBuilder.build()).thenReturn(entity);
		Mockito
				.when(adapter.executeMethodWithResponse(errorLog,
						e -> new SimpleHttpResponse(e.getStatusLine().getStatusCode(),
								e.getStatusLine().getReasonPhrase())))
					.thenReturn(httpResponse);
		Mockito.doCallRealMethod().when(adapter).invokeLogging(request);
		adapter.invokeLogging(request);

	}

}
