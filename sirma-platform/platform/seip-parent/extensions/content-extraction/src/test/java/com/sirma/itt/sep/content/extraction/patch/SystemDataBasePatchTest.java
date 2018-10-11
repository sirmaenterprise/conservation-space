package com.sirma.itt.sep.content.extraction.patch;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for {@link SystemDataBasePatch}.
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class SystemDataBasePatchTest {

	@InjectMocks
	private SystemDataBasePatch systemDataBasePatch;

	@Test
	public void should_ReturnCorrectPath() {
		Assert.assertTrue(systemDataBasePatch.getPath().endsWith("system-data-base-changelog.xml"));
	}

}