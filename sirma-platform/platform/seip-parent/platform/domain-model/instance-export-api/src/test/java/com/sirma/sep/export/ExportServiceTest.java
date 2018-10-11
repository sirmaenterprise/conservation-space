package com.sirma.sep.export;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.sep.export.pdf.PDFExportRequest.PDFExportRequestBuilder;
import com.sirma.sep.export.word.WordExportRequest;
import com.sirma.sep.export.word.WordExportRequest.WordExportRequestBuilder;

/**
 * Test for {@link ExportService}.
 *
 * @author A. Kunchev
 */
public class ExportServiceTest {

	@InjectMocks
	private ExportService service;

	private List<FileExporter<ExportRequest>> exportersList = new LinkedList<>();

	@Spy
	private Plugins<FileExporter<ExportRequest>> exporters = new Plugins<>("fileExporter", exportersList);

	@Mock
	private FileExporter<ExportRequest> exporter;

	@Before
	public void setup() {
		service = new ExportService();

		MockitoAnnotations.initMocks(this);

		exportersList.clear();
		exportersList.add(exporter);

		when(exporter.getName()).thenReturn(SupportedExportFormats.WORD.getFormat());

	}

	@Test(expected = NullPointerException.class)
	public void export_nullRequest() {
		service.export(null);
	}

	@Test(expected = ExportFailedException.class)
	public void export_noApplicableExporter() {
		service.export(new PDFExportRequestBuilder().setInstanceURI(URI.create("")).buildRequest());
	}

	@Test(expected = ExportFailedException.class)
	public void export_errorWithleExporting() throws ContentExportException {
		when(exporter.export(any(WordExportRequest.class))).thenThrow(new ContentExportException(""));
		service.export(new WordExportRequestBuilder().setInstanceId("instance-id").buildRequest());
	}

	@Test(expected = ExportFailedException.class)
	public void export_emptyOptionalAsExportResult() throws ContentExportException {
		when(exporter.export(any(WordExportRequest.class))).thenReturn(Optional.empty());
		service.export(new WordExportRequestBuilder().setInstanceId("instance-id").buildRequest());
	}

	@Test
	public void export_successfulExport() throws ContentExportException {
		File file = mock(File.class);
		when(exporter.export(any(WordExportRequest.class))).thenReturn(Optional.of(file));
		File result = service.export(new WordExportRequestBuilder().setInstanceId("instance-id").buildRequest());
		assertNotNull(result);
	}

}
