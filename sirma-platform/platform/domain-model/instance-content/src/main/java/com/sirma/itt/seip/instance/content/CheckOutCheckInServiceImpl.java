package com.sirma.itt.seip.instance.content;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.content.upload.ContentUploader;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.content.event.CheckOutEvent;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.lock.exception.UnlockException;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.rest.Activator;
import com.sirma.itt.seip.util.file.FileUtil;

/**
 * Control the updates of ms office document contents.
 *
 * @author nvelkov
 * @author Vilizar Tsonev
 */
@ApplicationScoped
public class CheckOutCheckInServiceImpl implements CheckOutCheckInService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final Set<String> MS_OFFICE_MIMETYPES;

	static {
		MS_OFFICE_MIMETYPES = new HashSet<>();
		MS_OFFICE_MIMETYPES.add("application/msword");
		MS_OFFICE_MIMETYPES.add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		MS_OFFICE_MIMETYPES.add("application/vnd.openxmlformats-officedocument.wordprocessingml.template");
		MS_OFFICE_MIMETYPES.add("application/vnd.ms-word.document.macroEnabled.12");
		MS_OFFICE_MIMETYPES.add("application/vnd.ms-word.template.macroEnabled.12");
		MS_OFFICE_MIMETYPES.add("application/vnd.ms-excel");
		MS_OFFICE_MIMETYPES.add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		MS_OFFICE_MIMETYPES.add("application/vnd.openxmlformats-officedocument.spreadsheetml.template");
		MS_OFFICE_MIMETYPES.add("application/vnd.ms-excel.sheet.macroEnabled.12");
		MS_OFFICE_MIMETYPES.add("application/vnd.ms-excel.template.macroEnabled.12");
		MS_OFFICE_MIMETYPES.add("application/vnd.ms-excel.addin.macroEnabled.12");
		MS_OFFICE_MIMETYPES.add("application/vnd.ms-excel.sheet.binary.macroEnabled.12");
		MS_OFFICE_MIMETYPES.add("application/vnd.ms-powerpoint");
		MS_OFFICE_MIMETYPES.add("application/vnd.openxmlformats-officedocument.presentationml.presentation");
		MS_OFFICE_MIMETYPES.add("application/vnd.openxmlformats-officedocument.presentationml.template");
		MS_OFFICE_MIMETYPES.add("application/vnd.openxmlformats-officedocument.presentationml.slideshow");
		MS_OFFICE_MIMETYPES.add("application/vnd.ms-powerpoint.addin.macroEnabled.12");
		MS_OFFICE_MIMETYPES.add("application/vnd.ms-powerpoint.presentation.macroEnabled.12");
		MS_OFFICE_MIMETYPES.add("application/vnd.ms-powerpoint.template.macroEnabled.12");
		MS_OFFICE_MIMETYPES.add("application/vnd.ms-powerpoint.slideshow.macroEnabled.12");
	}

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private LockService lockService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private EventService eventService;

	@Inject
	private ContentUploader contentUploader;

	@Inject
	private AuthorityService authorityService;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private DomainInstanceService instanceService;

	@Override
	public ContentInfo checkIn(Content content, Serializable contentId) {
		ContentInfo info = instanceContentService.getContent(contentId, "");
		if (!info.exists()) {
			throw new EmfApplicationException(labelProvider.getValue("document.checkin.content.not.found"));
		}

		InstanceReference instanceReference = instanceTypeResolver.resolveReference(info.getInstanceId()).orElseThrow(
				() -> new EmfApplicationException(labelProvider.getValue("document.checkin.deleted.instance")));

		Instance instance = instanceReference.toInstance();
		unlockInstance(instanceReference);

		if (!authorityService.isActionAllowed(instance, ActionTypeConstants.UPLOAD_NEW_VERSION, null)) {
			throw new EmfApplicationException(labelProvider.getValue("document.checkin.no.permission"));
		}

		ContentInfo updatedContentInfo = contentUploader.updateContent(instance, content, (String) contentId);

		// save any changes made to the instance during the operation
		instanceService
				.save(InstanceSaveContext.create(instance, new Operation(ActionTypeConstants.UPLOAD_NEW_VERSION)));
		return updatedContentInfo;
	}

	@Override
	public Optional<String> checkOut(Serializable contentId) {
		ContentInfo info = instanceContentService.getContent(contentId, Content.PRIMARY_CONTENT);
		if (info.exists() || info.getInstanceId() != null) {
			InstanceReference instanceReference = instanceTypeResolver
					.resolveReference(info.getInstanceId())
						.orElseThrow(() -> new EmfApplicationException(
								labelProvider.getValue("document.checkin.deleted.instance")));

			lockService.lock(instanceReference);
			eventService.fire(new CheckOutEvent(instanceReference.toInstance()));
			return Optional.of(buildUrl(info));
		}
		return Optional.empty();
	}

	/**
	 * Unlocks the instance. If it is already unlocked, or has been locked by another user,
	 * {@link EmfApplicationException} or {@link UnlockException} is thrown and the operation is aborted.
	 *
	 * @param instanceReference
	 *            is the instance reference
	 */
	private void unlockInstance(InstanceReference instanceReference) {
		LockInfo lockInfo = lockService.lockStatus(instanceReference);
		// if the instance is unlocked, check-in should be aborted to avoid concurrency issues when modifying
		if (!lockInfo.isLocked()) {
			throw new EmfApplicationException(labelProvider.getValue("document.checkin.unlocked.instance"));
		}
		lockService.unlock(instanceReference);
		Instance instance = instanceReference.toInstance();
		// this is made in order to refresh the instance's lock status before checking isActionAllowed
		instance.remove(DefaultProperties.LOCKED_BY);
	}

	/**
	 * Build the document download url from the provided documentPath and content info.
	 *
	 * @param info
	 *            the content info
	 * @return the download url
	 */
	private static String buildUrl(ContentInfo info) {
		StringBuilder stringBuilder = new StringBuilder()
				.append(Activator.ROOT_PATH)
					.append("/content/")
					.append(info.getInstanceId())
					.append("?download=true");
		if (MS_OFFICE_MIMETYPES.contains(info.getMimeType())) {
			Pair<String, String> nameAndExtension = FileUtil.splitNameAndExtension(info.getName());
			String name = nameAndExtension.getFirst();
			String extension = nameAndExtension.getSecond();
			// Files can't have file names containing :
			String contentId = info.getContentId().replaceFirst(":", "-");
			try {
				stringBuilder
						.append("&fileName=")
							.append(URLEncoder.encode(name, "UTF-8"))
							.append(".")
							.append(contentId)
							.append(".")
							.append(extension);
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("The UTF-8 encoding is unsupported: " + e.getMessage(), e);
			}
		}
		return stringBuilder.toString();
	}
}
