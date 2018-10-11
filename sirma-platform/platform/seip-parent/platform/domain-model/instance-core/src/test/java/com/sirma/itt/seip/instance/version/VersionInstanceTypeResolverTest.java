package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.testutil.fakes.InstanceTypeFake;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link VersionInstanceTypeResolver}.
 *
 * @author A. Kunchev
 */
public class VersionInstanceTypeResolverTest {

	@InjectMocks
	private VersionInstanceTypeResolver resolver;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Mock
	private InstanceTypes instanceTypes;

	@Before
	public void setup() {
		resolver = new VersionInstanceTypeResolver();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void resolveSerializable_nullId() {
		assertEquals(Optional.empty(), resolver.resolve((Serializable) null));
	}

	@Test
	public void resolveSerializable_notVersionId() {
		assertEquals(Optional.empty(), resolver.resolve("emf:instance-id"));
	}

	@Test
	public void resolveSerializable_notFoundInstance() {
		when(instanceVersionService.loadVersion("emf:instance-id-v1.5")).thenReturn(null);
		assertEquals(Optional.empty(), resolver.resolve("emf:instance-id-v1.5"));
	}

	@Test
	public void resolveSerializable_foundInstanceWithoutType() {
		Instance instance = new EmfInstance();
		instance.add(SEMANTIC_TYPE, "semantic-type-uri");

		when(instanceTypes.from("semantic-type-uri"))
				.thenReturn(Optional.of(InstanceTypeFake.build("emf:Object", "objectinstance")));
		when(instanceVersionService.loadVersion("emf:instance-id-v1.5")).thenReturn(instance);

		assertNotNull(resolver.resolve("emf:instance-id-v1.5").get());
	}

	@Test
	public void resolveReference_nullId() {
		assertEquals(Optional.empty(), resolver.resolveReference((Serializable) null));
	}

	@Test
	public void resolveReference_notVersionId() {
		assertEquals(Optional.empty(), resolver.resolveReference("emf:instance-id"));
	}

	@Test
	public void resolveReference_notFoundInstance() {
		when(instanceVersionService.loadVersion("emf:instance-id-v1.2")).thenReturn(null);
		assertEquals(Optional.empty(), resolver.resolveReference("emf:instance-id-v1.2"));
	}

	@Test
	public void resolveReference_foundInstance() {
		InstanceReferenceMock instance = InstanceReferenceMock.createGeneric("id");
		instance.setType(InstanceTypeFake.build("emf:Object", "objectinstance"));
		when(instanceVersionService.loadVersion("emf:instance-id-v1.2")).thenReturn(instance.toInstance());
		assertNotNull(resolver.resolveReference("emf:instance-id-v1.2").get());
	}

	@Test
	public void resolveCollectionOfSerializable_nullIds() {
		assertEquals(Collections.emptyMap(), resolver.resolve((Collection<Serializable>) null));
	}

	@Test
	public void resolveCollectionOfSerializable_emptyIds() {
		assertEquals(Collections.emptyMap(), resolver.resolve(Collections.emptyList()));
	}

	@Test
	public void resolveCollectionOfSerializable_noVersionIds() {
		assertEquals(Collections.emptyMap(), resolver.resolve(Arrays.asList("emf:instance-id-1", "emf:instance-id-2")));
	}

	@Test
	public void resolveCollectionOfSerializable_versionInstancesNotFound() {
		List<Serializable> ids = Arrays.asList("emf:instance-id-v1.5", "emf:instance-id-v1.8");
		when(instanceVersionService.loadVersionsById(ids)).thenReturn(new ArrayList<>());
		assertEquals(Collections.emptyMap(), resolver.resolve(ids));
	}

	@Test
	public void resolveCollectionOfSerializable_foundTwoInstances() {
		List<Serializable> ids = Arrays.asList("emf:instance-id-v1.5", "emf:instance-id-v1.8");
		EmfInstance instance1 = new EmfInstance();
		instance1.setId("emf:instance-id-v1.5");
		instance1.setType(InstanceTypeFake.build("emf:Object", "objectinstance"));

		EmfInstance instance2 = new EmfInstance();
		instance2.setId("emf:instance-id-v1.8");
		instance2.setType(InstanceTypeFake.build("emf:Object", "objectinstance"));

		when(instanceVersionService.loadVersionsById(ids)).thenReturn(Arrays.asList(instance1, instance2));
		assertEquals(2, resolver.resolve(ids).size());
	}

	@Test
	public void resolveReferences_nullIds() {
		assertEquals(Collections.emptyList(), resolver.resolveReferences((Collection<Serializable>) null));
	}

	@Test
	public void resolveReferences_emptyIds() {
		assertEquals(Collections.emptyList(), resolver.resolveReferences(new ArrayList<>()));
	}

	@Test
	public void resolveReferences_noVersionIds() {
		assertEquals(Collections.emptyList(),
				resolver.resolveReferences(Arrays.asList("emf:instance-id-1", "emf:instance-id-2")));
	}

	@Test
	public void resolveReferences_versionInstancesNotFound() {
		List<Serializable> ids = Arrays.asList("emf:instance-id-v1.9", "emf:instance-id-v.110");
		when(instanceVersionService.loadVersionsById(ids)).thenReturn(new ArrayList<>());
		assertEquals(Collections.emptyList(), resolver.resolveReferences(ids));
	}

	@Test
	public void resolveReferences_foundTwoInstances() {
		List<Serializable> ids = Arrays.asList("emf:instance-id-v1.9", "emf:instance-id-v1.10");
		EmfInstance instance1 = new EmfInstance();
		instance1.setId("emf:instance-id-v1.9");
		ReflectionUtils.setFieldValue(instance1, "reference", new InstanceReferenceMock(instance1));

		EmfInstance instance2 = new EmfInstance();
		instance2.setId("emf:instance-id-v1.10");
		ReflectionUtils.setFieldValue(instance2, "reference", new InstanceReferenceMock(instance2));

		when(instanceVersionService.loadVersionsById(ids)).thenReturn(Arrays.asList(instance1, instance2));
		assertEquals(2, resolver.resolveReferences(ids).size());
	}

	@Test
	public void resolveInstances_nullIds() {
		assertEquals(Collections.emptyList(), resolver.resolveInstances((Collection<Serializable>) null));
	}

	@Test
	public void resolveInstances_emptyIds() {
		assertEquals(Collections.emptyList(), resolver.resolveInstances(new ArrayList<>()));
	}

	@Test
	public void resolveInstances_noVersionIds() {
		assertEquals(Collections.emptyList(),
				resolver.resolveInstances(Arrays.asList("emf:instance-id", "emf:instance-id")));
	}

	@Test
	public void resolveInstances_noInstancesFound() {
		List<Serializable> ids = Arrays.asList("emf:instance-id-v1.1", "emf:instance-id-v1.2");
		when(instanceVersionService.loadVersionsById(ids)).thenReturn(new ArrayList<>());
		assertEquals(Collections.emptyList(), resolver.resolveInstances(ids));
	}

	@Test
	public void resolveInstances_foundTwoInstances() {
		List<Serializable> ids = Arrays.asList("emf:instance-id-v1.1", "emf:instance-id-v1.2");
		EmfInstance instance1 = new EmfInstance();
		instance1.setId("emf:instance-id-v1.1");
		instance1.setType(InstanceTypeFake.build("emf:Object", "objectinstance"));

		EmfInstance instance2 = new EmfInstance();
		instance2.setId("emf:instance-id-v1.2");
		instance2.setType(InstanceTypeFake.build("emf:Object", "objectinstance"));

		when(instanceVersionService.loadVersionsById(ids)).thenReturn(Arrays.asList(instance1, instance2));
		assertEquals(2, resolver.resolveInstances(ids).size());
	}

}
