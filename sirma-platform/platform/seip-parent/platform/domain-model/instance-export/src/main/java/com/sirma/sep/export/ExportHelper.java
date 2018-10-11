package com.sirma.sep.export;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.util.file.FileUtil;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.InstanceContentService;

/**
 * Helper class with methods used for different format export
 *
 * @author Stella D
 */
@ApplicationScoped
public class ExportHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "exported.file.expiration.time", type = Integer.class, sensitive = true, defaultValue = "24", label = "Configuration that sets the expiration time for the exported PDF files. The time is set in hours. The default value is 24. This time begins, when the file is stored in the content store. After it ends the file will be deleted from the store. ")
	private ConfigurationProperty<Integer> expirationTime;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private EventService eventService;

	/**
	 * Save file as instance content. Create downloadable url from given file.
	 *
	 * @param file
	 *            File used as instance content
	 * @param fileName
	 *            Optional file name for the resulting file. File extension will be generated
	 * @param targetId
	 *            extracted from request target id
	 * @param mimeType
	 *            mimetype of converted file
	 * @param exportExtention
	 *            string which shows file extension
	 * @param operation
	 *            user operation used for audit log
	 * @return Downloadable file URL
	 */
	@Transactional
	public String createDownloadableURL(File file, String fileName, String targetId, String mimeType,
			String exportExtention, String operation) {
		if (file == null || !file.exists()) {
			throw new EmfApplicationException("Failed to export file for instance with id = " + targetId + " file "
					+ (file == null ? "is missing!" : file.getName() + " doesn't exist!"));
		}

		String exportedFileName;
		if (StringUtils.isBlank(fileName)) {
			exportedFileName = file.getName();
		} else {
			String generatedFileName = file.getName();
			// If invalid extension is given it will be replaced with valid one or valid one will be added if there is
			// no extension
			String fileExtension = "." + FilenameUtils.getExtension(generatedFileName);
			exportedFileName = FileUtil.convertToValidFileName(fileName);
			if (!exportedFileName.endsWith(fileExtension)) {
				exportedFileName = FileUtil.convertToValidFileName(fileName) + fileExtension;
			}
		}

		String purpose = UUID.randomUUID() + exportExtention;
		Content content = Content
				.createEmpty()
					.setContent(file)
					.setName(exportedFileName)
					.setMimeType(mimeType)
					.setContentLength(file.length())
					.setVersionable(false)
					.setPurpose(purpose);

		// The exported file is not an actual instance so a dummy one is used instead.
		Instance instance = new EmfInstance();
		instance.setId(targetId);
		// for now this is synchronous, but it will be changed in the near future
		instanceContentService.saveContent(instance, content);
		deleteExportedFile(file);

		instanceContentService.deleteContent(targetId, purpose, expirationTime.get(), TimeUnit.HOURS);

		// The instance being exported should be loaded so the action could be correctly registered in the audit log.
		Instance exportedInstance = domainInstanceService.loadInstance(targetId);
		eventService.fire(new AuditableEvent(exportedInstance, operation));
		return "/instances/" + targetId + "/content?download=true&purpose=" + purpose;

	}

	/**
	 * Deletes the exported file from the server store, because we don't need it anymore.
	 *
	 * @param file
	 *            the file that should be deleted
	 */
	private static void deleteExportedFile(File file) {
		try {
			Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			LOGGER.warn("Could not delete the temporary export file: {}", file.getAbsolutePath(), e);
		}
	}
}
