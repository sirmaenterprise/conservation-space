package com.sirma.itt.seip.export.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.export.ExportHelper;
import com.sirma.itt.seip.export.ListDataXlsxExporter;

/**
 * Tests for {@link ExportListDataXlsxAction}
 *
 * @author gshevkedov
 */
@RunWith(MockitoJUnitRunner.class)
public class ExportListDataXlsxActionTest {

	private static final String URL = "/url/someUrl";
	private static final String TARGET_ID = "target-id";

	@Mock
	private ListDataXlsxExporter xlsxExporter;

	@Mock
	private ExportHelper exportHelper;

	@InjectMocks
	private ExportListDataXlsxAction exportXlsxAction;

	private ExportListDataXlsxRequest request;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		exportXlsxAction = new ExportListDataXlsxAction();
		MockitoAnnotations.initMocks(this);
		request = new ExportListDataXlsxRequest();
		request.setTargetId(TARGET_ID);
		request.setUserOperation(ExportListDataXlsxRequest.EXPORT_XLSX);
	}

	@Test
	public void testGetName() {
		assertEquals("exportXlsx", exportXlsxAction.getName());
	}

	@Test
	public void perform_successful() throws IOException {
		when(exportHelper.createDownloadableURL(null, null, TARGET_ID,
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "-export-xlsx", null))
						.thenReturn(URL);
		assertNotNull(exportXlsxAction.perform(request));
		assertEquals(URL, exportXlsxAction.perform(request).toString());
	}

	@Test
	public void perform_unsuccessful() {
		// pass MS Word mimetype instead of MS Excel
		when(exportHelper.createDownloadableURL(null, null, TARGET_ID,
				"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "-export-xlsx", null))
						.thenReturn(URL);
		assertNull(exportXlsxAction.perform(request));
	}
}
