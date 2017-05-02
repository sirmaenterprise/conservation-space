package com.sirma.cmf.web.navigation;

import java.io.Serializable;

import com.sirma.cmf.web.SerializedDocumentContext;

/**
 * NavigationPoint is data holder object for EmfSessionHandler.
 *
 * @author svelikov
 */
public class NavigationPoint {

	private String outcome;

	private String actionMethod;

	private String viewId;

	private SerializedDocumentContext serializedDocumentContext;

	private Serializable instanceId;

	/**
	 * Instantiates a new navigation point.
	 */
	public NavigationPoint() {
		// default constructor
	}

	/**
	 * Instantiates a new navigation point.
	 *
	 * @param outcome
	 *            the outcome
	 */
	public NavigationPoint(String outcome) {
		this.outcome = outcome;
	}

	/**
	 * Instantiates a new navigation point.
	 *
	 * @param outcome
	 *            the outcome
	 * @param actionMethod
	 *            the action method
	 * @param viewId
	 *            the view id
	 * @param instanceId
	 *            the instance id
	 */
	public NavigationPoint(String outcome, String actionMethod, String viewId, Serializable instanceId) {
		this.outcome = outcome;
		this.actionMethod = actionMethod;
		this.viewId = viewId;
		this.instanceId = instanceId;
	}

	/**
	 * Gets the action method.
	 *
	 * @return the action method
	 */
	public String getActionMethod() {
		return actionMethod;
	}

	/**
	 * Sets the action method.
	 *
	 * @param actionMethod
	 *            the new action method
	 */
	public void setActionMethod(String actionMethod) {
		this.actionMethod = actionMethod;
	}

	/**
	 * Gets the view id.
	 *
	 * @return the view id
	 */
	public String getViewId() {
		return viewId;
	}

	/**
	 * Sets the view id.
	 *
	 * @param viewId
	 *            the new view id
	 */
	public void setViewId(String viewId) {
		this.viewId = viewId;
	}

	/**
	 * Getter method for serializedDocumentContext.
	 *
	 * @return the serializedDocumentContext
	 */
	public SerializedDocumentContext getSerializedDocumentContext() {
		return serializedDocumentContext;
	}

	/**
	 * Setter method for serializedDocumentContext.
	 *
	 * @param serializedDocumentContext
	 *            the serializedDocumentContext to set
	 */
	public void setSerializedDocumentContext(SerializedDocumentContext serializedDocumentContext) {
		this.serializedDocumentContext = serializedDocumentContext;
	}

	/**
	 * Getter method for outcome.
	 *
	 * @return the outcome
	 */
	public String getOutcome() {
		return outcome;
	}

	/**
	 * Setter method for outcome.
	 *
	 * @param outcome
	 *            the outcome to set
	 */
	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NavigationPoint [outcome=");
		builder.append(outcome);
		builder.append(", actionMethod=");
		builder.append(actionMethod);
		builder.append(", viewId=");
		builder.append(viewId);
		builder.append(", serializedDocumentContext=");
		builder.append(serializedDocumentContext);
		builder.append(", instanceId=");
		builder.append(instanceId);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for instanceId.
	 *
	 * @return the instanceId
	 */
	public Serializable getInstanceId() {
		return instanceId;
	}

	/**
	 * Setter method for instanceId.
	 *
	 * @param instanceId
	 *            the instanceId to set
	 */
	public void setInstanceId(Serializable instanceId) {
		this.instanceId = instanceId;
	}
}