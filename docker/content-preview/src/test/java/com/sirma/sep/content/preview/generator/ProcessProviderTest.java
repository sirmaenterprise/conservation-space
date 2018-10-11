package com.sirma.sep.content.preview.generator;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Tests the {@link Process} provisioning from {@link ProcessProvider}
 *
 * @author Mihail Radkov
 */
public class ProcessProviderTest {

	@Test
	public void shouldProvideProcess() throws Exception {
		ProcessProvider processProvider = new ProcessProvider();
		// OS independent executable
		Process javaHelp = processProvider.getProcess(Arrays.asList("java", "-version"));
		Assert.assertNotNull(javaHelp);
		javaHelp.destroyForcibly();
	}
}
