package com.sirma.sep.export.xlsx.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.sep.export.ExportHelper;
import com.sirma.sep.export.ExportService;

/**
 * Tests for {@link ExportXlsxAction}
 *
 * @author gshevkedov
 */
@RunWith(MockitoJUnitRunner.class)
public class ExportXlsxActionTest {

	private static final String URL = "/url/someUrl";
	private static final String TARGET_ID = "target-id";

	private static final String PATH_TO_TEST_RESOURCE = "xlsx/utils/";
	private static final String TEST_FILE = "request.json";

	@Mock
	private ExportService exportService;

	@Mock
	private ExportHelper exportHelper;

	@InjectMocks
	private ExportXlsxAction exportXlsxAction;

	private ExportXlsxRequest request;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		exportXlsxAction = new ExportXlsxAction();
		MockitoAnnotations.initMocks(this);
		request = new ExportXlsxRequest();
		request.setTargetId(TARGET_ID);
		request.setUserOperation(ExportXlsxRequest.EXPORT_XLSX);
		request.setRequestJson(loadJsonRequest(PATH_TO_TEST_RESOURCE + TEST_FILE));
	}

	@Test
	public void testGetName() {
		assertEquals("exportXlsx", exportXlsxAction.getName());
	}

	@Test
	public void perform_successful() throws IOException {
		when(exportHelper.createDownloadableURL(null, null, TARGET_ID, "application/excel", "-export-xlsx",
				"exportXlsx")).thenReturn(URL);
		assertNotNull(exportXlsxAction.perform(request));
		assertEquals(URL, exportXlsxAction.perform(request).toString());
	}

	@Test
	public void perform_unsuccessful() {
		// pass MS Word mimetype instead of MS Excel
		when(exportHelper.createDownloadableURL(null, null, TARGET_ID,
				"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "-export-xlsx",
				"exportXlsx")).thenReturn(URL);
		assertNull(exportXlsxAction.perform(request));
	}

	private JsonObject loadJsonRequest(String pathToJsonRequest) throws IOException {
		try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(pathToJsonRequest);
				JsonReader reader = Json.createReader(resourceAsStream);) {
			return reader.readObject();
		}
	}

}
