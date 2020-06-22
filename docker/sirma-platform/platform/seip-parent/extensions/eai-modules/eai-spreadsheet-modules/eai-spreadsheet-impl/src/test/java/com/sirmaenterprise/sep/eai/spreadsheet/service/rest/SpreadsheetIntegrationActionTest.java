package com.sirmaenterprise.sep.eai.spreadsheet.service.rest;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.eai.service.IntegrateExternalObjectsService;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.ParsedInstance;

/**
 * Test for {@link SpreadsheetIntegrationAction}.
 *
 * @author gshevkedov
 */
@RunWith(MockitoJUnitRunner.class)
public class SpreadsheetIntegrationActionTest {

	@Mock
	private IntegrateExternalObjectsService integrateExternalObjectsService;

	@InjectMocks
	private SpreadsheetIntegrationAction action;

	@Test
	public void getName() throws Exception {
		assertEquals(SpreadsheetDataIntegrataionRequest.OPERATION_NAME, action.getName());
	}

	@Test
	public void perform_successful() throws Exception {
		Instance instance = mock(Instance.class);
		InstanceReference reference = mock(InstanceReference.class);
		when(reference.toInstance()).thenReturn(instance);

		SpreadsheetDataIntegrataionRequest request = mock(SpreadsheetDataIntegrataionRequest.class);
		when(request.getReport()).thenReturn(reference);

		ParsedInstance parsedInstance = mock(ParsedInstance.class);
		when(integrateExternalObjectsService.importInstances(request)).thenReturn(singletonList(parsedInstance));
		assertNotNull(action.perform(request));
	}

	@Test
	public void lockWhileExecuted_enabled() {
		assertTrue(action.shouldLockInstanceBeforeAction(mock(SpreadsheetDataIntegrataionRequest.class)));
	}
}