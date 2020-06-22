package com.sirma.itt.seip.io;

import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.io.ResourceSource;

/**
 * The Class ResourceSourceTest.
 */
public class ResourceSourceTest {

	/**
	 * Test init.
	 */
	@Test
	public void testInit() {
		try {
			new ResourceSource(null);
			Assert.fail("Should fail");
		} catch (Exception e) {
			// skip
		}
		new ResourceSource("/");
		new ResourceSource("/test");
		new ResourceSource("test");

	}

	/**
	 * Test get name.
	 */
	@Test
	public void testGetName() {
		com.sirma.itt.seip.io.ResourceSource resourceSource = new ResourceSource("/test");
		Assert.assertEquals(resourceSource.getName(), "/test");
		resourceSource = new ResourceSource("\test");
		Assert.assertEquals(resourceSource.getName(), "\test");
	}

	/**
	 * Test is accessible.
	 */
	@Test
	public void testIsAccessible() {
		ResourceSource resourceSource = new ResourceSource("file");
		Assert.assertTrue(resourceSource.isAccessible());
		resourceSource = new ResourceSource("test_doesnotexist");
		Assert.assertFalse(resourceSource.isAccessible());
	}

	/**
	 * Test load.
	 */
	@Test
	public void testLoad() {
		ResourceSource resourceSource = new ResourceSource("file");
		try (InputStream data = resourceSource.load()) {
			Assert.assertNotNull(data);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}

		resourceSource = new ResourceSource("folder/file");
		try (InputStream data = resourceSource.load()) {
			Assert.assertNotNull(data);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
}
