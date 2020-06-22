package com.sirma.itt.seip.instance.archive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DefinitionAccessor;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;

/**
 * Test for {@link ArchiveDefinitionAccessor}.
 *
 * @author A. Kunchev
 */
public class ArchiveDefinitionAccessorTest {

	@InjectMocks
	private DefinitionAccessor accessor;

	@Mock
	private DefinitionService definitionService;

	@Before
	public void setup() {
		accessor = new ArchiveDefinitionAccessor();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getSupportedObjects() {
		Set<Class<?>> supportedObjects = accessor.getSupportedObjects();
		assertEquals(1, supportedObjects.size());
		assertEquals(ArchivedInstance.class, supportedObjects.iterator().next());
	}

	@Test
	public void getAllDefinitions() {
		assertEquals(Collections.emptyList(), accessor.getAllDefinitions());
	}

	@Test
	public void getDefinitionString() {
		assertNull(accessor.getDefinition(""));
	}

	@Test
	public void getDefinitionStringLong() {
		assertNull(accessor.getDefinition("", 1L));
	}

	@Test
	public void getDefinitionInstance_noArchivedInstance() {
		assertNull(accessor.getDefinition(new EmfInstance()));
	}

	@Test
	public void getDefinitionInstance_withAccessorsNoDefinitionFound() {
		ArchivedInstance instance = getInstanceWithReference();
		when(definitionService.getInstanceDefinition(new EmfInstance())).thenReturn(null);
		assertNull(accessor.getDefinition(instance));
	}

	@Test
	public void getDefinitionInstance_withAccessorsDefinitionFound() {
		ArchivedInstance instance = getInstanceWithReference();
		DefinitionModel model = mock(DefinitionModel.class);
		when(definitionService.getInstanceDefinition(any(Instance.class))).thenReturn(model);
		assertEquals(model, accessor.getDefinition(instance));
	}

	private static ArchivedInstance getInstanceWithReference() {
		DataTypeDefinition typeDefinition = mock(DataTypeDefinition.class);
		when(typeDefinition.getJavaClass()).thenAnswer(a -> {
			return new EmfInstance().getClass();
		});
		ArchivedInstance instance = mock(ArchivedInstance.class);
		InstanceReference reference = mock(InstanceReference.class);
		when(reference.getReferenceType()).thenReturn(typeDefinition);
		when(instance.toReference()).thenReturn(reference);
		return instance;
	}

	@Test
	public void saveDefinition() {
		assertNull(accessor.saveDefinition(null));
	}

	@Test
	public void removeDefinition() {
		assertEquals(Collections.emptyList(), accessor.removeDefinition("", 1L, null));
	}

	@Test
	public void computeHash() {
		assertEquals(0, accessor.computeHash(null));
	}

}
