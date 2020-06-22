package com.sirma.sep.export.renders;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * This class represent object link widget in iDoc.
 * 
 * @author cdimitrov
 */
@Extension(target = IdocRenderer.PLUGIN_NAME, order = 7)
public class ObjectLinkWidgetRenderer extends BaseRenderer {

	private static final String OBJECT_LINK = "object-link";

	@Inject
	private InstanceService instanceService;

	@Override
	public boolean accept(ContentNode node) {
		return node.isWidget() && OBJECT_LINK.equals(((Widget) node).getName());
	}

	@Override
	public Element render(String currentInstanceId, ContentNode node) {
		// holder for header
		Element span = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");

		JsonObject jsonConfig = getWidgetConfiguration(node);

		// active or soft deleted instance
		Instance instance = instanceService.loadDeleted(jsonConfig.getString(SELECTED_OBJECT)).orElse(null);
		if (instance == null) {
			return span;
		}
		String compactHeaderHtml = instance.getString(DefaultProperties.HEADER_COMPACT);
		Document fixedCompactHeaderUrls = JsoupUtil.fixHeaderUrls(compactHeaderHtml,
				systemConfiguration.getUi2Url().get());
		span.append(fixedCompactHeaderUrls.body().children().html());
		span.addClass(OBJECT_LINK);
		return span;
	}

	@Override
	public void afterRender(Element newElement, ContentNode node) {
		Element element = node.getElement();
		element.after(newElement);
		element.remove();
	}
}