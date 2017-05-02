package com.sirma.itt.seip.export.renders;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.FontFactory;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.Widget;
import com.sirmaenterprise.sep.content.idoc.WidgetConfiguration;
import com.sirmaenterprise.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirmaenterprise.sep.export.renders.html.table.HtmlValueImageBuilder;

/**
 * Represents the iDoc ContentViewer Widget.
 * 
 * @author Hristo Lungov
 */
@ApplicationScoped
@Extension(target = IdocRenderer.PLUGIN_NAME, order = 5)
public class ContentViewerWidget extends BaseRenderer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentViewerWidget.class);
	protected static final String CONTENT_VIEWER_WIDGET_NAME = "content-viewer";
	protected static final String APPLICATION_PDF = "application/pdf";

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private LabelProvider labelProvider;

	@Override
	public boolean accept(ContentNode node) {
		// verify that node is correct widget
		if (node.isWidget() && CONTENT_VIEWER_WIDGET_NAME.equals(((Widget) node).getName())) {
			return true;
		}
		return false;
	}

	@Override
	public Element render(String currentInstanceId, ContentNode node) {
		Widget widget = (Widget) node;
		WidgetConfiguration configuration = widget.getConfiguration();
		// convert widget configuration to json
		JsonObject jsonConfiguration = IdocRenderer.toJson(configuration);
		// get search selection mode
		String selectionMode = IdocRenderer.getSelectionMode(jsonConfiguration);
		// get selected instances
		Collection<Instance> selectedInstances = getSelectedInstances(currentInstanceId, selectionMode,
				jsonConfiguration);
		// get the widget title
		String widgetTitle = IdocRenderer.getWidgetTitle(jsonConfiguration);
		// create table
		HtmlTableBuilder tableBuilder = new HtmlTableBuilder(widgetTitle);
		setWidth(tableBuilder, node.getElement(), NIGHTY_NINE_PRECENT);
		try {
			if (selectedInstances.isEmpty()) {
				if (MANUALLY.equals(selectionMode)) {
					HtmlTableBuilder.addNoResultRow(tableBuilder, labelProvider.getLabel(KEY_LABEL_SELECT_OBJECT_NONE));
				} else {
					HtmlTableBuilder.addNoResultRow(tableBuilder,
							labelProvider.getLabel(KEY_LABEL_SELECT_OBJECT_RESULTS_NONE));
				}
				return tableBuilder.build();
			}
			if (selectedInstances.size() > 1) {
				HtmlTableBuilder.addNoResultRow(tableBuilder, labelProvider.getLabel(KEY_LABEL_MORE_THAN_ONE));
				return tableBuilder.build();
			}
			Instance instance = selectedInstances.iterator().next();
			if (instance.isUploaded()) {
				convertInstanceContentToImage(tableBuilder, instance.getId().toString());
			} else {
				HtmlTableBuilder.addNoResultRow(tableBuilder, labelProvider.getLabel(KEY_LABEL_NO_PREVIEW_AVAILABLE));
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return tableBuilder.build();
	}

	/**
	 * Convert instance content to image.
	 *
	 * @param tableBuilder
	 *            the tablebuilder
	 * @param instanceId
	 *            the instance id
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void convertInstanceContentToImage(HtmlTableBuilder tableBuilder, String instanceId) throws IOException {
		ContentInfo contentInfo = instanceContentService.getContentPreview(instanceId, Content.PRIMARY_CONTENT);
		if (contentInfo.exists() && contentInfo.getMimeType() != null
				&& !contentInfo.getMimeType().startsWith("image")) {
			FontFactory.registerDirectories();
			try (PDDocument pdfDocument = PDDocument.load(contentInfo.getInputStream(),
					MemoryUsageSetting.setupMainMemoryOnly())) {
				PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);
				BufferedImage image = pdfRenderer.renderImage(0);
				tableBuilder.addTdValue(0, 0, new HtmlValueImageBuilder(image, contentInfo.getName()));
			}
		} else {
			HtmlTableBuilder.addNoResultRow(tableBuilder, labelProvider.getLabel(KEY_LABEL_NO_PREVIEW_AVAILABLE));
		}
	}

	/**
	 * Gets the selected instances from widget json configuration.
	 *
	 * @param currentInstanceId
	 *            the current instance id
	 * @param jsonConfiguration
	 *            the json configuration
	 * @return the selected instances
	 */
	private Collection<Instance> getSelectedInstances(String currentInstanceId, String selectionMode,
			JsonObject jsonConfiguration) {
		if (CURRENT.equals(selectionMode)) {
			return loadInstances(Arrays.asList(currentInstanceId), false);
		}
		if (MANUALLY.equals(selectionMode)) {
			if (jsonConfiguration.containsKey(SELECTED_OBJECT)) {
				Collection<Serializable> selectedObjects = Collections
						.singleton(jsonConfiguration.getString(SELECTED_OBJECT));
				return loadInstances(selectedObjects, false);
			}
			return Collections.emptyList();
		}
		return getSelectedInstancesFromCriteria(currentInstanceId, jsonConfiguration, true);
	}

}
