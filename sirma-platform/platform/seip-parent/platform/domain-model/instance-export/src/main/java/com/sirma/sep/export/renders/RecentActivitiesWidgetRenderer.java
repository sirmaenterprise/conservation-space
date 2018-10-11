package com.sirma.sep.export.renders;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.sirma.itt.emf.audit.processor.RecentActivity;
import com.sirma.itt.emf.audit.processor.StoredAuditActivitiesWrapper;
import com.sirma.itt.emf.audit.solr.service.RecentActivitiesRetriever;
import com.sirma.itt.emf.audit.solr.service.RecentActivitiesSentenceGenerator;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.time.FormattedDateTime;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirma.sep.export.renders.html.table.HtmlValueHtmlBuilder;
import com.sirma.sep.export.renders.html.table.HtmlValueTextBuilder;
import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Represents RecentActivities Widget in iDoc.
 *
 * @author Hristo Lungov
 */
@Extension(target = IdocRenderer.PLUGIN_NAME, order = 6)
public class RecentActivitiesWidgetRenderer extends BaseRenderer {

	protected static final String RECENT_ACTIVITIES_WIDGET_NAME = "recent-activities";

	/**
	 * Constant passed in widgets configuration. It is used in manually mode to store selected instance ids.
	 */
	private static final String SELECTED_ITEMS = "selectedItems";

	/**
	 * Constant passed in widgets configuration. It manage if current object have to be included to selected instances.
	 */
	private static final String INCLUDE_CURRENT_OBJECT = "includeCurrent";

	@Inject
	private RecentActivitiesRetriever retriever;

	@Inject
	private RecentActivitiesSentenceGenerator generator;

	@Override
	public boolean accept(ContentNode node) {
		// verify that node is correct widget
		return node.isWidget() && RECENT_ACTIVITIES_WIDGET_NAME.equals(((Widget) node).getName());
	}

	@Override
	public Element render(String currentInstanceId, ContentNode node) {
		JsonObject jsonConfiguration = getWidgetConfiguration(node);
		HtmlTableBuilder tableBuilder = renderWidgetFrame(jsonConfiguration);
		
		String widgetSelectionMode = IdocRenderer.getSelectionMode(jsonConfiguration);
		setWidth(tableBuilder, node.getElement(), NIGHTY_NINE_PRECENT);
		if (IdocRenderer.AUTOMATICALLY.equalsIgnoreCase(widgetSelectionMode) && !hasSearchCriteria(jsonConfiguration)) {
			return buildOneRowMessageTable(tableBuilder, KEY_LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA);
		}
		// get selected instances
		List<Serializable> selectedInstances = getSelectedInstancesIdentifiers(currentInstanceId, widgetSelectionMode, jsonConfiguration);
		// if include current object is true add current instance to selected instances
		if (jsonConfiguration.getBoolean(INCLUDE_CURRENT_OBJECT, false)) {
			selectedInstances.add(currentInstanceId);
		}
		return buildTable(jsonConfiguration, tableBuilder, selectedInstances);
	}

	/**
	 * Build table with object data widget.
	 * @param jsonConfiguration widget configuration.
	 * @param table the table builder
	 * @param selectedInstances the instance id.
	 * @return html representation of filled object date widget.
	 */
	private Element buildTable(JsonObject jsonConfiguration,
			HtmlTableBuilder table, List<Serializable> selectedInstances) {
		if (selectedInstances.isEmpty()) {
			return buildEmptySelectionTable(IdocRenderer.getSelectionMode(jsonConfiguration), table);
		}
		return buildManySelectionTable(table, selectedInstances);

	}

	/**
	 * Populate html table with recent activities of <code>selectedInstances</code>.
	 * @param table - the table builder.
	 * @param selectedInstances the selected instances.
	 * @return html representation of filled table with empty search result.
	 */
	private Element buildManySelectionTable(HtmlTableBuilder table, List<Serializable> selectedInstances) {
		// get stored audit activities
		StoredAuditActivitiesWrapper activities = retriever.getActivities(selectedInstances);
		// generate sentences
		List<RecentActivity> generated = generator.generateSentences(activities.getActivities());
		for (int i = 0; i < generated.size(); i++) {
			RecentActivity recentActivity = generated.get(i);
			// add user's header
			String hyperlink = IdocRenderer.getHyperlink(recentActivity.getUser(), DefaultProperties.HEADER_COMPACT,
					systemConfiguration.getUi2Url().get());
			table.addTdValue(i, 0, new HtmlValueHtmlBuilder(hyperlink));
			// add date of activity creation
			FormattedDateTime formattedDateTime = typeConverter.convert(FormattedDateTime.class,
					recentActivity.getTimestamp());
			table.addTdValue(i, 1, new HtmlValueTextBuilder(formattedDateTime.getFormatted()));
			// add sentence header
			Document sentence = JsoupUtil.fixHeaderUrls(recentActivity.getSentence(),
					systemConfiguration.getUi2Url().get());
			sentence.select("img").addClass(DefaultProperties.HEADER_COMPACT);
			table.addTdValue(i, 2, new HtmlValueHtmlBuilder(sentence.html()));
		}
		return table.build();
	}

	/**
	 * Gets the selected instances identifiers from widget json configuration.
	 *
	 * @param currentInstanceId
	 *            the current instance id
	 * @param selectionMode
	 *            the search selection mode
	 * @param jsonConfiguration
	 *            the json configuration
	 * @return the selected instances
	 */
	private List<Serializable> getSelectedInstancesIdentifiers(String currentInstanceId, String selectionMode,
			JsonObject jsonConfiguration) {
		String selectObjectMode = jsonConfiguration.getString(SELECT_OBJECT_MODE);
		if (CURRENT.equals(selectObjectMode)) {
			return Arrays.asList(currentInstanceId);
		}
		if (MANUALLY.equals(selectionMode)) {
			if (jsonConfiguration.containsKey(SELECTED_ITEMS)) {
				List<Serializable> selectedItems = JSON.jsonToList(jsonConfiguration.getJsonArray(SELECTED_ITEMS));
				List<Serializable> selectedItemsIds = selectedItems
						.stream()
							.map(map -> ((Map<String, Serializable>) map).get("id"))
							.collect(Collectors.toList());
				// resolve references because when ids saved in widget static content, but use may delete some
				// instance and widget will not be updated automatically, so we need to verify that saved ids in
				// widget content are existing
				return instanceResolver
						.resolveReferences(selectedItemsIds)
							.stream()
							.map(InstanceReference::getId)
							.collect(Collectors.toList());
			}
			return Collections.emptyList();
		}
		return getSelectedInstancesFromCriteria(currentInstanceId, jsonConfiguration, false)
				.stream()
					.map(Instance::getId)
					.collect(Collectors.toList());
	}

}