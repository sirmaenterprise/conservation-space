package com.sirma.sep.ocr.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sirma.sep.ocr.exception.OCRFailureException;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link TesseractOCRIntegration}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 29/11/2017
 */
public class TesseractOCRIntegrationTest {

	@InjectMocks
	private TesseractOCRIntegration tesseractService;
	@Mock
	private TesseractOCRProperties ocrProperties;
	@Mock
	private TesseractProvider provider;
	@Mock
	private Tesseract tesseract;

	private File tempFile;

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(provider.getProvider()).thenReturn(tesseract);

		// mock temp file that is going to be OCRed.
		Path tempFile = Files.createTempFile("tempFiles", ".tmp");
		List<String> lines = Collections.singletonList("Line");
		Files.write(tempFile, lines, Charset.defaultCharset(), StandardOpenOption.DELETE_ON_CLOSE);
		this.tempFile = tempFile.toFile();
	}

	@Test
	public void createDocument_successful() throws Exception {
		tesseractService.init();
		List<ITesseract.RenderedFormat> ocrOutputFileType;
		ocrOutputFileType = new ArrayList<>(1);
		ocrOutputFileType.add(ITesseract.RenderedFormat.PDF);

		File ocrFile = tesseractService.createDocument(tempFile, "eng");
		verify(tesseract).createDocuments(any(String.class), any(String.class), eq(ocrOutputFileType));
		assertNotNull(ocrFile);
		assertTrue(StringUtils.containsIgnoreCase(ocrFile.getName(), ".pdf"));
	}

	@Test
	public void should_default_language_be_used() throws Exception {
		File ocrFile = tesseractService.createDocument(tempFile, null);
		verify(tesseract).createDocuments(any(String.class), any(String.class), any(List.class));
		verify(ocrProperties, times(2)).getLanguage();
		assertNotNull(ocrFile);
	}

	@Test(expected = OCRFailureException.class)
	public void should_createDocuments_throw_exception() throws Exception {
		doThrow(new TesseractException()).when(tesseract).createDocuments(any(String.class), any(String.class), any
				(List.class));
		tesseractService.createDocument(tempFile, null);
	}

	@Test(expected = OCRFailureException.class)
	public void should_fail_when_provider_missing() throws Exception {
		when(provider.getProvider()).thenReturn(null);
		tesseractService.createDocument(tempFile, null);
	}
}