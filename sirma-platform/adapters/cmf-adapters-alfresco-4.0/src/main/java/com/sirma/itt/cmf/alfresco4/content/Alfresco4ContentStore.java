package com.sirma.itt.cmf.alfresco4.content;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService.UploadMode;
import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.DMSInstanceAdapterService;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentAdapterService;
import com.sirma.itt.seip.content.ContentStore;
import com.sirma.itt.seip.content.StoreException;
import com.sirma.itt.seip.content.StoreItemInfo;
import com.sirma.itt.seip.domain.DmsAware;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Content store that stores the actual content at Alfresco 4 server.
 *
 * @author BBonev
 */
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
		if (isFromThisStore(previousVersion) && source instanceof Instance && descriptor != null) {
			try {
				Instance instance = (Instance) source;
				UploadWrapperDescriptor adapter = new UploadWrapperDescriptor(descriptor.getContent(), null,
						instance.getProperties());

				DMSInstance copy = copyToDMsInstance(instance);
				copy.setDmsId(previousVersion.getRemoteId());

				FileAndPropertiesDescriptor propertiesDescriptor = documentAdapter.uploadNewVersion(copy, adapter);
				return createStoreInfo()
						.setRemoteId(propertiesDescriptor.getId())
							.setContentLength(adapter.uploadedSize());
			} catch (DMSException e) {
				throw new StoreException("Could not update content", e);
			}
		}
		return null;
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

	@Override
	public FileDescriptor getReadChannel(StoreItemInfo storeInfo) {
		if (isFromThisStore(storeInfo)) {
			try {
				return contentAdapterService.getContentDescriptor(storeInfo.getRemoteId());
			} catch (RuntimeException e) {
				throw new StoreException("Could not contact remote store for id: " + storeInfo.getRemoteId()
						+ " due to: " + e.getMessage(), e);
			}
		}
		return null;
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
		return null;
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
	public boolean delete(StoreItemInfo itemInfo) {
		if (isFromThisStore(itemInfo)) {
			DMSInstance instance = new EmfInstance();
			instance.setDmsId(itemInfo.getRemoteId());
			try {
				return instanceAdapter.deleteNode(instance);
			} catch (DMSException e) {
				LOGGER.warn("Could not delete content from DMS", e);
			}
		}
		return false;
	}

	@Override
	public String getName() {
		return STORE_NAME;
	}

}
