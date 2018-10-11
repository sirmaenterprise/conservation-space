package com.sirma.sep.instance.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.template.TemplateDefinitionImpl;

/**
 * Test for {@link TemplateDefinitionKryoInitializer}.
 *
 * @author A. Kunchev
 */
public class TemplateDefinitionKryoInitializerTest {

	private TemplateDefinitionKryoInitializer initializer;

	@Before
	public void setup() {
		initializer = new TemplateDefinitionKryoInitializer();
	}

	@Test
	public void getClassesToRegister() {
		List<Pair<Class<?>, Integer>> classesToRegister = initializer.getClassesToRegister();
		assertNotNull(classesToRegister);
		assertFalse(classesToRegister.isEmpty());
		assertEquals(1, classesToRegister.size());
		assertEquals(TemplateDefinitionImpl.class, classesToRegister.get(0).getFirst());
	}
}