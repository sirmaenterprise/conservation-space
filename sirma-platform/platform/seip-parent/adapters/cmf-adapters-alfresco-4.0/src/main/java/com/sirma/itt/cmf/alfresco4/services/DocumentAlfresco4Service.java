/**
 *
 */
package com.sirma.itt.cmf.alfresco4.services;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.AlfrescoUtils;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.alfresco4.descriptor.AlfrescoFileAndPropertiesDescriptor;
import com.sirma.itt.cmf.alfresco4.remote.AlfrescoUploader;
import com.sirma.itt.cmf.alfresco4.remote.ContentUpdateContext;
import com.sirma.itt.cmf.alfresco4.remote.ContentUploadContext;
import com.sirma.itt.cmf.alfresco4.services.convert.Converter;
import com.sirma.itt.cmf.alfresco4.services.convert.ConverterConstants;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.cmf.services.adapter.CMFSearchAdapterService;
import com.sirma.itt.cmf.services.adapter.ThumbnailGenerationMode;
import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.adapters.AdaptersConfiguration;
import com.sirma.itt.seip.adapters.remote.AlfrescoErrorReader;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.ContextualConcurrentMap;
import com.sirma.itt.seip.domain.DmsAware;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.search.Query;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.sep.content.descriptor.ContentPreviewDescriptor;

/**
 * The class adapter for documents in dms<->cmf communication. Has a methods to upload/update documents in dms.
 *
 * @author Borislav Banchev
 */
@ApplicationScoped
public class DocumentAlfresco4Service implements CMFDocumentAdapterService, AlfrescoCommunicationConstants {

	private static final String PREVIEW_URL = ServiceURIRegistry.CMF_TO_DMS_PROXY_SERVICE
			+ ServiceURIRegistry.CONTENT_ACCESS_URI;

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentAlfresco4Service.class);

	@Inject
	private RESTClient restClient;

	@Inject
	private AlfrescoUploader alfrescoUploader;

	@Inject
	@Converter(name = ConverterConstants.GENERAL)
	private DMSTypeConverter docConvertor;

	@Inject
	private ContextualConcurrentMap<String, String> libraryCache;

	@Inject
	private CMFSearchAdapterService searchAdapter;
	@Inject
	private AdaptersConfiguration adaptersConfiguration;

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService# uploadNewVersion
	 * (com.sirma.itt.cmf.beans.model.DMSInstance, com.sirma.itt.cmf.services.adapter.DMSFileDescriptor)
	 */
	@Override
	public FileAndPropertiesDescriptor uploadNewVersion(DMSInstance instance, UploadWrapperDescriptor descriptor)
			throws DMSException {
		AlfrescoUtils.validateExistingDMSInstance(instance);
		try {
			// get the file
			String uploadFile = alfrescoUploader.updateFile(buildUpdateContext(instance, descriptor));

			// upload the file now
			if (uploadFile != null) {
				JSONObject fileData = new JSONObject(uploadFile);
				Map<String, Serializable> dmsToCMFProperties = docConvertor.convertDMSToCMFProperties(fileData,
						instance, DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
				return new AlfrescoFileAndPropertiesDescriptor(fileData.getString(KEY_NODEREF), null,
						dmsToCMFProperties, restClient);
			}
		} catch (DMSClientException e) {
			throw new DMSException("Failure during upload new version of document on: " + instance, e);
		} catch (Exception e) {
			throw new DMSException("Failure during request on '" + instance + "': " + AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("File is not uploaded!");
	}

	private ContentUpdateContext buildUpdateContext(DMSInstance instance, UploadWrapperDescriptor descriptor) {
		return ContentUpdateContext
				.create(ServiceURIRegistry.CMF_ATTACH_TO_INSTANCE_SERVICE,
						instance.getString(DocumentProperties.WORKING_COPY_LOCATION, instance.getDmsId()))
					.setFilePart(alfrescoUploader.getPartSource(descriptor))
					.setContentType(TYPE_CM_CONTENT)
					.setProperties(getProperties(instance, descriptor))
					.setAspectProperties(Collections.singleton("cm:versionable"))
					.setMajorVersion(instance.getAs(DocumentProperties.IS_MAJOR_VERSION,
							value -> Boolean.valueOf(value.toString()), () -> Boolean.FALSE))
					.setVersionDescription(instance.getAsString(DocumentProperties.VERSION_DESCRIPTION, () -> ""))
					.setThumbnailMode(resolveThumbnailMode(instance));
	}

	private static String resolveThumbnailMode(DMSInstance instance) {
		if (!instance.isUploaded()) {
			return ThumbnailGenerationMode.NONE.toString();
		}

		String mode = instance.getAsString(DocumentProperties.DOCUMENT_THUMB_MODE);
		if (StringUtils.isNotBlank(mode)) {
			return mode;
		}

		return ThumbnailGenerationMode.ASYNCH.toString();
	}

	@Override
	public FileAndPropertiesDescriptor uploadContent(DMSInstance instance, UploadWrapperDescriptor descriptor,
			Set<String> aspectsToInclude) throws DMSException {
		try {
			String uploadFile = alfrescoUploader.uploadFile(buildUploadContext(aspectsToInclude, descriptor, instance));

			// upload the file now
			if (uploadFile != null) {
				JSONObject fileData = new JSONObject(uploadFile);
				Map<String, Serializable> dmsToCMFProperties = docConvertor.convertDMStoCMFPropertiesByValue(fileData,
						instance, DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);

				return new AlfrescoFileAndPropertiesDescriptor(fileData.getString(KEY_NODEREF), null,
						dmsToCMFProperties, restClient);
			}
		} catch (DMSClientException e) {
			throw new DMSException("Failure during uploading document: " + instance, e);
		} catch (Exception e) {
			throw new DMSException("Failure during request on '" + instance + "': " + AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("File is not uploaded!");
	}

	private ContentUploadContext buildUploadContext(Set<String> aspectsToInclude, UploadWrapperDescriptor descriptor,
			DMSInstance instance) throws DMSException {
		return ContentUploadContext
				.create(ServiceURIRegistry.CMF_ATTACH_TO_INSTANCE_SERVICE, descriptor.getUploadMode())
					.setFilePart(alfrescoUploader.getPartSource(descriptor))
					.setParentNodeId(getDmsId(descriptor))
					.setContentType(TYPE_CM_CONTENT)
					.setProperties(getProperties(instance, descriptor))
					.setAspectProperties(convertAspectsForDms(aspectsToInclude))
					.setThumbnailMode(resolveThumbnailMode(instance))
					.setOverwrite(Boolean.FALSE)
					.setMajorVersion(Boolean.TRUE);
	}

	private String getDmsId(UploadWrapperDescriptor descriptor) throws DMSException {
		String dmsId = descriptor.getContainerId();
		if (dmsId == null) {
			dmsId = getLibraryDMSId();
		}

		if (dmsId == null) {
			throw new DMSException("Target not specified and library not initialized");
		}

		return dmsId;
	}

	private Map<String, Serializable> getProperties(DMSInstance instance, UploadWrapperDescriptor descriptor) {
		Map<String, Serializable> propertiesProp = null;
		Map<String, Serializable> metadataToInclude = descriptor.getProperties();
		if (metadataToInclude == null) {
			propertiesProp = new HashMap<>(1);
		} else {
			Map<String, Serializable> copyOfData = new HashMap<>(metadataToInclude);
			copyOfData.remove(DefaultProperties.NAME);
			propertiesProp = docConvertor.convertCMFtoDMSProperties(copyOfData, instance,
					DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
			// remove name as it is automatically maintained
		}
		fixDMSName(descriptor, propertiesProp);
		return propertiesProp;
	}

	private static void fixDMSName(FileAndPropertiesDescriptor descriptor, Map<String, Serializable> properties) {
		if (properties.containsKey(AlfrescoCommunicationConstants.KEY_DMS_NAME)) {
			String dmsName = String.valueOf(properties.get(AlfrescoCommunicationConstants.KEY_DMS_NAME));
			dmsName = "NO_ID".equals(dmsName) ? descriptor.getId() : dmsName;

			// to be able to save with any non-word characters in the name
			dmsName = dmsName.replaceAll("[\\W]+", "_");

			properties.put(KEY_DMS_NAME, UUID.randomUUID() + "-" + dmsName);
		}
	}

	private Set<String> convertAspectsForDms(Set<String> aspectsToInclude) throws DMSException {
		Set<String> aspectsProp = null;
		if (aspectsToInclude != null) {
			aspectsProp = CollectionUtils.createHashSet(aspectsToInclude.size());
			for (String nextAspect : aspectsToInclude) {

				Pair<String, Serializable> dmsAspect = docConvertor.convertCMFtoDMSProperty(nextAspect, "",
						DMSTypeConverter.PROPERTIES_MAPPING);
				if (dmsAspect == null) {
					throw new DMSException("Invalid aspect provided (" + nextAspect + ")");
				}
				aspectsProp.add(dmsAspect.getFirst());
			}
		}
		return aspectsProp;
	}

	@Override
	public FileDescriptor getDocumentPreview(DmsAware document, String targetMimetype) throws DMSException {
		AlfrescoUtils.validateExistingDMSInstance(document);
		try {
			HttpMethod createdMethod = restClient.createMethod(new GetMethod(), "", true);
			String idPreview = document.getDmsId().replace(":/", "");
			String uri = MessageFormat.format(ServiceURIRegistry.CONTENT_TRANSFORM_SERVICE, idPreview, targetMimetype);
			String response = restClient.request(uri, createdMethod);
			if (response != null && createdMethod.getStatusCode() == 200) {
				JSONObject result = new JSONObject(response);
				if (result.has(KEY_NODEID)) {
					String idParts = result.getString(KEY_NODEID).replace(":/", "");
					LOGGER.debug("Generating uri content for {}", result.getString(KEY_NODEID));
					return new ContentPreviewDescriptor(MessageFormat.format(PREVIEW_URL, idParts), targetMimetype);
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Requested document: " + document + " is not transformed! ", e);
		} catch (Exception e) {
			throw new DMSException("Failure during request on '" + document + "': " + AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Requested document: " + document.getDmsId() + " is not transformed");
	}

	@Override
	public String getLibraryDMSId() {
		return libraryCache.computeIfAbsent(adaptersConfiguration.getDmsContainerId().get(), key -> {
			// TODO when R are ready - search by aspect in container
			SearchArguments<FileDescriptor> args = new SearchArguments<>();
			args.setQuery(new Query("PATH", "/app:company_home/st:sites/cm:" + key + "/cm:documentLibrary"));
			try {
				// we just need something to trigger dms search
				SearchArguments<FileDescriptor> search = searchAdapter.search(args, ObjectInstance.class);
				if (search.getResult().size() == 1) {
					return search.getResult().get(0).getId();
				}
			} catch (Exception e) {
				LOGGER.warn("Could not find the document library for site " + key, e);
			}
			return null;
		});
	}

}
