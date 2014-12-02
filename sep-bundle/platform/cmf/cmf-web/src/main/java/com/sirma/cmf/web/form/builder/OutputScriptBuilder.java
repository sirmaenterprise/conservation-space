package com.sirma.cmf.web.form.builder;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;

import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Builder for component that renders javascript in the DOM.
 * 
 * @author svelikov
 */
public class OutputScriptBuilder extends FormBuilder {

	private static final String JAVAX_FACES_RESOURCE_SCRIPT_RENDERER = "javax.faces.resource.Script";

	private static final String JAVAX_FACES_OUTPUT = "javax.faces.Output";

	/**
	 * Instantiates a new output script builder.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public OutputScriptBuilder(LabelProvider labelProvider, CodelistService codelistService) {
		super(labelProvider, codelistService);
	}

	@Override
	public UIComponent getComponentInstance() {
		UIOutput uiComponent = (UIOutput) createComponentInstance(JAVAX_FACES_OUTPUT,
				JAVAX_FACES_RESOURCE_SCRIPT_RENDERER);

		return uiComponent;
	}

}
