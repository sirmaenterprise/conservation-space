package com.sirma.sep.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link IdResolver}
 *
 * @author BBonev
 */
public class IdResolverTest {
	@InjectMocks
	private IdResolver resolver;
	@Mock
	private TypeConverter typeConverter;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldGetIdFromEntity() throws Exception {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instanceID");
		Optional<Serializable> resolved = resolver.resolve(instance);
		assertNotNull(resolved);
		assertTrue(resolved.isPresent());
		assertEquals(instance.getId(), resolved.get());
	}

	@Test
	public void shouldGetIdFromIdentity() throws Exception {
		InstanceReference reference = InstanceReferenceMock.createGeneric("emf:referenceId");
		Optional<Serializable> resolved = resolver.resolve(reference);
		assertNotNull(resolved);
		assertTrue(resolved.isPresent());
		assertEquals(reference.getId(), resolved.get());
	}

	@Test
	public void shouldTryConvertingArgumentToStringIfNotEntityOrIdentity() throws Exception {
		when(typeConverter.tryConvert(eq(String.class), anyString())).then(a -> a.getArgumentAt(1, String.class));
		Optional<Serializable> resolved = resolver.resolve("someId");
		assertNotNull(resolved);
		assertTrue(resolved.isPresent());
		assertEquals("someId", resolved.get());
		verify(typeConverter).tryConvert(String.class, "someId");
	}

	@Test
	public void shouldReturnEmptyOptionalOnNullArgument() throws Exception {
		Optional<Serializable> resolved = resolver.resolve(null);
		assertNotNull(resolved);
		assertFalse(resolved.isPresent());
	}
}
