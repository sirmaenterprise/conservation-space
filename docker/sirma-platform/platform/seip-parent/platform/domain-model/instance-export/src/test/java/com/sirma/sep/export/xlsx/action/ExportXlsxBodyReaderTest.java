package com.sirma.sep.export.xlsx.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Tests for {@link ExportXlsxBodyReader}
 *
 * @author gshevkedov
 */
public class ExportXlsxBodyReaderTest {

	private static final String TEST_FILE_EMPTY_JSON = "xlsx/rest/test-file-empty-json.json";
	private static final String TEST_FILE_AUTOMATICALLY = "xlsx/rest/test-file-automatically.json";
	private static final String EXPECTED_TARGET_ID = "instanceId";
	private static final String EXPECTED_FILE_NAME = "Workbook.xlsx";

	@Mock
	private RequestInfo request;

	@InjectMocks
	private ExportXlsxBodyReader reader;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		setupPathParamId();
	}

	@Test
	public void should_NotBeReadable_When_TypeIsNotSupported() {
		assertFalse(reader.isReadable(String.class, null, null, null));
	}

	@Test
	public void should_BeReadable_When_TypeIsSupported() {
		assertTrue(reader.isReadable(ExportXlsxRequest.class, null, null, null));
	}

	@Test(expected = BadRequestException.class)
	public void should_ThrowBadRequestException_When_RequestJsonIsEmpty() throws IOException {
		try (InputStream stream = loadTestJson(TEST_FILE_EMPTY_JSON)) {
			reader.readFrom(ExportXlsxRequest.class, null, null, null, null, stream);
		}
	}

	@Test
	public void should_ReturnCorrectExportXlsxRequest_When_ConfigurationIsCorrect() throws IOException {
		// Setup request.
		setupPathParamId();
		ExportXlsxRequest exportXlsxRequest = null;

		try (InputStream stream = loadTestJson(TEST_FILE_AUTOMATICALLY)) {
			exportXlsxRequest = reader.readFrom(ExportXlsxRequest.class, null, null, null, null, stream);
		}

		assertNotNull(exportXlsxRequest);
		assertEquals(EXPECTED_FILE_NAME, exportXlsxRequest.getFileName());
		assertEquals(EXPECTED_TARGET_ID, exportXlsxRequest.getTargetId());
		assertEquals(exportXlsxRequest.getOperation(), ExportXlsxRequest.EXPORT_XLSX);
		assertNotNull(exportXlsxRequest.getRequestJson());
	}

	private InputStream loadTestJson(String pathToFile) {
		return getClass().getClassLoader().getResourceAsStream(pathToFile);
	}

	private void setupPathParamId() {
		UriInfo uriInfo = mock(UriInfo.class);
		MultivaluedMap<String, String> queryParams = mock(MultivaluedMap.class);
		when(queryParams.get("id")).thenReturn(Arrays.asList(EXPECTED_TARGET_ID));
		when(request.getUriInfo()).thenReturn(uriInfo);
		when(request.getUriInfo().getQueryParameters()).thenReturn(queryParams);
		when(request.getUriInfo().getPathParameters()).thenReturn(queryParams);
	}
}
