package com.sirma.sep.content.preview.mimetype;

import org.junit.Test;

import java.util.Arrays;

/**
 * Tests the validation logic in {@link MimeTypesResolver}.
 *
 * @author Mihail Radkov
 */
public class MimeTypesResolverValidationTest {

	@Test(expected = IllegalArgumentException.class)
	public void initialization_shouldValidateMimeTypesWithoutName() {
		MimeType mimeType = new MimeType();
		initResolver(mimeType);
	}

	@Test(expected = NullPointerException.class)
	public void initialization_shouldValidateMimeTypesWithoutPreviewMapping() {
		MimeType mimeType = new MimeType();
		mimeType.setName("no/preview-support");
		initResolver(mimeType);
	}

	@Test(expected = NullPointerException.class)
	public void initialization_shouldValidateMimeTypesWithoutThumbnailMapping() {
		MimeType mimeType = new MimeType();
		mimeType.setName("no/preview-support");
		mimeType.setPreview(MimeTypeSupport.YES);
		initResolver(mimeType);
	}

	private void initResolver(MimeType... mimeType) {
		MimeTypesConfiguration mimeTypesConfiguration = new MimeTypesConfiguration();
		mimeTypesConfiguration.setMimeTypes(Arrays.asList(mimeType));
		new MimeTypesResolver(mimeTypesConfiguration);
	}

}
