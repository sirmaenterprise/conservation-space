package com.sirma.itt.objects.web.menu;

import java.util.Arrays;
import java.util.List;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.web.plugin.PageModelBuilder;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.library.LibraryProvider;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * Tests {@link ObjectsMenuItem.ObjectsLibraryList}
 * 
 * @author kirq4e
 */
public class ObjectsMenuItemTest {

	@Mock
	private UserPreferences userPreferences;

	@Mock
	private LibraryProvider libraryProvider;

	@Mock
	private PageModelBuilder modelBuilder;

	private ObjectsMenuItem.ObjectsLibraryList service;

	@BeforeClass
	public void init() {
		MockitoAnnotations.initMocks(this);

		List<ClassInstance> libraryElements = mockObjectLibrary();
		Mockito
				.when(libraryProvider.getAllowedLibraries(LibraryProvider.OBJECT_LIBRARY,
						ActionTypeConstants.VIEW_DETAILS))
					.thenReturn(libraryElements);
		Mockito.when(userPreferences.getLanguage()).thenReturn("en");

		Mockito.when(modelBuilder.buildTemplate(Matchers.anyMap(), Matchers.anyString())).thenReturn(
				"Builded template");

		service = new ObjectsMenuItem.ObjectsLibraryList();
		ReflectionUtils.setField(service, "userPreferences", userPreferences);
		ReflectionUtils.setField(service, "libraryProvider", libraryProvider);
		ReflectionUtils.setField(service, "modelBuilder", modelBuilder);
	}

	/**
	 * Tests build of page fragment for object library list
	 */
	@Test
	public void testGetPageFragment() {
		String pageFragment = service.getPageFragment();
		Assert.assertNotNull(pageFragment);
	}

	private static List<ClassInstance> mockObjectLibrary() {
		ClassInstance instance = new ClassInstance();
		instance.setId("emf:DomainObject");
		instance.setLabel("en", "Domain Object");

		return Arrays.asList(instance);
	}

}
