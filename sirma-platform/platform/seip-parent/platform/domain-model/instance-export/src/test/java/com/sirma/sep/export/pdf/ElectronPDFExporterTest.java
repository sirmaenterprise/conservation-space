package com.sirma.sep.export.pdf;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.testutil.rest.RandomPortGenerator;
import com.sirma.sep.export.ContentExportException;
import com.sirma.sep.export.pdf.PDFExportRequest.PDFExportRequestBuilder;

/**
 * Test for {@link ElectronPDFExporter}.
 *
 * @author A. Kunchev
 * @author bbanchev
 */
public class ElectronPDFExporterTest {

	private static final int PORT = RandomPortGenerator.generatePort(11000, 11999);

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().port(PORT));

	@InjectMocks
	private ElectronPDFExporter exporter;

	@Mock
	private TempFileProvider tempFileProvider;

	@Mock
	private ConfigurationProperty<String> exportServerURL;

	@Mock
	private ConfigurationProperty<Integer> timeout;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(timeout.get()).thenReturn(1000);
	}

	@Test
	public void initialize_tempFileProviderCalled() {
		exporter.initialize();
		verify(tempFileProvider).createLongLifeTempDir(anyString());
	}

	@Test
	public void exportToPdf_exportToolNotSet() throws ContentExportException {
		when(exportServerURL.isNotSet()).thenReturn(true);
		exporter.initialize();
		Optional<File> pdf = exporter.export(defaultRequest());
		assertFalse(pdf.isPresent());
	}

	@Test
	public void exportToPdf_exportToolSet_fileResult() throws ContentExportException, IOException {
		File file = mockTempFileProvider();
		try {
			JsonObjectBuilder requestData = setUpValidExporeter();
			byte[] rawData = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("pdf/blank.pdf"));
			wireMockRule.stubFor(post(urlEqualTo("/export/pdf"))
					.withRequestBody(new EqualToJsonPattern(requestData.build().toString(), Boolean.TRUE, Boolean.TRUE))
					.willReturn(aResponse().withStatus(200).withBody(rawData)));
			Optional<File> pdf = exporter.export(defaultRequest());
			assertTrue(pdf.isPresent());
			assertTrue(pdf.get().canRead());
		} finally {
			file.delete();
		}
	}

	private JsonObjectBuilder setUpValidExporeter() throws IOException {
		when(exportServerURL.isSet()).thenReturn(true);
		when(exportServerURL.get()).thenReturn("http://0.0.0.0:"+PORT+"/");
		when(timeout.get()).thenReturn(50);

		JsonObjectBuilder createObjectBuilder = Json
				.createObjectBuilder()
				.add("url", URI.create("http://local/ui2/emf:id").toASCIIString())
				.add("timeout", 50000L)
				.add("file-name", "myfile.txt");
		exporter.initialize();
		return createObjectBuilder;
	}

	private static PDFExportRequest defaultRequest() {
		return new PDFExportRequestBuilder()
				.setInstanceId("emf:id")
				.setInstanceURI(URI.create("http://local/ui2/emf:id"))
				.setFileName("myfile.txt")
				.buildRequest();
	}

	private File mockTempFileProvider() throws IOException {
		File file = File.createTempFile("pdf_store", "_pdf");
		file.delete();
		file.mkdirs();
		when(tempFileProvider.createLongLifeTempDir(anyString())).thenReturn(file);
		return file;
	}

	@Test
	public void getName() {
		assertEquals("pdf", exporter.getName());
	}

}
