package com.sirma.itt.seip.instance.editoffline.updaters;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.DocumentSummaryInformation;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.editoffline.exception.FileCustomPropertiesUpdateException;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Abstract class for all MSOffice files custom property updaters.
 *
 * @author T. Dossev
 */
public abstract class AbstractMSOfficeCustomPropertyUpdater implements CustomPropertyUpdater {

	/**
	 * Plug-in name for all MSOffice files custom property updaters.
	 */
	public static final String PLUGIN_NAME = "editOffline";
	public static final String VERSION = "version";

	public static final String REST_URL = "rest_url";

	private static final String TEMP_DIRECTORY = "editOffline_";
	private static final String TEMP_FILE_IN_PREFIX = "temp_";

	@Inject
	protected InstanceContentService instanceContentService;

	@Inject
	protected TempFileProvider tempFileProvider;

	@Inject
	protected SystemConfiguration systemConfiguration;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Override
	public boolean canUpdate(String mimeType) {
		return getSupportedMimeTypes().stream().anyMatch(mimeType::equalsIgnoreCase);
	}

	/**
	 * @return supported mime types.
	 */
	protected abstract List<String> getSupportedMimeTypes();

	@Override
	public File update(Serializable instanceId) {
		ContentInfo contentInfo = instanceContentService.getContent(instanceId, Content.PRIMARY_CONTENT);
		File fileIn = createFileIn(contentInfo);
		File fileOut = new File(fileIn.getParent(), contentInfo.getName());
		try{
			return updateFile(fileIn, fileOut , instanceId);
		} catch(FileCustomPropertiesUpdateException e){
			deleteFile(fileOut);
			throw e;
		} finally {
			deleteFile(fileIn);
		}
	}

	/**
	 * Takes an file, copies it's content to another file, sets custom property to the second one and returns it.
	 *
	 * @param fileIn
	 *            input file
	 * @param fileOut
	 *            output file
	 * @param instanceId
	 *            id of the instance containing uploaded file
	 * @return file with custom properties set
	 */
	protected abstract File updateFile(File fileIn, File fileOut, Serializable instanceId);

	/**
	 * Creates new temporary file in an unique temp directory.
	 *
	 * @return the created file
	 */
	protected File createFileIn() {
		return tempFileProvider.createTempFile(TEMP_FILE_IN_PREFIX, null, getExportDir());
	}

	/**
	 * Creates unique temporary directory.
	 *
	 * @return the created directory
	 */
	private File getExportDir() {
		return tempFileProvider.createTempDir(TEMP_DIRECTORY + UUID.randomUUID().toString());
	}

	/**
	 * Deletes file.
	 *
	 * @param file
	 *            to be deleted
	 */
	protected void deleteFile(File file) {
		tempFileProvider.deleteFile(file);
	}

	/**
	 * REST access url getter.
	 *
	 * @return the REST access url
	 */
	private String getRestRemoteAccessRUrl() {
		return systemConfiguration.getRESTRemoteAccessUrl().getOrFail().toString();
	}

	/**
	 * Getter for the total count of the versions for the instance.
	 *
	 * @param instanceId
	 *            the id of the instance which versions will be retrieved
	 * @return instance's versions total count
	 */
	private String getVersion(Serializable instanceId) {
		return instanceTypeResolver
				.resolveReference(instanceId)
				.map(InstanceReference::toInstance)
				.map(instance -> instance.getString(DefaultProperties.VERSION))
				.orElse("1.0");
	}

	protected String getRestUrl(Serializable instanceId) {
		return new StringBuilder(getRestRemoteAccessRUrl())
				.append("/instances/")
				.append(instanceId)
				.append("/actions/edit-offline-check-in?version=")
				.append(getVersion(instanceId))
				.toString();
	}

	protected File createFileIn(ContentInfo contentInfo) {
		File fileIn = createFileIn();
		try {
			contentInfo.writeTo(fileIn);
		} catch (IOException e) {
			deleteFile(fileIn);
			throw new FileCustomPropertiesUpdateException(fileIn.getName(),
					"Error during creation of edit offline temporary file: " + fileIn.getName(), e);
		}
		return fileIn;
	}

	/**
	 * Adds custom property to MSOffice 2007 files
	 *
	 * @param properties
	 *            document properties
	 * @param instanceId
	 *            instance id
	 */
	protected void addCustomPropertiesMSOffice2007(POIXMLProperties properties, Serializable instanceId) {
		POIXMLProperties.CustomProperties customProperties = properties.getCustomProperties();

		if (customProperties.contains(REST_URL)) {
			customProperties.getUnderlyingProperties().getPropertyList().remove(customProperties.getProperty(REST_URL));
		}
		customProperties.addProperty(REST_URL, getRestUrl(instanceId));
	}

	/**
	 * /** Adds custom property to MSOffice 2007 files
	 *
	 * @param documentSummaryInformation
	 *            holding document information and properties
	 * @param instanceId
	 *            instance id
	 */
	protected void addCustomPropertiesMSOffice2003(DocumentSummaryInformation documentSummaryInformation,
			Serializable instanceId) {
		CustomProperties customProperties = documentSummaryInformation.getCustomProperties();

		if (customProperties == null) {
			customProperties = new CustomProperties();
		} else if (customProperties.containsKey(REST_URL)) {
			customProperties.remove(REST_URL);
		}

		customProperties.put(REST_URL, getRestUrl(instanceId));
		documentSummaryInformation.setCustomProperties(customProperties);
	}
}
