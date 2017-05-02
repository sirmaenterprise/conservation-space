package com.sirma.itt.seip.eai.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.AtMost;
import org.mockito.internal.verification.Only;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.internal.ExternalInstanceIdentifier;
import com.sirma.itt.seip.eai.service.communication.BaseEAIServices;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationService;
import com.sirma.itt.seip.eai.service.communication.request.EAIRequestProvider;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * Test {@link IntegrateExternalObjectsService}
 * 
 * @author gshevkedov
 * @author bbanchev
 */
public class IntegrateExternalObjectsServiceTest {
	private static final String CMS = "CMS";
	@InjectMocks
	private IntegrateExternalObjectsService integrateExternalObjectsService;
	@Mock
	private EAICommunicationService communicationService;
	@Mock
	private EAIRequestProvider requestProvider;
	@Mock
	private Plugins<IntegrateObjectsServiceAdapter> integrationAdapters;

	@Test(expected = EmfRuntimeException.class)
	public void testImportInstancesEx() {
		new IntegrateExternalObjectsService().importInstances(Collections.singletonList(Mockito.mock(Instance.class)));
	}

	@Test
	public void testImportInstances() throws Exception {
		MockitoAnnotations.initMocks(this);

		List<Instance> items = Collections.singletonList(Mockito.mock(Instance.class));
		List<Instance> result = Collections.singletonList(Mockito.mock(Instance.class));
		IntegrateObjectsServiceAdapter adapter = prepareMock(items, result);

		Collection<Instance> importInstances = integrateExternalObjectsService.importInstances(items);
		Mockito.verify(adapter).extractExternalInstanceIdentifier(Mockito.eq(items.get(0)));
		Mockito.verify(adapter).importInstances(Mockito.any(Collection.class), Mockito.eq(true), Mockito.eq(true));
		Assert.assertEquals(result, importInstances);
		importInstances = integrateExternalObjectsService.importInstances(Collections.emptyList());
		Assert.assertEquals(Collections.emptyList(), importInstances);

	}

	@Test
	public void testImportInstancesEmpty() throws Exception {
		MockitoAnnotations.initMocks(this);
		IntegrateObjectsServiceAdapter adapter = Mockito.mock(IntegrateObjectsServiceAdapter.class);
		Mockito.when(integrationAdapters.get(CMS)).thenReturn(Optional.of(adapter));
		Mockito.when(integrationAdapters.iterator()).thenReturn(Collections.singletonList(adapter).iterator());
		Collection<Instance> importInstances = integrateExternalObjectsService.importInstances(Collections.emptyList());
		Mockito.verify(adapter, new AtMost(0)).extractExternalInstanceIdentifier(Mockito.any());
		Mockito.verify(adapter, new AtMost(0)).importInstances(Mockito.any(Collection.class), Mockito.eq(true),
				Mockito.eq(true));
		Assert.assertEquals(importInstances, Collections.emptyList());

	}

	@Test
	public void testImportInstancesFail() throws Exception {
		MockitoAnnotations.initMocks(this);
		List<Instance> items = Collections.singletonList(Mockito.mock(Instance.class));
		ExternalInstanceIdentifier instanceIdentifier = Mockito.mock(ExternalInstanceIdentifier.class);
		IntegrateObjectsServiceAdapter adapter = Mockito.mock(IntegrateObjectsServiceAdapter.class);

		Mockito.when(adapter.extractExternalInstanceIdentifier(Mockito.eq(items.get(0)))).thenReturn(
				instanceIdentifier);
		Mockito.when(adapter.getName()).thenReturn(CMS);
		Mockito
				.when(adapter.importInstances(Mockito.any(Collection.class), Mockito.eq(true), Mockito.eq(true)))
					.thenThrow(EAIReportableException.class);

		Mockito.when(integrationAdapters.get(CMS)).thenReturn(Optional.of(adapter));
		Mockito.when(integrationAdapters.iterator()).thenReturn(Collections.singletonList(adapter).iterator());
		Mockito.when(instanceIdentifier.getExternalId()).thenReturn("externalId");

		try {
			integrateExternalObjectsService.importInstances(items);
			Assert.fail("Exception should be thrown!");
		} catch (EmfRuntimeException e) {
			Mockito
					.when(requestProvider.provideRequest(Mockito.eq(CMS), Mockito.eq(BaseEAIServices.LOGGING),
							Mockito.any(EAIReportableException.class)))
						.thenReturn(new RequestInfo());
			Mockito.verify(communicationService, new Only()).invoke(Mockito.any(RequestInfo.class));
		}
	}

	private IntegrateObjectsServiceAdapter prepareMock(List<Instance> items, List<Instance> result)
			throws EAIException {
		ExternalInstanceIdentifier instanceIdentifier = Mockito.mock(ExternalInstanceIdentifier.class);
		IntegrateObjectsServiceAdapter adapter = Mockito.mock(IntegrateObjectsServiceAdapter.class);
		Mockito.when(adapter.extractExternalInstanceIdentifier(Mockito.eq(items.get(0)))).thenReturn(
				instanceIdentifier);
		Mockito.when(adapter.getName()).thenReturn(CMS);
		Mockito
				.when(adapter.importInstances(Mockito.any(Collection.class), Mockito.eq(true), Mockito.eq(true)))
					.thenAnswer(new Answer<Collection<Instance>>() {

						@Override
						public Collection<Instance> answer(InvocationOnMock invocation) throws Throwable {
							Collection<ExternalInstanceIdentifier> ids = (Collection<ExternalInstanceIdentifier>) invocation
									.getArguments()[0];
							Assert.assertEquals(instanceIdentifier, ids.iterator().next());
							return result;
						}
					});
		Mockito.when(integrationAdapters.get(CMS)).thenReturn(Optional.of(adapter));
		Mockito.when(integrationAdapters.iterator()).thenReturn(Collections.singletonList(adapter).iterator());
		Mockito.when(instanceIdentifier.getExternalId()).thenReturn("externalId");
		return adapter;
	}
}
