package com.sirmaenterprise.sep.bpm.camunda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirmaenterprise.sep.bpm.camunda.BPMDownloadAlfresco4Service.CamundaModels;

@RunWith(MockitoJUnitRunner.class)
public class BPMDownloadAlfresco4ServiceTest {
	@Mock
	private RESTClient restClient;
	@InjectMocks
	private BPMDownloadAlfresco4Service bpmnService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testRetrieveNoDefinitions() throws Exception {
		PostMethod mainRequest = mock(PostMethod.class);

		String response = "{\"data\":{\"items\":[]}}";
		when(restClient.request(eq(ServiceURIRegistry.CMF_SEARCH_SERVICE), eq(mainRequest))).thenReturn(response);
		String requestValue = "{\"paging\":{\"pageSize\":1000,\"skip\":0,\"maxSize\":1000},\"sort\":[{\"cm:modified\":false}],\"query\":\"PATH:\\\"/app:company_home/app:dictionary/app:workflow_defs/*\\\" AND TYPE:\\\"cm:content\\\" AND (name:\\\"*.bpmn\\\")\"}";
		when(restClient.createMethod(any(), eq(requestValue), eq(true))).thenReturn(mainRequest);

		List<FileDescriptor> retrievedDefinitions = bpmnService.retrieveDefinitions(CamundaModels.BPMN);
		verify(restClient).request(eq(ServiceURIRegistry.CMF_SEARCH_SERVICE), eq(mainRequest));
		assertNotNull(retrievedDefinitions);
		assertEquals(0, retrievedDefinitions.size());

		retrievedDefinitions = bpmnService.retrieveDefinitions();
		assertNotNull(retrievedDefinitions);
		assertEquals(0, retrievedDefinitions.size());
	}

	@Test
	public void testRetrieveDefinitions() throws Exception {
		PostMethod mainRequest = mock(PostMethod.class);

		String response = "{\"data\":{\"items\":[]}}";
		when(restClient.request(eq(ServiceURIRegistry.CMF_SEARCH_SERVICE), eq(mainRequest))).thenReturn(response);
		String requestValue = "{\"paging\":{\"pageSize\":1000,\"skip\":0,\"maxSize\":1000},\"sort\":[{\"cm:modified\":false}],\"query\":\"PATH:\\\"/app:company_home/app:dictionary/app:workflow_defs/*\\\" AND TYPE:\\\"cm:content\\\" AND (name:\\\"*.bpmn\\\" OR name:\\\"*.cmmn\\\" OR name:\\\"*.dmn\\\")\"}";
		when(restClient.createMethod(any(), eq(requestValue), eq(true))).thenReturn(mainRequest);

		response = "{\"data\":{\"items\":[{\"nodeRef\":\"workspace://SpaceStore/myNode\"}]}}";
		when(restClient.request(eq(ServiceURIRegistry.CMF_SEARCH_SERVICE), eq(mainRequest))).thenReturn(response);

		GetMethod detailsRequest = mock(GetMethod.class);

		when(restClient.createMethod(anyObject(), eq("test"), eq(true))).thenReturn(detailsRequest);

		String details = "{\"item\":{\"fileName\":\"test.bpmn\"}}";
		when(restClient.request(eq(ServiceURIRegistry.NODE_DETAILS + "workspace/SpaceStore/myNode"), any()))
				.thenReturn(details);

		List<FileDescriptor> retrievedDefinitions = bpmnService.retrieveDefinitions(CamundaModels.values());
		verify(restClient).request(eq(ServiceURIRegistry.CMF_SEARCH_SERVICE), eq(mainRequest));

		assertNotNull(retrievedDefinitions);
		assertEquals(1, retrievedDefinitions.size());
	}
}
