package com.sirma.itt.emf.web.rest;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.library.LibraryProvider;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Tests for header rest service.
 *
 * @author velikov
 */
public class HeaderRestServiceTest {

	@InjectMocks
	private HeaderRestService headerRestService = new HeaderRestService();

	@Mock
	private LibraryProvider libraryProvider;

	@Mock
	private UserPreferences userPreferences;

	@Mock
	private CodelistService codelistService;

	@Mock
	private DictionaryService dictionaryService;

	public HeaderRestServiceTest() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(userPreferences.getLanguage()).thenReturn("en");
	}

	@Test
	public void getLibrary_return_object_libraries_model() throws JSONException {
		Mockito
				.when(libraryProvider.getAllowedLibraries("object-library", ActionTypeConstants.VIEW_DETAILS))
					.thenReturn(buildClassInstanceList());
		String model = headerRestService.getLibrary("object");
		String expected = "[{\"name\":\"chd:type1\",\"label\":\"Type1\",\"href\":\"/search/basic-search.jsf?library=object-library&libraryTitle=Type1&objectType[]=chd:type1\"},{\"name\":\"chd:type2\",\"label\":\"Type2\",\"href\":\"/search/basic-search.jsf?library=object-library&libraryTitle=Type2&objectType[]=chd:type2\"}]";
		Assert.assertEquals(expected, model);
	}

	@Test
	public void getLibrary_return_project_libraries_model() throws JSONException {
		Mockito
				.when(libraryProvider.getAllowedLibraries("project-library", ActionTypeConstants.VIEW_DETAILS))
					.thenReturn(buildClassInstanceList());
		DefinitionMock definitionMock = new DefinitionMock();
		definitionMock.setIdentifier("1");
		PropertyDefinitionMock propertyDefinitionMock = new PropertyDefinitionMock();
		propertyDefinitionMock.setIdentifier("type");
		propertyDefinitionMock.setType(DefaultProperties.TYPE);
		propertyDefinitionMock.setCodelist(100);
		definitionMock.getFields().add(propertyDefinitionMock);
		Mockito.when(dictionaryService.find("Type1")).thenReturn(definitionMock);
		Mockito.when(dictionaryService.find("Type2")).thenReturn(definitionMock);

		String model = headerRestService.getLibrary("project");
		String expected = "[{\"name\":\"Type1\",\"label\":\"Type1\",\"href\":\"/search/basic-search.jsf?library=project-library&libraryTitle=Type1&objectType[]=Type1\"},{\"name\":\"Type2\",\"label\":\"Type2\",\"href\":\"/search/basic-search.jsf?library=project-library&libraryTitle=Type2&objectType[]=Type2\"}]";
		Assert.assertEquals(expected, model);
	}

	private List<ClassInstance> buildClassInstanceList() {
		List<ClassInstance> list = new ArrayList<>(2);
		list.add(buildClassInstance("Type1", "chd:type1"));
		list.add(buildClassInstance("Type2", "chd:type2"));
		return list;
	}

	private ClassInstance buildClassInstance(String label, String id) {
		ClassInstance instance = new ClassInstance();
		instance.setLabel("en", label);
		instance.setId(id);
		return instance;
	}

}
