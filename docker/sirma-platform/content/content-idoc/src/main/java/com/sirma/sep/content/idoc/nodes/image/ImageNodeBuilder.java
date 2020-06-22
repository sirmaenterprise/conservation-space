package com.sirma.sep.content.idoc.nodes.image;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.ContentNodeBuilder;

/**
 * Node builder that produces {@link ImageNode}s. The builder accepts only elements that has a tag &lt;img/&gt;
 *
 * @author BBonev
 */
public class ImageNodeBuilder implements ContentNodeBuilder {

	@Override
	public boolean accept(Element element) {
		return "img".equalsIgnoreCase(element.tagName());
	}

	@Override
	public ContentNode build(Element element) {
		return new ImageNode(element);
	}

}
