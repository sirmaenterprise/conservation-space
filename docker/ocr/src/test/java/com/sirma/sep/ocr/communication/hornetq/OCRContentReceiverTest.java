package com.sirma.sep.ocr.communication.hornetq;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.regex.Pattern;

import javax.jms.Message;

import com.sirma.sep.ocr.entity.ContentMessageAttributes;
import com.sirma.sep.ocr.exception.OCRFailureException;
import com.sirma.sep.ocr.service.DocumentProcessor;
import com.sirma.sep.ocr.service.TesseractOCRProperties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link OCRContentReceiver}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 25/10/2017
 */
public class OCRContentReceiverTest {

	@Mock
	private DocumentProcessor docProcessor;
	@Mock
	private TesseractOCRProperties ocrProperties;
	@Mock
	private OCRContentSender sender;
	@InjectMocks
	private OCRContentReceiver cut;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_receiveMessage() throws Exception {
		Message message = mock(Message.class);
		when(message.getStringProperty(ContentMessageAttributes.MIMETYPE.toString())).thenReturn("application/pdf");

		TesseractOCRProperties.Mimetype mimetype = new TesseractOCRProperties.Mimetype();
		mimetype.setPattern(Pattern.compile(".*"));
		when(ocrProperties.getMimetype()).thenReturn(mimetype);
		cut.receiveMessage(message);
		verify(docProcessor).process(any());
		verify(sender).sendOCRContent(any(), any(), any());
	}

	@Test(expected = OCRFailureException.class)
	public void test_receiveMessage_error() throws Exception {
		Message message = mock(Message.class);
		when(message.getStringProperty(ContentMessageAttributes.MIMETYPE.toString())).thenReturn("application/pdf");

		TesseractOCRProperties.Mimetype mimetype = new TesseractOCRProperties.Mimetype();
		mimetype.setPattern(Pattern.compile(".*"));
		when(ocrProperties.getMimetype()).thenReturn(mimetype);
		doThrow(new OCRFailureException(mock(Exception.class))).when(docProcessor).process(any());
		cut.receiveMessage(message);
	}

}