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

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.cmd.AcquireJobsCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.runtime.Job;

// TODO: Auto-generated Javadoc
/**
 * <p>Interface to the work management component of activiti.</p>
 * 
 * <p>This component is responsible for performing all background work 
 * ({@link Job Jobs}) scheduled by activiti.</p>
 * 
 * <p>You should generally only have one of these per Activiti instance (process 
 * engine) in a JVM.
 * In clustered situations, you can have multiple of these running against the
 * same queue + pending job list.</p>
 * 
 * @author Daniel Meyer
 */
public abstract class JobExecutor {
  
  /** The log. */
  private static Logger log = Logger.getLogger(JobExecutor.class.getName());

  /** The name. */
  protected String name = "JobExecutor["+getClass().getName()+"]";
  
  /** The command executor. */
  protected CommandExecutor commandExecutor;
  
  /** The acquire jobs cmd. */
  protected Command<AcquiredJobs> acquireJobsCmd;
  
  /** The acquire jobs runnable. */
  protected AcquireJobsRunnable acquireJobsRunnable;
  
  /** The rejected jobs handler. */
  protected RejectedJobsHandler rejectedJobsHandler;
  
  /** The job acquisition thread. */
  protected Thread jobAcquisitionThread;
  
  /** The is auto activate. */
  protected boolean isAutoActivate = false;
  
  /** The is active. */
  protected boolean isActive = false;
  
  /** The max jobs per acquisition. */
  protected int maxJobsPerAcquisition = 3;
  
  /** The wait time in millis. */
  protected int waitTimeInMillis = 5 * 1000;
  
  /** The lock owner. */
  protected String lockOwner = UUID.randomUUID().toString();
  
  /** The lock time in millis. */
  protected int lockTimeInMillis = 5 * 60 * 1000;
      
  /**
   * Start.
   */
  public void start() {
    if (isActive) {
      return;
    }
    log.info("Starting up the JobExecutor["+getClass().getName()+"].");
    ensureInitialization();    
    startExecutingJobs();
    isActive = true;
  }
  
  /**
   * Shutdown.
   */
  public synchronized void shutdown() {
    if (!isActive) {
      return;
    }
    log.info("Shutting down the JobExecutor["+getClass().getName()+"].");
    acquireJobsRunnable.stop();
    stopExecutingJobs();
    ensureCleanup();   
    isActive = false;
  }
  
  /**
   * Ensure initialization.
   */
  protected void ensureInitialization() { 
    acquireJobsCmd = new AcquireJobsCmd(this);
    acquireJobsRunnable = new AcquireJobsRunnable(this);  
  }
  
  /**
   * Ensure cleanup.
   */
  protected void ensureCleanup() {  
    acquireJobsCmd = null;
    acquireJobsRunnable = null;  
  }
  
  /**
   * Job was added.
   */
  public void jobWasAdded() {
    if(isActive) {
      acquireJobsRunnable.jobWasAdded();
    }
  }
  
  /**
   * Start executing jobs.
   */
  protected abstract void startExecutingJobs();
  
  /**
   * Stop executing jobs.
   */
  protected abstract void stopExecutingJobs(); 
  
  /**
   * Execute jobs.
   *
   * @param jobIds the job ids
   */
  protected abstract void executeJobs(List<String> jobIds);
  
  // getters and setters //////////////////////////////////////////////////////

  /**
   * Gets the command executor.
   *
   * @return the command executor
   */
  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  /**
   * Gets the wait time in millis.
   *
   * @return the wait time in millis
   */
  public int getWaitTimeInMillis() {
    return waitTimeInMillis;
  }

  /**
   * Sets the wait time in millis.
   *
   * @param waitTimeInMillis the new wait time in millis
   */
  public void setWaitTimeInMillis(int waitTimeInMillis) {
    this.waitTimeInMillis = waitTimeInMillis;
  }

  /**
   * Gets the lock time in millis.
   *
   * @return the lock time in millis
   */
  public int getLockTimeInMillis() {
    return lockTimeInMillis;
  }

  /**
   * Sets the lock time in millis.
   *
   * @param lockTimeInMillis the new lock time in millis
   */
  public void setLockTimeInMillis(int lockTimeInMillis) {
    this.lockTimeInMillis = lockTimeInMillis;
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
   * @param lockOwner the new lock owner
   */
  public void setLockOwner(String lockOwner) {
    this.lockOwner = lockOwner;
  }

  /**
   * Checks if is auto activate.
   *
   * @return true, if is auto activate
   */
  public boolean isAutoActivate() {
    return isAutoActivate;
  }

  /**
   * Sets the command executor.
   *
   * @param commandExecutor the new command executor
   */
  public void setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  /**
   * Sets the auto activate.
   *
   * @param isAutoActivate the new auto activate
   */
  public void setAutoActivate(boolean isAutoActivate) {
    this.isAutoActivate = isAutoActivate;
  }

  /**
   * Gets the max jobs per acquisition.
   *
   * @return the max jobs per acquisition
   */
  public int getMaxJobsPerAcquisition() {
    return maxJobsPerAcquisition;
  }
  
  /**
   * Sets the max jobs per acquisition.
   *
   * @param maxJobsPerAcquisition the new max jobs per acquisition
   */
  public void setMaxJobsPerAcquisition(int maxJobsPerAcquisition) {
    this.maxJobsPerAcquisition = maxJobsPerAcquisition;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }
  
  /**
   * Gets the acquire jobs cmd.
   *
   * @return the acquire jobs cmd
   */
  public Command<AcquiredJobs> getAcquireJobsCmd() {
    return acquireJobsCmd;
  }
  
  /**
   * Sets the acquire jobs cmd.
   *
   * @param acquireJobsCmd the new acquire jobs cmd
   */
  public void setAcquireJobsCmd(Command<AcquiredJobs> acquireJobsCmd) {
    this.acquireJobsCmd = acquireJobsCmd;
  }
    
  /**
   * Checks if is active.
   *
   * @return true, if is active
   */
  public boolean isActive() {
    return isActive;
  }
  
  /**
   * Gets the rejected jobs handler.
   *
   * @return the rejected jobs handler
   */
  public RejectedJobsHandler getRejectedJobsHandler() {
    return rejectedJobsHandler;
  }
    
  /**
   * Sets the rejected jobs handler.
   *
   * @param rejectedJobsHandler the new rejected jobs handler
   */
  public void setRejectedJobsHandler(RejectedJobsHandler rejectedJobsHandler) {
    this.rejectedJobsHandler = rejectedJobsHandler;
  }
  
  /**
   * Start job acquisition thread.
   */
  protected void startJobAcquisitionThread() {
		if (jobAcquisitionThread == null) {
			jobAcquisitionThread = new Thread(acquireJobsRunnable);
			jobAcquisitionThread.start();
		}
	}
	
	/**
	 * Stop job acquisition thread.
	 */
	protected void stopJobAcquisitionThread() {
		try {
			jobAcquisitionThread.join();
		} catch (InterruptedException e) {
			log.log(
					Level.WARNING,
					"Interrupted while waiting for the job Acquisition thread to terminate",
					e);
		}	
		jobAcquisitionThread = null;
	}
}
