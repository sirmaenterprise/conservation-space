package com.sirma.itt.seip.export;

import java.io.File;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.util.file.FileUtil;

/**
 * Helper class with methods used for different format export
 *
 * @author Stella D
 */
@ApplicationScoped
public class ExportHelper {

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
	 * Deletes the exported file from the server store, because we don't need it anymore.
	 *
	 * @param file
	 *            the file that should be deleted
	 */
	public static void deleteExportedFile(File file) {
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * Extract cookies from request and convert it to string pairs
	 *
	 * @param info
	 *            Request information
	 * @return cookies pair
	 */
	public static StringPair[] getRequestCookies(RequestInfo info) {
		return info
				.getHeaders()
					.getCookies()
					.entrySet()
					.stream()
					.map(Entry::getValue)
					.map(cookie -> new StringPair(cookie.getName(), cookie.getValue()))
					.toArray(StringPair[]::new);
	}

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
	public String createDownloadableURL(File file, String fileName, String targetId, String mimeType,
			String exportExtention, String operation) {
		if (!file.exists()) {
			throw new EmfApplicationException("Faild to export file for instance with id = " + targetId + " file "
					+ file.getName() + " don't exist");
		}

		String exportedFileName;
		if (StringUtils.isNullOrEmpty(fileName)) {
			exportedFileName = file.getName();
		} else {
			String generatedFileName = file.getName();
			// If invalid extension is given it will be replaced with valid one or valid one will be added if there is no extension
			String fileExtension = FilenameUtils.getExtension(generatedFileName);
			exportedFileName = FileUtil.convertToValidFileName(fileName) + "." + fileExtension;
		}

		String purpose = UUID.randomUUID() + exportExtention;
		Content content = Content.createEmpty().setContent(file).setName(exportedFileName).setMimeType(mimeType)
				.setContentLength(file.length()).setVersionable(false).setPurpose(purpose);

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
}