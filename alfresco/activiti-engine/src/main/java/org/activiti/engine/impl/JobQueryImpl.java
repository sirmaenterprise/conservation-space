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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;


// TODO: Auto-generated Javadoc
/**
 * The Class JobQueryImpl.
 *
 * @author Joram Barrez
 * @author Tom Baeyens
 * @author Falko Menge
 */
public class JobQueryImpl extends AbstractQuery<JobQuery, Job> implements JobQuery, Serializable {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The id. */
  protected String id;
  
  /** The process instance id. */
  protected String processInstanceId;
  
  /** The execution id. */
  protected String executionId;
  
  /** The retries left. */
  protected boolean retriesLeft;
  
  /** The executable. */
  protected boolean executable;
  
  /** The only timers. */
  protected boolean onlyTimers;
  
  /** The only messages. */
  protected boolean onlyMessages;
  
  /** The duedate higher then. */
  protected Date duedateHigherThen;
  
  /** The duedate lower then. */
  protected Date duedateLowerThen;
  
  /** The duedate higher then or equal. */
  protected Date duedateHigherThenOrEqual;
  
  /** The duedate lower then or equal. */
  protected Date duedateLowerThenOrEqual;
  
  /** The with exception. */
  protected boolean withException;
  
  /** The exception message. */
  protected String exceptionMessage;
  
  /**
   * Instantiates a new job query impl.
   */
  public JobQueryImpl() {
  }

  /**
   * Instantiates a new job query impl.
   *
   * @param commandContext the command context
   */
  public JobQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  /**
   * Instantiates a new job query impl.
   *
   * @param commandExecutor the command executor
   */
  public JobQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#jobId(java.lang.String)
   */
  public JobQuery jobId(String jobId) {
    if (jobId == null) {
      throw new ActivitiException("Provided job id is null");
    }
    this.id = jobId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#processInstanceId(java.lang.String)
   */
  public JobQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ActivitiException("Provided process instance id is null");
    }
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#executionId(java.lang.String)
   */
  public JobQueryImpl executionId(String executionId) {
    if (executionId == null) {
      throw new ActivitiException("Provided execution id is null");
    }
    this.executionId = executionId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#withRetriesLeft()
   */
  public JobQuery withRetriesLeft() {
    retriesLeft = true;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#executable()
   */
  public JobQuery executable() {
    executable = true;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#timers()
   */
  public JobQuery timers() {
    if (onlyMessages) {
      throw new ActivitiException("Cannot combine onlyTimers() with onlyMessages() in the same query");
    }
    this.onlyTimers = true;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#messages()
   */
  public JobQuery messages() {
    if (onlyTimers) {
      throw new ActivitiException("Cannot combine onlyTimers() with onlyMessages() in the same query");
    }
    this.onlyMessages = true;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#duedateHigherThen(java.util.Date)
   */
  public JobQuery duedateHigherThen(Date date) {
    if (date == null) {
      throw new ActivitiException("Provided date is null");
    }
    this.duedateHigherThen = date;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#duedateHigherThenOrEquals(java.util.Date)
   */
  public JobQuery duedateHigherThenOrEquals(Date date) {
    if (date == null) {
      throw new ActivitiException("Provided date is null");
    }
    this.duedateHigherThenOrEqual = date;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#duedateLowerThen(java.util.Date)
   */
  public JobQuery duedateLowerThen(Date date) {
    if (date == null) {
      throw new ActivitiException("Provided date is null");
    }
    this.duedateLowerThen = date;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#duedateLowerThenOrEquals(java.util.Date)
   */
  public JobQuery duedateLowerThenOrEquals(Date date) {
    if (date == null) {
      throw new ActivitiException("Provided date is null");
    }
    this.duedateLowerThenOrEqual = date;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#withException()
   */
  public JobQuery withException() {
    this.withException = true;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#exceptionMessage(java.lang.String)
   */
  public JobQuery exceptionMessage(String exceptionMessage) {
    if (exceptionMessage == null) {
      throw new ActivitiException("Provided exception message is null");
    }
    this.exceptionMessage = exceptionMessage;
    return this;
  }
  
  //sorting //////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#orderByJobDuedate()
   */
  public JobQuery orderByJobDuedate() {
    return orderBy(JobQueryProperty.DUEDATE);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#orderByExecutionId()
   */
  public JobQuery orderByExecutionId() {
    return orderBy(JobQueryProperty.EXECUTION_ID);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#orderByJobId()
   */
  public JobQuery orderByJobId() {
    return orderBy(JobQueryProperty.JOB_ID);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#orderByProcessInstanceId()
   */
  public JobQuery orderByProcessInstanceId() {
    return orderBy(JobQueryProperty.PROCESS_INSTANCE_ID);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.JobQuery#orderByJobRetries()
   */
  public JobQuery orderByJobRetries() {
    return orderBy(JobQueryProperty.RETRIES);
  }
  
  //results //////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeCount(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getJobManager()
      .findJobCountByQueryCriteria(this);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeList(org.activiti.engine.impl.interceptor.CommandContext, org.activiti.engine.impl.Page)
   */
  public List<Job> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getJobManager()
      .findJobsByQueryCriteria(this, page);
  }
  
  //getters //////////////////////////////////////////
  
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
   * Gets the retries left.
   *
   * @return the retries left
   */
  public boolean getRetriesLeft() {
    return retriesLeft;
  }
  
  /**
   * Gets the executable.
   *
   * @return the executable
   */
  public boolean getExecutable() {
    return executable;
  }
  
  /**
   * Gets the now.
   *
   * @return the now
   */
  public Date getNow() {
    return ClockUtil.getCurrentTime();
  }
  
  /**
   * Checks if is with exception.
   *
   * @return true, if is with exception
   */
  public boolean isWithException() {
    return withException;
  }
  
  /**
   * Gets the exception message.
   *
   * @return the exception message
   */
  public String getExceptionMessage() {
    return exceptionMessage;
  }
}
