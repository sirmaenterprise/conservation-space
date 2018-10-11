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
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.service.rest.SpreadSheetReadRequest;

@RunWith(MockitoJUnitRunner.class)
public class SpreadsheetReadRequestReaderTest {
	
	@Mock
	private DomainInstanceService domainInstanceService;
	
	@Mock
	private RequestInfo request;
	
	@InjectMocks
	private SpreadsheetReadRequestReader spreadsheetReadRequestReader;

	@Before
	public void setUp() throws Exception {
		setupPathParamId();
	}

	private void setupPathParamId() {
		MultivaluedMap<String, String> paramsMap = mock(MultivaluedMap.class);
		when(paramsMap.get("id")).thenReturn(Arrays.asList("emf:123123123"));
		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getPathParameters()).thenReturn(paramsMap);
		when(request.getUriInfo()).thenReturn(uriInfo);
	}
	
	@Test
	public void isReadable_correctClass() throws Exception {
		assertTrue(spreadsheetReadRequestReader.isReadable(SpreadSheetReadRequest.class, null, null, null));
	}

	@Test
	public void isReadable_incorrectClass() {
		assertFalse(spreadsheetReadRequestReader.isReadable(String.class, null, null, null));
	}

	@Test(expected = JsonException.class)
	public void readFrom_notAObject() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("[]".getBytes())) {
			spreadsheetReadRequestReader.readFrom(SpreadSheetReadRequest.class, null, null, null, null, stream);
		}
	}

	@Test
	public void testReadFrom() throws Exception {
		try (InputStream stream = SpreadsheetReadRequestReaderTest.class
				.getResourceAsStream("readFileRequest.json")) {
			InstanceReference reference = mock(InstanceReference.class);
			Instance instance = mock(Instance.class);
			when(domainInstanceService.loadInstance(Mockito.anyString())).thenReturn(instance);
			when(instance.toReference()).thenReturn(reference);
			SpreadSheetReadRequest readRequest = spreadsheetReadRequestReader.readFrom(SpreadSheetReadRequest.class, null, null, null, null, stream);
			assertEquals("emf:123123123", readRequest.getTargetId().toString());
		}
	}
	
	@Test
	public void testReadFromWithNullContext() throws Exception {
		try (InputStream stream = SpreadsheetReadRequestReaderTest.class
				.getResourceAsStream("readFileRequestWithNullContext.json")) {
			InstanceReference reference = mock(InstanceReference.class);
			Instance instance = mock(Instance.class);
			when(domainInstanceService.loadInstance(Mockito.anyString())).thenReturn(instance);
			when(instance.toReference()).thenReturn(reference);
			SpreadSheetReadRequest readRequest = spreadsheetReadRequestReader.readFrom(SpreadSheetReadRequest.class, null, null, null, null, stream);
			assertNull(readRequest.getContext());
		}
	}
}
