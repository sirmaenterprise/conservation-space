package com.sirmaenterprise.sep.bpm.camunda.rest.activator;

import javax.ws.rs.ApplicationPath;

import org.camunda.bpm.tasklist.impl.web.TasklistApplication;

/**
 * The {@link TasklistActivator} activates the Camunda Tasklist rest services.
 *
 * @author bbanchev
 */
@ApplicationPath("/api/tasklist")
public class TasklistActivator extends TasklistApplication {
	// no custom implementation
}
