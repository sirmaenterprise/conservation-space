package com.sirma.sep.export.renders;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.FontFactory;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirma.sep.export.renders.html.table.HtmlValueImageBuilder;

/**
 * Represents the iDoc ContentViewer Widget.
 *
 * @author Hristo Lungov
 */
@Extension(target = IdocRenderer.PLUGIN_NAME, order = 5)
public class ContentViewerWidgetRenderer extends BaseRenderer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentViewerWidgetRenderer.class);
	protected static final String CONTENT_VIEWER_WIDGET_NAME = "content-viewer";

	@Inject
	private InstanceContentService instanceContentService;

	@Override
	public boolean accept(ContentNode node) {
		// verify that node is correct widget
		return node.isWidget() && CONTENT_VIEWER_WIDGET_NAME.equals(((Widget) node).getName());
	}

	@Override
	public Element render(String currentInstanceId, ContentNode node) {
		JsonObject jsonConfiguration = getWidgetConfiguration(node);
		HtmlTableBuilder tableBuilder = renderWidgetFrame(jsonConfiguration);
		
		String selectionMode = IdocRenderer.getSelectionMode(jsonConfiguration);
		setWidth(tableBuilder, node.getElement(), NIGHTY_NINE_PRECENT);
		if (IdocRenderer.AUTOMATICALLY.equalsIgnoreCase(selectionMode) && !hasSearchCriteria(jsonConfiguration)) {
			return buildOneRowMessageTable(tableBuilder, KEY_LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA);
		}
		return buildTable(selectionMode, tableBuilder, getSelectedInstances(currentInstanceId, selectionMode, jsonConfiguration));
	}

	/**
	 * Build html table for object data widget.
	 * @param selectionMode widget selection mode.
	 * @param tableBuilder the table builder
	 * @param selectedInstances the instance id.
	 * @return html representation of filled object date widget.
	 */
	private Element buildTable(String selectionMode, HtmlTableBuilder tableBuilder,
			Collection<Instance> selectedInstances) {
		if (selectedInstances.isEmpty()) {
			return buildEmptySelectionTable(selectionMode, tableBuilder);
		}
		if (selectedInstances.size() > 1) {
			return buildOneRowMessageTable(tableBuilder, KEY_LABEL_MORE_THAN_ONE);
		}
		try {
			return buildOneResultTable(tableBuilder, selectedInstances.iterator().next());
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return tableBuilder.build();
	}

	/**
	 * Populate table with one search result.
	 * @param tableBuilder - the table builder.
	 * @param instance instance which content to be populated into table.
	 * @return html representation of filled table with one search result.
	 */
	private Element buildOneResultTable(HtmlTableBuilder tableBuilder, Instance instance)
			throws IOException {
		if (instance.isUploaded()) {
			convertInstanceContentToImage(tableBuilder, instance.getId().toString());
			return tableBuilder.build();
		}
		return buildOneRowMessageTable(tableBuilder, KEY_LABEL_NO_PREVIEW_AVAILABLE);
	}

	/**
	 * Convert instance content to image.
	 *
	 * @param tableBuilder
	 * 		the tablebuilder
	 * @param instanceId
	 * 		the instance id
	 * @throws IOException
	 * 		Signals that an I/O exception has occurred.
	 */
	private void convertInstanceContentToImage(HtmlTableBuilder tableBuilder, String instanceId) throws IOException {
		ContentInfo contentInfo = instanceContentService.getContentPreview(instanceId, Content.PRIMARY_CONTENT);
		boolean hasPreview = false;
		if (contentInfo.exists() && contentInfo.getMimeType() != null) {
			BufferedImage image;
			if (contentInfo.getMimeType().startsWith("image")) {
				image = ImageIO.read(contentInfo.getInputStream());
			} else {
				FontFactory.registerDirectories();
				try (PDDocument pdfDocument = PDDocument.load(contentInfo.getInputStream(),
															  MemoryUsageSetting.setupMainMemoryOnly())) {
					PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);
					image = pdfRenderer.renderImage(0);
				}
			}
			if (image != null) {
				tableBuilder.addTdValue(0, 0, new HtmlValueImageBuilder(image, contentInfo.getName()));
				hasPreview = true;
			}
		}

		if (!hasPreview) {
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
