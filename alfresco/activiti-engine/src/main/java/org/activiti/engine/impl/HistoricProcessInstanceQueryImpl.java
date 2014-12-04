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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.variable.VariableTypes;


// TODO: Auto-generated Javadoc
/**
 * The Class HistoricProcessInstanceQueryImpl.
 *
 * @author Tom Baeyens
 * @author Falko Menge
 */
public class HistoricProcessInstanceQueryImpl extends ExecutionVariableQueryImpl<HistoricProcessInstanceQuery, HistoricProcessInstance> implements HistoricProcessInstanceQuery {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The process instance id. */
  protected String processInstanceId;
  
  /** The process definition id. */
  protected String processDefinitionId;
  
  /** The business key. */
  protected String businessKey;
  
  /** The finished. */
  protected boolean finished = false;
  
  /** The unfinished. */
  protected boolean unfinished = false;
  
  /** The started by. */
  protected String startedBy;
  
  /** The super process instance id. */
  protected String superProcessInstanceId;
  
  /** The process key not in. */
  protected List<String> processKeyNotIn;
  
  /** The started before. */
  protected Date startedBefore;
  
  /** The started after. */
  protected Date startedAfter;
  
  /** The finished before. */
  protected Date finishedBefore;
  
  /** The finished after. */
  protected Date finishedAfter;
  
  /** The process definition key. */
  protected String processDefinitionKey;
  
  /** The process instance ids. */
  protected Set<String> processInstanceIds;
   
  /**
   * Instantiates a new historic process instance query impl.
   */
  public HistoricProcessInstanceQueryImpl() {
  }
  
  /**
   * Instantiates a new historic process instance query impl.
   *
   * @param commandContext the command context
   */
  public HistoricProcessInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }
  
  /**
   * Instantiates a new historic process instance query impl.
   *
   * @param commandExecutor the command executor
   */
  public HistoricProcessInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#processInstanceId(java.lang.String)
   */
  public HistoricProcessInstanceQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#processInstanceIds(java.util.Set)
   */
  public HistoricProcessInstanceQuery processInstanceIds(Set<String> processInstanceIds) {
    if (processInstanceIds == null) {
      throw new ActivitiException("Set of process instance ids is null");
    }
    if (processInstanceIds.isEmpty()) {
      throw new ActivitiException("Set of process instance ids is empty");
    }
    this.processInstanceIds = processInstanceIds;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#processDefinitionId(java.lang.String)
   */
  public HistoricProcessInstanceQueryImpl processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#processDefinitionKey(java.lang.String)
   */
  public HistoricProcessInstanceQuery processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#processInstanceBusinessKey(java.lang.String)
   */
  public HistoricProcessInstanceQuery processInstanceBusinessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#finished()
   */
  public HistoricProcessInstanceQuery finished() {
    this.finished = true;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#unfinished()
   */
  public HistoricProcessInstanceQuery unfinished() {
    this.unfinished = true;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#startedBy(java.lang.String)
   */
  public HistoricProcessInstanceQuery startedBy(String userId) {
    this.startedBy = userId;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#processDefinitionKeyNotIn(java.util.List)
   */
  public HistoricProcessInstanceQuery processDefinitionKeyNotIn(List<String> processDefinitionKeys) {
    this.processKeyNotIn = processDefinitionKeys;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#startedAfter(java.util.Date)
   */
  public HistoricProcessInstanceQuery startedAfter(Date date) {
    startedAfter = date;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#startedBefore(java.util.Date)
   */
  public HistoricProcessInstanceQuery startedBefore(Date date) {
    startedBefore = date;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#finishedAfter(java.util.Date)
   */
  public HistoricProcessInstanceQuery finishedAfter(Date date) {
    finishedAfter = date;
    finished = true;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#finishedBefore(java.util.Date)
   */
  public HistoricProcessInstanceQuery finishedBefore(Date date) {
    finishedBefore = date;
    finished = true;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#superProcessInstanceId(java.lang.String)
   */
  public HistoricProcessInstanceQuery superProcessInstanceId(String superProcessInstanceId) {
	 this.superProcessInstanceId = superProcessInstanceId;
	 return this;
  }
  
	/* (non-Javadoc)
	 * @see org.activiti.engine.history.HistoricProcessInstanceQuery#orderByProcessInstanceBusinessKey()
	 */
	public HistoricProcessInstanceQuery orderByProcessInstanceBusinessKey() {
    return orderBy(HistoricProcessInstanceQueryProperty.BUSINESS_KEY);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#orderByProcessInstanceDuration()
   */
  public HistoricProcessInstanceQuery orderByProcessInstanceDuration() {
    return orderBy(HistoricProcessInstanceQueryProperty.DURATION);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#orderByProcessInstanceStartTime()
   */
  public HistoricProcessInstanceQuery orderByProcessInstanceStartTime() {
    return orderBy(HistoricProcessInstanceQueryProperty.START_TIME);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#orderByProcessInstanceEndTime()
   */
  public HistoricProcessInstanceQuery orderByProcessInstanceEndTime() {
    return orderBy(HistoricProcessInstanceQueryProperty.END_TIME);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#orderByProcessDefinitionId()
   */
  public HistoricProcessInstanceQuery orderByProcessDefinitionId() {
    return orderBy(HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#orderByProcessInstanceId()
   */
  public HistoricProcessInstanceQuery orderByProcessInstanceId() {
    return orderBy(HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.ExecutionVariableQueryImpl#executeCount(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getHistoricProcessInstanceManager()
      .findHistoricProcessInstanceCountByQueryCriteria(this);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.ExecutionVariableQueryImpl#executeList(org.activiti.engine.impl.interceptor.CommandContext, org.activiti.engine.impl.Page)
   */
  public List<HistoricProcessInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getHistoricProcessInstanceManager()
      .findHistoricProcessInstancesByQueryCriteria(this, page);
  }
  
  /**
   * Gets the business key.
   *
   * @return the business key
   */
  public String getBusinessKey() {
    return businessKey;
  }
  
  /**
   * Checks if is open.
   *
   * @return true, if is open
   */
  public boolean isOpen() {
    return unfinished;
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
   * Gets the process definition key.
   *
   * @return the process definition key
   */
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }
  
  /**
   * Gets the process definition id like.
   *
   * @return the process definition id like
   */
  public String getProcessDefinitionIdLike() {
    return processDefinitionKey + ":%:%";
  }
  
  /**
   * Gets the process instance id.
   *
   * @return the process instance id
   */
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  
  /**
   * Gets the process instance ids.
   *
   * @return the process instance ids
   */
  public Set<String> getProcessInstanceIds() {
    return processInstanceIds;
  }
  
  /**
   * Gets the started by.
   *
   * @return the started by
   */
  public String getStartedBy() {
    return startedBy;
  }
  
  /**
   * Gets the super process instance id.
   *
   * @return the super process instance id
   */
  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }
  
  /**
   * Sets the super process instance id.
   *
   * @param superProcessInstanceId the new super process instance id
   */
  public void setSuperProcessInstanceId(String superProcessInstanceId) {
    this.superProcessInstanceId = superProcessInstanceId;
  }
  
  /**
   * Gets the process key not in.
   *
   * @return the process key not in
   */
  public List<String> getProcessKeyNotIn() {
    return processKeyNotIn;
  }
  
  /**
   * Gets the started after.
   *
   * @return the started after
   */
  public Date getStartedAfter() {
    return startedAfter;
  }
  
  /**
   * Gets the started before.
   *
   * @return the started before
   */
  public Date getStartedBefore() {
    return startedBefore;
  }
  
  /**
   * Gets the finished after.
   *
   * @return the finished after
   */
  public Date getFinishedAfter() {
    return finishedAfter;
  }
  
  /**
   * Gets the finished before.
   *
   * @return the finished before
   */
  public Date getFinishedBefore() {
    return finishedBefore;
  }
 
  
 // below is deprecated and to be removed in 5.12
  
  /* (non-Javadoc)
  * @see org.activiti.engine.impl.ExecutionVariableQueryImpl#ensureVariablesInitialized()
  */
 protected void ensureVariablesInitialized() {    
    VariableTypes types = Context.getProcessEngineConfiguration().getVariableTypes();
    for(QueryVariableValue var : queryVariableValues) {
      var.initialize(types);
    }
  }

  /** The start date by. */
  protected Date startDateBy;
  
  /** The start date on. */
  protected Date startDateOn;
  
  /** The finish date by. */
  protected Date finishDateBy;
  
  /** The finish date on. */
  protected Date finishDateOn;
  
  /** The start date on begin. */
  protected Date startDateOnBegin;
  
  /** The start date on end. */
  protected Date startDateOnEnd;
  
  /** The finish date on begin. */
  protected Date finishDateOnBegin;
  
  /** The finish date on end. */
  protected Date finishDateOnEnd;

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#startDateBy(java.util.Date)
   */
  @Deprecated
  public HistoricProcessInstanceQuery startDateBy(Date date) {
    this.startDateBy = this.calculateMidnight(date);;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#startDateOn(java.util.Date)
   */
  @Deprecated
  public HistoricProcessInstanceQuery startDateOn(Date date) {
    this.startDateOn = date;
    this.startDateOnBegin = this.calculateMidnight(date);
    this.startDateOnEnd = this.calculateBeforeMidnight(date);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#finishDateBy(java.util.Date)
   */
  @Deprecated
  public HistoricProcessInstanceQuery finishDateBy(Date date) {
    this.finishDateBy = this.calculateBeforeMidnight(date);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstanceQuery#finishDateOn(java.util.Date)
   */
  @Deprecated
  public HistoricProcessInstanceQuery finishDateOn(Date date) {
    this.finishDateOn = date;
    this.finishDateOnBegin = this.calculateMidnight(date);
    this.finishDateOnEnd = this.calculateBeforeMidnight(date);
    return this;
  }
  
  /**
   * Calculate before midnight.
   *
   * @param date the date
   * @return the date
   */
  @Deprecated
  private Date calculateBeforeMidnight(Date date){
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.DAY_OF_MONTH, 1);
    cal.add(Calendar.SECOND, -1);   
    return cal.getTime();
  }
  
  /**
   * Calculate midnight.
   *
   * @param date the date
   * @return the date
   */
  @Deprecated
  private Date calculateMidnight(Date date){
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.MILLISECOND, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.HOUR, 0);    
    return cal.getTime();
  }
}