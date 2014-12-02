package com.sirma.itt.cmf.content.extract;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.LocalFileDescriptor;
import com.sirma.itt.cmf.beans.LocalProxyFileDescriptor;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.content.configuration.ContentExtractionConfigurationProperties;
import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.content.extract.TextExtractor;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.io.TempFileProvider;
import com.sirma.itt.emf.plugin.Extension;

/**
 * The TikaContentExtractor is responsible for text extraction of text documents.
 */
@ApplicationScoped
@Extension(target = TextExtractor.TARGET_NAME)
public class TikaContentExtractor implements TextExtractor {

	/** The LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(TikaContentExtractor.class);

	/** The tika extractor. */
	@Config(name = ContentExtractionConfigurationProperties.CONTENT_EXTRACT_TIKA_LOCATION)
	@Inject
	private String tikaExtractor;

	/** The temp file provider. */
	@Inject
	private TempFileProvider tempFileProvider;

	/** The tika jar location. */
	private File tikaJarLocation;

	/**
	 * Initilize the jar location
	 */
	@PostConstruct
	public void initilize() {
		if (tikaExtractor != null) {
			File file = new File(tikaExtractor);
			if (file.canRead()) {
				tikaJarLocation = file;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String extract(FileDescriptor instance) throws Exception {
		try {
			final Parser parser = new AutoDetectParser();
			BodyContentHandler handler = new BodyContentHandler(-1);
			Metadata metadata = new Metadata();
			// TODO descriptor might be stream so reset might be needed
			try (BufferedInputStream download = new BufferedInputStream(instance.getInputStream());) {
				parser.parse(download, handler, metadata, new ParseContext());
				String mimetype = metadata.get(Metadata.CONTENT_TYPE);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Detected mimetype is: " + mimetype);
				}
			}
			return handler.toString();
			// if dom4j classes are missing or any other exception use the external
		} catch (NoClassDefFoundError e) {
			return execExternalParse(instance);
		} catch (Exception e) {
			return execExternalParse(instance);
		}
	}

	/**
	 * Execute external parse of the file using a system process.
	 *
	 * @param instance
	 *            the instance
	 * @return the string
	 * @throws Exception
	 *             the exception
	 */
	private String execExternalParse(FileDescriptor instance) throws Exception {
		if (tikaJarLocation == null) {
			return "";
		}
		File fileFromDescriptor = getFileFromDescriptor(instance);
		if (fileFromDescriptor == null) {
			return "";
		}
		String output = null;
		try {

			output = fileFromDescriptor.getAbsolutePath() + ".extracted";
			ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar",
					tikaJarLocation.getName(), fileFromDescriptor.getAbsolutePath(), output);
			processBuilder.directory(tikaJarLocation.getParentFile());
			Process exec = processBuilder.start();
			int waitFor = exec.waitFor();
			if (waitFor == 0) {
				try (BufferedReader fileReader = new BufferedReader(new FileReader(output));) {
					String line = null;
					StringBuilder builder = new StringBuilder();
					while ((line = fileReader.readLine()) != null) {
						builder.append(line);
					}
					return builder.toString();
				}
			}
		} finally {
			if (output != null) {
				new File(output).delete();
			}
		}
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isApplicable(Instance instance) throws Exception {

		// TIKA supports almost all mimetypes and returns empty string even if no extraction is
		// possible without errors but wont be nessary for all types
		if ((instance != null)
				&& (instance.getProperties().get(DocumentProperties.MIMETYPE) instanceof String)) {
			String mimetype = (String) instance.getProperties().get(
					DocumentProperties.MIMETYPE);
			if ((mimetype != null) && !mimetype.startsWith("audio/")
					&& !mimetype.startsWith("video/") && !mimetype.startsWith("image/")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the applicable input stream from the provided descriptor.
	 *
	 * @param descriptorInput
	 *            the descriptor input data
	 * @return the applicable tika input stream or throws {@link EmfRuntimeException} on error
	 * @throws Exception
	 *             on any error
	 */
	private File getFileFromDescriptor(FileDescriptor descriptorInput) throws Exception {

		FileDescriptor descriptor = descriptorInput;
		if (descriptor instanceof UploadWrapperDescriptor) {
			// downgrade it
			descriptor = ((UploadWrapperDescriptor) descriptor).getDelegate();
		}
		if (descriptor == null) {
			throw new EmfRuntimeException("Extraction data is not provided!");
		}
		if (descriptor instanceof LocalProxyFileDescriptor) {
			return new File(descriptor.getId());
		} else if (descriptor instanceof LocalFileDescriptor) {
			return new File(descriptor.getId());
		}
		File createdTempFile = tempFileProvider.createTempFile(UUID.randomUUID().toString(), "");
		FileOutputStream fileOutputStream = new FileOutputStream(createdTempFile);
		IOUtils.copy(descriptor.getInputStream(), fileOutputStream);
		IOUtils.closeQuietly(fileOutputStream);
		return createdTempFile;
	}
}
