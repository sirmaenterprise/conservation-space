package com.sirma.sep.definition.serialization.kryo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.Pair;

/**
 * Test for {@link DefinitionsKryoInitializer}.
 * 
 * @author A. Kunchev
 */
public class DefinitionsKryoInitializerTest {

	private DefinitionsKryoInitializer initializer;

	@Before
	public void setup() {
		initializer = new DefinitionsKryoInitializer();
	}

	@Test
	public void getClassesToRegister() {
		List<Pair<Class<?>, Integer>> classesToRegister = initializer.getClassesToRegister();
		assertNotNull(classesToRegister);
		assertEquals(14, classesToRegister.size());
	}
}
