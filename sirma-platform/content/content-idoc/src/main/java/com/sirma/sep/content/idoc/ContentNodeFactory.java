package com.sirma.sep.content.idoc;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.nodes.DefaultNodeBuilder;

/**
 * Factory that uses {@link ContentNodeBuilder} implementations to build {@link ContentNode} elements
 *
 * @author BBonev
 */
public class ContentNodeFactory {

	private static final ContentNodeFactory INSTANCE = new ContentNodeFactory();

	private Set<ContentNodeBuilder> builders = new LinkedHashSet<>();

	/**
	 * Gets the single instance of ContentNodeFactory.
	 *
	 * @return single instance of ContentNodeFactory
	 */
	public static ContentNodeFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * Gets the content item build from the first applicable builder. If no registered builder is found then the
	 * {@link DefaultNodeBuilder} will be used to create a default node
	 *
	 * @param element
	 *            the element that need to be build
	 * @return the content item
	 */
	public ContentNode getContentItem(Element element) {
		for (ContentNodeBuilder builder : builders) {
			if (builder.accept(element)) {
				return builder.build(element);
			}
		}
		return DefaultNodeBuilder.INSTANCE.build(element);
	}

	/**
	 * Register builder.
	 *
	 * @param builder
	 *            the builder
	 * @return true, if successful
	 */
	public boolean registerBuilder(ContentNodeBuilder builder) {
		return builders.add(builder);
	}

	/**
	 * Unregister builder.
	 *
	 * @param builder
	 *            the builder
	 * @return true, if successful
	 */
	public boolean unregisterBuilder(ContentNodeBuilder builder) {
		return builders.remove(builder);
	}
}
