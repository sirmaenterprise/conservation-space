package com.sirma.itt.seip.instance.types;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.instance.InstanceTypeResolver;

/**
 * Test for {@link ObjectInstanceTypeResolver}.
 *
 * @author A. Kunchev
 */
@RunWith(MockitoJUnitRunner.class)
public class ObjectInstanceTypeResolverTest {

	@InjectMocks
	private ObjectInstanceTypeResolver resolver;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Test
	public void canResolve() {
		// last resolver in the chain, always returns true
		assertTrue(resolver.canResolve("id"));
	}

	@Test
	public void resolve() {
		resolver.resolve("emf:instance-id");
		verify(instanceTypeResolver).resolve("emf:instance-id");
	}
}