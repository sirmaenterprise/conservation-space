package com.sirma.itt.seip.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

/**
 * Test For {@link ResourceLoadUtil}
 *
 * @author BBonev
 */
public class ResourceLoadUtilTest {


	@Test
	public void loadResource() throws Exception {

		assertNull(ResourceLoadUtil.loadResource(getClass(), null));
		assertNull(ResourceLoadUtil.loadResource(getClass(), ""));
		assertNull(ResourceLoadUtil.loadResource(getClass(), "invalidFile.txt"));

		assertEquals("test", ResourceLoadUtil.loadResource(getClass(), "testFile.txt"));
		assertEquals("test", ResourceLoadUtil.loadResource(null, "testFile.txt"));

		assertEquals("test", ResourceLoadUtil.loadResource(getClass(),
				getClass().getPackage().getName().replace('.', '/') + "/testFile.txt"));
	}

	@Test
	public void loadResources() throws Exception {

		Collection<String> resources = ResourceLoadUtil.loadResources(getClass());
		assertNotNull(resources);
		assertTrue(resources.isEmpty());
		resources = ResourceLoadUtil.loadResources(getClass(), "");
		assertNotNull(resources);
		assertTrue(resources.isEmpty());
		resources = ResourceLoadUtil.loadResources(getClass(), new String[0]);
		assertNotNull(resources);
		assertTrue(resources.isEmpty());
		resources = ResourceLoadUtil.loadResources(getClass(), "invalidFile.txt");
		assertNotNull(resources);
		assertTrue(resources.isEmpty());

		List<String> expected = Arrays.asList("test");
		assertEquals(expected, ResourceLoadUtil.loadResources(getClass(), "testFile.txt"));
		assertEquals(expected, ResourceLoadUtil.loadResources(null, "testFile.txt"));

		assertEquals(expected, ResourceLoadUtil.loadResources(getClass(),
				getClass().getPackage().getName().replace('.', '/') + "/testFile.txt"));
	}

}
