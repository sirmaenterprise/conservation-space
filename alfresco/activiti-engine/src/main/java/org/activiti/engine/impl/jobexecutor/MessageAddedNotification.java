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

import java.util.logging.Logger;

import org.activiti.engine.impl.cfg.TransactionListener;
import org.activiti.engine.impl.interceptor.CommandContext;


// TODO: Auto-generated Javadoc
/**
 * The Class MessageAddedNotification.
 *
 * @author Tom Baeyens
 */
public class MessageAddedNotification implements TransactionListener {
  
  /** The log. */
  private static Logger log = Logger.getLogger(MessageAddedNotification.class.getName());
  
  /** The job executor. */
  protected JobExecutor jobExecutor;
  
  /**
   * Instantiates a new message added notification.
   *
   * @param jobExecutor the job executor
   */
  public MessageAddedNotification(JobExecutor jobExecutor) {
    this.jobExecutor = jobExecutor;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.cfg.TransactionListener#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public void execute(CommandContext commandContext) {
    log.fine("notifying job executor of new job");
    jobExecutor.jobWasAdded();
  }
}
