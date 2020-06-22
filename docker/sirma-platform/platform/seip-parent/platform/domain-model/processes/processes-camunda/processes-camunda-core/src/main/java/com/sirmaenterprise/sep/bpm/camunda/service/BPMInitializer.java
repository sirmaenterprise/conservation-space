package com.sirmaenterprise.sep.bpm.camunda.service;

import javax.ejb.EJB;
import javax.inject.Singleton;

import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.tasks.TransactionMode;

/**
 * Triggers Camunda Engine initialization at the desired {@link StartupPhase}.
 * 
 * @author bbanchev
 */
@Singleton
public class BPMInitializer {

	@EJB
	private SepProcessApplication bpmApplication;

	@Startup(phase = StartupPhase.AFTER_APP_START, async = true, transactionMode = TransactionMode.NOT_SUPPORTED)
	protected void initialize() {
		// instantiate the singleton
		bpmApplication.getName();
	}

}
