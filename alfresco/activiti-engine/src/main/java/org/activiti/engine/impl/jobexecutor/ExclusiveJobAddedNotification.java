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
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;

// TODO: Auto-generated Javadoc
/**
 * The Class ExclusiveJobAddedNotification.
 *
 * @author Daniel Meyer
 */
public class ExclusiveJobAddedNotification implements TransactionListener {
    
  /** The job id. */
  protected final String jobId;

  /**
   * Instantiates a new exclusive job added notification.
   *
   * @param jobId the job id
   */
  public ExclusiveJobAddedNotification(String jobId) {
    this.jobId = jobId;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.cfg.TransactionListener#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public void execute(CommandContext commandContext) {
    JobExecutorContext jobExecutorContext = Context.getJobExecutorContext();
    jobExecutorContext.getCurrentProcessorJobQueue().add(jobId);
  }

}
