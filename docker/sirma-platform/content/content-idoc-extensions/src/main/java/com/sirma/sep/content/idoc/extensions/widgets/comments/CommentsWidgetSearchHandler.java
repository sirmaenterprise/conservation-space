package com.sirma.sep.content.idoc.extensions.widgets.comments;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.gson.JsonObject;
import com.sirma.itt.seip.annotations.AnnotationSearchRequest;
import com.sirma.itt.seip.annotations.AnnotationService;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.annotations.rest.AnnotationWriter;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.version.VersionProperties.WidgetsHandlerContextProperties;
import com.sirma.itt.seip.time.DateRange;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.WidgetResults;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetSearchHandler;
import com.sirma.sep.content.idoc.nodes.widgets.comments.CommentsWidget;
import com.sirma.sep.content.idoc.nodes.widgets.comments.CommentsWidgetConfiguration;

/**
 * Base search handler for {@link CommentsWidget}. This handler executes two consecutive searches. The first one parses
 * the configuration of the widget and executes search with build arguments, which will retrieve the instances which
 * comments should be shown in the widget. The second one is the actual comments search for the found instances for the
 * first search. For it is used {@link AnnotationService}.
 * <p>
 * Supports search for versions and building {@link DateRange} based on specific date, when the version is created. This
 * {@link Date} should be provided by the {@link HandlerContext} under '<b>versionCreationDate</b>' key.
 * <p>
 * As result this handler will return {@link Map} with information about found annotations(comments). The map contains:
 * <p>
 * <table border="1">
 * <tr align="center">
 * <td>key</td>
 * <td>value</td>
 * </tr>
 * <tr align="center">
 * <td>annotations</td>
 * <td>found annotations from the annotations service search. They are converted using {@link AnnotationWriter} before
 * putting them in the result map</td>
 * </tr>
 * <tr align="center">
 * <td>annotationsCount</td>
 * <td>the count of all annotations that are found for all instances. It is needed for paging and showing the total
 * count of the comments</td>
 * </tr>
 * <tr align="center">
 * <td>instanceIds</td>
 * <td>the ids of the found target instance for the annotations found from the annotation service search. They are
 * needed so that they could be used to retrieve their headers. It isn't done here, because there are cases, where this
 * ids should be processed by another logic before their header generation(versioning for example)</td>
 * </tr>
 * </table>
 *
 * @author A. Kunchev
 */
public class CommentsWidgetSearchHandler extends AbstractWidgetSearchHandler<CommentsWidget> {

	static final String ANNOTATIONS_RESULT_MAP_KEY = "annotations";
	static final String ANNOTATIONS_COUNT_RESULT_MAP_KEY = "annotationsCount";
	static final String INSTANCE_IDS_RESULT_MAP_KEY = "instanceIds";
	static final String DATE_RANGE_KEY = "dateRange";

	private static final String VALUE_KEY = "value";
	private static final String OPERATOR_KEY = "operator";
	private static final String BEFORE = "before";
	private static final String AFTER = "after";

	// same value is passed for the request from the web and it is constant
	private static final int DEFAULT_OFFSET = 1;

	@Inject
	private AnnotationService annotationService;

	@Inject
	private DateConverter dateConverter;

	@Inject
	private AnnotationWriter annotationWriter;

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof CommentsWidget;
	}

	@Override
	public HandlerResult handle(CommentsWidget widget, HandlerContext context) {
		HandlerResult searchResults = super.handle(widget, context);
		Set<String> instanceIds = getInstanceIds(widget, context, searchResults);
		CommentsWidgetConfiguration configuration = widget.getConfiguration();
		if (isEmpty(instanceIds)) {
			// we make sure to clean up the store if no results are found from this search
			configuration.setSearchResults(WidgetResults.EMPTY);
			return new HandlerResult(widget);
		}

		DateRange range = getDateRange(configuration, context);
		AnnotationSearchRequest annotationRequest = buildAnnotationsRequest(configuration, instanceIds, range);
		Collection<Annotation> annotations = annotationService.searchAnnotations(annotationRequest);
		int annotationsCount = annotationService.searchAnnotationsCountOnly(annotationRequest);

		try {
			Object convertedAnnotations = annotationWriter.convert(annotations);
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(ANNOTATIONS_RESULT_MAP_KEY, convertedAnnotations);
			resultMap.put(ANNOTATIONS_COUNT_RESULT_MAP_KEY, annotationsCount);
			resultMap.put(INSTANCE_IDS_RESULT_MAP_KEY, getAnnotationTargetIds(annotations));
			resultMap.put(DATE_RANGE_KEY, range);
			configuration.setSearchResults(WidgetResults.fromSearch(resultMap));
			return new HandlerResult(widget, resultMap);
		} catch (IOException e) {
			throw new EmfRuntimeException("There was a problem with the annotation converting for comments widget.", e);
		}
	}

	/**
	 * Retrieves the ids of the instances found by the main search. Also if the current instance is included in the
	 * search, its id is also returned in the result {@link Set}.
	 */
	private static Set<String> getInstanceIds(CommentsWidget widget, HandlerContext context, HandlerResult handle) {
		Optional<Collection<String>> result = handle.getResult();
		Set<String> ids = new HashSet<>();
		result.ifPresent(ids::addAll);

		CommentsWidgetConfiguration configuration = widget.getConfiguration();
		if (configuration.isCurrentObjectIncluded()) {
			ids.add(context.getCurrentInstanceId());
		}

		return ids;
	}

	/**
	 * Builds {@link DateRange} based on the information provided by the widget configuration.
	 * <p>
	 * Supports search for versions and building {@link DateRange} based on specific date, when the version is created.
	 * This {@link Date} should be provided by the {@link HandlerContext} under '<b>versionCreationDate</b>' key.
	 */
	private DateRange getDateRange(CommentsWidgetConfiguration configuration, HandlerContext context) {
		JsonObject dateFilter = configuration.getFilterCriteria();
		Date versionDate = context.getIfSameType(WidgetsHandlerContextProperties.VERSION_DATE_KEY, Date.class);
		if (dateFilter == null && versionDate == null) {
			return new DateRange(null, null);
		}

		if (versionDate != null) {
			return buildVersionDateRange(dateFilter, versionDate);
		}

		if (dateFilter == null || !dateFilter.has(VALUE_KEY)) {
			return new DateRange(null, null);
		}

		String dateAsString = dateFilter.get(VALUE_KEY).getAsString();
		String operator = dateFilter.get(OPERATOR_KEY).getAsString();
		return buildDateRange(dateAsString, operator);
	}

	/**
	 * Builds {@link DateRange} based on the version creation date. This is done so that we could restrict the comments
	 * that are shown in the widget to the moment when the version is created.
	 */
	private DateRange buildVersionDateRange(JsonObject dateFilter, Date versionDate) {
		if (dateFilter != null && dateFilter.has(VALUE_KEY) && dateFilter.has(OPERATOR_KEY)
				&& AFTER.equals(dateFilter.get(OPERATOR_KEY).getAsString())) {
			String dateAsString = dateFilter.get(VALUE_KEY).getAsString();
			return new DateRange(dateConverter.parseDate(dateAsString), versionDate);
		}
		return new DateRange(null, versionDate);
	}

	private DateRange buildDateRange(String dateAsString, String operator) {
		switch (operator) {
			case AFTER:
				return new DateRange(dateConverter.parseDate(dateAsString), null);
			case BEFORE:
				return new DateRange(null, dateConverter.parseDate(dateAsString));
			default:
				return new DateRange(null, null);
		}
	}

	private static AnnotationSearchRequest buildAnnotationsRequest(CommentsWidgetConfiguration configuration,
			Collection<String> instanceIds, DateRange range) {
		return new AnnotationSearchRequest()
				.setInstanceIds(new ArrayList<>(instanceIds))
					.setUserIds(configuration.getSelectedUsers())
					.setLimit(configuration.getLimit())
					.setStatus(configuration.getStatus())
					.setText(configuration.getText())
					.setDateRange(range)
					.setOffset(DEFAULT_OFFSET);
	}

	private static Collection<Serializable> getAnnotationTargetIds(Collection<Annotation> annotations) {
		return annotations.stream().map(Annotation::getTargetId).collect(Collectors.toSet());
	}
}
