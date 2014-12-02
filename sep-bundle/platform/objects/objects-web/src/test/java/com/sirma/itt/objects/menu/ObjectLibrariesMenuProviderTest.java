package com.sirma.itt.objects.menu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.SemanticDefinitionService;
import com.sirma.itt.emf.instance.model.ClassInstance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.objects.web.menu.ObjectLibrariesMenuProvider;
import com.sirma.itt.objects.web.menu.ObjectLibraryMenuItem;

/**
 * Tests for ObjectLibrariesMenuProviderTest class.
 *
 * @author svelikov
 */
@Test
public class ObjectLibrariesMenuProviderTest {

	private static final String CHD_PAINTINGS = "chd:Paintings";
	private static final String PAINTINGS = "Paintings";
	private static final String CHD_BOOKS = "chd:Books";
	private static final String BOOKS = "Books";
	private static final String LANGUAGE = "en";

	private final ObjectLibrariesMenuProvider menuProvider;

	private final SemanticDefinitionService semanticDefinitionService;

	/**
	 * Instantiates a new semantic object libraries menu provider test.
	 */
	public ObjectLibrariesMenuProviderTest() {
		menuProvider = new ObjectLibrariesMenuProvider();

		semanticDefinitionService = Mockito.mock(SemanticDefinitionService.class);

		ReflectionUtils.setField(menuProvider, "semanticDefinitionService",
				semanticDefinitionService);
	}

	/**
	 * Test for getObjectLibraryMenuItems.
	 */
	public void getObjectLibraryMenuItemsTest() {
		List<ClassInstance> objectLibrary = new ArrayList<>();

		Mockito.when(semanticDefinitionService.getObjectLibrary()).thenReturn(objectLibrary);
		//
		menuProvider.init();
		List<ObjectLibraryMenuItem> menuItems = menuProvider.getObjectLibraryMenuItems();
		Assert.assertNotNull(menuItems);
		Assert.assertTrue(menuItems.isEmpty());

		//
		objectLibrary.add(buildSemanticInstanceClass(BOOKS, CHD_BOOKS));
		objectLibrary.add(buildSemanticInstanceClass(PAINTINGS, CHD_PAINTINGS));
		menuProvider.init();
		menuItems = menuProvider.getObjectLibraryMenuItems();
		Assert.assertNotNull(menuItems);
		Assert.assertTrue(menuItems.size() == 2);
		ObjectLibraryMenuItem menuItem1 = menuItems.get(0);
		Assert.assertEquals(menuItem1.getLabel(), BOOKS);
		Assert.assertEquals(menuItem1.getObjectType(), CHD_BOOKS);
		ObjectLibraryMenuItem menuItem2 = menuItems.get(1);
		Assert.assertEquals(menuItem2.getLabel(), PAINTINGS);
		Assert.assertEquals(menuItem2.getObjectType(), CHD_PAINTINGS);
	}

	/**
	 * Builds the semantic instance class.
	 *
	 * @param title
	 *            the title
	 * @param id
	 *            the id
	 * @return the instance
	 */
	private ClassInstance buildSemanticInstanceClass(String title, Serializable id) {
		ClassInstance semanticClass = new ClassInstance();
		semanticClass.setId(id);
		Map<String, Serializable> properties = new HashMap<>();
		semanticClass.setProperties(properties);
		properties.put(DefaultProperties.TITLE, title);
		semanticClass.setLabel(LANGUAGE, title);
		return semanticClass;
	}

}
