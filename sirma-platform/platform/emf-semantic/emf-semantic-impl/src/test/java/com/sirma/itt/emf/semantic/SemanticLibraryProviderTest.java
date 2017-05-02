package com.sirma.itt.emf.semantic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.mocks.SemanticDefinitionServiceMock;
import com.sirma.itt.emf.mocks.search.SearchServiceMock;
import com.sirma.itt.emf.semantic.library.SemanticLibraryProvider;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.library.LibraryProvider;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Tests SemanticLibraryProvider
 *
 * @author kirq4e
 */
public class SemanticLibraryProviderTest extends GeneralSemanticTest<SemanticLibraryProvider> {

	private SemanticDefinitionService semanticDefinitionService;

	@BeforeClass
	public void init() {
		service = new SemanticLibraryProvider();
		semanticDefinitionService = new SemanticDefinitionServiceMock(context);
		ReflectionUtils.setField(service, "semanticDefinitionService", semanticDefinitionService);
		NamespaceRegistryService namespaceRegistryService = new NamespaceRegistryMock(context);
		ReflectionUtils.setField(service, "namespaceRegistryService", namespaceRegistryService);

		SearchService searchService = new SearchServiceMock(context);
		ReflectionUtils.setField(service, "searchService", searchService);
		UserPreferences preferences = mock(UserPreferences.class);
		when(preferences.getLanguage()).thenReturn("en");
		ReflectionUtils.setField(service, "userPreferences", preferences);
	}

	@Test
	public void testGetLibrariesForAdmin() {
		AuthorityService authorityService = Mockito.mock(AuthorityService.class);
		Mockito.when(authorityService.isAdminOrSystemUser()).thenReturn(Boolean.TRUE);
		ReflectionUtils.setField(service, "authorityService", authorityService);

		List<Instance> allowedLibraries = service.getLibraries(ActionTypeConstants.VIEW_DETAILS);

		Assert.assertFalse(allowedLibraries.isEmpty());
	}

	@Test
	public void testGetAllowedLibrariesForAdmin() {
		AuthorityService authorityService = Mockito.mock(AuthorityService.class);
		Mockito.when(authorityService.isAdminOrSystemUser()).thenReturn(Boolean.TRUE);
		ReflectionUtils.setField(service, "authorityService", authorityService);

		List<ClassInstance> allowedLibraries = service.getAllowedLibraries(LibraryProvider.OBJECT_LIBRARY,
				ActionTypeConstants.VIEW_DETAILS);

		List<ClassInstance> library = semanticDefinitionService.getLibrary(LibraryProvider.OBJECT_LIBRARY);
		Assert.assertEquals(allowedLibraries, library);
		Assert.assertFalse(allowedLibraries.isEmpty());
	}

	@Test
	@SuppressWarnings("boxing")
	public void testGetAllowedLibrariesForNonAdmin() {
		AuthorityService authorityService = Mockito.mock(AuthorityService.class);
		Mockito.when(authorityService.isAdminOrSystemUser()).thenReturn(Boolean.FALSE);
		Mockito
				.when(authorityService.isActionAllowed(Matchers.any(Instance.class), Matchers.anyString(),
						Matchers.anyString()))
					.thenReturn(Boolean.FALSE);
		ReflectionUtils.setField(service, "authorityService", authorityService);

		List<ClassInstance> allowedLibraries = service.getAllowedLibraries(LibraryProvider.OBJECT_LIBRARY,
				ActionTypeConstants.VIEW_DETAILS);

		Assert.assertTrue(allowedLibraries.isEmpty());
	}

	@Test
	@SuppressWarnings("boxing")
	public void testGetAllowedLibrariesForNonAdminWithPermissions() {
		AuthorityService authorityService = Mockito.mock(AuthorityService.class);
		Mockito.when(authorityService.isAdminOrSystemUser()).thenReturn(Boolean.FALSE);
		Mockito
				.when(authorityService.isActionAllowed(Matchers.any(ClassInstance.class), Matchers.anyString(),
						Matchers.any()))
					.thenReturn(Boolean.TRUE);
		ReflectionUtils.setField(service, "authorityService", authorityService);

		List<ClassInstance> allowedLibraries = service.getAllowedLibraries(LibraryProvider.OBJECT_LIBRARY,
				ActionTypeConstants.VIEW_DETAILS);

		Assert.assertFalse(allowedLibraries.isEmpty());
		List<ClassInstance> library = semanticDefinitionService.getLibrary(LibraryProvider.OBJECT_LIBRARY);
		for (ClassInstance classInstance : allowedLibraries) {
			Assert.assertTrue(library.contains(classInstance));
		}
	}

	@Test
	public void testGetAllowedLibrariesForNonExistingLibrary() {
		AuthorityService authorityService = Mockito.mock(AuthorityService.class);
		ReflectionUtils.setField(service, "authorityService", authorityService);

		List<ClassInstance> allowedLibraries = service.getAllowedLibraries("test", ActionTypeConstants.VIEW_DETAILS);
		Assert.assertTrue(allowedLibraries.isEmpty());
	}

	@Test
	public void testGetLibraryElementForExistingElement() {
		String testUri = "emf:Project";
		ClassInstance element = service.getLibraryElement(LibraryProvider.OBJECT_LIBRARY, testUri);
		Assert.assertNotNull(element);
		Assert.assertEquals(element.getId(), EMF.PROJECT.toString());
	}

	@Test
	public void testGetLibraryElementForNonExistingElement() {
		String testUri = "emf:TEST23";
		ClassInstance element = service.getLibraryElement(LibraryProvider.OBJECT_LIBRARY, testUri);
		Assert.assertNull(element);
	}

	@Test
	public void testGetLibraryElementForNonExistingLibrary() {
		String testUri = "emf:TEST23";
		ClassInstance element = service.getLibraryElement("test", testUri);
		Assert.assertNull(element);
	}

	@Override
	protected String getTestDataFile() {
		return "SemanticDefinitionServiceTestData.ttl";
	}

}
