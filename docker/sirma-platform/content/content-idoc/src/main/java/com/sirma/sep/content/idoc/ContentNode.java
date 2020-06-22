package com.sirma.sep.content.idoc;

import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.nodes.WidgetNode;
import com.sirma.sep.content.idoc.nodes.layout.LayoutManagerNode;
import com.sirma.sep.content.idoc.nodes.layout.LayoutNode;

/**
 * Represents a single content element in a document or a {@link SectionNode}
 *
 * @author BBonev
 */
public interface ContentNode {

	/** Element id attribute */
	String ELEMENT_ID = "id";

	/**
	 * Checks if the current node represents a widget instance and it's save to be cast to {@link WidgetNode}.
	 *
	 * @return true, if is widget and <code>false</code> if it's not.
	 */
	default boolean isWidget() {
		return false;
	}

	/**
	 * Checks if the current node represents a layout instance and it's save to be cast to {@link LayoutNode}.
	 *
	 * @return true, if is layout and <code>false</code> if it's not.
	 */
	default boolean isLayout() {
		return false;
	}

	/**
	 * Checks if the current node represents only a text and it's save to be cast to {@link com.sirma.sep.content.idoc.nodes.TextNode}.
	 *
	 * @return true, if is text node and <code>false</code> if it's not.
	 */
	default boolean isTextNode() {
		return false;
	}

	/**
	 * Checks if the current node represents a widget instance and it's save to be cast to {@link LayoutManagerNode}.
	 *
	 * @return true, if is layout manager
	 */
	default boolean isLayoutManager() {
		return false;
	}

	/**
	 * Checks if the current node represents a image node that carry information about included image (external or
	 * embedded)
	 *
	 * @return true, if is image node
	 */
	default boolean isImage() {
		return false;
	}

	/**
	 * Gets a property from the current node or <code>null</code>
	 *
	 * @param key
	 *            the property name to fetch
	 * @return the property value or <code>null</code> if no such property is present
	 */
	String getProperty(String key);

	/**
	 * Sets a property value in the current node. If the node has a property with the given name the value will be
	 * updated otherwise no changes will be made.
	 *
	 * @param key
	 *            the property name
	 * @param value
	 *            the new property value
	 * @return true if the property was present and the value was successfully updated and <code>false</code> if no such
	 *         property was present and nothing was updated.
	 */
	boolean setProperty(String key, String value);

	/**
	 * Adds new property or updated existing value in the current node. If the node has a property with the given name
	 * the value will be updated otherwise the property will be added .
	 *
	 * @param key
	 *            the property name
	 * @param value
	 *            the new property value
	 */
	void addProperty(String key, String value);

	/**
	 * Removes the property identified by the given name. If no such property is defined the method should do nothing.
	 *
	 * @param key
	 *            the property name to remove.
	 */
	void removeProperty(String key);

	/**
	 * Gets the current node id.
	 *
	 * @return the id or <code>null</code> if no id is present
	 */
	String getId();

	/**
	 * Updates an existing node id.
	 *
	 * @param id
	 *            the new id
	 */
	void setId(String id);

	/**
	 * Generate new random id and set it to the current node if supports id attribute
	 */
	default void generateNewId() {
		setId(RandomStringUtils.randomAlphanumeric(8));
	}

	/**
	 * Removes the node from the IDOC.
	 */
	void remove();

	/**
	 * Gets the jsoup element.
	 *
	 * @return the element
	 */
	Element getElement();

	/**
	 * Provides the HTML of the current node.
	 *
	 * @return the node as HTML.
	 */
	default String asHtml() {
		return getElement().outerHtml();
	}

}
