package com.sirma.itt.emf.serialization.kryo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.Pair;

/**
 * Test for {@link EmfKryoInitializer}.
 *
 * @author A. Kunchev
 */
public class EmfKryoInitializerTest {

	private EmfKryoInitializer initializer;

	@Before
	public void setup() {
		initializer = new EmfKryoInitializer();
	}

	@Test
	public void getClassesToRegister() {
		List<Pair<Class<?>, Integer>> classesToRegister = initializer.getClassesToRegister();
		assertNotNull(classesToRegister);
		assertEquals(19, classesToRegister.size());
	}
}
