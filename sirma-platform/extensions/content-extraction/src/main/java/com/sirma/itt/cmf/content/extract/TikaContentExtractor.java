package com.sirma.itt.cmf.content.extract;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.content.TextExtractorExtension;
import com.sirma.itt.seip.content.descriptor.LocalFileDescriptor;
import com.sirma.itt.seip.content.descriptor.LocalProxyFileDescriptor;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * The TikaContentExtractor is responsible for text extraction of text documents. This handler is the most generic and
 * should be invoked as final step
 *
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = TextExtractorExtension.TARGET_NAME, order = 15)
public class TikaContentExtractor implements TextExtractorExtension {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/**
	 * Pattern that matches everything except all audio, video, image, binary file mimetypes and xml/html files.
	 */
	public static final String DEFAULT_TIKA_MIMETYPE_PATTERN = "^(?!audio.+|video.+|image.+|application/octet-stream|text/xml|text/x?html|application/xml).+";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "content.extract.tika.location", type = File.class, system = true, sensitive = true, label = "Location for the externally managed jar file containing a tika extraction executor!")
	private ConfigurationProperty<File> tikaExtractor;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "content.extract.tika.mimetype.pattern", type = Pattern.class, defaultValue = DEFAULT_TIKA_MIMETYPE_PATTERN, sensitive = true, system = true, label = "The pattern used to check if a file mimetype is applicable for Tika content extration.")
	private ConfigurationProperty<Pattern> mimetypeMatchPattern;

	/** The temp file provider. */
	@Inject
	private TempFileProvider tempFileProvider;

	@Override
	public String extract(FileDescriptor instance) throws IOException {
		try {
			final Parser parser = new AutoDetectParser();
			BodyContentHandler handler = new BodyContentHandler(-1);
			Metadata metadata = new Metadata();
			// TODO descriptor might be stream so reset might be needed
			try (BufferedInputStream download = new BufferedInputStream(instance.getInputStream());) {
				parser.parse(download, handler, metadata, new ParseContext());
				String mimetype = metadata.get(HttpHeaders.CONTENT_TYPE);
				LOGGER.debug("Detected mimetype is: {} for {}", mimetype, instance.getId());
			}
			String content = handler.toString();
			if (isExtractionSuccessful(content)) {
				return content;
			}
			LOGGER.warn("Tika extractor failed to extract proper content from {}", instance.getId());
			return null;
			// if dom4j classes are missing or any other exception use the external
		} catch (NoClassDefFoundError e) {
			LOGGER.trace("Fallback to external parser! There is missing some required library", e);
			return execExternalParse(instance);
		} catch (Exception e) {
			LOGGER.error("Fallback to external parser!", e);
			return execExternalParse(instance);
		}
	}

	private static boolean isExtractionSuccessful(String content) {
		return StringUtils.isNotNullOrEmpty(content);
	}

	/**
	 * Execute external parse of the file using a system process.
	 *
	 * @param instance
	 *            the instance
	 * @return the extracted by external tool content
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private String execExternalParse(FileDescriptor instance) throws IOException {
		if (tikaExtractor.isNotSet()) {
			return "";
		}
		File fileFromDescriptor = getFileFromDescriptor(instance);
		if (fileFromDescriptor == null) {
			return "";
		}
		File result = null;
		try {
			String output = fileFromDescriptor.getAbsolutePath() + ".extracted";
			result = new File(output);
			File location = tikaExtractor.get();
			ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", location.getName(),
					fileFromDescriptor.getAbsolutePath(), output);
			processBuilder.directory(location.getParentFile());
			Process exec = processBuilder.start();

			if (waitForProcess(exec) && result.exists()) {
				try (BufferedReader fileReader = new BufferedReader(
						new InputStreamReader(new FileInputStream(result), StandardCharsets.UTF_8))) {
					StringWriter writer = new StringWriter((int) result.length());
					IOUtils.copy(fileReader, writer);
					return writer.toString();
				}
			}
		} finally {
			tempFileProvider.deleteFile(fileFromDescriptor);
			tempFileProvider.deleteFile(result);
		}
		return "";
	}

	private static boolean waitForProcess(Process process) {
		try {
			int waitFor = process.waitFor();
			return waitFor == 0;
		} catch (InterruptedException e) {
			throw new EmfRuntimeException(e);
		}
	}

	@Override
	public boolean isApplicable(String mimetype) {
		if (StringUtils.isNullOrEmpty(mimetype)) {
			return false;
		}
		// TIKA supports almost all mimetypes and returns empty string even if no extraction is
		// possible without errors but wont be nessary for all types
		return mimetypeMatchPattern.get().matcher(mimetype).matches();
	}

	/**
	 * Gets the applicable input stream from the provided descriptor.
	 *
	 * @param descriptorInput
	 *            the descriptor input data
	 * @return the applicable tika input stream or throws {@link EmfRuntimeException} on error
	 * @throws IOException
	 *             on any error
	 */
	private File getFileFromDescriptor(FileDescriptor descriptorInput) throws IOException {

		FileDescriptor descriptor = descriptorInput;
		if (descriptor instanceof UploadWrapperDescriptor) {
			// downgrade it
			descriptor = ((UploadWrapperDescriptor) descriptor).getDelegate();
		}
		if (descriptor == null) {
			throw new EmfRuntimeException("Extraction data is not provided!");
		}
		if (descriptor instanceof LocalProxyFileDescriptor || descriptor instanceof LocalFileDescriptor) {
			return new File(descriptor.getId());
		}
		File createdTempFile = tempFileProvider.createTempFile(UUID.randomUUID().toString(), "");
		descriptor.writeTo(createdTempFile);

		return createdTempFile;
	}
}
