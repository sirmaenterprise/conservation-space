package com.sirma.cmf.web.form.builder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;

import org.richfaces.component.UICommandButton;

import com.sirma.cmf.web.form.BuilderCssConstants;
import com.sirma.cmf.web.form.ComponentType;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.model.Sortable;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Builder for table that displays outgoing task documents.
 * 
 * @author svelikov
 */
public class OutgoingTaskDocumentsTableBuilder extends DatatableBuilder {

	private static final String ORG_RICHFACES_COMMAND_BUTTON_RENDERER = "org.richfaces.CommandButtonRenderer";

	private static final String ORG_RICHFACES_COMMAND_BUTTON = "org.richfaces.CommandButton";

	private static final String TABLE_VAR = "item";

	/**
	 * Instantiates a new task documents table builder.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public OutgoingTaskDocumentsTableBuilder(LabelProvider labelProvider,
			CodelistService codelistService) {
		super(labelProvider, codelistService);
	}

	@Override
	public void build() {
		super.build();
		UICommandButton actionButton = createUpdateModelActionButton();
		getTableWrapper().getChildren().add(actionButton);
	}

	@Override
	public String getTableVarAttribute() {
		return TABLE_VAR;
	}

	@Override
	public UIComponent getComponentInstance() {

		HtmlDataTable dataTable = getDataTable();

		dataTable.setColumnClasses("link-column,buttons-column");

		Map<String, Object> attributes = dataTable.getAttributes();

		String valueExpressionString = "#{outgoingDocumentUploadController."
				+ propertyDefinition.getName() + "}";
		setTableValue(dataTable, valueExpressionString);

		// set the codelist attribute to the table component
		Integer codelist = propertyDefinition.getCodelist();
		if (codelist != null) {
			attributes.put("codelist", codelist);
			// set the filters attribute
			Set<String> set = propertyDefinition.getFilters();
			List<String> filters;
			if ((set != null) && !set.isEmpty()) {
				filters = new LinkedList<String>(set);
			} else {
				filters = new LinkedList<String>();
			}
			attributes.put("filters", filters);
		}
		List<Sortable> controlFields = getSortedControlFields();
		int columnsCount = controlFields.size();

		// build table columns
		List<HtmlColumn> htmlColumns = new ArrayList<HtmlColumn>(columnsCount);
		for (int i = 0; i < columnsCount; i++) {
			PropertyDefinition columnProperty = (PropertyDefinition) controlFields.get(i);
			HtmlColumn column = getColumn(columnProperty);
			// assume the first column is holding the link
			String propertyName = columnProperty.getName();
			if ("type".equals(propertyName)) {
				setBodyCellWithLink(column, columnProperty);
			} else if ("uploadOutgoingDocument".equals(propertyName)) {
				setFileUploadTrigger(column, columnProperty);
			}
			htmlColumns.add(column);
		}

		dataTable.getChildren().addAll(htmlColumns);

		return dataTable;
	}

	/**
	 * Setter that generate link with icon for outgoing document region. Here we apply expressions
	 * for retrieving document title, icon and styles.
	 * 
	 * @param htmlColumn
	 *            column that support all outgoing documents
	 * @param columnProperty
	 *            available column properties
	 */
	protected void setBodyCellWithLink(HtmlColumn htmlColumn, PropertyDefinition columnProperty) {

		String columnPropertyName = columnProperty.getName();
		if (StringUtils.isNullOrEmpty(columnPropertyName)) {
			log.error("CMFWeb: DatatableBuilder - missing value for ControlParam " + columnProperty);
		} else {
			HtmlOutputText header = (HtmlOutputText) builderHelper
					.getComponent(ComponentType.OUTPUT_TEXT);
			// expression for retrieving document title
			String headerVEString = "#{labelBuilder.getTaskDocumentLinkLabel(" + TABLE_VAR + ")}";
			header.setValueExpression("value", createValueExpression(headerVEString, String.class));
			header.setEscape(false);

			// component that will holds document title and action
			UIComponent headerWrapper = builderHelper.getComponent(ComponentType.OUTPUT_PANEL);
			String styleClassExpressionString = "#{documentAction.getStylesForDocumentType(item.documentInstance,true)}";
			ValueExpression styleClassExpression = createValueExpression(
					styleClassExpressionString, String.class);
			headerWrapper.setValueExpression("styleClass", styleClassExpression);
			headerWrapper.getChildren().add(header);

			List<UIComponent> columnChildren = htmlColumn.getChildren();
			columnChildren.add(headerWrapper);
		}
	}

	/**
	 * Sets the file upload trigger.
	 * 
	 * @param column
	 *            the column
	 * @param columnProperty
	 *            the column property
	 */
	public void setFileUploadTrigger(HtmlColumn column, PropertyDefinition columnProperty) {

		UICommandButton commandButton = (UICommandButton) createComponentInstance(
				ORG_RICHFACES_COMMAND_BUTTON, ORG_RICHFACES_COMMAND_BUTTON_RENDERER);

		// set button id
		String fieldId = getIdForField(propertyDefinition.getName());
		commandButton.setId(fieldId);

		// set the button's style class
		addStyleClass(commandButton, BuilderCssConstants.CMF_STANDARD_BUTTON + " "
				+ "file-upload-trigger-button");

		// set disabled attribute
		String disabledExpression = "#{" + TABLE_VAR + ".uploaded}";
		commandButton.setValueExpression("disabled",
				createValueExpression(disabledExpression, Boolean.class));

		String onCompleteAttribute = "CMF.fileUploadInTask.init(EMF.documentContext.currentInstanceId, EMF.documentContext.currentInstanceType, EMF.currentPath, event)";
		// set oncomplete attribute
		commandButton.setOncomplete(onCompleteAttribute);

		commandButton.setValue(labelProvider.getValue("cmf.document.upload.button.upload"));
		commandButton.setRender("outgoingDocumentUploadPopup");
		commandButton.setExecute("@this");
		commandButton.setRendered(true);
		commandButton.setImmediate(true);

		if (formViewMode != FormViewMode.EDIT) {
			commandButton.setDisabled(true);
		}

		column.getChildren().add(commandButton);
	}

	/**
	 * Creates the update model action button. This button is added once in the wrapper of the
	 * table. It should be invoked programmatatically after document upload.
	 * 
	 * @return the uI command button
	 */
	private UICommandButton createUpdateModelActionButton() {
		UICommandButton commandButton = (UICommandButton) createComponentInstance(
				ORG_RICHFACES_COMMAND_BUTTON, ORG_RICHFACES_COMMAND_BUTTON_RENDERER);

		// set button id
		String fieldId = getIdForField(propertyDefinition.getName() + "_updateModelButton");
		commandButton.setId(fieldId);

		// set the button's style class
		addStyleClass(commandButton, "hidden update-model-button");

		// set action
		String methodExpressionString = "#{outgoingDocumentUploadController.updateModel()}";
		MethodExpression actionMethodExpressionString = createMethodExpression(
				methodExpressionString, new Class<?>[] { DocumentInstance.class });
		commandButton.setActionExpression(actionMethodExpressionString);

		commandButton.setRender("taskDetails");
		commandButton.setExecute("@this");
		commandButton.setRendered(true);
		commandButton.setImmediate(true);

		return commandButton;
	}

}
