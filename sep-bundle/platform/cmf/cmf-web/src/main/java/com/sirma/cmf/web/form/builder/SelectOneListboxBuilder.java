package com.sirma.cmf.web.form.builder;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlSelectOneListbox;

import com.sirma.cmf.web.form.control.PicklistControlParameter;
import com.sirma.cmf.web.form.picklist.PicklistConstants;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * SelectOneListboxBuilder.
 * 
 * @author svelikov
 */
public class SelectOneListboxBuilder extends ControlBuilder {

	/** The Constant JAVAX_FACES_LISTBOX. */
	private static final String JAVAX_FACES_LISTBOX = "javax.faces.Listbox";

	/** The Constant JAVAX_FACES_SELECT_ITEMS. */
	private static final String JAVAX_FACES_SELECT_ITEMS = "javax.faces.SelectItems";

	/** The Constant JAVAX_FACES_SELECT_ITEM. */
	private static final String JAVAX_FACES_SELECT_ITEM = "javax.faces.SelectItem";

	/** The Constant JAVAX_FACES_HTML_SELECT_ONE_MENU. */
	private static final String JAVAX_FACES_HTML_SELECT_ONE_LISTBOX = "javax.faces.HtmlSelectOneListbox";

	/**
	 * Instantiates a new select one listbox builder.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public SelectOneListboxBuilder(LabelProvider labelProvider, CodelistService codelistService) {
		super(labelProvider, codelistService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent getComponentInstance() {

		HtmlSelectOneListbox uiComponent = (HtmlSelectOneListbox) createComponentInstance(
				JAVAX_FACES_HTML_SELECT_ONE_LISTBOX, JAVAX_FACES_LISTBOX);

		uiComponent.getAttributes().put("size", 4);

		List<UIComponent> children = uiComponent.getChildren();

		// create an empty select item element to represent no selection
		// label
		UISelectItem selectItem = (UISelectItem) createComponentInstance(JAVAX_FACES_SELECT_ITEM);
		selectItem.getAttributes().put("noSelectionOption", Boolean.TRUE);
		children.add(selectItem);

		// create select items element
		UISelectItems selectItems = (UISelectItems) createComponentInstance(JAVAX_FACES_SELECT_ITEMS);

		setValueExpression(uiComponent, selectItems);

		selectItems.setValueExpression("itemValue",
				createValueExpression("#{current.name}", String.class));

		selectItems.setValueExpression("itemLabel",
				createValueExpression("#{current.getDisplayName()}", String.class));

		selectItems.setValueExpression("var", createValueExpression("current", Object.class));
		children.add(selectItems);

		return uiComponent;

	}

	/**
	 * Sets the value expression.
	 * 
	 * @param uiComponent
	 *            the ui component
	 * @param selectItems
	 *            the select items
	 */
	private void setValueExpression(HtmlSelectOneListbox uiComponent, UISelectItems selectItems) {

		List<ControlParam> controlParams = propertyDefinition.getControlDefinition()
				.getControlParams();

		String filterName = getFilterName(controlParams);
		uiComponent.getAttributes().put(PicklistConstants.FILTERNAME_ATTR, filterName);

		Map<String, List<String>> keywords = getKeywords(controlParams);
		if (!keywords.isEmpty()) {
			uiComponent.getAttributes().put(PicklistConstants.KEYWORDS_ATTR, keywords);
		}

		// call loadItems with filter string in order to get required object
		// list populated
		String exprString = "#{workflowSelectItemAction.loadItems()}";

		selectItems.setValueExpression("value", createValueExpression(exprString, List.class));
	}

	/**
	 * Assembles a map with keywords.
	 * 
	 * @param controlParams
	 *            the control params
	 * @return the keywords
	 */
	private Map<String, List<String>> getKeywords(List<ControlParam> controlParams) {

		List<ControlParam> keywordParams = getParametersByName(
				PicklistControlParameter.KEYWORD.name(), controlParams);

		Map<String, List<String>> keywordsMapping = new LinkedHashMap<String, List<String>>();

		if (keywordParams != null && !keywordParams.isEmpty()) {
			for (ControlParam keywordParam : keywordParams) {
				String keywordId = keywordParam.getIdentifier();
				String keywordsString = keywordParam.getValue();

				if (StringUtils.isNotNullOrEmpty(keywordsString)) {
					keywordsString = keywordsString.replaceAll(" ", "");
					List<String> keywords = Arrays.asList(keywordsString.split(","));
					keywordsMapping.put(keywordId, keywords);
				}
			}
		}

		return keywordsMapping;
	}

	/**
	 * Gets the name to be used for the event for loading of items for this listbox.
	 * 
	 * @param controlParams
	 *            the parameters
	 * @return the filter name
	 */
	private String getFilterName(List<ControlParam> controlParams) {
		String filterName = propertyDefinition.getName();
		ControlParam filterNameParam = getControlParameter(
				PicklistControlParameter.FILTER_NAME.name(), controlParams);

		if (filterNameParam != null) {
			String providedFiltername = filterNameParam.getValue();
			if (StringUtils.isNotNullOrEmpty(providedFiltername)) {
				filterName = providedFiltername;
			}
		}

		return filterName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<String> getValueResultType() {
		return String.class;
	}

}
