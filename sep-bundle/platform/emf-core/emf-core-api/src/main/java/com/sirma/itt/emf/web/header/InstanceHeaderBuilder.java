package com.sirma.itt.emf.web.header;

import java.util.List;

import com.sirma.itt.emf.instance.model.Instance;

/**
 * Instance header building functionality.
 * 
 * @author svelikov
 */
public interface InstanceHeaderBuilder {

	/**
	 * Gets the parents.
	 * 
	 * @param currentInstance
	 *            the current instance
	 * @param displayMode
	 *            the display mode
	 * @param reverseOrder
	 *            the reverse order flag
	 * @return the parents
	 */
	List<Instance> getParents(Instance currentInstance, String displayMode, boolean reverseOrder);

	/**
	 * Gets the icon name or content according to the mode.
	 * 
	 * @param instance
	 *            the instance
	 * @param mode
	 *            the header mode
	 * @param size
	 *            the icon size
	 * @param renderThumbnail
	 *            icon thumbnail specifier
	 * @return the icon name or content
	 */
	String getIcon(Instance instance, String mode, String size, boolean renderThumbnail);

	/**
	 * Gets the breadcrumb icon for a given instance type.
	 * 
	 * @param instanceType
	 *            the instance type
	 * @return the breadcrumb icon
	 */
	String getBreadcrumbIcon(String instanceType);

	/**
	 * Get the icon size. NOTE: It will be nice if this method is private for the
	 * 
	 * @param mode
	 *            tree header mode
	 * @param size
	 *            tree header size
	 * @return icon size as string {@link TreeHeaderBuilder}, but we need to access or calculate
	 *         icon size in the view.
	 */
	String getIconSize(String mode, String size);

	/**
	 * Gets the header.
	 * 
	 * @param instance
	 *            the instance
	 * @param mode
	 *            the mode
	 * @param disableLinks
	 *            the disable links
	 * @return the header
	 */
	String getHeader(Instance instance, String mode, boolean disableLinks);

	/**
	 * Gets the tree header style class.
	 * 
	 * @param customClass
	 *            the custom class
	 * @param mode
	 *            the mode
	 * @return the tree header style class
	 */
	String getTreeHeaderStyleClass(String customClass, String mode);

	/**
	 * Gets the default header style class.
	 * 
	 * @param instance
	 *            the instance
	 * @param size
	 *            the size
	 * @return the default header style class
	 */
	String getDefaultHeaderStyleClass(Instance instance, String size);

	/**
	 * Gets the compact header style class.
	 * 
	 * @param instance
	 *            the instance
	 * @param size
	 *            the size
	 * @param isFirst
	 *            the is first
	 * @param isLast
	 *            the is last
	 * @return the compact header style class
	 */
	String getCompactHeaderStyleClass(Instance instance, String size, boolean isFirst,
			boolean isLast);

	/**
	 * Gets the breadcrumb header style class.
	 * 
	 * @param instance
	 *            the instance
	 * @param size
	 *            the size
	 * @return the breadcrumb header style class
	 */
	String getBreadcrumbHeaderStyleClass(Instance instance, String size);

	/**
	 * Checks if is default mode.
	 * 
	 * @param mode
	 *            the mode
	 * @return true, if is default mode
	 */
	boolean isDefaultMode(String mode);

	/**
	 * Checks if is compact mode.
	 * 
	 * @param mode
	 *            the mode
	 * @return true, if is compact mode
	 */
	boolean isCompactMode(String mode);

	/**
	 * Checks if is breadcrumb mode.
	 * 
	 * @param mode
	 *            the mode
	 * @return true, if is breadcrumb mode
	 */
	boolean isBreadcrumbMode(String mode);

	/**
	 * If should render current instance in header. By default current instance is always visible.
	 * 
	 * @param display
	 *            current tree head display
	 * @param instance
	 *            the instance
	 * @param mode
	 *            the mode
	 * @return boolean
	 */
	boolean renderCurrent(String display, Instance instance, String mode);

	/**
	 * Gets the node padding for compact header according to the index, mode and size.
	 * 
	 * @param index
	 *            the index
	 * @param size
	 *            the size
	 * @return the node padding
	 */
	String getCompactHeaderNodePadding(int index, String size);

}