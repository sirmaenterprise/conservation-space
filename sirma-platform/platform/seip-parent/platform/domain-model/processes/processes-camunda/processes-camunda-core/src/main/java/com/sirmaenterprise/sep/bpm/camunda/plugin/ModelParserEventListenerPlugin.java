package com.sirmaenterprise.sep.bpm.camunda.plugin;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.transformer.CmmnTransformListener;

/**
 * Plugin to register additional custom listeners for BPMN, CMMN.
 * 
 * @author bbanchev
 */
public class ModelParserEventListenerPlugin extends AbstractProcessEnginePlugin {

	private Collection<BpmnParseListener> preBpmnParseListener;
	private Collection<CmmnTransformListener> preCmmnTransformListeners;

	/**
	 * @param preBpmnParseListener
	 *            is a list of pre bpmn parsing listeners to add. Might be null or empty
	 * @param preCmmnTransformListeners
	 *            is a list of pre cmmn transform listeners to add. Might be null or empty
	 */
	public ModelParserEventListenerPlugin(Collection<BpmnParseListener> preBpmnParseListener,
			Collection<CmmnTransformListener> preCmmnTransformListeners) {
		this.preBpmnParseListener = preBpmnParseListener;
		this.preCmmnTransformListeners = preCmmnTransformListeners;
	}

	@Override
	public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
		// bpmn
		List<BpmnParseListener> preParseListeners = processEngineConfiguration.getCustomPreBPMNParseListeners();
		if (preParseListeners == null) {
			preParseListeners = new LinkedList<>();
			processEngineConfiguration.setCustomPreBPMNParseListeners(preParseListeners);
		}
		if (preBpmnParseListener != null) {
			preParseListeners.addAll(preBpmnParseListener);
		}
		// cmmn
		List<CmmnTransformListener> cmmnListeners = processEngineConfiguration.getCustomPreCmmnTransformListeners();
		if (cmmnListeners == null) {
			cmmnListeners = new LinkedList<>();
			processEngineConfiguration.setCustomPreCmmnTransformListeners(cmmnListeners);
		}
		if (preCmmnTransformListeners != null) {
			cmmnListeners.addAll(preCmmnTransformListeners);
		}
	}

}
