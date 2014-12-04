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

package org.activiti.engine.impl.mail;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandExecutor;


// TODO: Auto-generated Javadoc
/**
 * The Class MailScanner.
 *
 * @author Tom Baeyens
 */
public class MailScanner {
  
  /** The log. */
  private static Logger log = Logger.getLogger(MailScanner.class.getName());
  
  /** The thread. */
  protected Thread thread = null;
  
  /** The is active. */
  protected boolean isActive = false;
  
  /** The command executor. */
  protected CommandExecutor commandExecutor;
  
  /** The is auto activate. */
  protected boolean isAutoActivate = false;
  
  /** The mail scan scheduler thread. */
  protected MailScanSchedulerThread mailScanSchedulerThread;
  
  /** The thread pool queue. */
  protected BlockingQueue<Runnable> threadPoolQueue;
  
  /** The thread pool executor. */
  protected ThreadPoolExecutor threadPoolExecutor;
  
  /** The queue size. */
  protected int queueSize = 1;
  
  /** The core pool size. */
  protected int corePoolSize = 3;
  
  /** The max pool size. */
  private int maxPoolSize = 10;
  
  /**
   * Start.
   */
  public synchronized void start() {
    if(isActive) {
      // Already started, nothing to do
      log.info("Ignoring duplicate MailScanner start invocation");
      return;
    } else {
      isActive = true;
      
      if (mailScanSchedulerThread==null) {
        mailScanSchedulerThread = new MailScanSchedulerThread(this);
      }
      if (threadPoolQueue==null) {
        threadPoolQueue = new ArrayBlockingQueue<Runnable>(queueSize);
      }
      if (threadPoolExecutor==null) {
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, threadPoolQueue);
        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
      }
      
      // Create the mail scan scheduler
      log.fine("MailScanner is starting the "+MailScanSchedulerThread.class.getName());
      mailScanSchedulerThread.start();
    }
  }
  
  /**
   * Shutdown.
   */
  public void shutdown() {
    if(!isActive) {
      log.info("Ignoring request to shut down non-active MailScanner");
      return;
    }
    
    log.info("Shutting down the MailScanner");
    mailScanSchedulerThread.shutdown();
    
    // Ask the thread pool to finish and exit
    threadPoolExecutor.shutdown();
    
    // Waits for 1 minute to finish all currently executing scans
    try {
      threadPoolExecutor.awaitTermination(60L, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
        throw new ActivitiException("Timeout during shutdown of mail scanner. The current running scans could not end withing 60 seconds after shutdown operation.", e);
    }
    
    isActive = false;

    // Clear references
    threadPoolExecutor = null;
    mailScanSchedulerThread = null;
  }
  
  /**
   * Adds the user.
   *
   * @param userId the user id
   * @param userPassword the user password
   */
  public void addUser(String userId, String userPassword) {
    if (isActive && mailScanSchedulerThread != null) {
      mailScanSchedulerThread.addUser(userId, userPassword);
    }
  }

  /**
   * Removes the user.
   *
   * @param userId the user id
   */
  public void removeUser(String userId) {
    if (mailScanSchedulerThread != null) {
      mailScanSchedulerThread.removeUser(userId);
    }
  }

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
   * Gets the queue size.
   *
   * @return the queue size
   */
  public int getQueueSize() {
    return queueSize;
  }
  
  /**
   * Sets the queue size.
   *
   * @param queueSize the new queue size
   */
  public void setQueueSize(int queueSize) {
    this.queueSize = queueSize;
  }
  
  /**
   * Gets the core pool size.
   *
   * @return the core pool size
   */
  public int getCorePoolSize() {
    return corePoolSize;
  }
  
  /**
   * Sets the core pool size.
   *
   * @param corePoolSize the new core pool size
   */
  public void setCorePoolSize(int corePoolSize) {
    this.corePoolSize = corePoolSize;
  }

  /**
   * Gets the max pool size.
   *
   * @return the max pool size
   */
  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  /**
   * Sets the max pool size.
   *
   * @param maxPoolSize the new max pool size
   */
  public void setMaxPoolSize(int maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
  }
  
  /**
   * Gets the thread pool queue.
   *
   * @return the thread pool queue
   */
  public BlockingQueue<Runnable> getThreadPoolQueue() {
    return threadPoolQueue;
  }

  /**
   * Sets the thread pool queue.
   *
   * @param threadPoolQueue the new thread pool queue
   */
  public void setThreadPoolQueue(BlockingQueue<Runnable> threadPoolQueue) {
    this.threadPoolQueue = threadPoolQueue;
  }

  /**
   * Gets the thread pool executor.
   *
   * @return the thread pool executor
   */
  public ThreadPoolExecutor getThreadPoolExecutor() {
    return threadPoolExecutor;
  }
  
  /**
   * Sets the thread pool executor.
   *
   * @param threadPoolExecutor the new thread pool executor
   */
  public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
    this.threadPoolExecutor = threadPoolExecutor;
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
   * Checks if is auto activate.
   *
   * @return true, if is auto activate
   */
  public boolean isAutoActivate() {
    return isAutoActivate;
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
   * Gets the mail scan scheduler thread.
   *
   * @return the mail scan scheduler thread
   */
  public MailScanSchedulerThread getMailScanSchedulerThread() {
    return mailScanSchedulerThread;
  }

  
  /**
   * Sets the mail scan scheduler thread.
   *
   * @param mailScanScheduler the new mail scan scheduler thread
   */
  public void setMailScanSchedulerThread(MailScanSchedulerThread mailScanScheduler) {
    this.mailScanSchedulerThread = mailScanScheduler;
  }

  
  /**
   * Sets the active.
   *
   * @param isActive the new active
   */
  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }

  /**
   * Sets the command executor.
   *
   * @param commandExecutor the new command executor
   */
  public void setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }
}
