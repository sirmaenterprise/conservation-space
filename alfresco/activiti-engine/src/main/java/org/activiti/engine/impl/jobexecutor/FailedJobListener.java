/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.impl.cfg.TransactionListener;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving failedJob events.
 * The class that is interested in processing a failedJob
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addFailedJobListener<code> method. When
 * the failedJob event occurs, that object's appropriate
 * method is invoked.
 *
 * @author Frederik Heremans
 */
public class FailedJobListener implements TransactionListener {

  /** The command executor. */
  protected CommandExecutor commandExecutor;
  
  /** The job id. */
  protected String jobId;
  
  /** The exception. */
  protected Throwable exception;

  /**
   * Instantiates a new failed job listener.
   *
   * @param commandExecutor the command executor
   * @param jobId the job id
   * @param exception the exception
   */
  public FailedJobListener(CommandExecutor commandExecutor, String jobId, Throwable exception) {
    this.commandExecutor = commandExecutor;
    this.jobId = jobId;
    this.exception = exception;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.cfg.TransactionListener#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public void execute(CommandContext commandContext) {
    commandExecutor.execute(commandContext.getFailedJobCommandFactory()
                                          .getCommand(jobId, exception));
  }

}
