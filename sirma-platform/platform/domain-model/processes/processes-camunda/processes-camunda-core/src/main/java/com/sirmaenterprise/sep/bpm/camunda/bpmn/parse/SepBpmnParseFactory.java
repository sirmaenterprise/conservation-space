package com.sirmaenterprise.sep.bpm.camunda.bpmn.parse;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;

/**
 * Factory providing the {@link SepBpmnParse}
 * 
 * @author bbanchev
 */
public class SepBpmnParseFactory implements BpmnParseFactory {
	@Override
	public BpmnParse createBpmnParse(BpmnParser bpmnParser) {
		return new SepBpmnParse(bpmnParser);
	}
}