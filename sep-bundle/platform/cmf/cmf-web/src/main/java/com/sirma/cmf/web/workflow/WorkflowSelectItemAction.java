package com.sirma.cmf.web.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.Action;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.event.ItemsFilter;
import com.sirma.itt.emf.resources.event.LoadItemsEvent;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.model.User;

/**
 * The Class WorkflowSelectItemAction.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class WorkflowSelectItemAction extends Action implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4260065824606186697L;

	/** The people service. */
	@Inject
	private ResourceService resourceService;

	/** The cmf users. */
	private List<User> emfUsers;

	/**
	 * Load users observer.
	 * 
	 * @param event
	 *            the event
	 */
	// TODO: used from picklist
	public void loadUsers(@Observes @ItemsFilter("users") LoadItemsEvent event) {

		log.debug("CMFWeb: Executing Observer WorkflowSelectItemAction.loadUsers");

		List<PicklistItem> items = new ArrayList<PicklistItem>();

		List<Resource> allCMFUsers = resourceService.getAllResources(ResourceType.USER, null);

		for (Resource emfUser : allCMFUsers) {
			String name = emfUser.getIdentifier();
			items.add(new PicklistItem(name));
		}

		FacesContext.getCurrentInstance().getExternalContext().getRequestMap()
				.put("picklistItems", items);
	}

	/**
	 * Getter method for cmfUsers.
	 * 
	 * @return the cmfUsers
	 */
	public List<User> getCmfUsers() {
		return emfUsers;
	}

	/**
	 * Setter method for cmfUsers.
	 * 
	 * @param emfUsers
	 *            the cmfUsers to set
	 */
	public void setCmfUsers(List<User> emfUsers) {
		this.emfUsers = emfUsers;
	}
}
