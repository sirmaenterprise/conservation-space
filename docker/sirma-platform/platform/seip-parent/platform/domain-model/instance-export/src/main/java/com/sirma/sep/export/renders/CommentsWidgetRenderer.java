package com.sirma.sep.export.renders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.jsoup.nodes.Element;

import com.sirma.itt.seip.annotations.AnnotationSearchRequest;
import com.sirma.itt.seip.annotations.AnnotationService;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirma.sep.export.services.HtmlTableAnnotationService;

/**
 * Render for comments-widget. It read widget configuration prepare search request, execute search, build table and
 * insert it into word document.
 *
 * @author Boyan Tonchev
 */
@Extension(target = IdocRenderer.PLUGIN_NAME, order = 30)
public class CommentsWidgetRenderer extends BaseRenderer {

	private static final String CONFIGURATION_PROPERTY_TEXT = "text";

	private static final String CONFIGURATION_PROPERTY_STATUS = "status";

	private static final String CONFIGURATION_PROPERTY_USER_IDS = "userIds";

	/**
	 * Name of the comments widget.
	 */
	protected static final String COMMENTS_WIDGET_NAME = "comments-widget";

	@Inject
	private AnnotationService annotationService;

	@Inject
	private HtmlTableAnnotationService htmlTableAnnotationService;

	@Override
	public boolean accept(ContentNode node) {
		return node != null && node.isWidget() && COMMENTS_WIDGET_NAME.equals(((Widget) node).getName());
	}

	@Override
	public Element render(String currentInstanceId, ContentNode node) {
		JsonObject jsonConfiguration = getWidgetConfiguration(node);
		HtmlTableBuilder table = renderWidgetFrame(jsonConfiguration);		
		
		String selectionMode = IdocRenderer.getSelectionMode(jsonConfiguration);
		if (IdocRenderer.AUTOMATICALLY.equalsIgnoreCase(selectionMode) && !hasSearchCriteria(jsonConfiguration)) {
			return buildOneRowMessageTable(table, KEY_LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA);
		}
		AnnotationSearchRequest fetchSearchRequest = fetchSearchRequest(currentInstanceId, selectionMode,
				jsonConfiguration);
		// if null search request return no results warn message
		if (fetchSearchRequest == null) {
			return buildOneRowMessageTable(table, KEY_LABEL_NO_COMMENTS);
		}
		Collection<Annotation> annotations = annotationService.searchAnnotations(fetchSearchRequest);
		// if no annotations found return no comments message
		if (annotations.isEmpty()) {
			return buildOneRowMessageTable(table, KEY_LABEL_NO_COMMENTS);
		}

		HtmlTableBuilder annotationTable = htmlTableAnnotationService.createAnnotationTable(table, annotations);
		setWidth(annotationTable, node.getElement(), NIGHTY_NINE_PRECENT);
		return annotationTable.build();
	}

	/**
	 * Prepare search request for annotations of instance with id <code>currentInstanceId</code>.
	 *
	 * @param currentInstanceId
	 *            id of instance for which search will be executed.
	 * @param jsonConfiguration
	 *            configuration of comments widget.
	 * @return created search arguments.
	 */
	private AnnotationSearchRequest fetchSearchRequest(String currentInstanceId, String selectionMode,
			JsonObject jsonConfiguration) {

		List<String> userIds = JSON.getStringArray(jsonConfiguration, CONFIGURATION_PROPERTY_USER_IDS);
		String status = jsonConfiguration.getString(CONFIGURATION_PROPERTY_STATUS, null);
		String text = jsonConfiguration.getString(CONFIGURATION_PROPERTY_TEXT, null);

		List<String> foundInstancesIds = getSelectedInstances(currentInstanceId, selectionMode, jsonConfiguration);
		if (jsonConfiguration.getBoolean(SELECT_CURRENT_OBJECT, false)) {
			foundInstancesIds.add(currentInstanceId);
		}

		if (MANUALLY.equals(selectionMode) && foundInstancesIds.isEmpty()) {
			return null;
		}

		return new AnnotationSearchRequest()
				.setDateRange(
						jsonToDateRangeConverter.convertDateRange(jsonConfiguration.getJsonObject("filterCriteria")))
					.setInstanceIds(foundInstancesIds)
					.setUserIds(userIds)
					.setStatus(status)
					.setText(text);
	}

	/**
	 * Gets the selected instances from widget json configuration.
	 *
	 * @param currentInstanceId
	 *            the current instance id
	 * @param selectionMode
	 *            the search selection mode
	 * @param jsonConfiguration
	 *            the json configuration
	 * @return the selected instances
	 */
	private List<String> getSelectedInstances(String currentInstanceId, String selectionMode,
			JsonObject jsonConfiguration) {
		if (MANUALLY.equals(selectionMode)) {
			if (jsonConfiguration.containsKey(SELECTED_OBJECTS)) {
				List<Serializable> selectedObjects = JSON.jsonToList(jsonConfiguration.getJsonArray(SELECTED_OBJECTS));
				// resolve references because when ids saved in widget static content, but use may delete some
				// instance and widget will not be updated automatically, so we need to verify that saved ids in
				// widget content are existing
				return instanceResolver
						.resolveReferences(selectedObjects)
							.stream()
							.map(InstanceReference::getId)
							.collect(Collectors.toList());
			}
			return new ArrayList<>();
		}
		return getSelectedInstancesFromCriteria(currentInstanceId, jsonConfiguration, false)
				.stream()
					.map(Instance::getId)
					.map(Serializable::toString)
					.collect(Collectors.toList());
	}
}
