package com.sirma.cmf.web.form.builder;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;

import org.richfaces.component.UIMediaOutput;

import com.sirma.cmf.web.form.ComponentType;
import com.sirma.cmf.web.form.control.MediaOutputControlParameter;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Builder for media output control.
 * 
 * @author svelikov
 */
public class MediaOutputBuilder extends ControlBuilder {

	/**
	 * Instantiates a new media output builder.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public MediaOutputBuilder(LabelProvider labelProvider, CodelistService codelistService) {
		super(labelProvider, codelistService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UIComponent getComponentInstance() {

		ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();
		// all control parameters
		List<ControlParam> controlParameters = controlDefinition.getControlParams();

		// create component wrapper panel
		HtmlPanelGroup wrapperPanel = (HtmlPanelGroup) builderHelper
				.getComponent(ComponentType.OUTPUT_PANEL);
		wrapperPanel.setValueExpression("rendered",
				createValueExpression("#{workflowHistoryInstance.active}", Boolean.class));
		wrapperPanel.getAttributes().put("styleClass", "workflow-process-diagram");
		List<UIComponent> wrapperChildren = wrapperPanel.getChildren();

		// create component label
		wrapperChildren.add(createComponentlabel(controlParameters));

		// create missing image panel

		// create media output component
		UIMediaOutput mediaOutputComponent = (UIMediaOutput) builderHelper
				.getComponent(ComponentType.A4J_MEDIA_OUTPUT);
		mediaOutputComponent.setElement("img");
		mediaOutputComponent.setCacheable(false);
		// mediaOutputComponent.setSession(true);
		// TODO: hardcoded value
		mediaOutputComponent.setValueExpression("value",
				createValueExpression("#{workflowDetails.picKey}", String.class));
		mediaOutputComponent.setValueExpression("createContent",
				createValueExpression("#{drawBean.render}", Object.class));
		mediaOutputComponent.setMimeType("image/png");
		mediaOutputComponent.getAttributes().put("styleClass", "process-diagram");
		// element="img" cacheable="false" session="true"
		// createContent="#{drawBean.render}"
		// value="#{workflowDetails.picKey}" mimeType="image/png"
		// styleClass="process-diagram"
		wrapperChildren.add(mediaOutputComponent);

		return wrapperPanel;
	}

	/**
	 * Creates the componentlabel.
	 * 
	 * @param controlParameters
	 *            the control parameters
	 * @return the uI component
	 */
	private UIComponent createComponentlabel(List<ControlParam> controlParameters) {
		HtmlOutputText labelComponent = (HtmlOutputText) builderHelper
				.getComponent(ComponentType.OUTPUT_TEXT);
		String labelText = "Hardcoded label";
		ControlParam labelParameter = getParametersByName(
				MediaOutputControlParameter.CONTROL_LABEL.name(), controlParameters).get(0);
		if (labelParameter != null) {
			labelText = labelParameter.getValue();
		}
		labelComponent.setValue(labelText);

		return labelComponent;
	}
}
