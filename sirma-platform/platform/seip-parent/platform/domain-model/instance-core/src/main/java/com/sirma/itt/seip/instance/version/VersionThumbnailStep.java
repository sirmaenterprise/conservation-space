package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.THUMBNAIL_IMAGE;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.rendition.ThumbnailService;

/**
 * Handles thumbnail registering for the version instance. When the thumbnail is added through action (like: 'Add
 * thumbnail'), it is stored as instance property with key {@link DefaultProperties#THUMBNAIL_IMAGE} and we can add it
 * for the version directly. If the target instance is uploaded document and the thumbnail is resolved from the
 * {@link Content#PRIMARY_CONTENT}, we just register new thumbnail for the version instance with the same end point and
 * provider as the target instance. This way, if the thumbnail entity for the original instance is changed, we have
 * separated one for the versions. <br />
 * <b>If the instance has no thumbnail as property or the target instance is not uploaded, the step will do nothing.</b>
 *
 * @author A. Kunchev
 * @see ThumbnailService#addThumbnail
 * @see ThumbnailService#register
 */
// TODO at the moment this is implemented in the previews/thumbnails generation service
// This step will be disabled, until we refactor the generation and move the logic from there, here because it should be
// part of this process
@Extension(target = VersionStep.TARGET_NAME, enabled = false, order = 30)
public class VersionThumbnailStep implements VersionStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private ThumbnailService thumbnailService;

	@Override
	public String getName() {
		return "versionThumbnail";
	}

	@Override
	public void execute(VersionContext context) {
		Optional<Instance> versionOptional = context.getVersionInstance();
		if (!versionOptional.isPresent()) {
			LOGGER.debug("Version instance is required for thumbnail versioning. [{}] step will do nothing"
					+ " for instance - {}.", getName(), context.getTargetInstanceId());
			return;
		}

		Instance version = versionOptional.get();
		// when the thumbnail is added through action, add it directly
		if (version.isValueNotNull(THUMBNAIL_IMAGE)) {
			thumbnailService.addThumbnail(version.toReference(), version.getAsString(THUMBNAIL_IMAGE));
		} else {
			// register thumbnail, if the target instance is uploaded
			thumbnailService.register(version);
		}
	}

}
