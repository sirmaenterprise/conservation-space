package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Handles version instance contents assigning. Creates new records for the contents of the real instance, but mapped to
 * the version instance instead. This way we could open specific contents with version id without additional logic that
 * checks for versions and etc. First all latest contents for the target instance are retrieved, then from them are
 * build {@link ContentImport} objects with version instance assigned to them and then they are imported.<br />
 * <b>NOTE - Only relevant contents are imported for the version instance, all others should be filtered.</b> <br />
 * <b>NOTE - This step should be executed after persist step, when the version id is added to the context.</b>
 *
 * @author A. Kunchev
 * @see VersionPersistStep
 */
@Extension(target = VersionStep.TARGET_NAME, enabled = true, order = 20)
public class CopyContentOnNewVersionStep implements VersionStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Set<String> NOT_ALLOWED_VERSION_CONTENTS = new HashSet<>(
			// Content#PRIMARY_VIEW is processed in specific version step
			Arrays.asList("queriesResult", "draft", "export", "manifest", Content.PRIMARY_VIEW, "embeddedImage",
					"ocr", Content.PRIMARY_CONTENT_PREVIEW));

	@Inject
	private InstanceContentService instanceContentService;

	@Override
	public String getName() {
		return "copyContentOnNewVersion";
	}

	@Override
	public void execute(VersionContext context) {
		String targetInstanceId = context.getTargetInstanceId();
		String versionId = context.getVersionInstanceId();
		if (StringUtils.isBlank(versionId)) {
			LOGGER.debug("There is no version instance id! No content will be stored for instance - {}.", versionId);
			return;
		}

		Collection<ContentInfo> contents = instanceContentService.getContentsForInstance(targetInstanceId,
				NOT_ALLOWED_VERSION_CONTENTS);
		if (isEmpty(contents)) {
			LOGGER.debug("Could not find any content for instance with id - {}.", targetInstanceId);
			return;
		}

		List<ContentImport> contentsToImport = contents
				.stream()
					// remove all for the current version, if any
					.map(info -> removeCurrentForVersion(versionId, info))
					// copy all existing from original to the version
					.filter(ContentInfo::exists)
					.map(info -> convertToContentImport(info, versionId))
					.collect(Collectors.toList());

		if (contentsToImport.isEmpty()) {
			LOGGER.trace("There are no contents that should be assigned to the version. All found are filtered.");
			return;
		}

		instanceContentService.importContent(contentsToImport);
	}

	/**
	 * Removes the content for the current version, if there is any. All content of the current version should be
	 * removed in order to update them. <br />
	 * If there isn't new content for the original instance => the additional contents are removed, which also counts as
	 * update of the version contents.
	 *
	 * @return the passed {@link ContentInfo} so that it could be reused in the stream chaining
	 */
	private ContentInfo removeCurrentForVersion(String versionId, ContentInfo newInfo) {
		String purpose = newInfo.getContentPurpose();
		if (instanceContentService.deleteContent(versionId, purpose)) {
			LOGGER.trace("Removed [{}] content for instance - [{}].", purpose, versionId);
		}

		return newInfo;
	}

	/**
	 * Copies information about specific content and builds {@link ContentImport} object from it.
	 */
	private static ContentImport convertToContentImport(ContentInfo source, String versionId) {
		return ContentImport.copyFrom(source).setInstanceId(versionId);
	}
}