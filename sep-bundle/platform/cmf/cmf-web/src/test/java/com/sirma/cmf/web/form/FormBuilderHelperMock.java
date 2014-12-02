package com.sirma.cmf.web.form;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlInputTextarea;
import javax.faces.component.html.HtmlMessage;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.component.html.HtmlSelectManyCheckbox;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.component.html.HtmlSelectOneRadio;

import org.richfaces.component.UICommandLink;
import org.richfaces.component.UIMediaOutput;

import com.sirma.cmf.web.form.builder.FormBuilderHelper;

/**
 * The Class FormBuilderHelperMock.
 * 
 * @author svelikov
 */
public class FormBuilderHelperMock extends FormBuilderHelper {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent getComponent(ComponentType componentType) {
		UIComponent component = null;
		if (componentType == ComponentType.COMMAND_LINK) {
			component = createComponentInstance("javax.faces.HtmlCommandLink", "javax.faces.Link");
			component = new HtmlCommandLink();
		} else if (componentType == ComponentType.A4J_COMMAND_LINK) {
			component = new UICommandLink();
		} else if (componentType == ComponentType.GRAPHIC_IMAGE) {
			component = new HtmlGraphicImage();
		} else if (componentType == ComponentType.OUTPUT_PANEL) {
			component = new HtmlPanelGroup();
		} else if (componentType == ComponentType.OUTPUT_TEXT) {
			component = new HtmlOutputText();
		} else if (componentType == ComponentType.A4J_MEDIA_OUTPUT) {
			component = new UIMediaOutput();
		} else if (componentType == ComponentType.SELECT_MANY_CHECKBOX) {
			component = new HtmlSelectManyCheckbox();
		} else if (componentType == ComponentType.SELECT_BOOLEAN_CHECKBOX) {
			component = new HtmlSelectBooleanCheckbox();
		} else if (componentType == ComponentType.OUTPUT_LABEL) {
			component = new HtmlOutputLabel();
		} else if (componentType == ComponentType.RADIO_BUTTON_GROUP) {
			component = new HtmlSelectOneRadio();
		} else if (componentType == ComponentType.INPUT_HIDDEN) {
			component = new HtmlInputHidden();
		} else if (componentType == ComponentType.INPUT_TEXT) {
			component = new HtmlInputText();
		} else if (componentType == ComponentType.INPUT_TEXT_AREA) {
			component = new HtmlInputTextarea();
		} else if (componentType == ComponentType.SELECT_ONE_MENU) {
			component = new HtmlSelectOneMenu();
		} else if (componentType == ComponentType.SELECT_ITEM) {
			component = new UISelectItem();
		} else if (componentType == ComponentType.SELECT_ITEMS) {
			component = new UISelectItems();
		} else if (componentType == ComponentType.HTML_MESSAGE) {
			component = new HtmlMessage();
		}

		return component;
	}

}
