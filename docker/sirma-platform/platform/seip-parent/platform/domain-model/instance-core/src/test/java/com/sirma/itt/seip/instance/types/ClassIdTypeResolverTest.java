package com.sirma.itt.seip.instance.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

/**
 * Test for {@link ClassIdTypeResolver}.
 *
 * @author A. Kunchev
 */
public class ClassIdTypeResolverTest {

	private ClassIdTypeResolver resolver = new ClassIdTypeResolver();

	@Test
	public void canResolve() {
		assertTrue(resolver.canResolve("emf:Case"));
		assertTrue(resolver.canResolve("emf:RecordSpace"));

		assertFalse(resolver.canResolve("emf:instance-id"));
	}

	@Test
	public void resolve() {
		// always returns empty result to prevent stack overflow
		assertEquals(Optional.empty(), resolver.resolve("id"));
	}
}