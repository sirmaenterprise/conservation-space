package com.sirma.sep.ocr.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import javax.jms.BytesMessage;

import com.sirma.sep.ocr.ServiceTest;
import com.sirma.sep.ocr.entity.InputDocument;
import com.sirma.sep.ocr.exception.OCRFailureException;

import org.junit.Test;
import org.mockito.internal.verification.Times;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Tests for {@link DocumentProcessor}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 */
public class DocumentProcessorTest extends ServiceTest {

	@MockBean
	private TesseractOCRIntegration ocrService;
	@Autowired
	private DocumentProcessor documentProcessor;

	@Test
	public void testProcess() throws Exception {
		InputDocument document = mock(InputDocument.class);
		BytesMessage bytesMessage = mock(BytesMessage.class);
		when(document.getOriginalMessage()).thenReturn(bytesMessage);
		when(document.getFileName()).thenReturn("testFile");
		when(document.getFileExtension()).thenReturn(".pdf");
		File testFile = new File("test");
		when(ocrService.createDocument(any(File.class), any(String.class))).thenReturn(testFile);
		documentProcessor.process(document);
		verify(ocrService, new Times(1)).createDocument(any(File.class), any(String.class));
	}

	@Test(expected = OCRFailureException.class)
	public void testProcess_generic_exception() throws Exception {
		InputDocument document = mock(InputDocument.class);
		when(ocrService.createDocument(any(File.class), any(String.class))).thenThrow(new IllegalArgumentException());
		documentProcessor.process(document);
	}

	@Test(expected = OCRFailureException.class)
	public void testProcess_exception() throws Exception {
		InputDocument document = mock(InputDocument.class);
		when(ocrService.createDocument(any(File.class), any(String.class))).thenThrow(new OCRFailureException("error",
				mock(Exception
						.class)));
		documentProcessor.process(document);
	}
}
