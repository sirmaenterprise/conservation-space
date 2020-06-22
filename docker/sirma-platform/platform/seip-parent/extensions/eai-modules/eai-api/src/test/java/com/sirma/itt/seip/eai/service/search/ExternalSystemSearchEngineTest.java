package com.sirma.itt.seip.eai.service.search;

import static com.sirma.itt.seip.eai.mock.MockProvider.mockSystem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Condition.Junction;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
import com.sirma.itt.seip.eai.configuration.EAIConfigurationProvider;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.model.ResultPaging;
import com.sirma.itt.seip.eai.model.ServiceRequest;
import com.sirma.itt.seip.eai.model.ServiceResponse;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.eai.model.internal.SearchResultInstances;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchOrderCriterion;
import com.sirma.itt.seip.eai.service.EAIConfigurationService;
import com.sirma.itt.seip.eai.service.communication.BaseEAIServices;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationService;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationServiceAdapter;
import com.sirma.itt.seip.eai.service.communication.request.EAIRequestProvider;
import com.sirma.itt.seip.eai.service.communication.request.EAIRequestProviderAdapter;
import com.sirma.itt.seip.eai.service.communication.response.EAIResponseReader;
import com.sirma.itt.seip.eai.service.communication.response.EAIResponseReaderAdapter;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * The Class ExternalSystemSearchTest.
 */
public class ExternalSystemSearchEngineTest {
	private static final String CMS = "CMS";

	private ExternalSystemSearchEngine externalSystemSearchEngine;

	@InjectMocks
	private EAICommunicationService communicationService;
	@InjectMocks
	private EAIRequestProvider requestProvider;
	@InjectMocks
	private EAIResponseReader responseReader;
	@InjectMocks
	private EAIConfigurationService integrationService;
	@Mock
	private Plugins<EAICommunicationServiceAdapter> communicationAdapters;
	@Mock
	private Plugins<EAIRequestProviderAdapter> requestProviderAdapters;
	@Mock
	private Plugins<EAIResponseReaderAdapter> responseReaderAdapters;
	@Mock
	private Plugins<EAIConfigurationProvider> configurationProviders;

	private EAICommunicationServiceAdapter mockCommunicationAdapter;

	private EAIRequestProviderAdapter mockRequestAdapter;

	private EAIResponseReaderAdapter mockResponseAdapter;

	private ServiceResponse response;

	private ServiceRequest request;

	@Before
	public void setupMocks() throws Exception {

		externalSystemSearchEngine = new ExternalSystemSearchEngine() {
		};
		request = new ServiceRequest() {
		};
		response = new ServiceResponse() {
		};
		mockCommunicationAdapter = mock(EAICommunicationServiceAdapter.class);

		mockRequestAdapter = mock(EAIRequestProviderAdapter.class);
		Mockito
				.when(mockRequestAdapter.buildRequest(Mockito.eq(BaseEAIServices.SEARCH), any(SearchArguments.class)))
					.thenReturn(request);

		mockResponseAdapter = mock(EAIResponseReaderAdapter.class);

		MockitoAnnotations.initMocks(this);
		when(communicationAdapters.get(CMS)).thenReturn(Optional.of(mockCommunicationAdapter));
		when(requestProviderAdapters.get(CMS)).thenReturn(Optional.of(mockRequestAdapter));
		when(responseReaderAdapters.get(CMS)).thenReturn(Optional.of(mockResponseAdapter));

		ReflectionUtils.setFieldValue(externalSystemSearchEngine, "communicationService", communicationService);
		ReflectionUtils.setFieldValue(externalSystemSearchEngine, "requestProvider", requestProvider);
		ReflectionUtils.setFieldValue(externalSystemSearchEngine, "responseReader", responseReader);
		ReflectionUtils.setFieldValue(externalSystemSearchEngine, "integrationService", integrationService);
	}

	/**
	 * Test search with external facilities.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void search() throws Exception {
		SearchArguments<Instance> arguments = new SearchArguments<>();

		Condition searchTree = SearchCriteriaBuilder.createConditionBuilder()
				.setCondition(Junction.AND)
				.build();
		arguments.setContext(CMS);
		arguments.setCondition(searchTree);

		when(mockResponseAdapter.parseResponse(Mockito.any(ResponseInfo.class)))
				.thenAnswer(invocation -> {
					ResponseInfo responseInfo = (ResponseInfo) invocation.getArguments()[0];
					assertEquals(request, responseInfo.getRequest());
					assertEquals(BaseEAIServices.SEARCH, responseInfo.getServiceId());
					assertEquals(CMS, responseInfo.getSystemId());
					assertEquals(response, responseInfo.getResponse());
					SearchResultInstances<Object> searchResultInstances = new SearchResultInstances<>();
					searchResultInstances.setPaging(new ResultPaging());
					searchResultInstances.getPaging().setTotal(101);
					searchResultInstances.getPaging().setSkip(1);
					searchResultInstances.getPaging().setLimit(100);
					searchResultInstances.setInstances(new LinkedList<>());
					searchResultInstances.getInstances().add(Mockito.mock(Instance.class));
					return searchResultInstances;
				});
		when(mockCommunicationAdapter.invoke(Mockito.any(RequestInfo.class))).thenAnswer(invocation -> {
			RequestInfo requestInfo = (RequestInfo) invocation.getArguments()[0];
			assertEquals(request, requestInfo.getRequest());
			assertEquals(BaseEAIServices.SEARCH, requestInfo.getServiceId());
			assertEquals(CMS, requestInfo.getSystemId());

			return response;
		});

		externalSystemSearchEngine.search(Instance.class, arguments);
		verify(mockRequestAdapter).buildRequest(Mockito.eq(BaseEAIServices.SEARCH), any(SearchArguments.class));
		verify(mockCommunicationAdapter).invoke(Mockito.any(RequestInfo.class));
		verify(mockResponseAdapter).parseResponse(Mockito.any(ResponseInfo.class));

		List<Instance> result = arguments.getResult();
		assertEquals(1, result.size());
		assertEquals(1, arguments.getSkipCount());
		assertEquals(101, arguments.getTotalItems());
		assertEquals(100, arguments.getPageSize());
	}

	/**
	 * Test search with external facilities with error.
	 */
	@Test
	public void searchWithError() throws Exception {
		SearchArguments<Instance> arguments = new SearchArguments<>();

		Condition searchTree = SearchCriteriaBuilder.createConditionBuilder()
				.setCondition(Junction.AND)
				.setRules(new LinkedList<>())
				.build();
		arguments.setContext(CMS);
		arguments.setCondition(searchTree);

		when(mockResponseAdapter.parseResponse(Mockito.any(ResponseInfo.class)))
				.thenThrow(new EAIReportableException("some errors during processing"));
		when(mockCommunicationAdapter.invoke(Mockito.any(RequestInfo.class))).thenAnswer(invocation -> {
			RequestInfo requestInfo = (RequestInfo) invocation.getArguments()[0];
			assertTrue(requestInfo.getServiceId() == BaseEAIServices.SEARCH
					|| requestInfo.getServiceId() == BaseEAIServices.LOGGING);
			return response;
		});
		EAIConfigurationProvider eaiConfigurationProvider = mockSystem(CMS, Boolean.TRUE, Boolean.TRUE);
		when(eaiConfigurationProvider.getSystemClientId()).thenReturn(new ConfigurationPropertyMock<>("id:id"));
		when(configurationProviders.stream()).thenReturn(Stream.of(eaiConfigurationProvider));

		externalSystemSearchEngine.search(Instance.class, arguments);
		verify(mockRequestAdapter).buildRequest(eq(BaseEAIServices.SEARCH), any(SearchArguments.class));
		verify(mockCommunicationAdapter, times(2)).invoke(any(RequestInfo.class));
		verify(mockResponseAdapter).parseResponse(any(ResponseInfo.class));
		assertEquals(
				"Failed to process response of id:id! System is notified!\nOriginal cause: some errors during processing",
				arguments.getSearchError().getMessage());
	}

	@Test
	public void testReadSearchParametersWithDefaultSort() throws Exception {
		EAIConfigurationProvider eaiConfigurationProvider = mockSystem(CMS, Boolean.TRUE, Boolean.TRUE);
		when(eaiConfigurationProvider.getSystemClientId()).thenReturn(new ConfigurationPropertyMock<>("id:id"));
		// two streams for prepareSearchArguments
		when(configurationProviders.stream()).thenReturn(Stream.of(eaiConfigurationProvider),
				Stream.of(eaiConfigurationProvider));

		SearchRequest searchRequest = new SearchRequest(new HashMap<>());
		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchRequest = new SearchRequest(new HashMap<>());
		searchRequest.add("context", CMS);

		SearchModelConfiguration searchConfiguration = mock(SearchModelConfiguration.class);
		when(eaiConfigurationProvider.getSearchConfiguration())
				.thenReturn(new ConfigurationPropertyMock<>(searchConfiguration));
		when(searchConfiguration.getOrderData()).thenReturn(null);
		externalSystemSearchEngine.prepareSearchArguments(searchRequest, searchArguments);
		assertTrue(searchArguments.getSorters().isEmpty());
		EntitySearchOrderCriterion entitySearchOrderCriterion = new EntitySearchOrderCriterion();
		entitySearchOrderCriterion.setPropertyId("id_sort");
		when(searchConfiguration.getOrderData()).thenReturn(Arrays.asList(entitySearchOrderCriterion));
		externalSystemSearchEngine.prepareSearchArguments(searchRequest, searchArguments);
		assertFalse(searchArguments.getSorters().isEmpty());
		assertEquals("id_sort", searchArguments.getSorters().get(0).getSortField());
	}

	@Test
	public void testIsSupported() throws Exception {
		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		assertFalse(externalSystemSearchEngine.isSupported(Instance.class, searchArguments));
		searchArguments.setContext("some ctx");
		assertTrue(externalSystemSearchEngine.isSupported(Instance.class, searchArguments));
	}

}