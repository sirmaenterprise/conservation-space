package com.sirma.sep.content.idoc;

import java.util.stream.Stream;

import com.sirma.sep.content.idoc.nodes.TextNode;
import com.sirma.sep.content.idoc.nodes.layout.LayoutManagerNode;
import com.sirma.sep.content.idoc.nodes.layout.LayoutNode;

/**
 * Defines methods for accessing different node types from a container node. Nodes containers are nodes that have child
 * nodes.
 *
 * @author BBonev
 */
public interface NodeContainer {

	/**
	 * Gets stream of all widget nodes for all sections in the order of their appearance.
	 *
	 * @return the widgets stream
	 */
	Stream<Widget> widgets();

	/**
	 * Gets stream of all layout nodes for all sections in the order of their appearance.
	 *
	 * @return the layout nodes stream
	 */
	Stream<LayoutNode> layouts();

	/**
	 * Gets stream of all layout menagers nodes for all sections in the order of their appearance.
	 *
	 * @return the layout manager nodes stream
	 */
	Stream<LayoutManagerNode> layoutManagerNodes();

	/**
	 * Gets stream of all layout menagers nodes for all sections in the order of their appearance.
	 *
	 * @return the text nodes stream
	 */
	Stream<TextNode> textNodes();

	/**
	 * Gets a stream of all nodes in all sections in the order of their appearance.
	 *
	 * @return the children stream
	 */
	Stream<ContentNode> children();

}