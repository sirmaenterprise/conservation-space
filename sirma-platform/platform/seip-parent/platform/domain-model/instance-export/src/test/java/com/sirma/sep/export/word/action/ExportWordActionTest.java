package com.sirma.sep.export.word.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.export.ExportHelper;
import com.sirma.sep.export.ExportService;

/**
 * Test for {@link ExportWordAction}.
 *
 * @author Stella D
 */
public class ExportWordActionTest {

	@InjectMocks
	private ExportWordAction action;

	@Mock
	private ExportService exportService;

	@Mock
	private ExportHelper exportHelper;

	/**
	 * Setup mockito before each test method to run
	 */
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Gets the name.
	 */
	@Test
	public void getName() {
		assertEquals(ExportWordRequest.EXPORT_WORD, action.getName());
	}

	/**
	 * Perform_successful.
	 */
	@Test
	public void perform_successful() {
		mock(File.class);
		ExportWordRequest request = new ExportWordRequest();
		request.setTargetId("target-id");
		request.setUrl("page-to-export-url");
		request.setUserOperation(ExportWordRequest.EXPORT_WORD);
		when(exportHelper.createDownloadableURL(null, null, "target-id",
				"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "-export-word",
				"exportWord")).thenReturn("/url/someUrl");
		String downloadLink = action.perform(request);
		assertNotNull(downloadLink);
	}
}
