package com.sirma.itt.emf.semantic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.search.SearchServiceMock;
import com.sirma.itt.emf.semantic.library.SemanticLibraryProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * Tests SemanticLibraryProvider
 *
 * @author kirq4e
 */
public class SemanticLibraryProviderTest extends GeneralSemanticTest<SemanticLibraryProvider> {

	@BeforeClass
	public void init() {
		service = new SemanticLibraryProvider();

		SearchService searchService = new SearchServiceMock(context);
		ReflectionUtils.setFieldValue(service, "searchService", searchService);
		UserPreferences preferences = mock(UserPreferences.class);
		when(preferences.getLanguage()).thenReturn("en");
		ReflectionUtils.setFieldValue(service, "userPreferences", preferences);
	}

	@Test
	public void testGetLibrariesForAdmin() {
		noTransaction();
		AuthorityService authorityService = Mockito.mock(AuthorityService.class);
		Mockito.when(authorityService.isAdminOrSystemUser()).thenReturn(Boolean.TRUE);
		ReflectionUtils.setFieldValue(service, "authorityService", authorityService);

		List<Instance> allowedLibraries = service.getLibraries(ActionTypeConstants.VIEW_DETAILS);

		Assert.assertFalse(allowedLibraries.isEmpty());
	}

	@Override
	protected String getTestDataFile() {
		return "SemanticDefinitionServiceTestData.ttl";
	}

}
