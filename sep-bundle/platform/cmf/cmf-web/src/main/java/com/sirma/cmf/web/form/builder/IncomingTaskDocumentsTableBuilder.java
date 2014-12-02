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
import javax.faces.component.html.HtmlPanelGroup;

import org.richfaces.component.UICommandButton;

import com.sirma.cmf.web.form.BuilderCssConstants;
import com.sirma.cmf.web.form.ComponentType;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.model.Sortable;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Builder for table that displays the incoming task documents.
 * 
 * @author svelikov
 */
public class IncomingTaskDocumentsTableBuilder extends DatatableBuilder {

	private static final String TABLE_VAR = "item";

	/**
	 * Instantiates a new task documents table builder.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public IncomingTaskDocumentsTableBuilder(LabelProvider labelProvider,
			CodelistService codelistService) {
		super(labelProvider, codelistService);
	}

	@Override
	public void build() {
		super.build();
	}

	@Override
	public String getTableVarAttribute() {
		return TABLE_VAR;
	}

	@Override
	public UIComponent getComponentInstance() {
		HtmlPanelGroup wrapper = (HtmlPanelGroup) createComponentInstance(
				"javax.faces.HtmlPanelGroup", "javax.faces.Group");
		addStyleClass(wrapper, "incoming-documents-table-wrapper");

		setLinkDocumentButton(wrapper);

		HtmlDataTable dataTable = getDataTable();
		Map<String, Object> attributes = dataTable.getAttributes();

		// check if we have defined a codelist and filters to work with
		Integer codelist = propertyDefinition.getCodelist();
		if (codelist != null) {
			Set<String> set = propertyDefinition.getFilters();
			if (set != null) {
				List<String> filters = new LinkedList<String>(set);
				// set the codelist attribute to the table component
				attributes.put("codelist", codelist);
				// set the filters attribute
				attributes.put("filters", filters);
			} else {
				log.warn("Found codelist for incomming documents table "
						+ propertyDefinition.getIdentifier() + " but no filters found!");
			}
		}

		dataTable.setColumnClasses("link-column,buttons-column");

		addStyleClass(dataTable, "incoming-documents-table");

		String valueExpressionString = "#{incomingTaskDocumentsController."
				+ propertyDefinition.getName() + "}";
		setTableValue(dataTable, valueExpressionString);

		String renderedExpressionString = "#{incomingTaskDocumentsController."
				+ propertyDefinition.getName() + ".size() > 0}";
		dataTable.setValueExpression("rendered",
				createValueExpression(renderedExpressionString, Boolean.class));

		List<Sortable> controlFields = getSortedControlFields();
		int columnsCount = controlFields.size();

		// build table columns
		List<HtmlColumn> htmlColumns = new ArrayList<HtmlColumn>(columnsCount);
		for (int i = 0; i < columnsCount; i++) {
			PropertyDefinition columnProperty = (PropertyDefinition) controlFields.get(i);
			HtmlColumn column = getColumn(columnProperty);
			String propertyName = columnProperty.getName();
			if ("type".equals(propertyName)) {
				setBodyCellWithLink(column, columnProperty);
			} else if ("actionsColumn".equals(propertyName)) {
				setRemoveIncomingDocumentButton(column, columnProperty);
			}

			htmlColumns.add(column);
		}

		dataTable.getChildren().addAll(htmlColumns);
		wrapper.getChildren().add(dataTable);
		return wrapper;
	}

	/**
	 * Sets the remove incoming document button.
	 * 
	 * @param column
	 *            the column
	 * @param columnProperty
	 *            the column property
	 */
	private void setRemoveIncomingDocumentButton(HtmlColumn column,
			PropertyDefinition columnProperty) {

		String fieldId = getIdForField(propertyDefinition.getName()) + "_removeLinkButton";
		String actionExpressionString = "#{incomingTaskDocumentsController.removeIncomingDocument("
				+ TABLE_VAR + ")}";
		MethodExpression actionExpression = createMethodExpression(actionExpressionString,
				new Class<?>[] {});
		String styleClass = BuilderCssConstants.CMF_STANDARD_BUTTON + " "
				+ "remove-document-button";
		String render = "incomingDocumentsRegion";
		String oncomplete = "EMF.blockUI.hideAjaxBlocker();";
		UICommandButton ajaxButton = builderHelper.createAjaxButton(fieldId, actionExpression,
				null, oncomplete,
				labelProvider.getValue("cmf.workflow.task.document.remove.incoming"), render,
				"@this", styleClass, true, true);
		String onclick = "EMF.blockUI.showAjaxLoader();";
		ajaxButton.setOnclick(onclick);

		if (formViewMode != FormViewMode.EDIT) {
			ajaxButton.setDisabled(true);
		}

		column.getChildren().add(ajaxButton);
	}

	/**
	 * Setter that generate link with icon for incoming document region. Here we apply expressions
	 * for retrieving document title, icon and styles.
	 * 
	 * @param htmlColumn
	 *            column that support all incoming documents
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
			String documentTitle = "#{labelBuilder.getTaskDocumentLinkLabel(" + TABLE_VAR + ")}";
			header.setValueExpression("value", createValueExpression(documentTitle, String.class));
			header.setEscape(false);

			// component that will holds document title and action
			UIComponent headerWrapper = builderHelper.getComponent(ComponentType.OUTPUT_PANEL);
			String styleClassExpressionString = "#{documentAction.getStylesForDocumentType(item.documentInstance, false)}";
			ValueExpression styleClassExpression = createValueExpression(
					styleClassExpressionString, String.class);
			headerWrapper.setValueExpression("styleClass", styleClassExpression);

			List<UIComponent> columnChildren = htmlColumn.getChildren();
			columnChildren.add(headerWrapper);
			headerWrapper.getChildren().add(header);
			columnChildren.add(headerWrapper);
		}
	}

	/**
	 * Sets the file upload trigger.
	 * 
	 * @param wrapper
	 *            the new link document button
	 */
	public void setLinkDocumentButton(HtmlPanelGroup wrapper) {
		String fieldId = getDocumentLinkButtonId();
		String actionExpressionString = "#{incomingTaskDocumentsController.showAvailableDocuments()}";
		MethodExpression actionExpression = createMethodExpression(actionExpressionString,
				new Class<?>[] {});
		String oncompleteAttribute = "RichFaces.$('formId\\:uploadedDocumentsPopup').show(); EMF.blockUI.hideAjaxBlocker();";
		String styleClass = BuilderCssConstants.CMF_STANDARD_BUTTON + " " + "link-documents-button";
		String render = "availableDocumentsPopup,uploadedDocumentsPopup";
		UICommandButton ajaxButton = builderHelper.createAjaxButton(fieldId, actionExpression,
				null, oncompleteAttribute,
				labelProvider.getValue("cmf.workflow.task.document.add.incoming"), render, "@this",
				styleClass, true, true);
		String onclick = "EMF.blockUI.showAjaxLoader();";
		ajaxButton.setOnclick(onclick);
		if (formViewMode != FormViewMode.EDIT) {
			ajaxButton.setDisabled(true);
		}

		wrapper.getChildren().add(ajaxButton);
	}

	/**
	 * Gets the document link button id.
	 * 
	 * @return the document link button id
	 */
	private String getDocumentLinkButtonId() {
		return getIdForField(propertyDefinition.getName()) + "_linkButton";
	}

}
