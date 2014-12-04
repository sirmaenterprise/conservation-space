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
package org.activiti.engine.impl.persistence.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.runtime.Job;

// TODO: Auto-generated Javadoc
/**
 * Stub of the common parts of a Job. You will normally work with a subclass of
 * JobEntity, such as {@link TimerEntity} or {@link MessageEntity}.
 *
 * @author Tom Baeyens
 * @author Nick Burch
 * @author Dave Syer
 * @author Frederik Heremans
 */
public abstract class JobEntity implements Serializable, Job, PersistentObject {

  /** The Constant DEFAULT_EXCLUSIVE. */
  public static final boolean DEFAULT_EXCLUSIVE = true;
  
  /** The Constant DEFAULT_RETRIES. */
  public static final int DEFAULT_RETRIES = 3;
  
  /** The Constant MAX_EXCEPTION_MESSAGE_LENGTH. */
  private static final int MAX_EXCEPTION_MESSAGE_LENGTH = 255;

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The id. */
  protected String id;
  
  /** The revision. */
  protected int revision;

  /** The duedate. */
  protected Date duedate;

  /** The lock owner. */
  protected String lockOwner = null;
  
  /** The lock expiration time. */
  protected Date lockExpirationTime = null;

  /** The execution id. */
  protected String executionId = null;
  
  /** The process instance id. */
  protected String processInstanceId = null;

  /** The is exclusive. */
  protected boolean isExclusive = DEFAULT_EXCLUSIVE;

  /** The retries. */
  protected int retries = DEFAULT_RETRIES;

  /** The job handler type. */
  protected String jobHandlerType = null;
  
  /** The job handler configuration. */
  protected String jobHandlerConfiguration = null;
  
  /** The exception byte array. */
  protected ByteArrayEntity exceptionByteArray;
  
  /** The exception byte array id. */
  protected String exceptionByteArrayId;
  
  /** The exception message. */
  protected String exceptionMessage;

  /**
   * Execute.
   *
   * @param commandContext the command context
   */
  public void execute(CommandContext commandContext) {
    ExecutionEntity execution = null;
    if (executionId != null) {
      execution = commandContext.getExecutionManager().findExecutionById(executionId);
    }

    Map<String, JobHandler> jobHandlers = Context.getProcessEngineConfiguration().getJobHandlers();
    JobHandler jobHandler = jobHandlers.get(jobHandlerType);

    jobHandler.execute(jobHandlerConfiguration, execution, commandContext);
  }
  
  /**
   * Insert.
   */
  public void insert() {
    DbSqlSession dbSqlSession = Context
      .getCommandContext()
      .getDbSqlSession();
    
    dbSqlSession.insert(this);
    
    // add link to execution
    if(executionId != null) {
      ExecutionEntity execution = Context.getCommandContext()
        .getExecutionManager()
        .findExecutionById(executionId);
      execution.addJob(this);
    }
  }
  
  /**
   * Delete.
   */
  public void delete() {
    DbSqlSession dbSqlSession = Context
      .getCommandContext()
      .getDbSqlSession();

    dbSqlSession.delete(getClass(), id);

    // Also delete the job's exception byte array
    if (exceptionByteArrayId != null) {
      dbSqlSession.delete(ByteArrayEntity.class, exceptionByteArrayId);
    }
    
    // remove link to execution
    if(executionId != null) {
      ExecutionEntity execution = Context.getCommandContext()
        .getExecutionManager()
        .findExecutionById(executionId);
      execution.removeJob(this);
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("lockOwner", lockOwner);
    persistentState.put("lockExpirationTime", lockExpirationTime);
    persistentState.put("retries", retries);
    persistentState.put("duedate", duedate);
    persistentState.put("exceptionMessage", exceptionMessage);
    if(exceptionByteArrayId != null) {
      persistentState.put("exceptionByteArrayId", exceptionByteArrayId);      
    }
    return persistentState;
  }
  
  /**
   * Gets the revision next.
   *
   * @return the revision next
   */
  public int getRevisionNext() {
    return revision+1;
  }

  /**
   * Sets the execution.
   *
   * @param execution the new execution
   */
  public void setExecution(ExecutionEntity execution) {
    executionId = execution.getId();
    processInstanceId = execution.getProcessInstanceId();
    execution.addJob(this);
  }

  // getters and setters //////////////////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.Job#getExecutionId()
   */
  public String getExecutionId() {
    return executionId;
  }
  
  /**
   * Sets the execution id.
   *
   * @param executionId the new execution id
   */
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.Job#getRetries()
   */
  public int getRetries() {
    return retries;
  }
  
  /**
   * Sets the retries.
   *
   * @param retries the new retries
   */
  public void setRetries(int retries) {
    this.retries = retries;
  }
  
  /**
   * Gets the exception stacktrace.
   *
   * @return the exception stacktrace
   */
  public String getExceptionStacktrace() {
    String exception = null;
    ByteArrayEntity byteArray = getExceptionByteArray();
    if(byteArray != null) {
      try {
        exception = new String(byteArray.getBytes(), "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new ActivitiException("UTF-8 is not a supported encoding");
      }
    }
    return exception;
  }
  
  /**
   * Gets the lock owner.
   *
   * @return the lock owner
   */
  public String getLockOwner() {
    return lockOwner;
  }
  
  /**
   * Sets the lock owner.
   *
   * @param claimedBy the new lock owner
   */
  public void setLockOwner(String claimedBy) {
    this.lockOwner = claimedBy;
  }
  
  /**
   * Gets the lock expiration time.
   *
   * @return the lock expiration time
   */
  public Date getLockExpirationTime() {
    return lockExpirationTime;
  }
  
  /**
   * Sets the lock expiration time.
   *
   * @param claimedUntil the new lock expiration time
   */
  public void setLockExpirationTime(Date claimedUntil) {
    this.lockExpirationTime = claimedUntil;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.Job#getProcessInstanceId()
   */
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  
  /**
   * Sets the process instance id.
   *
   * @param processInstanceId the new process instance id
   */
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  
  /**
   * Checks if is exclusive.
   *
   * @return true, if is exclusive
   */
  public boolean isExclusive() {
    return isExclusive;
  }
  
  /**
   * Sets the exclusive.
   *
   * @param isExclusive the new exclusive
   */
  public void setExclusive(boolean isExclusive) {
    this.isExclusive = isExclusive;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.Job#getId()
   */
  public String getId() {
    return id;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#setId(java.lang.String)
   */
  public void setId(String id) {
    this.id = id;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.Job#getDuedate()
   */
  public Date getDuedate() {
    return duedate;
  }
  
  /**
   * Sets the duedate.
   *
   * @param duedate the new duedate
   */
  public void setDuedate(Date duedate) {
    this.duedate = duedate;
  }
  
  /**
   * Sets the exception stacktrace.
   *
   * @param exception the new exception stacktrace
   */
  public void setExceptionStacktrace(String exception) {
    byte[] exceptionBytes = null;
    if(exception == null) {
      exceptionBytes = null;      
    } else {
      
      try {
        exceptionBytes = exception.getBytes("UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new ActivitiException("UTF-8 is not a supported encoding");
      }
    }   
    
    ByteArrayEntity byteArray = getExceptionByteArray();
    if(byteArray == null) {
      byteArray = new ByteArrayEntity("job.exceptionByteArray", exceptionBytes);
      Context
        .getCommandContext()
        .getDbSqlSession()
        .insert(byteArray);
      exceptionByteArrayId = byteArray.getId();
      exceptionByteArray = byteArray;
    } else {
      byteArray.setBytes(exceptionBytes);
    }
  }
  
  /**
   * Gets the job handler type.
   *
   * @return the job handler type
   */
  public String getJobHandlerType() {
    return jobHandlerType;
  }
  
  /**
   * Sets the job handler type.
   *
   * @param jobHandlerType the new job handler type
   */
  public void setJobHandlerType(String jobHandlerType) {
    this.jobHandlerType = jobHandlerType;
  }
  
  /**
   * Gets the job handler configuration.
   *
   * @return the job handler configuration
   */
  public String getJobHandlerConfiguration() {
    return jobHandlerConfiguration;
  }
  
  /**
   * Sets the job handler configuration.
   *
   * @param jobHandlerConfiguration the new job handler configuration
   */
  public void setJobHandlerConfiguration(String jobHandlerConfiguration) {
    this.jobHandlerConfiguration = jobHandlerConfiguration;
  }
  
  /**
   * Gets the revision.
   *
   * @return the revision
   */
  public int getRevision() {
    return revision;
  }
  
  /**
   * Sets the revision.
   *
   * @param revision the new revision
   */
  public void setRevision(int revision) {
    this.revision = revision;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.Job#getExceptionMessage()
   */
  public String getExceptionMessage() {
    return exceptionMessage;
  }

  /**
   * Sets the exception message.
   *
   * @param exceptionMessage the new exception message
   */
  public void setExceptionMessage(String exceptionMessage) {
    if(exceptionMessage != null && exceptionMessage.length() > MAX_EXCEPTION_MESSAGE_LENGTH) {
      this.exceptionMessage = exceptionMessage.substring(0, MAX_EXCEPTION_MESSAGE_LENGTH);
    } else {
      this.exceptionMessage = exceptionMessage;      
    }
  }
  
  /**
   * Gets the exception byte array id.
   *
   * @return the exception byte array id
   */
  public String getExceptionByteArrayId() {
    return exceptionByteArrayId;
  }

  /**
   * Gets the exception byte array.
   *
   * @return the exception byte array
   */
  private ByteArrayEntity getExceptionByteArray() {
    if ((exceptionByteArray == null) && (exceptionByteArrayId != null)) {
      exceptionByteArray = Context
        .getCommandContext()
        .getDbSqlSession()
        .selectById(ByteArrayEntity.class, exceptionByteArrayId);
    }
    return exceptionByteArray;
  }
}
