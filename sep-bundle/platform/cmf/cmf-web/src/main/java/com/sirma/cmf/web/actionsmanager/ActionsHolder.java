package com.sirma.cmf.web.actionsmanager;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.model.Action;

/**
 * Holds lazy loaded actions for some instance or dashlet.
 * 
 * @author svelikov
 */
@Named
@ViewScoped
public class ActionsHolder implements Serializable {

	private static final long serialVersionUID = 7216720363574421750L;

	private List<Action> instanceActions;

	@Inject
	private ActionsProvider actionsProvider;

	/**
	 * Evaluate actions by instance.
	 * 
	 * @param instance
	 *            the instance
	 * @param placeholder
	 *            the placeholder
	 * @param context
	 *            the context
	 */
	public void evaluateActionsByInstance(Instance instance, String placeholder, Instance context) {
		instanceActions = actionsProvider.evaluateActionsByInstance(instance, placeholder, context);
	}

	/**
	 * Evaluate actions by instance no context.
	 * 
	 * @param instance
	 *            the instance
	 * @param placeholder
	 *            the placeholder
	 */
	public void evaluateActionsByInstanceNoContext(Instance instance, String placeholder) {
		instanceActions = actionsProvider.evaluateActionsByInstance(instance, placeholder, null);
	}

	/**
	 * Evaluate actions for dashlet.
	 * 
	 * @param panel
	 *            the panel
	 */
	public void evaluateActionsForDashlet(DashboardPanelActionBase panel) {
		instanceActions = actionsProvider.loadDashletActions(panel);
	}

	/**
	 * Getter method for instanceActions.
	 * 
	 * @return the instanceActions
	 */
	public List<Action> getInstanceActions() {
		return instanceActions;
	}

	/**
	 * Setter method for instanceActions.
	 * 
	 * @param instanceActions
	 *            the instanceActions to set
	 */
	public void setInstanceActions(List<Action> instanceActions) {
		this.instanceActions = instanceActions;
	}

}
