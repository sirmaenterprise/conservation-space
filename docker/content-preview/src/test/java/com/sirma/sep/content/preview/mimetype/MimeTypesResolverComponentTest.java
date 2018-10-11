package com.sirma.sep.content.preview.mimetype;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Tests the mime type resolving in {@link MimeTypesResolver} with initialized SpringBoot context.
 *
 * @author Mihail Radkov
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.config.name=application,mimetypes")
public class MimeTypesResolverComponentTest {

	@Autowired
	private MimeTypesResolver mimeTypesResolver;

	@Test(expected = IllegalArgumentException.class)
	public void resolve_shouldBlowForNullArgument() {
		mimeTypesResolver.resolve(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void resolve_shouldBlowForInvalidArgument() {
		mimeTypesResolver.resolve("");
	}

	@Test
	public void resolve_availableMimetype() {
		assertMimeType("mime1", "mime1", MimeTypeSupport.SELF, MimeTypeSupport.YES);
		assertMimeType("mime2", "mime2.*", MimeTypeSupport.NO, MimeTypeSupport.YES);
		assertMimeType("mime3", "mime3", MimeTypeSupport.YES, MimeTypeSupport.NO);
	}

	@Test
	public void resolve_shouldSupportWildcardMapping() {
		assertMimeType("mime2abc", "mime2.*", MimeTypeSupport.NO, MimeTypeSupport.YES);
		assertMimeType("mime4abc", "mime4.*", MimeTypeSupport.YES, MimeTypeSupport.NO);
	}

	@Test
	public void resolve_shouldResolveOverriddenMimeType() {
		assertMimeType("mime4", "mime4", MimeTypeSupport.NO, MimeTypeSupport.NO);
	}

	private void assertMimeType(String mimetype, String name, MimeTypeSupport preview, MimeTypeSupport thumbnail) {
		Optional<MimeType> resolved = mimeTypesResolver.resolve(mimetype);
		Assert.assertTrue(resolved.isPresent());
		Assert.assertEquals(name, resolved.get().getName());
		Assert.assertEquals(preview, resolved.get().getPreview());
		Assert.assertEquals(thumbnail, resolved.get().getThumbnail());
	}

}
