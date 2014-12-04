package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.impl.interceptor.Command;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating FailedJobCommand objects.
 */
public interface FailedJobCommandFactory {
	
	/**
	 * Gets the command.
	 *
	 * @param jobId the job id
	 * @param exception the exception
	 * @return the command
	 */
	public Command<Object> getCommand(String jobId, Throwable exception);

}
