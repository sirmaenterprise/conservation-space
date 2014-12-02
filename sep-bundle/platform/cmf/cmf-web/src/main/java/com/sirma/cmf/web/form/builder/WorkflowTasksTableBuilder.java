package com.sirma.cmf.web.form.builder;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;

import com.sirma.cmf.web.form.ComponentType;
import com.sirma.cmf.web.form.control.DataTableControlParameter;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.model.Sortable;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Builder for table that displays workflow tasks.
 * 
 * @author svelikov
 */
public class WorkflowTasksTableBuilder extends DatatableBuilder {

	private static final String TABLE_VAR = "item";

	/**
	 * Instantiates a new workflow tasks table builder.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public WorkflowTasksTableBuilder(LabelProvider labelProvider, CodelistService codelistService) {
		super(labelProvider, codelistService);
	}

	@Override
	public void build() {
		super.build();
	}

	@Override
	public UIComponent getComponentInstance() {
		HtmlDataTable dataTable = getDataTable();

		List<ControlParam> tableControlParams = getControlParams();

		setTableValue(dataTable, propertyDefinition.getName(), tableControlParams);

		setTableCaption(tableControlParams, dataTable);

		List<Sortable> controlFields = getSortedControlFields();
		int columnsCount = controlFields.size();

		// build table columns
		List<HtmlColumn> htmlColumns = new ArrayList<HtmlColumn>(columnsCount);
		for (int i = 0; i < columnsCount; i++) {

			PropertyDefinition columnProperty = (PropertyDefinition) controlFields.get(i);

			HtmlColumn column = getColumn(columnProperty);
			setHeader(column, columnProperty);

			// assume the first column is holding the link
			if (i == 0) {
				setBodyWithLink(column, columnProperty);
			} else {
				setBody(column, columnProperty);
			}

			htmlColumns.add(column);
		}

		dataTable.getChildren().addAll(htmlColumns);

		return dataTable;
	}

	/**
	 * Sets the table value.
	 * 
	 * @param dataTable
	 *            the data table
	 * @param propertyName
	 *            the property name
	 * @param tableControlParams
	 *            the table control params
	 */
	private void setTableValue(HtmlDataTable dataTable, String propertyName,
			List<ControlParam> tableControlParams) {
		// String baseInstanceName = getBaseInstanceName();
		// if (StringUtils.isNullOrEmpty(baseInstanceName)) {
		// List<ControlParam> tableValueParameter = getParametersByName("TABLE_VALUE",
		// tableControlParams);
		// if (!tableValueParameter.isEmpty()) {
		// baseInstanceName = tableValueParameter.get(0).getValue();
		// }
		// baseInstanceName = getInstanceName();
		// }
		String valueExpressionString = "#{workflowTasksHolder." + propertyName + "}";
		dataTable.setValueExpression("value",
				createValueExpression(valueExpressionString, List.class));
	}

	/**
	 * Sets the table caption.
	 * 
	 * @param tableControlParams
	 *            the table control params
	 * @param dataTable
	 *            the data table
	 */
	protected void setTableCaption(List<ControlParam> tableControlParams, HtmlDataTable dataTable) {

		List<ControlParam> foundParameters = getParametersByName(
				DataTableControlParameter.TABLE_HEADER.name(), tableControlParams);

		String tableCaptionLableId = "";
		if (!foundParameters.isEmpty()) {
			tableCaptionLableId = foundParameters.get(0).getValue();
		}
		String tableCaptionLabel = "table caption";
		if (StringUtils.isNotNullOrEmpty(tableCaptionLableId)) {
			tableCaptionLabel = labelProvider.getLabel(tableCaptionLableId);

			HtmlOutputText captionLabel = (HtmlOutputText) createComponentInstance(
					JAVAX_FACES_HTML_OUTPUT_TEXT, JAVAX_FACES_TEXT);

			captionLabel.setValueExpression("value",
					createValueExpression(tableCaptionLabel, String.class));

			dataTable.getFacets().put("caption", captionLabel);
		}

		dataTable.setCaptionClass("generated-table-caption");
	}

	/**
	 * Sets the body with link.
	 * 
	 * @param htmlColumn
	 *            the html column
	 * @param columnProperty
	 *            the body param
	 */
	protected void setBodyWithLink(HtmlColumn htmlColumn, PropertyDefinition columnProperty) {

		String fieldName = columnProperty.getDefaultValue();

		if (StringUtils.isNullOrEmpty(fieldName)) {
			log.error("CMFWeb: DatatableBuilder - missing value for ControlParam " + columnProperty);
		} else {
			HtmlOutputText header = (HtmlOutputText) builderHelper
					.getComponent(ComponentType.OUTPUT_TEXT);
			String headerVEString = "#{bookmarkUtil.addTargetBlank(" + TABLE_VAR
					+ ".properties['compact_header'])}";
			header.setValueExpression("value", createValueExpression(headerVEString, String.class));
			header.setEscape(false);

			UIComponent headerWrapper = builderHelper.getComponent(ComponentType.OUTPUT_PANEL);
			addStyleClass(headerWrapper, "taskinstance");
			List<UIComponent> columnChildren = htmlColumn.getChildren();
			columnChildren.add(headerWrapper);
			headerWrapper.getChildren().add(header);

			columnChildren.add(headerWrapper);
		}
	}

	/**
	 * Sets the body.
	 * 
	 * @param htmlColumn
	 *            the html column
	 * @param columnProperty
	 *            the body param
	 */
	protected void setBody(HtmlColumn htmlColumn, PropertyDefinition columnProperty) {

		HtmlOutputText body = (HtmlOutputText) createComponentInstance(
				JAVAX_FACES_HTML_OUTPUT_TEXT, JAVAX_FACES_TEXT);

		String valueExpressionString = "";
		String fieldName = columnProperty.getDefaultValue();

		if (StringUtils.isNullOrEmpty(fieldName)) {
			log.error("CMFWeb: DatatableBuilder - missing value for ControlParam " + columnProperty);
		} else {

			valueExpressionString = getValueExpressionString(columnProperty, TABLE_VAR);

			body.setValueExpression("value",
					createValueExpression(valueExpressionString, String.class));

			htmlColumn.getChildren().add(body);
		}
	}

	@Override
	public String getTableVarAttribute() {
		return TABLE_VAR;
	}
}
