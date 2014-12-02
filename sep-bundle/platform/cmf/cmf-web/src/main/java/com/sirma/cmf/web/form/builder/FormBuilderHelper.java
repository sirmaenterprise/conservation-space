package com.sirma.cmf.web.form.builder;

import java.util.ArrayList;
import java.util.List;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;

import org.richfaces.component.UICommandButton;
import org.richfaces.component.UICommandLink;

import com.sirma.cmf.web.form.ComponentType;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.model.ControlParam;

/**
 * Helper methods used in form builders.
 */
public class FormBuilderHelper {

	protected static final String ORG_RICHFACES_COMMAND_BUTTON_RENDERER = "org.richfaces.CommandButtonRenderer";

	protected static final String ORG_RICHFACES_COMMAND_BUTTON = "org.richfaces.CommandButton";

	protected static final String JAVAX_FACES_TEXT = "javax.faces.Text";

	protected static final String JAVAX_FACES_HTML_OUTPUT_TEXT = "javax.faces.HtmlOutputText";

	private static final String JAVAX_FACES_TEXTAREA_RENDERER = "javax.faces.Textarea";

	private static final String JAVAX_FACES_HTML_INPUT_TEXTAREA = "javax.faces.HtmlInputTextarea";

	private static final String JAVAX_FACES_SELECT_ITEMS = "javax.faces.SelectItems";

	private static final String JAVAX_FACES_SELECT_ITEM = "javax.faces.SelectItem";

	private static final String JAVAX_FACES_MENU_RENDERER = "javax.faces.Menu";

	private static final String JAVAX_FACES_HTML_SELECT_ONE_MENU = "javax.faces.HtmlSelectOneMenu";

	private static final String JAVAX_FACES_HTML_OUTPUT_LINK = "javax.faces.HtmlOutputLink";

	private static final String JAVAX_FACES_LINK_RENDERER = "javax.faces.Link";

	/**
	 * Gets the component.
	 * 
	 * @param componentType
	 *            the component type
	 * @return the component
	 */
	public UIComponent getComponent(ComponentType componentType) {

		UIComponent component = null;

		if (componentType == ComponentType.COMMAND_LINK) {
			component = createComponentInstance("javax.faces.HtmlCommandLink", "javax.faces.Link");
		} else if (componentType == ComponentType.OUTPUT_LINK) {
			component = createComponentInstance(JAVAX_FACES_HTML_OUTPUT_LINK,
					JAVAX_FACES_LINK_RENDERER);
		} else if (componentType == ComponentType.A4J_COMMAND_LINK) {
			component = createComponentInstance("org.richfaces.CommandLink",
					"org.richfaces.CommandLinkRenderer");
		} else if (componentType == ComponentType.GRAPHIC_IMAGE) {
			component = createComponentInstance("javax.faces.HtmlGraphicImage", "javax.faces.Image");
		} else if (componentType == ComponentType.OUTPUT_PANEL) {
			component = createComponentInstance("javax.faces.HtmlPanelGroup", "javax.faces.Group");
		} else if (componentType == ComponentType.OUTPUT_TEXT) {
			component = createComponentInstance(JAVAX_FACES_HTML_OUTPUT_TEXT, JAVAX_FACES_TEXT);
		} else if (componentType == ComponentType.A4J_MEDIA_OUTPUT) {
			component = createComponentInstance("org.richfaces.MediaOutput",
					"org.richfaces.MediaOutputRenderer");
		} else if (componentType == ComponentType.SELECT_MANY_CHECKBOX) {
			component = createComponentInstance("javax.faces.HtmlSelectManyCheckbox",
					"javax.faces.Checkbox");
		} else if (componentType == ComponentType.SELECT_BOOLEAN_CHECKBOX) {
			component = createComponentInstance("javax.faces.HtmlSelectBooleanCheckbox",
					"javax.faces.Checkbox");
		} else if (componentType == ComponentType.OUTPUT_LABEL) {
			component = createComponentInstance("javax.faces.HtmlOutputLabel", "javax.faces.Label");
		} else if (componentType == ComponentType.RADIO_BUTTON_GROUP) {
			component = createComponentInstance("javax.faces.HtmlSelectOneRadio",
					"javax.faces.Radio");
		} else if (componentType == ComponentType.INPUT_HIDDEN) {
			component = createComponentInstance("javax.faces.HtmlInputHidden", "javax.faces.Hidden");
		} else if (componentType == ComponentType.INPUT_TEXT) {
			component = createComponentInstance("javax.faces.HtmlInputText", "javax.faces.Text");
		} else if (componentType == ComponentType.INPUT_TEXT_AREA) {
			component = createComponentInstance(JAVAX_FACES_HTML_INPUT_TEXTAREA,
					JAVAX_FACES_TEXTAREA_RENDERER);
		} else if (componentType == ComponentType.SELECT_ONE_MENU) {
			component = createComponentInstance(JAVAX_FACES_HTML_SELECT_ONE_MENU,
					JAVAX_FACES_MENU_RENDERER);
		} else if (componentType == ComponentType.SELECT_ITEM) {
			component = createComponentInstance(JAVAX_FACES_SELECT_ITEM);
		} else if (componentType == ComponentType.SELECT_ITEMS) {
			component = createComponentInstance(JAVAX_FACES_SELECT_ITEMS);
		} else if (componentType == ComponentType.A4J_OUTPUT_PANEL) {
			component = createComponentInstance("org.richfaces.OutputPanel",
					"org.richfaces.OutputPanelRenderer");
		} else if (componentType == ComponentType.HTML_MESSAGE) {
			component = createComponentInstance("javax.faces.HtmlMessage", "javax.faces.Message");
		}

		return component;
	}

	/**
	 * Creates the component instance.
	 * 
	 * @param componentType
	 *            the component type
	 * @param componentRenderer
	 *            the component renderer
	 * @return the uI component
	 */
	protected UIComponent createComponentInstance(String componentType, String componentRenderer) {

		FacesContext facesContext = FacesContext.getCurrentInstance();

		UIComponent createdComponent = facesContext.getApplication().createComponent(facesContext,
				componentType, componentRenderer);

		return createdComponent;
	}

	/**
	 * Creates the component instance.
	 * 
	 * @param componentType
	 *            the component type
	 * @return the uI component
	 */
	protected UIComponent createComponentInstance(String componentType) {

		FacesContext facesContext = FacesContext.getCurrentInstance();

		UIComponent createdComponent = facesContext.getApplication().createComponent(componentType);

		return createdComponent;
	}

	/**
	 * Gets the parameters by name.
	 * 
	 * @param parameterName
	 *            the parameter name
	 * @param controlParams
	 *            the control params
	 * @return the parameters by name
	 */
	public List<ControlParam> getParametersByName(String parameterName,
			List<ControlParam> controlParams) {

		List<ControlParam> foundParameters = new ArrayList<ControlParam>();

		for (ControlParam controlParam : controlParams) {
			if (parameterName.equals(controlParam.getName())) {
				foundParameters.add(controlParam);
			}
		}

		return foundParameters;
	}

	/**
	 * Gets the parameter by name. If is found requested parameter, then it is returned. If more
	 * than one parameters with given name exists, then the first one is returned. Returns null if
	 * no parameter with given name is found.
	 * 
	 * @param parameterName
	 *            the parameter name
	 * @param controlParams
	 *            the control params
	 * @return the parameter by name
	 */
	public ControlParam getParameterByName(String parameterName, List<ControlParam> controlParams) {

		ControlParam foundParameter = null;

		for (ControlParam controlParam : controlParams) {
			if (parameterName.equals(controlParam.getName())) {
				foundParameter = controlParam;
				break;
			}
		}

		return foundParameter;
	}

	/**
	 * Creates an ajax button.
	 * 
	 * @param id
	 *            the id
	 * @param actionExpression
	 *            the action expression
	 * @param disabledExpression
	 *            the disabled expression
	 * @param oncompleteAttribute
	 *            the oncomplete attribute
	 * @param buttonValue
	 *            the button value
	 * @param render
	 *            the render
	 * @param execute
	 *            the execute
	 * @param styleClass
	 *            the style class
	 * @param rendered
	 *            the rendered
	 * @param immediate
	 *            the immediate
	 * @return the ajax button
	 */
	public UICommandButton createAjaxButton(String id, MethodExpression actionExpression,
			ValueExpression disabledExpression, String oncompleteAttribute, String buttonValue,
			String render, String execute, String styleClass, Boolean rendered, Boolean immediate) {

		UICommandButton commandButton = (UICommandButton) createComponentInstance(
				ORG_RICHFACES_COMMAND_BUTTON, ORG_RICHFACES_COMMAND_BUTTON_RENDERER);

		if (StringUtils.isNotNullOrEmpty(id)) {
			commandButton.setId(id);
		}

		if (actionExpression != null) {
			commandButton.setActionExpression(actionExpression);
		}

		if (disabledExpression != null) {
			commandButton.setValueExpression("disabled", disabledExpression);
		}

		if (StringUtils.isNotNullOrEmpty(oncompleteAttribute)) {
			commandButton.setOncomplete(oncompleteAttribute);
		}

		if (StringUtils.isNotNullOrEmpty(buttonValue)) {
			commandButton.setValue(buttonValue);
		} else {
			commandButton.setValue("");
		}

		if (StringUtils.isNotNullOrEmpty(render)) {
			commandButton.setRender(render);
		}

		if (StringUtils.isNotNullOrEmpty(execute)) {
			commandButton.setExecute(execute);
		}

		if (StringUtils.isNotNullOrEmpty(styleClass)) {
			commandButton.getAttributes().put("styleClass", styleClass);
		}

		if (rendered != null) {
			commandButton.setRendered(rendered);
		}

		if (immediate != null) {
			commandButton.setImmediate(immediate);
		}

		return commandButton;
	}

	/**
	 * Creates an ajax link.
	 * 
	 * @param linkId
	 *            the link id
	 * @param actionExpression
	 *            the action expression
	 * @param linkValueExpression
	 *            the link value expression
	 * @param disabledExpression
	 *            the disabled expression
	 * @param immediate
	 *            the immediate
	 * @param render
	 *            the render
	 * @param execute
	 *            the execute
	 * @return the ajax link
	 */
	public UICommandLink createAjaxLink(String linkId, MethodExpression actionExpression,
			String linkValueExpression, ValueExpression disabledExpression, Boolean immediate,
			String render, String execute) {
		UICommandLink link = (UICommandLink) getComponent(ComponentType.A4J_COMMAND_LINK);

		if (StringUtils.isNotNullOrEmpty(linkId)) {
			link.setId(linkId);
		}

		if (actionExpression != null) {
			link.setActionExpression(actionExpression);
		}

		if (disabledExpression != null) {
			link.setValueExpression("disabled", disabledExpression);
		}

		if (immediate != null) {
			link.setImmediate(immediate);
		}

		if (StringUtils.isNotNullOrEmpty(render)) {
			link.setRender(render);
		}

		if (StringUtils.isNotNullOrEmpty(execute)) {
			link.setExecute(execute);
		}

		HtmlOutputText linkValue = (HtmlOutputText) createComponentInstance(
				JAVAX_FACES_HTML_OUTPUT_TEXT, JAVAX_FACES_TEXT);
		linkValue.setValueExpression("value",
				createValueExpression(linkValueExpression, String.class));
		linkValue.setEscape(false);

		link.getChildren().add(linkValue);
		return link;
	}

	/**
	 * Creates the output link.
	 * 
	 * @param fieldName
	 *            the field name
	 * @param valueExpressionString
	 *            the value expression string
	 * @param disabledExpressionString
	 *            the disabled expression string
	 * @param linkLabelExpressionString
	 *            the link label expression string
	 * @return the html output link
	 */
	public HtmlOutputLink createOutputLink(String fieldName, String valueExpressionString,
			String disabledExpressionString, String linkLabelExpressionString) {
		HtmlOutputLink link = (HtmlOutputLink) createComponentInstance(
				JAVAX_FACES_HTML_OUTPUT_LINK, JAVAX_FACES_LINK_RENDERER);

		if (StringUtils.isNotNullOrEmpty(fieldName)) {
			link.setId(fieldName);
		}

		if (StringUtils.isNotNullOrEmpty(valueExpressionString)) {
			link.setValueExpression("value",
					createValueExpression(valueExpressionString, String.class));
		}

		if (StringUtils.isNotNullOrEmpty(disabledExpressionString)) {
			link.setValueExpression("disabled",
					createValueExpression(disabledExpressionString, Boolean.class));
		} else {
			link.setDisabled(false);
		}

		HtmlOutputText outputText = (HtmlOutputText) createComponentInstance(
				JAVAX_FACES_HTML_OUTPUT_TEXT, JAVAX_FACES_TEXT);
		outputText.setEscape(false);

		if (StringUtils.isNotNullOrEmpty(linkLabelExpressionString)) {
			outputText.setValueExpression("value",
					createValueExpression(linkLabelExpressionString, String.class));
		} else {
			outputText.setValue("Generated link label!!! Check definition.");
		}

		link.getChildren().add(outputText);

		return link;
	}

	/**
	 * Creates a graphic image.
	 * 
	 * @param imageName
	 *            the image name
	 * @return the link image
	 */
	public HtmlGraphicImage createGraphicImage(String imageName) {
		HtmlGraphicImage image = (HtmlGraphicImage) getComponent(ComponentType.GRAPHIC_IMAGE);
		image.setValueExpression("value",
				createValueExpression("../images:" + imageName + "", String.class));

		return image;
	}

	/**
	 * Creates a value expression.
	 * 
	 * @param stringValueExpression
	 *            The value expression string.
	 * @param valueType
	 *            The expected return type from evaluation of the value expression.
	 * @return created ValueExpression.
	 */
	private ValueExpression createValueExpression(String stringValueExpression, Class<?> valueType) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return facesContext
				.getApplication()
				.getExpressionFactory()
				.createValueExpression(facesContext.getELContext(), stringValueExpression,
						valueType);
	}
}
