package com.sirma.sep.export.services;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import com.sirma.itt.seip.annotations.AnnotationService;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.annotations.model.AnnotationProperties;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.sep.export.renders.IdocRenderer;
import com.sirma.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirma.sep.export.renders.html.table.HtmlValueElementBuilder;
import com.sirma.sep.export.renders.html.table.HtmlValueHtmlBuilder;
import com.sirma.sep.export.renders.html.table.HtmlValueTextBuilder;
import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Service for annotations.
 *
 * @author Boyan Tonchev
 */
@ApplicationScoped
public class HtmlTableAnnotationService {

	public static final String PROPERTY_EMF_CREATED_ON = "emf:createdOn";

	/**
	 * Will be used as values for type column.
	 */
	public static final String KEY_LABEL_TOPIC = "comments.widget.topic";
	public static final String KEY_LABEL_REPLY = "comments.widget.reply";

	/**
	 * Label keys of table columns.
	 */
	public static final String KEY_LABEL_OBJECT = "comments.widget.object";
	public static final String KEY_LABEL_TYPE = "comments.widget.type";
	public static final String KEY_LABEL_CREATOR = "comments.widget.creator";
	public static final String KEY_LABEL_CREATED_ON = "comments.widget.createdOn";
	public static final String KEY_LABEL_TEXT = "comments.widget.text";
	public static final String KEY_LABEL_STATUS = "comments.widget.status";

	public static final int FIRST_COLUMN_INDEX = 0;
	public static final int SECOND_COLUMN_INDEX = 1;
	public static final int THIRD_COLUMN_INDEX = 2;
	public static final int FOURTH_COLUMN_INDEX = 3;
	public static final int FIFTH_COLUMN_INDEX = 4;
	public static final int SIXTH_COLUMN_INDEX = 5;

	public static final int DEFAULT_CODE_LIST_OF_EMF_STATUS_LABELS = 4;

	@Inject
	protected SystemConfiguration systemConfiguration;

	@Inject
	private AnnotationService annotationService;

	@Inject
	private DateConverter dateConverter;

	@Inject
	private InstanceService instanceService;

	@Inject
	private InstanceLoadDecorator instanceLoadDecorator;

	@Inject
	private CodelistService codelistService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private UserPreferences userPreferences;

	@Inject
	private LabelProvider labelProvider;

	/**
	 * Creates the table headings.
	 *
	 * @param table
	 * 		the table
	 */
	public void createTableHeadings(HtmlTableBuilder table) {
		int trIndex = table.getRowCount();
		table.addTdValue(trIndex, FIRST_COLUMN_INDEX, new HtmlValueTextBuilder(labelProvider.getLabel(KEY_LABEL_OBJECT), true));
		table.addTdValue(trIndex, SECOND_COLUMN_INDEX, new HtmlValueTextBuilder(labelProvider.getLabel(KEY_LABEL_TYPE), true));
		table.addTdValue(trIndex, THIRD_COLUMN_INDEX, new HtmlValueTextBuilder(labelProvider.getLabel(KEY_LABEL_CREATOR), true));
		table.addTdValue(trIndex, FOURTH_COLUMN_INDEX, new HtmlValueTextBuilder(labelProvider.getLabel(
				KEY_LABEL_CREATED_ON), true));
		table.addTdValue(trIndex, FIFTH_COLUMN_INDEX, new HtmlValueTextBuilder(labelProvider.getLabel(KEY_LABEL_TEXT), true));
		table.addTdValue(trIndex, SIXTH_COLUMN_INDEX, new HtmlValueTextBuilder(labelProvider.getLabel(KEY_LABEL_STATUS), true));
	}

	/**
	 * Create {@link HtmlTableBuilder} builder and populate it with <code>annotations</code>. If any annotation from
	 * <code>annotations</code> has replays it will be load and populate into builder too. Created table will have
	 * following header row: *
	 *
	 * <pre>
	 * ---------------------------------------------------
	 * | Object |       | Creator | Date | Text | Status |
	 * ---------------------------------------------------
	 * </pre>
	 *
	 * @param tableTitle
	 *            title of table.
	 * @param annotations
	 *            - annotation which will be populate into the {@link HtmlTableBuilder} object.
	 * @return created builder.
	 */
	public HtmlTableBuilder createAnnotationTable(String tableTitle, Collection<Annotation> annotations) {
		HtmlTableBuilder table = new HtmlTableBuilder(tableTitle);
		createTableHeadings(table);
		return createAnnotationTable(table, annotations);
	}

	/**
	 * Populate {@link HtmlTableBuilder} builder with <code>annotations</code>. If any annotation from
	 * <code>annotations</code> has replays it will be load and populate into builder too. Created table will have
	 * following header row: *
	 *
	 * <pre>
	 * ---------------------------------------------------
	 * | Object |       | Creator | Date | Text | Status |
	 * ---------------------------------------------------
	 * </pre>
	 *
	 * @param table
	 *            already initted table
	 * @param annotations
	 *            - annotation which will be populate into the {@link HtmlTableBuilder} object.
	 * @return created builder.
	 */
	public HtmlTableBuilder createAnnotationTable(HtmlTableBuilder table, Collection<Annotation> annotations) {
		if (!annotations.isEmpty()) {
			List<String> targetInstances = getTargetInstancesFromResult(annotations);
			Map<String, String> instanceHeaders = getCompactInstanceHeaders(targetInstances);
			int codelistOfStatus = fetchStatusCodelist();
			int rowIndex = table.getRowCount();
			for (Annotation annotation : annotations) {
				Annotation replays = annotationService.loadAnnotation((String) annotation.getId()).get();
				rowIndex = addRow(table, instanceHeaders.get(annotation.getTargetId()), replays, rowIndex,
								  codelistOfStatus) + 1;
			}
		}
		return table;
	}

	/**
	 * Create row with annotation information and add it to table. If annotation has replays it will be load and
	 * populate into builder too.
	 *
	 * @param table
	 *            the table
	 * @param instanceHeader
	 *            map with key -> annotated instance id and value compact header.
	 * @param annotation
	 *            the annotation.
	 * @param rowIndex
	 * 		index of row where the new row have to be inserted.
	 * @param codelistOfStatus
	 * 		code list with labels for emf:status field.
	 * @return last populated row of <code>table</code>
	 */
	private int addRow(HtmlTableBuilder table, String instanceHeader, Annotation annotation, int rowIndex,
			int codelistOfStatus) {
		int result = rowIndex;
		table.addTdValue(rowIndex, FIRST_COLUMN_INDEX, new HtmlValueHtmlBuilder(instanceHeader));

		addAnnotationInformation(table, annotation, rowIndex, codelistOfStatus);
		Collection<Annotation> replies = annotation.getReplies();
		if (!replies.isEmpty()) {
			for (Annotation replay : replies) {
				addAnnotationInformation(table, replay, ++result, codelistOfStatus);
			}
		}
		return result;
	}

	/**
	 * Create cells populate it with annotation properties and add it to row with index <code>rowIndex</code>.
	 *
	 * @param table
	 *            the table.
	 * @param annotation
	 *            the annotation.
	 * @param rowIndex
	 *            index of row where cells have to be added.
	 * @param codelistOfStatus
	 * 		code list with labels for emf:status field.
	 */
	private void addAnnotationInformation(HtmlTableBuilder table, Annotation annotation, int rowIndex,
			int codelistOfStatus) {
		Serializable createdBy = annotation.getCreatedBy();
		// We miss first column it is for annotated instance
		int columnIndex = 1;

		if (annotation.isReply()) {
			table.addTdValue(rowIndex, columnIndex++, new HtmlValueTextBuilder(labelProvider.getLabel(KEY_LABEL_REPLY)));
		} else {
			table.addTdValue(rowIndex, columnIndex++, new HtmlValueTextBuilder(labelProvider.getLabel(KEY_LABEL_TOPIC)));
		}

		if (createdBy instanceof EmfUser) {
			instanceLoadDecorator.decorateInstance((Instance) createdBy);
			String hyperlink = IdocRenderer.getHyperlink((Instance) createdBy, DefaultProperties.HEADER_COMPACT, systemConfiguration.getUi2Url().get());
			table.addTdValue(rowIndex, columnIndex++, new HtmlValueHtmlBuilder(hyperlink));
		}
		Serializable serializable = annotation.getProperties().get(PROPERTY_EMF_CREATED_ON);
		table.addTdValue(rowIndex, columnIndex++, new HtmlValueTextBuilder(serializable == null ? "" : dateConverter.formatDateTime((Date) serializable)));
		table.addTdValue(rowIndex, columnIndex++, new HtmlValueElementBuilder(new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), ""), (String) annotation.getComment()));

		String currentStatus = annotation.getCurrentStatus();
		CodeValue codeValue = codelistService.getCodeValue(codelistOfStatus, currentStatus);
		table.addTdValue(rowIndex, columnIndex, new HtmlValueTextBuilder(codeValue == null ? currentStatus : codeValue.getDescription(new Locale(userPreferences.getLanguage()))));
	}

	/**
	 * Extracts all unique target instances from the collection of {@link Annotation} returned from the search.
	 *
	 * @param annotations
	 *            the page of annotations returned from the search
	 * @return all unique target instances
	 */
	private static List<String> getTargetInstancesFromResult(Collection<Annotation> annotations) {
		return annotations.stream().map(annotation -> annotation.getString(AnnotationProperties.HAS_TARGET)).distinct().collect(Collectors.toList());
	}

	/**
	 * Gets the mapping between the IDs of the provided instances and their compact headers.
	 *
	 * @param instanceIds
	 *            the list with the instance ids
	 * @return the map with the instance ids and headers
	 */
	private Map<String, String> getCompactInstanceHeaders(List<String> instanceIds) {
		List<Instance> instances = instanceService.loadByDbId(instanceIds);
		instanceLoadDecorator.decorateResult(instances);
		Map<String, String> idsToHeaders = CollectionUtils.createHashMap(instanceIds.size());
		instances.forEach(instance -> idsToHeaders.put((String) instance.getId(), IdocRenderer.getHyperlink(instance, DefaultProperties.HEADER_COMPACT, systemConfiguration.getUi2Url().get())));
		return idsToHeaders;
	}

	/**
	 * Fetch codelist for annotation status from definition.
	 */
	private int fetchStatusCodelist() {
		DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(new Annotation());
		return instanceDefinition == null ? DEFAULT_CODE_LIST_OF_EMF_STATUS_LABELS
				: instanceDefinition.getField(DefaultProperties.EMF_STATUS).map(PropertyDefinition::getCodelist).orElse(
						DEFAULT_CODE_LIST_OF_EMF_STATUS_LABELS);
	}
}