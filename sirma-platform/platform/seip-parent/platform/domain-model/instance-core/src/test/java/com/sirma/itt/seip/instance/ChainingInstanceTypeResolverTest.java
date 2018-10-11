package com.sirma.itt.seip.instance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link ChainingInstanceTypeResolver}.
 *
 * @author A. Kunchev
 */
public class ChainingInstanceTypeResolverTest {

	@InjectMocks
	private ChainingInstanceTypeResolver resolver;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	private List<InstanceTypeResolver> resolversList = new ArrayList<>();

	@Spy
	private Plugins<InstanceTypeResolver> resolvers = new Plugins<>("", resolversList);

	@Before
	public void setup() {
		resolver = new ChainingInstanceTypeResolver();
		MockitoAnnotations.initMocks(this);
		resolversList.clear();
		resolversList.add(instanceTypeResolver);
	}

	@Test
	public void resolveSerializable() {
		when(instanceTypeResolver.resolve("instance-id")).thenReturn(Optional.empty());
		Optional<InstanceType> type = resolver.resolve("instance-id");
		verify(instanceTypeResolver).resolve("instance-id");
		assertEquals(Optional.empty(), type);
	}

	@Test
	public void resolveReference() {
		when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(Optional.of(new InstanceReferenceMock()));
		Optional<InstanceReference> reference = resolver.resolveReference("instance-id");
		verify(instanceTypeResolver).resolveReference("instance-id");
		assertNotEquals(Optional.empty(), reference);
	}

	@Test
	public void resolveCollectionOfSerializable() {
		List<Serializable> ids = Arrays.asList("instance-id-1", "instance-id-2");
		resolver.resolve(ids);
		verify(instanceTypeResolver).resolve(ids);
	}

	@Test
	public void resolveReferences() {
		List<Serializable> ids = Arrays.asList("instance-id-1", "instance-id-2");
		when(instanceTypeResolver.resolveReferences(ids))
				.thenReturn(Arrays.asList(new InstanceReferenceMock("instance-id-2", null, new EmfInstance()),
						new InstanceReferenceMock("instance-id-1", null, new EmfInstance())));
		Collection<InstanceReference> references = resolver.resolveReferences(ids);
		assertEquals("instance-id-1", references.iterator().next().getId());
	}

	@Test
	public void resolveInstances() {
		List<Serializable> ids = Arrays.asList("instance-id-1", "instance-id-2");
		InstanceReference instance1 = InstanceReferenceMock.createGeneric("instance-id-1");
		InstanceReference instance2 = InstanceReferenceMock.createGeneric("instance-id-2");

		when(instanceTypeResolver.resolveInstances(ids))
				.thenReturn(Arrays.asList(instance1.toInstance(), instance2.toInstance()));
		Collection<Instance> instances = resolver.resolveInstances(ids);
		assertEquals("instance-id-1", instances.iterator().next().getId());
	}

	@Test
	public void exist() {
		List<Serializable> ids = Arrays.asList("instance-id-1", "instance-id-2");
		resolver.exist(ids);
		verify(instanceTypeResolver).exist(ids);
	}
}
