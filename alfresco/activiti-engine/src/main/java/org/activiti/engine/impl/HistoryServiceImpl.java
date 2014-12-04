/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricDetailQuery;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.cmd.DeleteHistoricProcessInstanceCmd;
import org.activiti.engine.impl.cmd.DeleteHistoricTaskInstanceCmd;

// TODO: Auto-generated Javadoc
/**
 * The Class HistoryServiceImpl.
 *
 * @author Tom Baeyens
 * @author Christian Stettler
 */
public class HistoryServiceImpl extends ServiceImpl implements HistoryService {

  /* (non-Javadoc)
   * @see org.activiti.engine.HistoryService#createHistoricProcessInstanceQuery()
   */
  public HistoricProcessInstanceQuery createHistoricProcessInstanceQuery() {
    return new HistoricProcessInstanceQueryImpl(commandExecutor);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.HistoryService#createHistoricActivityInstanceQuery()
   */
  public HistoricActivityInstanceQuery createHistoricActivityInstanceQuery() {
    return new HistoricActivityInstanceQueryImpl(commandExecutor);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.HistoryService#createHistoricTaskInstanceQuery()
   */
  public HistoricTaskInstanceQuery createHistoricTaskInstanceQuery() {
    return new HistoricTaskInstanceQueryImpl(commandExecutor);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.HistoryService#createHistoricDetailQuery()
   */
  public HistoricDetailQuery createHistoricDetailQuery() {
    return new HistoricDetailQueryImpl(commandExecutor);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.HistoryService#deleteHistoricTaskInstance(java.lang.String)
   */
  public void deleteHistoricTaskInstance(String taskId) {
    commandExecutor.execute(new DeleteHistoricTaskInstanceCmd(taskId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.HistoryService#deleteHistoricProcessInstance(java.lang.String)
   */
  public void deleteHistoricProcessInstance(String processInstanceId) {
    commandExecutor.execute(new DeleteHistoricProcessInstanceCmd(processInstanceId));
  }
}
