package com.sirma.cmf.web.instance.landingpage;

/**
 * The Interface InstanceItemSelector is used on the object landing pages when new object is to be
 * created.
 * 
 * @author svelikov
 */
public interface InstanceItemSelector {

	/**
	 * Item selected action.
	 */
	void itemSelectedAction();

	/**
	 * Item selected action.
	 * 
	 * @param componentId
	 *            the component id
	 */
	void itemSelectedAction(String componentId);

}
