package com.sirma.sep.content.preview.service;

import com.sirma.sep.content.preview.mimetype.MimeType;
import com.sirma.sep.content.preview.mimetype.MimeTypesResolver;
import com.sirma.sep.content.preview.model.ContentPreviewRequest;
import com.sirma.sep.content.preview.model.ContentPreviewResponse;
import com.sirma.sep.content.preview.generator.ContentPreviewGenerator;
import com.sirma.sep.content.preview.util.FileUtils;
import com.sirma.sep.content.preview.util.TimeTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Service for generating content preview and/or thumbnail from provided {@link ContentPreviewRequest}.
 *
 * @author Mihail Radkov
 */
@Service
public class ContentPreviewService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final MimeTypesResolver mimeTypesResolver;
	private final ContentPreviewGenerator contentPreviewGenerator;

	/**
	 * Instantiates a preview service with the provided {@link ContentPreviewGenerator}.
	 *
	 * @param mimeTypesResolver
	 * 		service for resolving supported by the application {@link MimeType}s
	 * @param contentPreviewGenerator
	 * 		the generator to be used by the service for producing previews and thumbnails
	 */
	@Autowired
	public ContentPreviewService(MimeTypesResolver mimeTypesResolver, ContentPreviewGenerator contentPreviewGenerator) {
		this.mimeTypesResolver = mimeTypesResolver;
		this.contentPreviewGenerator = contentPreviewGenerator;
	}

	public boolean isContentSupported(String mimeType) {
		return mimeTypesResolver.resolve(mimeType).filter(ContentPreviewService::isSupported).isPresent();
	}

	/**
	 * Processes provided {@link ContentPreviewRequest} into {@link ContentPreviewResponse} containing generated preview
	 * and / or thumbnail.
	 * <p>
	 * If the mimetype returned from {@link ContentPreviewRequest#getMimetype()} is <code>application/pdf</code> or an
	 * image then only a thumbnail is generated and the resulting {@link ContentPreviewResponse} will lack a {@link
	 * File} for preview.
	 *
	 * @param previewRequest
	 * 		the request to be processes by the service. Must not be <code>null</code>!
	 * @return a {@link ContentPreviewResponse} containing the generated preview and/or thumbnail
	 */
	public ContentPreviewResponse processRequest(ContentPreviewRequest previewRequest) {
		File content = previewRequest.getContent();
		verifyFileExistence(content);

		MimeType mimeType = getMimeType(previewRequest);
		ContentPreviewResponse previewResponse = new ContentPreviewResponse();

		if (shouldSkipPreview(mimeType) && mimeType.supportsThumbnail()) {
			previewResponse.setThumbnail(createThumbnail(content));
		}

		if (mimeType.supportsPreview()) {
			File preview = createPreview(content, previewRequest.getTimeoutMultiplier());
			if (preview.exists()) {
				previewResponse.setPreview(preview);
				if (mimeType.supportsThumbnail()) {
					previewResponse.setThumbnail(createThumbnail(preview));
				}
			}
		}

		if (mimeType.supportsPreview() && previewResponse.getPreview() == null) {
			LOGGER.warn("No preview was generated for instance [id={}] having {} for mimetype",
					previewRequest.getInstanceId(), previewRequest.getMimetype());
		}

		if (mimeType.supportsThumbnail() && previewResponse.getThumbnail() == null) {
			LOGGER.warn("No thumbnail was generated for instance [id={}] having {} for mimetype",
					previewRequest.getInstanceId(), previewRequest.getMimetype());
		}

		return previewResponse;
	}

	private MimeType getMimeType(ContentPreviewRequest previewRequest) {
		Optional<MimeType> resolvedMimetype = mimeTypesResolver.resolve(previewRequest.getMimetype());
		if (!resolvedMimetype.isPresent()) {
			throw new IllegalArgumentException("Missing mimetype mapping for " + previewRequest.getMimetype());
		}
		return resolvedMimetype.get();
	}

	private File createPreview(File file, int timeoutMultiplier) {
		TimeTracker tracker = TimeTracker.create();

		File generatedPreview = contentPreviewGenerator.generatePreview(file, timeoutMultiplier);

		LOGGER.debug("Content preview generation took {} ms", tracker.stopInMs());
		return generatedPreview;
	}

	private String createThumbnail(File preview) {
		TimeTracker tracker = TimeTracker.create();

		File generatedThumbnail = null;
		byte[] thumbnailBytes;
		try {
			generatedThumbnail = contentPreviewGenerator.generateThumbnail(preview);
			thumbnailBytes = Files.readAllBytes(generatedThumbnail.toPath());
		} catch (Exception ex) {
			FileUtils.deleteFile(preview);
			throw new IllegalStateException("Cannot generate thumbnail!", ex);
		} finally {
			FileUtils.deleteFile(generatedThumbnail);
		}

		LOGGER.debug("Content thumbnail generation took {} ms", tracker.stopInMs());
		return Base64Utils.encodeToString(thumbnailBytes);
	}

	private static boolean isSupported(MimeType mimeType) {
		return mimeType.supportsPreview() || mimeType.isSelfPreview() || mimeType.supportsThumbnail();
	}

	private static void verifyFileExistence(File document) {
		if (document == null) {
			throw new IllegalArgumentException("Cannot provide null file for preview and thumbnail generation!");
		} else if (!document.exists()) {
			throw new IllegalArgumentException("File does not exist for path " + document.getPath());
		}
	}

	private static boolean shouldSkipPreview(MimeType mimeType) {
		return !mimeType.supportsPreview() || mimeType.isSelfPreview();
	}

}
