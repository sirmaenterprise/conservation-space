package com.sirma.itt.seip.instance.version.revert;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.instance.version.CopyContentOnNewVersionStep;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Reverts the additional contents stored for the version instance. The process represents retrieving the version
 * contents and importing them as new contents for the current instance. <br>
 * This step will process all other contents that are stored for the version except the view, because it is handled in
 * another step, where it is processed additionally. <br>
 * If there is no additional content stored for the version, the step will do nothing. <br>
 * This step supports rollback in case that revert operation fails. It will delete the generated content from this step,
 * if any.
 * <p>
 * <b>This step should be executed before the reverted instance is saved, otherwise it will mess the contents stored for
 * the version, because the reverted instance will hold references to the previous contents instead of reverted!</b>
 * </p>
 *
 * @author A. Kunchev
 * @see CopyContentOnNewVersionStep
 */
@Extension(target = RevertStep.EXTENSION_NAME, enabled = true, order = 45)
public class RevertContentStep implements RevertStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	// view content is processed by another step
	private static final Collection<String> CONTENTS_TO_SKIP = Collections.singleton(Content.PRIMARY_VIEW);

	private static final String IMPORTED_CONTENTS = "$importedContents$";

	@Inject
	private InstanceContentService instanceContentService;

	@Override
	public String getName() {
		return "revertContent";
	}

	@Override
	public void invoke(RevertContext context) {
		Serializable versionId = context.getVersionId();
		Collection<ContentInfo> contents = instanceContentService.getContentsForInstance(versionId, CONTENTS_TO_SKIP);
		if (isEmpty(contents)) {
			LOGGER.debug("There are no additional contents to be reverted from version - {}", versionId);
			return;
		}

		Serializable currentId = context.getCurrentInstanceId();
		List<ContentImport> contentsToImport = contents
				.stream()
					.filter(ContentInfo::exists)
					.map(info -> ContentImport.copyFrom(info).setInstanceId(currentId))
					.collect(Collectors.toList());

		if (contentsToImport.isEmpty()) {
			LOGGER.trace("There are no additional contents that should be reverted for instance - {}", currentId);
			return;
		}

		context.put(IMPORTED_CONTENTS, instanceContentService.importContent(contentsToImport));
	}

	@Override
	public void rollback(RevertContext context) {
		Collection<String> contentIdentifiers = context.getIfSameType(IMPORTED_CONTENTS, Collection.class);
		if (isNotEmpty(contentIdentifiers)) {
			contentIdentifiers.forEach(id -> instanceContentService.deleteContent(id, null));
		}
	}

}
