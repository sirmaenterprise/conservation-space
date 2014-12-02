package com.sirma.cmf.web.form.builder;

import java.util.List;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlSelectOneMenu;

import com.sirma.cmf.web.form.ComponentType;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Builder for select one menu fields.
 * 
 * @author svelikov
 */
public class SelectOneMenuBuilder extends FormBuilder {

	/**
	 * Instantiates a new select one menu builder.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public SelectOneMenuBuilder(LabelProvider labelProvider, CodelistService codelistService) {
		super(labelProvider, codelistService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent getComponentInstance() {

		HtmlSelectOneMenu uiComponent = (HtmlSelectOneMenu) builderHelper
				.getComponent(ComponentType.SELECT_ONE_MENU);

		List<UIComponent> children = uiComponent.getChildren();

		addNoselectionOption(children);

		addSelectItems(children);

		return uiComponent;
	}

	/**
	 * Create select items element.
	 * 
	 * @param children
	 *            the children
	 */
	public void addSelectItems(List<UIComponent> children) {
		UISelectItems selectItems = (UISelectItems) builderHelper
				.getComponent(ComponentType.SELECT_ITEMS);

		Integer codelistNumber = propertyDefinition.getCodelist();
		// set value expression
		String valueExpressionString = createValueExpressionString(codelistNumber);
		selectItems.setValueExpression("value",
				createValueExpression(valueExpressionString, Set.class));
		selectItems.setValueExpression(
				"itemLabel",
				createValueExpression("#{cls.getDescription(" + codelistNumber + ", current)}",
						String.class));
		// set var attribute
		selectItems.setValueExpression("var", createValueExpression("current", Object.class));
		children.add(selectItems);
	}

	/**
	 * Create an empty select item element to represent no selection label.
	 * 
	 * @param children
	 *            the children
	 */
	public void addNoselectionOption(List<UIComponent> children) {
		UISelectItem selectItem = (UISelectItem) builderHelper
				.getComponent(ComponentType.SELECT_ITEM);
		selectItem.getAttributes().put("noSelectionOption", Boolean.TRUE);
		children.add(selectItem);
	}

	/**
	 * Creates the value expression string.
	 * 
	 * @param codelistNumber
	 *            the codelist number
	 * @return the string
	 */
	public String createValueExpressionString(Integer codelistNumber) {

		// TODO: build and set the filters array as an attribute to the uicomponent
		StringBuilder expr = new StringBuilder("#{cls.getFilteredCodeValues(");
		expr.append(codelistNumber).append(", formUtil.toArray('");

		Set<String> filters = propertyDefinition.getFilters();
		if (filters != null && !filters.isEmpty()) {
			for (String filterId : filters) {
				expr.append(filterId).append("','");
			}

			expr.setLength(expr.length() - 3);
		}

		expr.append("')).keySet()}");

		return expr.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<String> getValueResultType() {
		return String.class;
	}

}
