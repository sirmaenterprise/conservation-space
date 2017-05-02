/**
 * 
 */
package com.sirmaenterprise.sep.eai.spreadsheet.service.rest.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.json.JsonException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.service.rest.SpreadsheetDataIntegrataionRequest;

/**
 * Tests for {@link SpreadsheetDataIntegrataionRequestReader}.
 * 
 * @author gshevkedov
 */
@RunWith(MockitoJUnitRunner.class)
public class SpreadsheetDataIntegrataionRequestReaderTest {

	@Mock
	private DomainInstanceService domainInstanceService;

	@InjectMocks
	private SpreadsheetDataIntegrataionRequestReader spreadsheetDataIntegrataionRequestReader;

	@Mock
	private RequestInfo request;

	@Before
	public void setUp() throws Exception {
		setupPathParamId();
	}

	private void setupPathParamId() {
		MultivaluedMap<String, String> paramsMap = mock(MultivaluedMap.class);
		when(paramsMap.get("id")).thenReturn(Arrays.asList("emf:456456456456"));
		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getPathParameters()).thenReturn(paramsMap);
		when(request.getUriInfo()).thenReturn(uriInfo);
	}
	
	@Test
	public void isReadable_correctClass() throws Exception {
		assertTrue(spreadsheetDataIntegrataionRequestReader.isReadable(SpreadsheetDataIntegrataionRequest.class, null, null, null));
	}

	@Test
	public void isReadable_incorrectClass() {
		assertFalse(spreadsheetDataIntegrataionRequestReader.isReadable(String.class, null, null, null));
	}

	@Test(expected = JsonException.class)
	public void readFrom_notAObject() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("[]".getBytes())) {
			spreadsheetDataIntegrataionRequestReader.readFrom(SpreadsheetDataIntegrataionRequest.class, null, null, null, null, stream);
		}
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.rest.io.SpreadsheetDataIntegrataionRequestReader#readFrom(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)}
	 * .
	 */
	@Test
	public void testReadFrom() throws Exception {
		try (InputStream stream = SpreadsheetReadRequestReaderTest.class
				.getResourceAsStream("importFileRequest.json")) {
			InstanceReference reference = mock(InstanceReference.class);
			Instance instance = mock(Instance.class);
			when(domainInstanceService.loadInstance(Mockito.anyString())).thenReturn(instance);
			when(instance.toReference()).thenReturn(reference);
			SpreadsheetDataIntegrataionRequest importRequest = spreadsheetDataIntegrataionRequestReader.readFrom(SpreadsheetDataIntegrataionRequest.class, null, null, null, null, stream);
			assertEquals("emf:456456456456", importRequest.getTargetId().toString());
		}
	}
	
	/**
	 * Test method for readFrom with context null params
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.rest.io.SpreadsheetDataIntegrataionRequestReader#readFrom(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)}
	 * .
	 */
	@Test
	public void testReadFromWithNullContext() throws Exception {
		try (InputStream stream = SpreadsheetReadRequestReaderTest.class
				.getResourceAsStream("importFileRequestWithNullContext.json")) {
			InstanceReference reference = mock(InstanceReference.class);
			Instance instance = mock(Instance.class);
			when(domainInstanceService.loadInstance(Mockito.anyString())).thenReturn(instance);
			when(instance.toReference()).thenReturn(reference);
			SpreadsheetDataIntegrataionRequest importRequest = spreadsheetDataIntegrataionRequestReader.readFrom(SpreadsheetDataIntegrataionRequest.class, null, null, null, null, stream);
			assertNull(importRequest.getContext());
		}
	}
	
	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.rest.io.SpreadsheetDataIntegrataionRequestReader#readFrom(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)}
	 * .
	 */
	@Test(expected = BadRequestException.class)
	public void testReadFromMissingData() throws Exception {
		try (InputStream stream = SpreadsheetReadRequestReaderTest.class
				.getResourceAsStream("importFileRequestMissingData.json")) {
			InstanceReference reference = mock(InstanceReference.class);
			Instance instance = mock(Instance.class);
			when(domainInstanceService.loadInstance(Mockito.anyString())).thenReturn(instance);
			when(instance.toReference()).thenReturn(reference);
			spreadsheetDataIntegrataionRequestReader.readFrom(SpreadsheetDataIntegrataionRequest.class, null, null, null, null, stream);
		}
	}
	
	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.rest.io.SpreadsheetDataIntegrataionRequestReader#readFrom(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)}
	 * .
	 */
	@Test(expected = BadRequestException.class)
	public void testReadFromMissingReport() throws Exception {
		try (InputStream stream = SpreadsheetReadRequestReaderTest.class
				.getResourceAsStream("importFileRequestMissingReport.json")) {
			InstanceReference reference = mock(InstanceReference.class);
			Instance instance = mock(Instance.class);
			when(domainInstanceService.loadInstance(Mockito.anyString())).thenReturn(instance);
			when(instance.toReference()).thenReturn(reference);
			spreadsheetDataIntegrataionRequestReader.readFrom(SpreadsheetDataIntegrataionRequest.class, null, null, null, null, stream);
		}
	}

}
