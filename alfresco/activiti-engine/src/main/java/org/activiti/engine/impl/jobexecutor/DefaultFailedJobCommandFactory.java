package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.impl.cmd.DecrementJobRetriesCmd;
import org.activiti.engine.impl.interceptor.Command;


// TODO: Auto-generated Javadoc
/**
 * A factory for creating DefaultFailedJobCommand objects.
 */
public class DefaultFailedJobCommandFactory implements FailedJobCommandFactory {

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.jobexecutor.FailedJobCommandFactory#getCommand(java.lang.String, java.lang.Throwable)
   */
  public Command<Object> getCommand(String jobId, Throwable exception) {
    return new DecrementJobRetriesCmd(jobId, exception);
  }

}
