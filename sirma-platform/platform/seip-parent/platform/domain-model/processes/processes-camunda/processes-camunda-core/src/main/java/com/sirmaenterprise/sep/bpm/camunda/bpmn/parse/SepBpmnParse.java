package com.sirmaenterprise.sep.bpm.camunda.bpmn.parse;

import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.core.variable.mapping.InputParameter;
import org.camunda.bpm.engine.impl.core.variable.mapping.IoMapping;
import org.camunda.bpm.engine.impl.core.variable.mapping.OutputParameter;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.util.xml.Problem;

/**
 * Extends the default {@link BpmnParse} by adding additional auto populated elements
 * 
 * @author bbanchev
 */
public class SepBpmnParse extends BpmnParse {

	private IoMapping inputOutput = new IoMapping();

	/**
	 * Instantiate the {@link BpmnParse} extension by invoking the delegated constructor
	 * {@link BpmnParse#BpmnParse(BpmnParser)}
	 * 
	 * @param parser
	 *            to set
	 */
	public SepBpmnParse(BpmnParser parser) {
		super(parser);
		// dummy instance for the check
		inputOutput.addInputParameter(new InputParameter(null, null));
		inputOutput.addOutputParameter(new OutputParameter(null, null));
	}

	/**
	 * Append IO mapping for each supported activity to force creation of local execution context for those
	 * activities<br>
	 */
	@Override
	protected void parseActivityInputOutput(Element activityElement, ActivityImpl activity) {
		int errorsBeforeCheck = getProblems().size();
		checkActivityInputOutputSupported(activityElement, activity, inputOutput);
		if (getProblems().size() == errorsBeforeCheck) {
			Element extensions = activityElement.element("extensionElements");
			if (extensions == null) {
				extensions = new Element(null, "extensionElements", "extensionElements", null, null);
				activityElement.add(extensions);
			}
			Element inputOutputElement = extensions.elementNS(BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS, "inputOutput");
			if (inputOutputElement == null) {
				extensions.add(new Element(BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS.getNamespaceUri(), "inputOutput", null,
						null, null));
			}
		} else {
			cleanUpCheckErros(errorsBeforeCheck);
		}
		// keep invoke original code
		super.parseActivityInputOutput(activityElement, activity);
	}

	private void cleanUpCheckErros(int previousSize) {
		List<Problem> problems = getProblems();
		while (problems.size() != previousSize) {
			problems.remove(problems.size() - 1);
		}
	}

}
