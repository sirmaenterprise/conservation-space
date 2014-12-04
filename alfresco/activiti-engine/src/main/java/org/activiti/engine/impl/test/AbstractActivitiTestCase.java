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

package org.activiti.engine.impl.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.AssertionFailedError;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.impl.util.LogUtil.ThreadLogMode;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Assert;


// TODO: Auto-generated Javadoc
/**
 * The Class AbstractActivitiTestCase.
 *
 * @author Tom Baeyens
 */
public abstract class AbstractActivitiTestCase extends PvmTestCase {

  /** The log. */
  private static Logger log = Logger.getLogger(PluggableActivitiTestCase.class.getName());
  
  /** The Constant TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK. */
  private static final List<String> TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK = Arrays.asList(
    "ACT_GE_PROPERTY"
  );

  /** The process engine. */
  protected ProcessEngine processEngine; 
  
  /** The thread rendering mode. */
  protected ThreadLogMode threadRenderingMode = DEFAULT_THREAD_LOG_MODE;
  
  /** The deployment id. */
  protected String deploymentId;
  
  /** The exception. */
  protected Throwable exception;

  /** The process engine configuration. */
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  
  /** The repository service. */
  protected RepositoryService repositoryService;
  
  /** The runtime service. */
  protected RuntimeService runtimeService;
  
  /** The task service. */
  protected TaskService taskService;
  
  /** The form service. */
  protected FormService formService;
  
  /** The history service. */
  protected HistoryService historyService;
  
  /** The identity service. */
  protected IdentityService identityService;
  
  /** The management service. */
  protected ManagementService managementService;
  
  /**
   * Initialize process engine.
   */
  protected abstract void initializeProcessEngine();
  
  /* (non-Javadoc)
   * @see junit.framework.TestCase#runBare()
   */
  @Override
  public void runBare() throws Throwable {
    initializeProcessEngine();
    if (repositoryService==null) {
      initializeServices();
    }

    log.severe(EMPTY_LINE);

    try {
      
      deploymentId = TestHelper.annotationDeploymentSetUp(processEngine, getClass(), getName());
      
      super.runBare();

    }  catch (AssertionFailedError e) {
      log.severe(EMPTY_LINE);
      log.log(Level.SEVERE, "ASSERTION FAILED: "+e, e);
      exception = e;
      throw e;
      
    } catch (Throwable e) {
      log.severe(EMPTY_LINE);
      log.log(Level.SEVERE, "EXCEPTION: "+e, e);
      exception = e;
      throw e;
      
    } finally {
      TestHelper.annotationDeploymentTearDown(processEngine, deploymentId, getClass(), getName());
      assertAndEnsureCleanDb();
      ClockUtil.reset();
    }
  }

  /**
   * Each test is assumed to clean up all DB content it entered.
   * After a test method executed, this method scans all tables to see if the DB is completely clean.
   * It throws AssertionFailed in case the DB is not clean.
   * If the DB is not clean, it is cleaned by performing a create a drop.
   *
   * @throws Throwable the throwable
   */
  protected void assertAndEnsureCleanDb() throws Throwable {
    log.fine("verifying that db is clean after test");
    Map<String, Long> tableCounts = managementService.getTableCount();
    StringBuilder outputMessage = new StringBuilder();
    for (String tableName : tableCounts.keySet()) {
      String tableNameWithoutPrefix = tableName.replace(processEngineConfiguration.getDatabaseTablePrefix(), "");
      if (!TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK.contains(tableNameWithoutPrefix)) {
        Long count = tableCounts.get(tableName);
        if (count!=0L) {
          outputMessage.append("  "+tableName + ": " + count + " record(s) ");
        }
      }
    }
    if (outputMessage.length() > 0) {
      outputMessage.insert(0, "DB NOT CLEAN: \n");
      log.severe(EMPTY_LINE);
      log.severe(outputMessage.toString());
      
      log.info("dropping and recreating db");
      
      CommandExecutor commandExecutor = ((ProcessEngineImpl)processEngine).getProcessEngineConfiguration().getCommandExecutorTxRequired();
      commandExecutor.execute(new Command<Object>() {
        public Object execute(CommandContext commandContext) {
          DbSqlSession session = commandContext.getSession(DbSqlSession.class);
          session.dbSchemaDrop();
          session.dbSchemaCreate();
          return null;
        }
      });

      if (exception!=null) {
        throw exception;
      } else {
        Assert.fail(outputMessage.toString());
      }
    } else {
      log.info("database was clean");
    }
  }


  /**
   * Initialize services.
   */
  protected void initializeServices() {
    processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
    taskService = processEngine.getTaskService();
    formService = processEngine.getFormService();
    historyService = processEngine.getHistoryService();
    identityService = processEngine.getIdentityService();
    managementService = processEngine.getManagementService();
  }
  
  /**
   * Assert process ended.
   *
   * @param processInstanceId the process instance id
   */
  public void assertProcessEnded(final String processInstanceId) {
    ProcessInstance processInstance = processEngine
      .getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstanceId)
      .singleResult();
    
    if (processInstance!=null) {
      throw new AssertionFailedError("Expected finished process instance '"+processInstanceId+"' but it was still in the db"); 
    }
  }

  /**
   * Wait for job executor to process all jobs.
   *
   * @param maxMillisToWait the max millis to wait
   * @param intervalMillis the interval millis
   */
  public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    jobExecutor.start();

    try {
      Timer timer = new Timer();
      InteruptTask task = new InteruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean areJobsAvailable = true;
      try {
        while (areJobsAvailable && !task.isTimeLimitExceeded()) {
          Thread.sleep(intervalMillis);
          areJobsAvailable = areJobsAvailable();
        }
      } catch (InterruptedException e) {
      } finally {
        timer.cancel();
      }
      if (areJobsAvailable) {
        throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      jobExecutor.shutdown();
    }
  }

  /**
   * Wait for job executor on condition.
   *
   * @param maxMillisToWait the max millis to wait
   * @param intervalMillis the interval millis
   * @param condition the condition
   */
  public void waitForJobExecutorOnCondition(long maxMillisToWait, long intervalMillis, Callable<Boolean> condition) {
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    jobExecutor.start();

    try {
      Timer timer = new Timer();
      InteruptTask task = new InteruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean conditionIsViolated = true;
      try {
        while (conditionIsViolated) {
          Thread.sleep(intervalMillis);
          conditionIsViolated = !condition.call();
        }
      } catch (InterruptedException e) {
      } catch (Exception e) {
        throw new ActivitiException("Exception while waiting on condition: "+e.getMessage(), e);
      } finally {
        timer.cancel();
      }
      if (conditionIsViolated) {
        throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      jobExecutor.shutdown();
    }
  }

  /**
   * Are jobs available.
   *
   * @return true, if successful
   */
  public boolean areJobsAvailable() {
    return !managementService
      .createJobQuery()
      .executable()
      .list()
      .isEmpty();
  }

  /**
   * The Class InteruptTask.
   */
  private static class InteruptTask extends TimerTask {
    
    /** The time limit exceeded. */
    protected boolean timeLimitExceeded = false;
    
    /** The thread. */
    protected Thread thread;
    
    /**
     * Instantiates a new interupt task.
     *
     * @param thread the thread
     */
    public InteruptTask(Thread thread) {
      this.thread = thread;
    }
    
    /**
     * Checks if is time limit exceeded.
     *
     * @return true, if is time limit exceeded
     */
    public boolean isTimeLimitExceeded() {
      return timeLimitExceeded;
    }
    
    /* (non-Javadoc)
     * @see java.util.TimerTask#run()
     */
    public void run() {
      timeLimitExceeded = true;
      thread.interrupt();
    }
  }
}
