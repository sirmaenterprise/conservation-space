package com.sirma.sep.export.renders.html.table;

import org.jsoup.nodes.Element;

/**
 * Interface for all value builders.
 * 
 * @author Boyan Tonchev
 */
public interface HtmlValueBuilder extends HtmlBuilder {

	/**
	 * Build value and add it to <code>td</code>
	 * 
	 * @param td
	 *            - td where created value have to be added.
	 */
	void build(Element td);
}
