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

package org.activiti.engine.impl;

import java.util.List;

import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricDetailQuery;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


// TODO: Auto-generated Javadoc
/**
 * The Class HistoricDetailQueryImpl.
 *
 * @author Tom Baeyens
 */
public class HistoricDetailQueryImpl extends AbstractQuery<HistoricDetailQuery, HistoricDetail> implements HistoricDetailQuery {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The task id. */
  protected String taskId;
  
  /** The process instance id. */
  protected String processInstanceId;
  
  /** The activity id. */
  protected String activityId;
  
  /** The activity instance id. */
  protected String activityInstanceId;
  
  /** The type. */
  protected String type;
  
  /** The exclude task related. */
  protected boolean excludeTaskRelated = false;

  /**
   * Instantiates a new historic detail query impl.
   */
  public HistoricDetailQueryImpl() {
  }

  /**
   * Instantiates a new historic detail query impl.
   *
   * @param commandContext the command context
   */
  public HistoricDetailQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  /**
   * Instantiates a new historic detail query impl.
   *
   * @param commandExecutor the command executor
   */
  public HistoricDetailQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetailQuery#processInstanceId(java.lang.String)
   */
  public HistoricDetailQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetailQuery#activityId(java.lang.String)
   */
  public HistoricDetailQuery activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetailQuery#activityInstanceId(java.lang.String)
   */
  public HistoricDetailQuery activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetailQuery#taskId(java.lang.String)
   */
  public HistoricDetailQuery taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetailQuery#formProperties()
   */
  public HistoricDetailQuery formProperties() {
    this.type = "FormProperty";
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetailQuery#variableUpdates()
   */
  public HistoricDetailQuery variableUpdates() {
    this.type = "VariableUpdate";
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetailQuery#excludeTaskDetails()
   */
  public HistoricDetailQuery excludeTaskDetails() {
    this.excludeTaskRelated = true;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeCount(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistoricDetailManager()
      .findHistoricDetailCountByQueryCriteria(this);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeList(org.activiti.engine.impl.interceptor.CommandContext, org.activiti.engine.impl.Page)
   */
  public List<HistoricDetail> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getHistoricDetailManager()
      .findHistoricDetailsByQueryCriteria(this, page);
  }
  
  // order by /////////////////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetailQuery#orderByProcessInstanceId()
   */
  public HistoricDetailQuery orderByProcessInstanceId() {
    orderBy(HistoricDetailQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetailQuery#orderByTime()
   */
  public HistoricDetailQuery orderByTime() {
    orderBy(HistoricDetailQueryProperty.TIME);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetailQuery#orderByVariableName()
   */
  public HistoricDetailQuery orderByVariableName() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_NAME);
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetailQuery#orderByFormPropertyId()
   */
  public HistoricDetailQuery orderByFormPropertyId() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_NAME);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetailQuery#orderByVariableRevision()
   */
  public HistoricDetailQuery orderByVariableRevision() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_REVISION);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetailQuery#orderByVariableType()
   */
  public HistoricDetailQuery orderByVariableType() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_TYPE);
    return this;
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  /**
   * Gets the process instance id.
   *
   * @return the process instance id
   */
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  
  /**
   * Gets the task id.
   *
   * @return the task id
   */
  public String getTaskId() {
    return taskId;
  }
  
  /**
   * Gets the activity id.
   *
   * @return the activity id
   */
  public String getActivityId() {
    return activityId;
  }
  
  /**
   * Gets the type.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }
  
  /**
   * Gets the exclude task related.
   *
   * @return the exclude task related
   */
  public boolean getExcludeTaskRelated() {
    return excludeTaskRelated;
  }
}
