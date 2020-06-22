package com.sirma.sep.export.rest;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.sep.export.pdf.action.ExportPDFRequest;
import com.sirma.sep.export.rest.ExportRestService;
import com.sirma.sep.export.word.action.ExportWordRequest;
import com.sirma.sep.export.xlsx.action.ExportXlsxRequest;

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
		verify(actions).callSlowAction(request);
	}

	@Test
	public void exportToWord_callActionCalled() {
		ExportWordRequest request = new ExportWordRequest();
		service.exportToWord(request);
		verify(actions).callSlowAction(request);
	}

	@Test
	public void exportToExcel_callActionCalled() {
		ExportXlsxRequest request = new ExportXlsxRequest();
		service.exportToXlsx(request);
		verify(actions).callSlowAction(request);
	}

}
