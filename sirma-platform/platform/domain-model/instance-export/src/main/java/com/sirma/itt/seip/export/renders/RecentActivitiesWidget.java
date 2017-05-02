package com.sirma.itt.seip.export.renders;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.sirma.itt.emf.audit.processor.RecentActivity;
import com.sirma.itt.emf.audit.processor.StoredAuditActivitiesWrapper;
import com.sirma.itt.emf.audit.solr.service.RecentActivitiesRetriever;
import com.sirma.itt.emf.audit.solr.service.RecentActivitiesSentenceGenerator;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.export.renders.utils.JsoupUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.time.FormattedDateTime;
import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.Widget;
import com.sirmaenterprise.sep.content.idoc.WidgetConfiguration;
import com.sirmaenterprise.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirmaenterprise.sep.export.renders.html.table.HtmlValueHyperlinkBuilder;
import com.sirmaenterprise.sep.export.renders.html.table.HtmlValueTextBuilder;

/**
 * Represents RecentActivities Widget in iDoc.
 *
 * @author Hristo Lungov
 */
@ApplicationScoped
@Extension(target = IdocRenderer.PLUGIN_NAME, order = 6)
public class RecentActivitiesWidget extends BaseRenderer {

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

	@Inject
	private LabelProvider labelProvider;

	@Override
	public boolean accept(ContentNode node) {
		// verify that node is correct widget
		if (node.isWidget() && RECENT_ACTIVITIES_WIDGET_NAME.equals(((Widget) node).getName())) {
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
		// search selection mode
		String selectionMode = IdocRenderer.getSelectionMode(jsonConfiguration);
		// get selected instances
		List<Serializable> selectedInstances = getSelectedInstancesIdentifiers(currentInstanceId, selectionMode,
				jsonConfiguration);
		// if include current object is true add current instance to selected instances
		if (jsonConfiguration.getBoolean(INCLUDE_CURRENT_OBJECT, false)) {
			selectedInstances.add(currentInstanceId);
		}
		// get the widget title
		String widgetTitle = IdocRenderer.getWidgetTitle(jsonConfiguration);

		HtmlTableBuilder table = new HtmlTableBuilder(widgetTitle);
		setWidth(table, node.getElement(), NIGHTY_NINE_PRECENT);
		if (selectedInstances.isEmpty()) {
			if (MANUALLY.equals(selectionMode)) {
				HtmlTableBuilder.addNoResultRow(table, labelProvider.getLabel(KEY_LABEL_SELECT_OBJECT_NONE));
			} else {
				HtmlTableBuilder.addNoResultRow(table, labelProvider.getLabel(KEY_LABEL_SELECT_OBJECT_RESULTS_NONE));
			}
			setWidth(table, node.getElement(), NIGHTY_NINE_PRECENT);
			return table.build();
		}
		// get stored audit activities
		StoredAuditActivitiesWrapper activities = retriever.getActivities(selectedInstances);
		// generate sentences
		List<RecentActivity> generated = generator.generateSentences(activities.getActivities());
		for (int i = 0; i < generated.size(); i++) {
			RecentActivity recentActivity = generated.get(i);
			// add user's header
			String hyperlink = IdocRenderer.getHyperlink(recentActivity.getUser(), DefaultProperties.HEADER_COMPACT,
					systemConfiguration.getUi2Url().get());
			table.addTdValue(i, 0, new HtmlValueHyperlinkBuilder(hyperlink));
			// add date of activity creation
			FormattedDateTime formattedDateTime = typeConverter.convert(FormattedDateTime.class,
					recentActivity.getTimestamp());
			table.addTdValue(i, 1, new HtmlValueTextBuilder(formattedDateTime.getFormatted()));
			// add sentence header
			Document sentence = JsoupUtil.fixHeaderUrls(recentActivity.getSentence(),
					systemConfiguration.getUi2Url().get());
			sentence.select("img").addClass(DefaultProperties.HEADER_COMPACT);
			table.addTdValue(i, 2, new HtmlValueHyperlinkBuilder(sentence.html()));
		}
		setWidth(table, node.getElement(), NIGHTY_NINE_PRECENT);
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
							.map(InstanceReference::getIdentifier)
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