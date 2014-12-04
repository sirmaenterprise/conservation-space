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

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


// TODO: Auto-generated Javadoc
/**
 * The Class HistoricActivityInstanceQueryImpl.
 *
 * @author Tom Baeyens
 */
public class HistoricActivityInstanceQueryImpl extends AbstractQuery<HistoricActivityInstanceQuery, HistoricActivityInstance> 
    implements HistoricActivityInstanceQuery {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The activity instance id. */
  protected String activityInstanceId;
  
  /** The process instance id. */
  protected String processInstanceId;
  
  /** The execution id. */
  protected String executionId;
  
  /** The process definition id. */
  protected String processDefinitionId;
  
  /** The activity id. */
  protected String activityId;
  
  /** The activity name. */
  protected String activityName;
  
  /** The activity type. */
  protected String activityType;
  
  /** The assignee. */
  protected String assignee;
  
  /** The finished. */
  protected boolean finished;
  
  /** The unfinished. */
  protected boolean unfinished;

  /**
   * Instantiates a new historic activity instance query impl.
   */
  public HistoricActivityInstanceQueryImpl() {
  }
  
  /**
   * Instantiates a new historic activity instance query impl.
   *
   * @param commandContext the command context
   */
  public HistoricActivityInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }
  
  /**
   * Instantiates a new historic activity instance query impl.
   *
   * @param commandExecutor the command executor
   */
  public HistoricActivityInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeCount(org.activiti.engine.impl.interceptor.CommandContext)
   */
  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistoricActivityInstanceManager()
      .findHistoricActivityInstanceCountByQueryCriteria(this);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeList(org.activiti.engine.impl.interceptor.CommandContext, org.activiti.engine.impl.Page)
   */
  @Override
  public List<HistoricActivityInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getHistoricActivityInstanceManager()
      .findHistoricActivityInstancesByQueryCriteria(this, page);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#processInstanceId(java.lang.String)
   */
  public HistoricActivityInstanceQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#executionId(java.lang.String)
   */
  public HistoricActivityInstanceQueryImpl executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#processDefinitionId(java.lang.String)
   */
  public HistoricActivityInstanceQueryImpl processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#activityId(java.lang.String)
   */
  public HistoricActivityInstanceQueryImpl activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#activityName(java.lang.String)
   */
  public HistoricActivityInstanceQueryImpl activityName(String activityName) {
    this.activityName = activityName;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#activityType(java.lang.String)
   */
  public HistoricActivityInstanceQueryImpl activityType(String activityType) {
    this.activityType = activityType;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#taskAssignee(java.lang.String)
   */
  public HistoricActivityInstanceQueryImpl taskAssignee(String assignee) {
    this.assignee = assignee;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#finished()
   */
  public HistoricActivityInstanceQueryImpl finished() {
    this.finished = true;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#unfinished()
   */
  public HistoricActivityInstanceQueryImpl unfinished() {
    this.unfinished = true;
    return this;
  }

  // ordering /////////////////////////////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#orderByHistoricActivityInstanceDuration()
   */
  public HistoricActivityInstanceQueryImpl orderByHistoricActivityInstanceDuration() {
    orderBy(HistoricActivityInstanceQueryProperty.DURATION);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#orderByHistoricActivityInstanceEndTime()
   */
  public HistoricActivityInstanceQueryImpl orderByHistoricActivityInstanceEndTime() {
    orderBy(HistoricActivityInstanceQueryProperty.END);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#orderByExecutionId()
   */
  public HistoricActivityInstanceQueryImpl orderByExecutionId() {
    orderBy(HistoricActivityInstanceQueryProperty.EXECUTION_ID);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#orderByHistoricActivityInstanceId()
   */
  public HistoricActivityInstanceQueryImpl orderByHistoricActivityInstanceId() {
    orderBy(HistoricActivityInstanceQueryProperty.HISTORIC_ACTIVITY_INSTANCE_ID);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#orderByProcessDefinitionId()
   */
  public HistoricActivityInstanceQueryImpl orderByProcessDefinitionId() {
    orderBy(HistoricActivityInstanceQueryProperty.PROCESS_DEFINITION_ID);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#orderByProcessInstanceId()
   */
  public HistoricActivityInstanceQueryImpl orderByProcessInstanceId() {
    orderBy(HistoricActivityInstanceQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#orderByHistoricActivityInstanceStartTime()
   */
  public HistoricActivityInstanceQueryImpl orderByHistoricActivityInstanceStartTime() {
    orderBy(HistoricActivityInstanceQueryProperty.START);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#orderByActivityId()
   */
  public HistoricActivityInstanceQuery orderByActivityId() {
    orderBy(HistoricActivityInstanceQueryProperty.ACTIVITY_ID);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#orderByActivityName()
   */
  public HistoricActivityInstanceQueryImpl orderByActivityName() {
    orderBy(HistoricActivityInstanceQueryProperty.ACTIVITY_NAME);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#orderByActivityType()
   */
  public HistoricActivityInstanceQueryImpl orderByActivityType() {
    orderBy(HistoricActivityInstanceQueryProperty.ACTIVITY_TYPE);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstanceQuery#activityInstanceId(java.lang.String)
   */
  public HistoricActivityInstanceQueryImpl activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
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
   * Gets the execution id.
   *
   * @return the execution id
   */
  public String getExecutionId() {
    return executionId;
  }
  
  /**
   * Gets the process definition id.
   *
   * @return the process definition id
   */
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  
  /**
   * Gets the activity id.
   *
   * @return the activity id
   */
  public String getActivityId() {
    return activityId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#getOrderBy()
   */
  public String getOrderBy() {
    return orderBy;
  }
  
  /**
   * Gets the activity name.
   *
   * @return the activity name
   */
  public String getActivityName() {
    return activityName;
  }
  
  /**
   * Gets the activity type.
   *
   * @return the activity type
   */
  public String getActivityType() {
    return activityType;
  }
  
  /**
   * Gets the assignee.
   *
   * @return the assignee
   */
  public String getAssignee() {
    return assignee;
  }
  
  /**
   * Checks if is finished.
   *
   * @return true, if is finished
   */
  public boolean isFinished() {
    return finished;
  }
  
  /**
   * Checks if is unfinished.
   *
   * @return true, if is unfinished
   */
  public boolean isUnfinished() {
    return unfinished;
  }
  
  /**
   * Gets the activity instance id.
   *
   * @return the activity instance id
   */
  public String getActivityInstanceId() {
    return activityInstanceId;
  }
}
