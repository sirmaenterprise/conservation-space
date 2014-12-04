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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: Auto-generated Javadoc
/**
 * <p>This is a simple implementation of the {@link JobExecutor} using self-managed
 * threads for performing background work.</p>
 * 
 * <p>This implementation uses a {@link ThreadPoolExecutor} backed by a queue to which
 * work is submitted.</p>
 * 
 * <p><em>NOTE: use this class in environments in which self-management of threads 
 * is permitted. Consider using a different thread-management strategy in 
 * J(2)EE-Environments.</em></p>
 * 
 * @author Daniel Meyer
 */
public class DefaultJobExecutor extends JobExecutor {
  
  /** The log. */
  private static Logger log = Logger.getLogger(DefaultJobExecutor.class.getName());
  
  /** The queue size. */
  protected int queueSize = 3;
  
  /** The core pool size. */
  protected int corePoolSize = 3;
  
  /** The max pool size. */
  private int maxPoolSize = 10;

  /** The thread pool queue. */
  protected BlockingQueue<Runnable> threadPoolQueue;
  
  /** The thread pool executor. */
  protected ThreadPoolExecutor threadPoolExecutor;
    
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.jobexecutor.JobExecutor#startExecutingJobs()
   */
  protected void startExecutingJobs() {
    if (threadPoolQueue==null) {
      threadPoolQueue = new ArrayBlockingQueue<Runnable>(queueSize);
    }
    if (threadPoolExecutor==null) {
      threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, threadPoolQueue);      
      threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
    }
    startJobAcquisitionThread(); 
  }
    
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.jobexecutor.JobExecutor#stopExecutingJobs()
   */
  protected void stopExecutingJobs() {
	stopJobAcquisitionThread();
    
    // Ask the thread pool to finish and exit
    threadPoolExecutor.shutdown();

    // Waits for 1 minute to finish all currently executing jobs
    try {
      if(!threadPoolExecutor.awaitTermination(60L, TimeUnit.SECONDS)) {
        log.log(Level.WARNING, "Timeout during shutdown of job executor. "
                + "The current running jobs could not end within 60 seconds after shutdown operation.");        
      }              
    } catch (InterruptedException e) {
      log.log(Level.WARNING, "Interrupted while shutting down the job executor. ", e);
    }

    threadPoolExecutor = null;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.jobexecutor.JobExecutor#executeJobs(java.util.List)
   */
  public void executeJobs(List<String> jobIds) {
    try {
      threadPoolExecutor.execute(new ExecuteJobsRunnable(this, jobIds));
    }catch (RejectedExecutionException e) {
      rejectedJobsHandler.jobsRejected(this, jobIds);
    }
  }
  
  // getters and setters ////////////////////////////////////////////////////// 
  
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
    
}

