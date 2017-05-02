/**
 * 
 */
package com.sirmaenterprise.sep.eai.spreadsheet.service.rest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.eai.service.IntegrateExternalObjectsService;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.ParsedInstance;

/**
 * @author gshevkedov
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SpreadsheetIntegrationActionTest {
	
	@Mock
	private IntegrateExternalObjectsService integrateExternalObjectsService;
	@InjectMocks
	private SpreadsheetIntegrationAction spreadsheetIntegrationAction;

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.rest.SpreadsheetIntegrationAction#getName()}.
	 */
	@Test
	public void testGetName() throws Exception {
		assertEquals(SpreadsheetDataIntegrataionRequest.OPERATION_NAME, spreadsheetIntegrationAction.getName());
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.rest.SpreadsheetIntegrationAction#perform(com.sirmaenterprise.sep.eai.spreadsheet.service.rest.SpreadsheetDataIntegrataionRequest)}
	 * .
	 */
	@Test
	public void testPerform() throws Exception {
		SpreadsheetDataIntegrataionRequest request = Mockito.mock(SpreadsheetDataIntegrataionRequest.class);
		InstanceReference reference = Mockito.mock(InstanceReference.class);
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(request.getReport()).thenReturn(reference);
		Mockito.when(reference.toInstance()).thenReturn(instance);
		Collection<Object> values = new ArrayList<>();
		ParsedInstance parsedInstance = Mockito.mock(ParsedInstance.class);
		values.add(parsedInstance);
		Mockito.when(integrateExternalObjectsService.importInstances(request)).thenReturn(values);
		assertNotNull(spreadsheetIntegrationAction.perform(request));
	}

}
