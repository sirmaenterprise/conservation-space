package com.sirma.itt.cmf.alfresco4.content;

import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService.UploadMode;
import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.DMSInstanceAdapterService;
import com.sirma.itt.seip.content.ContentAdapterService;
import com.sirma.itt.seip.domain.DmsAware;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentStore;
import com.sirma.sep.content.ContentStoreMissMatchException;
import com.sirma.sep.content.DeleteContentData;
import com.sirma.sep.content.StoreException;
import com.sirma.sep.content.StoreItemInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Content store that stores the actual content at Alfresco 4 server.
 *
 * @author BBonev
 */
@Named(Alfresco4ContentStore.STORE_NAME)
@ApplicationScoped
public class Alfresco4ContentStore implements ContentStore {

	public static final String STORE_NAME = "alfresco4";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Set<String> ATTACHMENT_ASPECT = Collections
			.singleton(DocumentProperties.TYPE_DOCUMENT_ATTACHMENT);

	@Inject
	private ContentAdapterService contentAdapterService;
	@Inject
	private CMFDocumentAdapterService documentAdapter;
	@Inject
	private DMSInstanceAdapterService instanceAdapter;

	@Inject
	private SecurityContextManager securityContextManager;

	@Override
	public StoreItemInfo add(Serializable target, Content descriptor) {
		if (!(target instanceof Instance) || descriptor == null) {
			return null;
		}
		Instance instance = (Instance) target;
		try {
			UploadWrapperDescriptor adapter = new UploadWrapperDescriptor(descriptor.getContent(),
					documentAdapter.getLibraryDMSId(), instance.getProperties());
			adapter.setUploadMode(UploadMode.CUSTOM);
			FileAndPropertiesDescriptor result = documentAdapter.uploadContent((DMSInstance) instance, adapter,
					getContentAspect());
			return createStoreInfo().setRemoteId(result.getId()).setContentLength(adapter.uploadedSize());
		} catch (DMSException e) {
			throw new StoreException("Could not upload content for instance " + instance.getId(), e);
		}
	}

	/**
	 * Gets the content aspect to be used for the persisted content
	 *
	 * @return the content aspect
	 */
	@SuppressWarnings("static-method")
	protected Set<String> getContentAspect() {
		return ATTACHMENT_ASPECT;
	}

	@Override
	public StoreItemInfo update(Serializable source, Content descriptor, StoreItemInfo previousVersion) {
		if (!(source instanceof Instance && descriptor != null)) {
			throw new IllegalArgumentException("Missing required parameters");
		}
		if (!isFromThisStore(previousVersion)) {
			throw new ContentStoreMissMatchException(getName(),
					previousVersion == null ? null : previousVersion.getProviderType());
		}
		String remoteId = previousVersion.getRemoteId();
		try {
			Instance instance = (Instance) source;
			UploadWrapperDescriptor adapter = new UploadWrapperDescriptor(descriptor.getContent(), null,
					instance.getProperties());

			DMSInstance copy = copyToDMsInstance(instance);
			copy.setDmsId(remoteId);

			FileAndPropertiesDescriptor propertiesDescriptor = documentAdapter.uploadNewVersion(copy, adapter);
			return createStoreInfo()
					.setRemoteId(propertiesDescriptor.getId())
					.setContentLength(adapter.uploadedSize());
		} catch (DMSException e) {
			LOGGER.debug("Failed to update content due to - [{}]."
					+ " Attempting to recover by adding the content as new.", e.getMessage());
			return addIfContentDoesNotExist(source, descriptor, remoteId, e);
		}
	}

	private static DMSInstance copyToDMsInstance(Instance instance) {
		DMSInstance copy;
		if (instance instanceof DMSInstance) {
			copy = (DMSInstance) ReflectionUtils.newInstance(instance.getClass());
		} else {
			copy = new EmfInstance();
		}
		copy.addAllProperties(instance.getOrCreateProperties());
		copy.setIdentifier(instance.getIdentifier());
		copy.setRevision(instance.getRevision());
		copy.setId(instance.getId());

		return copy;
	}

	private StoreItemInfo addIfContentDoesNotExist(Serializable source, Content descriptor, String remoteId,
			DMSException cause) {
		if (contentAdapterService.getContentDescriptor(remoteId) == null) {
			return add(source, descriptor);
		}

		throw new StoreException("Could not update content with remote id - " + remoteId, cause);
	}

	@Override
	public FileDescriptor getReadChannel(StoreItemInfo storeInfo) {
		if (isFromThisStore(storeInfo)) {
			if (StringUtils.isBlank(storeInfo.getRemoteId())) {
				return null;
			}
			try {
				return contentAdapterService.getContentDescriptor(storeInfo.getRemoteId());
			} catch (RuntimeException e) {
				throw new StoreException("Could not contact remote store for id: " + storeInfo.getRemoteId()
						+ " due to: " + e.getMessage(), e);
			}
		}
		throw new ContentStoreMissMatchException(getName(), storeInfo == null ? null : storeInfo.getProviderType());
	}

	@Override
	public FileDescriptor getPreviewChannel(StoreItemInfo storeInfo) {
		if (isFromThisStore(storeInfo)) {
			try {
				DmsAware instance = DmsAware.create(storeInfo.getRemoteId());
				String remoteId = documentAdapter.getDocumentPreview(instance, getTargetMimeType(storeInfo)).getId();
				remoteId = remoteId.replaceFirst(ServiceURIRegistry.CMF_TO_DMS_PROXY_SERVICE + "/", "");
				return contentAdapterService.getContentDescriptor(remoteId);
			} catch (DMSException e) {
				throw new StoreException("Could not contact remote store for id: " + storeInfo.getRemoteId()
						+ " due to: " + e.getMessage(), e);
			}
		}
		throw new ContentStoreMissMatchException(getName(), storeInfo == null ? null : storeInfo.getProviderType());
	}

	private static String getTargetMimeType(StoreItemInfo storeInfo) {
		String mimetypeCurrent = storeInfo.getContentType();
		String mimetypeTarget = "application/pdf";
		if (mimetypeCurrent != null && mimetypeCurrent.startsWith("image/")) {
			mimetypeTarget = mimetypeCurrent;
		}
		return mimetypeTarget;
	}

	@Override
	public boolean isTwoPhaseDeleteSupported() {
		return true;
	}

	@Override
	public Optional<DeleteContentData> prepareForDelete(StoreItemInfo itemInfo) {
		if (!isFromThisStore(itemInfo)) {
			throw new ContentStoreMissMatchException(getName(), itemInfo == null ? null : itemInfo.getProviderType());
		}
		DeleteContentData data = new DeleteContentData()
				.addProperty("dmsId", itemInfo.getRemoteId())
				.setStoreName(getName());
		return Optional.of(data);
	}

	@Override
	public void delete(DeleteContentData deleteContentData) {
		ContentStore.super.delete(deleteContentData);
		StoreItemInfo systemInfo = createStoreInfo();
		systemInfo.setRemoteId((String) deleteContentData.get("dmsId"));
		securityContextManager.executeAsTenant(deleteContentData.getTenantId()).function(this::delete, systemInfo);
	}

	@Override
	public boolean delete(StoreItemInfo itemInfo) {
		if (isFromThisStore(itemInfo)) {
			DMSInstance instance = new EmfInstance();
			instance.setDmsId(itemInfo.getRemoteId());
			try {
				return instanceAdapter.deleteNode(instance);
			} catch (DMSException e) {
				LOGGER.warn("Could not delete content from DMS", e);
			}
			return false;
		}
		throw new ContentStoreMissMatchException(getName(), itemInfo == null ? null : itemInfo.getProviderType());
	}

	@Override
	public String getName() {
		return STORE_NAME;
	}
}
