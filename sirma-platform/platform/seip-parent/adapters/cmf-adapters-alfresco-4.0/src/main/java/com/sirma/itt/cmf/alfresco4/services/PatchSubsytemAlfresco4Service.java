package com.sirma.itt.cmf.alfresco4.services;

import java.io.File;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.alfresco4.remote.AlfrescoUploader;
import com.sirma.itt.cmf.alfresco4.remote.ContentUploadContext;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService.UploadMode;
import com.sirma.itt.cmf.services.adapter.ThumbnailGenerationMode;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.patch.service.PatchSubsytemAdapterService;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import com.sirma.sep.content.descriptor.LocalFileDescriptor;

/**
 * Implementation of DMS integration for patch subsystem
 */
@ApplicationScoped
public class PatchSubsytemAlfresco4Service implements PatchSubsytemAdapterService, AlfrescoCommunicationConstants {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final long serialVersionUID = 8656657709178204961L;

	@Inject
	private AlfrescoUploader alfrescoUploader;

	@Override
	public boolean backupPatch(File zipFile, String name) throws DMSException {
		Map<String, Serializable> props = new HashMap<>();
		String title = name == null ? ISO8601DateFormat.format(Calendar.getInstance()) : name;
		props.put(KEY_DMS_TITLE, title);
		try {
			return alfrescoUploader.uploadFile(buildUploadContext(zipFile, props)) != null;
		} catch (DMSClientException e) {
			LOGGER.error("Failed to upload patch to DMS", e);
		}
		return false;

	}

	private ContentUploadContext buildUploadContext(File zipFile, Map<String, Serializable> props) {
		return ContentUploadContext
				.create(ServiceURIRegistry.UPLOAD_SERVICE_URI, UploadMode.DIRECT)
					.setFilePart(alfrescoUploader.getPartSource(new LocalFileDescriptor(zipFile)))
					.setFolder("/app:company_home/app:dictionary/app:models")
					.setContentType(TYPE_CM_CONTENT)
					.setProperties(props)
					.setAspectProperties(Collections.singleton("cm:lockable"))
					.setThumbnailMode(ThumbnailGenerationMode.NONE.toString())
					.setOverwrite(Boolean.FALSE)
					.setMajorVersion(Boolean.TRUE);
	}

}
