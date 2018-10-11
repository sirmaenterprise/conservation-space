package com.sirma.sep.ocr.service;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import com.sirma.sep.ocr.exception.OCRFailureException;

import net.sourceforge.tess4j.ITesseract;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * {@link TesseractOCRIntegration} is high level service for OCR processing of documents and images. As backend
 * implementation is used the {@link ITesseract} engine.The service accept various formats and return the recognized
 * content.
 *
 * @author bbanchev
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 */
@Component
@Profile("service")
public class TesseractOCRIntegration {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String PDF_FILE_EXTENSION = ".pdf";
	private static final String TESSERACT_FAIL_ERROR_MSG = "Tesseract OCR failure for: ";

	private TesseractOCRProperties ocrProperties;
	private TesseractProvider tesseract;

	private List<ITesseract.RenderedFormat> ocrOutputFileType;

	/**
	 * Inject the needed beans.
	 *
	 * @param ocrProperties the application configuration properties..
	 */
	@Autowired
	public TesseractOCRIntegration(TesseractOCRProperties ocrProperties, TesseractProvider tesseractProvider) {
		this.ocrProperties = ocrProperties;
		this.tesseract = tesseractProvider;
	}

	@PostConstruct
	public void init() {
		// OCR output type setter.
		ocrOutputFileType = new ArrayList<>(1);
		ocrOutputFileType.add(ITesseract.RenderedFormat.PDF);
	}

	/**
	 * Executes OCR by delegating provided file to {@link ITesseract} service.
	 * Generates searchable pdf file.
	 *
	 * @param inputFile the file to process.
	 * @return the recognized content in searchable pdf file or null if {@link ITesseract} service is not initialized.
	 * @throws OCRFailureException on any OCR failure.
	 */
	File createDocument(File inputFile, String language) throws OCRFailureException {
		if (tesseract.getProvider() == null) {
			throw new OCRFailureException("Could not get the Tesseract provider, message will not be processed.");
		}
		String inputFileName = inputFile.getName();
		String outputFileName = FilenameUtils.getFullPath(inputFileName)
				.concat(FilenameUtils.getBaseName(inputFileName));
		setOcrLanguage(language);
		try {
			tesseract.getProvider().createDocuments(inputFile.getPath(), outputFileName, ocrOutputFileType);
		} catch (Exception e) {
			throw new OCRFailureException(TESSERACT_FAIL_ERROR_MSG + inputFileName + " !", e);
		}
		return new File(outputFileName + PDF_FILE_EXTENSION);
	}

	/**
	 * Sets the ocr language. The value is used from the message, if however it is missing from the message we use
	 * the default one.
	 */
	private void setOcrLanguage(String language) {
		if (StringUtils.isEmpty(language)) {
			tesseract.getProvider().setLanguage(ocrProperties.getLanguage());
			LOGGER.warn("Was unable to read the ocr language from the jms message. Using the container configuration "
					+ "instead [{}]", ocrProperties.getLanguage());
		} else {
			LOGGER.debug("Language {} was passed from the jms message. Using it to execute the OCR.", language);
			tesseract.getProvider().setLanguage(language);
		}
	}
}
