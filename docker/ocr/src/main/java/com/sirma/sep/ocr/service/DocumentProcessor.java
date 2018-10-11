package com.sirma.sep.ocr.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.invoke.MethodHandles;
import java.util.UUID;

import javax.jms.Message;

import com.sirma.sep.ocr.entity.InputDocument;
import com.sirma.sep.ocr.exception.OCRFailureException;
import com.sirma.sep.ocr.utils.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * The {@link DocumentProcessor} is handler for incoming document that handles the document, send it for OCR and return
 * the result to the result queue.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 */
@Component
@Profile("service")
public class DocumentProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final File WORKING_DIR = createTempDir("OCR_source");

	private final TesseractOCRIntegration ocrService;

	/**
	 * Injects the beans in the current class.
	 *
	 * @param ocrService the ocr service.
	 */
	@Autowired
	public DocumentProcessor(TesseractOCRIntegration ocrService) {
		this.ocrService = ocrService;
	}

	/**
	 * Process new document request and transfers the recognize text back to the completed ocr queue.
	 *
	 * @param document the input document source data.
	 * @throws OCRFailureException the OCR failure exception.
	 */
	public File process(InputDocument document) throws OCRFailureException {
		File downloaded = null;
		try {
			Message origin = document.getOriginalMessage();
			downloaded = File.createTempFile(UUID.randomUUID() + document.getFileName(), document.getFileExtension(),
					WORKING_DIR);
			try (BufferedOutputStream bufferedOutput = new BufferedOutputStream(new FileOutputStream(downloaded))) {
				origin.setObjectProperty("JMS_HQ_SaveStream", bufferedOutput);
			}
			File ocredFile = ocrService.createDocument(downloaded, document.getOcrLanguage());
			LOGGER.debug("Document with details: [{}] is successfully recognized with result: [{}]!", document,
					ocredFile.getAbsolutePath());
			return ocredFile;
		} catch (OCRFailureException e) {
			throw e;
		} catch (Exception e) {
			throw new OCRFailureException(e);
		} finally {
			FileUtils.deleteFile(downloaded);
		}
	}

	/**
	 * Creates a temporary directory in the 'java.io.tmpdir' with the specified name
	 *
	 * @param dirName is the directory name
	 * @return the created directory or {@link RuntimeException} if directory could not be created
	 */
	private static File createTempDir(String dirName) {
		final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
		File newTempDir = new File(sysTempDir, dirName);

		if (newTempDir.exists() || newTempDir.mkdirs()) {
			return newTempDir;
		}
		throw new WritingFileFailedException("Failed to create temp dir named " + newTempDir.getAbsolutePath());
	}
}
