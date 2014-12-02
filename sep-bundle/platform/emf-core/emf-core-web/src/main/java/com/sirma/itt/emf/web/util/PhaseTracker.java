/**
 * Copyright (c) 2014 19.06.2014 , Sirma ITT. /* /**
 */
package com.sirma.itt.emf.web.util;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs the time between JSF phases.
 * 
 * @author Adrian Mitev
 */
public class PhaseTracker implements PhaseListener {

	private static final long serialVersionUID = 8303661238536162987L;

	private static final Logger LOGGER = LoggerFactory.getLogger(PhaseTracker.class);

	@Override
	public void beforePhase(PhaseEvent event) {
		LOGGER.info("Before phase: " + event.getPhaseId());
		event.getFacesContext().getExternalContext().getRequestMap()
				.put("PhaseTracker", System.currentTimeMillis());
	}

	@Override
	public void afterPhase(PhaseEvent event) {
		Long time = (Long) event.getFacesContext().getExternalContext().getRequestMap()
				.get("PhaseTracker");
		LOGGER.info("After phase: " + event.getPhaseId() + " "
				+ (System.currentTimeMillis() - time) + " msec");
	}

	@Override
	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

}
