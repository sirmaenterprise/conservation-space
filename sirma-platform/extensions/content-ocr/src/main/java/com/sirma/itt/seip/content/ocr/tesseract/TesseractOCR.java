package com.sirma.itt.seip.content.ocr.tesseract;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.content.descriptor.LocalFileDescriptor;
import com.sirma.itt.seip.content.descriptor.LocalProxyFileDescriptor;
import com.sirma.itt.seip.content.ocr.OCREngine;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.plugin.Extension;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 * The Class TesseractOCR is responsible to execute ocr on content by Tesseract
 * Engine.
 * 
 * @author Hristo Lungov
 */
@Singleton
@Extension(target = OCREngine.TARGET_NAME, order = 1)
public class TesseractOCR implements OCREngine {

	private static final String OCR_TESSERACT_DATAPATH = "ocr.tesseract.datapath";
	private static final String OCR_TESSERACT_LANGUAGES = "ocr.tesseract.languages";
	public static final String APPLICATION_PDF = "application/pdf";
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String DEFAULT_TESSERACT_MIMETYPE_PATTERN = "^(image.+|" + APPLICATION_PDF + ")";
	private static final Pattern APPLICABLE_PATTERN = Pattern.compile(DEFAULT_TESSERACT_MIMETYPE_PATTERN);

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "ocr.tesseract.enabled", type = Boolean.class, defaultValue = "true", label = "Tesseract OCR Plugin Enabled/Disabled.")
	private ConfigurationProperty<Boolean> isTesseractEnabled;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = OCR_TESSERACT_DATAPATH, type = String.class, label = "Tesseract OCR Plugin DataPath.")
	private ConfigurationProperty<String> tesseractDataPath;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = OCR_TESSERACT_LANGUAGES, type = String.class, defaultValue = "eng", label = "Tesseract OCR Plugin Languages.")
	private ConfigurationProperty<String> tesseractLanguages;

	@Inject
	@Configuration(TESSERACT_INSTANCE)
	private ConfigurationProperty<ITesseract> tesseractService;

	@Inject
	private TempFileProvider tempFileProvider;

	@ConfigurationGroupDefinition(type = ITesseract.class, properties = { OCR_TESSERACT_DATAPATH,
			OCR_TESSERACT_LANGUAGES })
	private static final String TESSERACT_INSTANCE = "orc.tesseract.service";

	/**
	 * Builds the tesseract instance.
	 *
	 * @param converterContext
	 *            the converter context
	 * @return the itesseract instance
	 */
	@ConfigurationConverter(TESSERACT_INSTANCE)
	static ITesseract buildTesseractInstance(GroupConverterContext converterContext) {
		String dataPath = converterContext.get(OCR_TESSERACT_DATAPATH);
		if (StringUtils.isNullOrEmpty(dataPath)) {
			LOGGER.error("TESSERACT OCR WILL BE DISABLED, because of missing config: {}", OCR_TESSERACT_DATAPATH);
			return null;
		}
		String languages = converterContext.get(OCR_TESSERACT_LANGUAGES);
		if (StringUtils.isNullOrEmpty(dataPath)) {
			LOGGER.error("TESSERACT OCR WILL BE DISABLED, because of missing config: {}", OCR_TESSERACT_LANGUAGES);
			return null;
		}
		ITesseract tesseract = new Tesseract();
		tesseract.setDatapath(dataPath);
		tesseract.setLanguage(languages);
		ImageIO.scanForPlugins();
		return tesseract;
	}

	@Override
	public boolean isApplicable(String mimetype) {
		if (!isTesseractEnabled.get()) {
			return false;
		}
		if (StringUtils.isNullOrEmpty(mimetype)) {
			return false;
		}
		// TIKA supports almost all mimetypes and returns empty string even if
		// no extraction is
		// possible without errors but wont be nessary for all types
		return APPLICABLE_PATTERN.matcher(mimetype).matches();
	}

	@Override
	public String doOcr(String mimeType, FileDescriptor descriptor) throws IOException {
		File inputFile = null;
		try {
			inputFile = getFileFromDescriptor(mimeType, descriptor);

			// Activates the JNA Debugging
			if (LOGGER.isTraceEnabled()) {
				System.setProperty("jna.debug_load", "true");
				System.setProperty("jna.debug_load.jna", "true");
			}
			ITesseract tess = tesseractService.get();
			return TesseractOCR.executeOcr(descriptor, inputFile, tess);
		} catch (MimeTypeException e) {
			throw new EmfRuntimeException("Tesseract OCR failure for " + descriptor.getId() + " !", e);
		} finally {
			tempFileProvider.deleteFile(inputFile);
		}
	}

	private static String executeOcr(FileDescriptor descriptor, File inputFile, ITesseract tess) {
		try {
			if (tess == null) {
				return null;
			}
			return tess.doOCR(inputFile);
		} catch (TesseractException e) {
			throw new EmfRuntimeException("Tesseract OCR failure for " + descriptor.getId() + " !", e);
		}
	}

	/**
	 * Gets the applicable input stream from the provided descriptor.
	 *
	 * @param mimeType
	 *            the mime type
	 * @param descriptorInput
	 *            the descriptor input
	 * @return the file from descriptor
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws MimeTypeException
	 *             the mime type exception
	 */
	private File getFileFromDescriptor(String mimeType, FileDescriptor descriptorInput)
			throws IOException, MimeTypeException {
		FileDescriptor descriptor = descriptorInput;
		if (descriptor instanceof UploadWrapperDescriptor) {
			descriptor = ((UploadWrapperDescriptor) descriptor).getDelegate();
		}
		if (descriptor == null) {
			throw new EmfRuntimeException("FileDescriptor data is not provided!");
		}
		if (descriptor instanceof LocalProxyFileDescriptor || descriptor instanceof LocalFileDescriptor) {
			return new File(descriptor.getId());
		}
		MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
		MimeType type = allTypes.forName(mimeType);
		File createdTempFile = tempFileProvider.createTempFile(UUID.randomUUID().toString(), type.getExtension());
		descriptor.writeTo(createdTempFile);
		return createdTempFile;
	}

}
