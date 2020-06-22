package com.sirma.itt.seip.permissions.db.patches;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.event.SemanticDefinitionsReloaded;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.library.LibraryProvider;
import com.sirma.itt.seip.permissions.EntityPermissions;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Test for {@link LibrariesPermissionInitializer}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 03/11/2017
 */
public class LibrariesPermissionInitializerTest {
	@InjectMocks
	private LibrariesPermissionInitializer permissionInitializer;

	@Mock
	private SemanticDefinitionService semanticService;
	@Spy
	private InstanceProxyMock<SemanticDefinitionService> semanticDefinitionService = new InstanceProxyMock<>();
	@Mock
	private PermissionService permissionService;
	@Mock
	private ResourceService resourceService;
	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		semanticDefinitionService.set(semanticService);

		EmfGroup everyone = new EmfGroup("everyone", "everyone");
		everyone.setId("emf:everyone");
		when(resourceService.getAllOtherUsers()).thenReturn(everyone);
		when(namespaceRegistryService.getShortUri(anyString())).then(a -> a.getArgumentAt(0, String.class));

		TypeConverter converter = mock(TypeConverter.class);
		when(converter.convert(eq(InstanceReference.class), any(ObjectInstance.class))).then(
				a -> InstanceReferenceMock.createGeneric(a.getArgumentAt(1,
						Instance.class)));
		TypeConverterUtil.setTypeConverter(converter);
	}

	@Test
	public void initializeLibraryPermissions_shouldInitOnlyLibrariesWithoutPermissions() throws Exception {
		ClassInstance withPermissions = new ClassInstance();
		withPermissions.setId("emf:WithPermissions");
		ClassInstance withoutPermissions = new ClassInstance();
		withoutPermissions.setId("emf:WithoutPermissions");
		mockSemanticService(Arrays.asList(withoutPermissions, withPermissions));

		InstanceReferenceMock withoutPermissionsRef = InstanceReferenceMock.createGeneric(withoutPermissions);
		when(permissionService.getPermissionsInfo(withoutPermissionsRef)).thenReturn(Optional.empty());
		mockPermissionsService(withPermissions, true);

		permissionInitializer.initializeLibraryPermissions();

		verifyThatPermissionsAreSet(withoutPermissionsRef);
	}

	@Test
	public void initializeLibraryPermissions_shouldChangeFlagForLibraries() {
		ClassInstance library = new ClassInstance();
		library.setId("emf:Library");
		mockSemanticService(Arrays.asList(library));
		mockPermissionsService(library, false);

		permissionInitializer.initializeLibraryPermissions();

		ArgumentCaptor<Collection<PermissionsChange>> argCaptor = ArgumentCaptor.forClass(Collection.class);
		verify(permissionService).setPermissions(eq(InstanceReferenceMock.createGeneric(library)), argCaptor.capture());

		Collection<PermissionsChange> changes = argCaptor.getValue();
		assertEquals(1, changes.size());
		assertTrue(changes.iterator().next() instanceof PermissionsChange.SetLibraryIndicatorChange);
	}

	@Test
	public void initializeLibraryPermissionsOnModelsReload_shouldInitOnlyLibrariesWithoutPermissions() {
		ClassInstance library = new ClassInstance();
		library.setId("emf:Library");
		mockSemanticService(Arrays.asList(library));

		InstanceReferenceMock withoutPermissionsRef = InstanceReferenceMock.createGeneric(library);
		when(permissionService.getPermissionsInfo(withoutPermissionsRef)).thenReturn(Optional.empty());

		permissionInitializer.initializeLibraryPermissionsOnModelsReload(new SemanticDefinitionsReloaded());

		verifyThatPermissionsAreSet(withoutPermissionsRef);
	}

	private void mockSemanticService(List<ClassInstance> libraries) {
		when(semanticService.getLibrary(LibraryProvider.OBJECT_LIBRARY)).thenReturn(libraries);
	}

	private void mockPermissionsService(ClassInstance instance, boolean isLibrary) {
		EntityPermissions entityPermissions = new EntityPermissions((String) instance.getId(), null, null, false, false,
				isLibrary);

		when(permissionService.getPermissionsInfo(InstanceReferenceMock.createGeneric(instance)))
				.thenReturn(Optional.of(entityPermissions));
	}

	private void verifyThatPermissionsAreSet(InstanceReferenceMock instanceRef) {
		verify(permissionService).setPermissions(eq(instanceRef), argThat(CustomMatcher.ofPredicate(list -> {
			long extraChanges = list.stream()
					.filter(change -> !(change instanceof PermissionsChange.SetLibraryIndicatorChange))
					.filter(change -> !(change instanceof PermissionsChange.AddRoleAssignmentChange))
					.count();
			return list.size() == 2 && extraChanges == 0;
		})));
	}

}
