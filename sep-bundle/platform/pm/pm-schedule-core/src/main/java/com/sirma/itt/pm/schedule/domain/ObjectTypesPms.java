package com.sirma.itt.pm.schedule.domain;

import com.sirma.itt.pm.domain.ObjectTypesPm;

/**
 * Instance types qualifiers to identify the allowed service implementations.
 * 
 * @author BBonev
 */
public interface ObjectTypesPms extends ObjectTypesPm {

	/** The schedule. */
	String SCHEDULE = "schedule";

	/** The schedule entry. */
	String SCHEDULE_ENTRY = "scheduleEntry";
}
