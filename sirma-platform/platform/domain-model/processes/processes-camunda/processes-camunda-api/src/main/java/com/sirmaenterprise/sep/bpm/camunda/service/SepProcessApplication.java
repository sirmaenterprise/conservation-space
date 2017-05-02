package com.sirmaenterprise.sep.bpm.camunda.service;

import javax.enterprise.event.Observes;

import org.camunda.bpm.application.ProcessApplicationInterface;

import com.sirma.itt.seip.definition.event.LoadAllDefinitions;

/**
 * The {@link SepProcessApplication} is extension to the default ejb implementation that also has a business method for
 * model reloading.
 *
 * @author bbanchev
 */
public interface SepProcessApplication extends ProcessApplicationInterface {

	/**
	 * Load all models trigger. Models (bpmn,cmmn,dmn) are loaded from the registered source.
	 *
	 * @param event
	 *            the operation trigger
	 */
	void loadAllDefinitions(@Observes LoadAllDefinitions event);
}
