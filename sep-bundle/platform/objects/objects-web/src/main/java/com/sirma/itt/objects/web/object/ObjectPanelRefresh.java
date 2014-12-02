package com.sirma.itt.objects.web.object;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.emf.instance.InstanceContextInitializer;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.objects.services.ObjectService;

/**
 * Handles refreshing of the tree location panel when working with object.
 * 
 * @author sdjulgerova
 */
@Named
@RequestScoped
public class ObjectPanelRefresh {

	@Inject
	private DocumentContext documentContext;

	@Inject
	private ObjectService objectService;
	
	@Inject
	private InstanceContextInitializer instanceContextInitializer;

	private String id;

	/**
	 * Loads a object instance by id and sets it in the context.
	 */
	public void refresh() {
		Instance objInstance = objectService.loadByDbId(id);
		instanceContextInitializer.restoreHierarchy(objInstance, documentContext.getRootInstance());
		documentContext.setCurrentInstance(objInstance);
	}

	/**
	 * Getter method for id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Setter method for id.
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

}
