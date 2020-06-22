package com.sirma.itt.seip.instance.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.instance.InstanceTypeResolver;

/**
 * Test for {@link VersionTypeResolver}.
 *
 * @author A. Kunchev
 */
@RunWith(MockitoJUnitRunner.class)
public class VersionTypeResolverTest {

	@InjectMocks
	private VersionTypeResolver resolver;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Test
	public void canResolve() {
		assertTrue(resolver.canResolve("emf:version-id-v1.11"));
		assertFalse(resolver.canResolve("emf:version-id"));
	}

	@Test
	public void resolve() {
		resolver.resolve("emf:instance-id-v1.11");
		verify(instanceTypeResolver).resolve("emf:instance-id");
	}
}