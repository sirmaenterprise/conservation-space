package com.sirmaenterprise.sep.models;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * Test for {@link ModelsInfo}
 *
 * @author BBonev
 */
public class ModelsInfoTest {


	@Test
	public void testValidate() throws Exception {

		ModelsInfo info = new ModelsInfo();
		// roots
		ModelInfo modelInfo = new ModelInfo("1", "1", "class");
		modelInfo.setIsAccessible(true);
		info.add(modelInfo);
		info.add(new ModelInfo("2", "2", "class"));
		info.add(new ModelInfo("3", "3", "class"));

		modelInfo = new ModelInfo("1.1", "1.1", "class", "1", false);
		modelInfo.setIsAccessible(true);
		info.add(modelInfo);
		info.add(new ModelInfo("1.2", "1.2", "class", "1", false));
		info.add(new ModelInfo("1.3", "1.3", "class", "1", false));
		info.add(new ModelInfo("2.1", "2.1", "class", "2", false));

		modelInfo = new ModelInfo("1.1.1", "1.1.1", "class", "1.1", false);
		modelInfo.setIsAccessible(true);
		info.add(modelInfo);
		modelInfo = new ModelInfo("1.1.2", "1.1.2", "definition", "1.1", false);
		modelInfo.setIsAccessible(true);
		info.add(modelInfo);
		info.add(new ModelInfo("1.3.1", "1.3.1", "class", "1.3", false));

		modelInfo = new ModelInfo("1.1.1.1", "1.1.1.1", "definition", "1.1.1", false);
		modelInfo.setIsAccessible(true);
		info.add(modelInfo);
		modelInfo = new ModelInfo("1.1.1.2", "1.1.1.2", "definition", "1.1.1", false);
		modelInfo.setIsAccessible(true);
		info.add(modelInfo);

		info.add(new ModelInfo("1.1.1.2.1", "1.1.1.2.1", "definition", "1.1.1.2", false));

		info.validateAndCleanUp();

		Set<String> entries = new HashSet<>();
		for (ModelInfo model : info) {
			entries.add(model.getId());
		}
		Set<String> shouldBePresent = new HashSet<>(Arrays.asList("1", "1.1", "1.1.1", "1.1.1.1", "1.1.1.2", "1.1.2"));
		assertTrue(entries.containsAll(shouldBePresent));

		List<String> shouldNotbePresent = Arrays.asList("2", "3", "2.1", "1.1.1.2.1", "1.2", "1.3", "1.3.1");
		for (String id : shouldNotbePresent) {
			assertFalse("Result should not contain " + id, entries.contains(id));
		}

		entries.removeAll(shouldNotbePresent);
		entries.removeAll(shouldBePresent);
		assertTrue("Should not have any leftovers: " + entries, entries.isEmpty());
	}
}
