package com.sirma.cmf.web.form.builder;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;

import com.sirma.cmf.web.form.control.DataTableControlParameter;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.model.Sortable;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.util.SortableComparator;

/**
 * DatatableBuilder is base class that wraps common functionality for building datatables and guides
 * the building process.
 * 
 * @author svelikov
 */
public abstract class DatatableBuilder extends ControlBuilder {

	protected static final SortableComparator FIELDS_COMPARATOR = new SortableComparator();

	private static final String JAVAX_FACES_TABLE = "javax.faces.Table";

	private static final String JAVAX_FACES_HTML_DATA_TABLE = "javax.faces.HtmlDataTable";

	protected static final String JAVAX_FACES_TEXT = "javax.faces.Text";

	protected static final String JAVAX_FACES_HTML_OUTPUT_TEXT = "javax.faces.HtmlOutputText";

	private static final String GENERATED_TABLE_STYLE_CLASS = "generated-table";

	private static final String OWNER = "owner";

	private HtmlDataTable dataTable;

	private UIComponent tableWrapper;

	/**
	 * Instantiates a new datatable builder.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public DatatableBuilder(LabelProvider labelProvider, CodelistService codelistService) {
		super(labelProvider, codelistService);
	}

	/**
	 * Gets the table var attribute.
	 * 
	 * @return the table var attribute
	 */
	public abstract String getTableVarAttribute();

	@Override
	public void build() {
		String displayStatusKey = getRenderedStatusKey(propertyDefinition, formViewMode);

		boolean displayStatus = renderStatusMap.get(displayStatusKey);

		if (trace) {
			String msg = MessageFormat.format(
					"CMFWeb: building property [{0}] with display status key [{1} = {2}]",
					propertyDefinition.getName(), displayStatusKey, displayStatus);
			log.trace(msg);
		}

		// if display status is true, then go ahead and build the field
		if (displayStatus) {
			tableWrapper = buildFieldWrapper();
			List<UIComponent> tableWrapperChildren = tableWrapper.getChildren();

			UIComponent uiComponent = buildField();
			tableWrapperChildren.add(uiComponent);

			// https://ittruse.ittbg.com/jira/browse/CMF-819 fix broken empty tables
			String tableFixScript = "(function() {var rows = $('[id$="
					+ dataTable.getId()
					+ "] tbody tr');if(rows.length === 1) {var tds = rows.eq(0).find('td');if(tds.length === 1 && tds.eq(0).text() === '') {rows.remove();}}})()";
			HtmlOutputText tableFixScriptField = getScriptOutput(tableFixScript);
			tableWrapperChildren.add(tableFixScriptField);

			List<UIComponent> containerChildren = container.getChildren();
			containerChildren.add(tableWrapper);
		}
	}

	@Override
	public UIComponent buildField() {

		createDatatable();

		UIComponent uiComponent = getComponentInstance();

		return uiComponent;
	}

	/**
	 * Creates the datatable.
	 */
	protected void createDatatable() {
		dataTable = (HtmlDataTable) createComponentInstance(JAVAX_FACES_HTML_DATA_TABLE,
				JAVAX_FACES_TABLE);

		String propertyName = propertyDefinition.getName();

		dataTable.setId(propertyName);

		dataTable.setVar(getTableVarAttribute());

		Map<String, Object> tableAttributes = dataTable.getAttributes();

		List<ControlParam> tableControlParams = getControlParams();

		setTableStyleClass(tableAttributes, tableControlParams);

		setTableCssStyle(tableAttributes, tableControlParams);
	}

	/**
	 * Sets the table value.
	 * 
	 * @param dataTable
	 *            the data table
	 * @param valueExpressionString
	 *            the value expression string
	 */
	protected void setTableValue(HtmlDataTable dataTable, String valueExpressionString) {

		dataTable.setValueExpression("value",
				createValueExpression(valueExpressionString, List.class));
	}

	/**
	 * Gets the control fields.
	 * 
	 * @return the control fields
	 */
	protected List<Sortable> getSortedControlFields() {
		ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();
		// all control fields: every field defines one table column with its
		// header label, id, value argument, value type
		List<Sortable> controlFields = new ArrayList<Sortable>();
		controlFields.addAll(controlDefinition.getFields());
		Collections.sort(controlFields, FIELDS_COMPARATOR);
		return controlFields;
	}

	/**
	 * Gets the control params.
	 * 
	 * @return the control params
	 */
	protected List<ControlParam> getControlParams() {
		// all control parameters: table value, table caption title, styleClass
		// and others
		ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();
		return controlDefinition.getControlParams();
	}

	/**
	 * Sets the table css style.
	 * 
	 * @param tableAttributes
	 *            the table attributes
	 * @param tableControlParams
	 *            the table control params
	 */
	protected void setTableCssStyle(Map<String, Object> tableAttributes,
			List<ControlParam> tableControlParams) {
		String tableStyle = getTableCssStyle(tableControlParams);
		if (StringUtils.isNotNullOrEmpty(tableStyle)) {
			tableAttributes.put(DataTableControlParameter.STYLE_CLASS.getParam(), tableStyle);
		}
	}

	/**
	 * Sets the table style class.
	 * 
	 * @param tableAttributes
	 *            the table attributes
	 * @param tableControlParams
	 *            the table control params
	 */
	protected void setTableStyleClass(Map<String, Object> tableAttributes,
			List<ControlParam> tableControlParams) {

		String generatedTableStyleClass = GENERATED_TABLE_STYLE_CLASS;
		String tableStyleClass = getTableStyleClass(tableControlParams);
		if (StringUtils.isNotNullOrEmpty(tableStyleClass)) {
			generatedTableStyleClass = generatedTableStyleClass + " " + tableStyleClass;
		}
		tableAttributes.put(DataTableControlParameter.STYLE_CLASS.getParam(),
				generatedTableStyleClass);
	}

	/**
	 * Gets the column.
	 * 
	 * @param columnProperty
	 *            the header param
	 * @return the column
	 */
	protected HtmlColumn getColumn(PropertyDefinition columnProperty) {
		HtmlColumn column = new HtmlColumn();

		column.setId(columnProperty.getName());

		return column;
	}

	/**
	 * Sets the header.
	 * 
	 * @param htmlColumn
	 *            the html column
	 * @param columnProperty
	 *            the header param
	 */
	protected void setHeader(HtmlColumn htmlColumn, PropertyDefinition columnProperty) {

		HtmlOutputText header = (HtmlOutputText) createComponentInstance(
				"javax.faces.HtmlOutputText", "javax.faces.Text");

		String labelId = columnProperty.getLabel();
		if (StringUtils.isNullOrEmpty(labelId)) {
			log.error("CMFWeb: DatatableBuilder - missing labelId for " + columnProperty);
		} else {
			header.setValueExpression("value", createValueExpression(labelId, String.class));
		}

		htmlColumn.setHeader(header);
	}

	/**
	 * Gets the table style class.
	 * 
	 * @param tableControlParams
	 *            the table control params
	 * @return the table style class
	 */
	protected String getTableStyleClass(List<ControlParam> tableControlParams) {
		List<ControlParam> parameters = getParametersByName(
				DataTableControlParameter.STYLE_CLASS.name(), tableControlParams);
		String styleClass = "";
		if (!parameters.isEmpty()) {
			styleClass = parameters.get(0).getValue();
		}
		return styleClass;
	}

	/**
	 * Gets the table css style.
	 * 
	 * @param tableControlParams
	 *            the table control params
	 * @return the table css style
	 */
	private String getTableCssStyle(List<ControlParam> tableControlParams) {

		List<ControlParam> foundParameters = getParametersByName(
				DataTableControlParameter.STYLE.name(), tableControlParams);

		String cssStyle = "";
		if (!foundParameters.isEmpty()) {
			cssStyle = foundParameters.get(0).getValue();
		}

		return cssStyle;
	}

	/**
	 * Gets the value expression string.
	 * 
	 * @param columnProperty
	 *            the column property
	 * @param tableVar
	 *            the table var
	 * @return the value expression string
	 */
	protected String getValueExpressionString(PropertyDefinition columnProperty, String tableVar) {

		String expressionString = "";

		String value = columnProperty.getDefaultValue();

		String type = columnProperty.getDataType().getName();

		Integer codelistNumber = columnProperty.getCodelist();
		// there is a codelist attached
		if (codelistNumber != null) {

			expressionString = getValueExpressionStringForCodelist(tableVar, value, codelistNumber);

		} else if (DataTypeDefinition.DATE.equals(type)) {

			expressionString = "#{dateUtil.getFormattedDate(" + tableVar + ".properties['" + value
					+ "'])}";

		} else if (DataTypeDefinition.DATETIME.equals(type)) {

			expressionString = "#{dateUtil.getFormattedDateTime(" + tableVar + ".properties['"
					+ value + "'])}";

		} else if (isUserField(value)) {

			expressionString = "#{labelBuilder.getDisplayNameForUser(" + tableVar + ".properties['"
					+ value + "']," + tableVar + ")}";

		} else {

			expressionString = getValueExpressionString(tableVar, value);

		}

		return expressionString;
	}

	/**
	 * Checks if is user field.
	 * 
	 * @param value
	 *            the value
	 * @return true, if is user field
	 */
	private boolean isUserField(String value) {

		return OWNER.equals(value);
	}

	/**
	 * Getter method for dataTable.
	 * 
	 * @return the dataTable
	 */
	public HtmlDataTable getDataTable() {
		return dataTable;
	}

	/**
	 * Getter method for tableWrapper.
	 * 
	 * @return the tableWrapper
	 */
	public UIComponent getTableWrapper() {
		return tableWrapper;
	}

	/**
	 * Setter method for tableWrapper.
	 * 
	 * @param tableWrapper
	 *            the tableWrapper to set
	 */
	public void setTableWrapper(UIComponent tableWrapper) {
		this.tableWrapper = tableWrapper;
	}

}
