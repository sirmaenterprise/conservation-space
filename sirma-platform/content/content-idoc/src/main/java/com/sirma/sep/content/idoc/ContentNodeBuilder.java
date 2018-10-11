package com.sirma.sep.content.idoc;

import org.jsoup.nodes.Element;

/**
 * Builder for {@link ContentNode} instances. In order for the builder's method {@link #build(Element)} to be called the
 * method {@link #accept(Element)} should return <code>true</code>.
 * <p>
 * Builders should be registered in the {@link ContentNodeFactory#registerBuilder(ContentNodeBuilder)}. There is a CDI
 * extension that will instantiate and register all builders during server startup.
 *
 * @author BBonev
 */
public interface ContentNodeBuilder {

	/**
	 * Tests if the current builder could build a {@link ContentNode} from the given {@link Element}. If possible it
	 * should return <code>true</code>.
	 *
	 * @param element
	 *            the element to test
	 * @return true, if the node is supported
	 */
	boolean accept(Element element);

	/**
	 * Builds a {@link ContentNode} from the given {@link Element}. The method will be called only with elements that
	 * have been accepted by the {@link #accept(Element)} method first.
	 *
	 * @param element
	 *            the element to use for node building
	 * @return the content node
	 */
	ContentNode build(Element element);
}
