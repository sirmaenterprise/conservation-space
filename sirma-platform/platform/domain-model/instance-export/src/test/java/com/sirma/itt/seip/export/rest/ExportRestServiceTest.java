package com.sirma.itt.seip.export.rest;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Test for {@link ExportRestService}.
 *
 * @author A. Kunchev
 */
public class ExportRestServiceTest {

	@InjectMocks
	private ExportRestService service;

	@Mock
	private Actions actions;

	@Before
	public void setup() {
		service = new ExportRestService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void exportToPDF_callActionCalled() {
		ExportPDFRequest request = new ExportPDFRequest();
		service.exportToPDF(request);
		verify(actions).callAction(request);
	}

	@Test
	public void exportToWord_callActionCalled() {
		ExportWordRequest request = new ExportWordRequest();
		service.exportToWord(request);
		verify(actions).callAction(request);
	}

	@Test
	public void exportToExcel_callActionCalled() {
		ExportListDataXlsxRequest request = new ExportListDataXlsxRequest();
		service.exportToXlsx(request);
		verify(actions).callAction(request);
	}

}
