package com.sirma.itt.objects.web.menu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import com.sirma.cmf.web.rest.SearchQueryParameters;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.definition.SemanticDefinitionService;
import com.sirma.itt.emf.instance.model.ClassInstance;
import com.sirma.itt.emf.security.CurrentUser;
import com.sirma.itt.emf.security.model.User;

/**
 * Provider for the object libraries main menu retrieving allowed libraries from the semantic db.
 *
 * @author svelikov
 */
@RequestScoped
public class ObjectLibrariesMenuProvider implements Serializable {

	private static final long serialVersionUID = -7690783105961725837L;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	@CurrentUser
	private User currentUser;

	@Inject
	@Config(name = EmfConfigurationProperties.SYSTEM_LANGUAGE, defaultValue = "bg")
	private String defaultLanguage;

	private List<ObjectLibraryMenuItem> objectLibraryMenuItems;

	/**
	 * Inits the provider.
	 */
	@PostConstruct
	public void init() {
		objectLibraryMenuItems = buildObjectLibrariesMenuItems();
	}

	/**
	 * Builds the object libraries menu items.
	 *
	 * @return the list
	 */
	public List<ObjectLibraryMenuItem> buildObjectLibrariesMenuItems() {
		List<ObjectLibraryMenuItem> libraries = new ArrayList<>();

		List<ClassInstance> objectLibrary = semanticDefinitionService.getObjectLibrary();
		for (ClassInstance instance : objectLibrary) {
			Map<String, Serializable> properties = instance.getProperties();
			ObjectLibraryMenuItem menuItem = new ObjectLibraryMenuItem(
					instance.getLabel("en"), (String) instance.getId());
			menuItem.addParameter(SearchQueryParameters.OBJECT_TYPE, (String) instance.getId());
			libraries.add(menuItem);
		}

		return libraries;
	}

	/**
	 * Gets the object library menu items.
	 *
	 * @return the object library menu items
	 */
	public List<ObjectLibraryMenuItem> getObjectLibraryMenuItems() {
		return objectLibraryMenuItems;
	}

}
