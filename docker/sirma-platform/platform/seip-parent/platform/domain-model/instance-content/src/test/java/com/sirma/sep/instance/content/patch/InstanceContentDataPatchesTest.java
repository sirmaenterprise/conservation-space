package com.sirma.sep.instance.content.patch;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

/**
 * Test for {@link InstanceContentDataPatches}.
 *
 * @author A. Kunchev
 */
public class InstanceContentDataPatchesTest {

	@Test
	public void getPath_shouldLeadToFile() throws IOException {
		String path = new InstanceContentDataPatches().getPath();
		try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
			assertNotNull(stream);
		}
	}
}
