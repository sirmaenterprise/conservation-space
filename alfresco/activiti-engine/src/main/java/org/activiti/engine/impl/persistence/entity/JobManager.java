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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.TransactionListener;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.jobexecutor.ExclusiveJobAddedNotification;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.jobexecutor.JobExecutorContext;
import org.activiti.engine.impl.jobexecutor.MessageAddedNotification;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.Job;


// TODO: Auto-generated Javadoc
/**
 * The Class JobManager.
 *
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class JobManager extends AbstractManager {

  /**
   * Send.
   *
   * @param message the message
   */
  public void send(MessageEntity message) {
    message.insert();
    hintJobExecutor(message);    
  }
 
  /**
   * Schedule.
   *
   * @param timer the timer
   */
  public void schedule(TimerEntity timer) {
    Date duedate = timer.getDuedate();
    if (duedate==null) {
      throw new ActivitiException("duedate is null");
    }

    timer.insert();
    
    // Check if this timer fires before the next time the job executor will check for new timers to fire.
    // This is highly unlikely because normally waitTimeInMillis is 5000 (5 seconds)
    // and timers are usually set further in the future
    
    JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();
    int waitTimeInMillis = jobExecutor.getWaitTimeInMillis();
    if (duedate.getTime() < (ClockUtil.getCurrentTime().getTime()+waitTimeInMillis)) {
      hintJobExecutor(timer);
    }
  }
  
  /**
   * Hint job executor.
   *
   * @param job the job
   */
  protected void hintJobExecutor(JobEntity job) {  
    JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();
    JobExecutorContext jobExecutorContext = Context.getJobExecutorContext();
    TransactionListener transactionListener = null;
    if(job.isExclusive() 
            && jobExecutorContext != null 
            && jobExecutorContext.isExecutingExclusiveJob()) {
      // lock job & add to the queue of the current processor
      Date currentTime = ClockUtil.getCurrentTime();
      job.setLockExpirationTime(new Date(currentTime.getTime() + jobExecutor.getLockTimeInMillis()));
      job.setLockOwner(jobExecutor.getLockOwner());
      transactionListener = new ExclusiveJobAddedNotification(job.getId());      
    } else {
      // notify job executor:      
      transactionListener = new MessageAddedNotification(jobExecutor);
    }
    Context.getCommandContext()
    .getTransactionContext()
    .addTransactionListener(TransactionState.COMMITTED, transactionListener);
  }
 
  /**
   * Cancel timers.
   *
   * @param execution the execution
   */
  public void cancelTimers(ExecutionEntity execution) {
    List<TimerEntity> timers = Context
      .getCommandContext()
      .getJobManager()
      .findTimersByExecutionId(execution.getId());
    
    for (TimerEntity timer: timers) {
      timer.delete();
    }
  }

  /**
   * Find job by id.
   *
   * @param jobId the job id
   * @return the job entity
   */
  public JobEntity findJobById(String jobId) {
    return (JobEntity) getDbSqlSession().selectOne("selectJob", jobId);
  }
  
  /**
   * Find next jobs to execute.
   *
   * @param page the page
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<JobEntity> findNextJobsToExecute(Page page) {
    Date now = ClockUtil.getCurrentTime();
    return getDbSqlSession().selectList("selectNextJobsToExecute", now, page);
  }
  
  /**
   * Find jobs by execution id.
   *
   * @param executionId the execution id
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<Job> findJobsByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectJobsByExecutionId", executionId);
  }
  
  /**
   * Find exclusive jobs to execute.
   *
   * @param processInstanceId the process instance id
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<JobEntity> findExclusiveJobsToExecute(String processInstanceId) {
    Map<String,Object> params = new HashMap<String, Object>();
    params.put("pid", processInstanceId);
    params.put("now",ClockUtil.getCurrentTime());
    return getDbSqlSession().selectList("selectExclusiveJobsToExecute", params);
  }


  /**
   * Find unlocked timers by duedate.
   *
   * @param duedate the duedate
   * @param page the page
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<TimerEntity> findUnlockedTimersByDuedate(Date duedate, Page page) {
    final String query = "selectUnlockedTimersByDuedate";
    return getDbSqlSession().selectList(query, duedate, page);
  }

  /**
   * Find timers by execution id.
   *
   * @param executionId the execution id
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<TimerEntity> findTimersByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectTimersByExecutionId", executionId);
  }

  /**
   * Find jobs by query criteria.
   *
   * @param jobQuery the job query
   * @param page the page
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page) {
    final String query = "selectJobByQueryCriteria";
    return getDbSqlSession().selectList(query, jobQuery, page);
  }

  /**
   * Find jobs by configuration.
   *
   * @param jobHandlerType the job handler type
   * @param jobHandlerConfiguration the job handler configuration
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<Job> findJobsByConfiguration(String jobHandlerType, String jobHandlerConfiguration) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("handlerType", jobHandlerType);
    params.put("handlerConfiguration", jobHandlerConfiguration);
    return getDbSqlSession().selectList("selectJobsByConfiguration", params);
  }

  /**
   * Find job count by query criteria.
   *
   * @param jobQuery the job query
   * @return the long
   */
  public long findJobCountByQueryCriteria(JobQueryImpl jobQuery) {
    return (Long) getDbSqlSession().selectOne("selectJobCountByQueryCriteria", jobQuery);
  }

}
