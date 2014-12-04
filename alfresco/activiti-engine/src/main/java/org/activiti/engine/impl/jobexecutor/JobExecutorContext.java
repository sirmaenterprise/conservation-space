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

import java.util.LinkedList;
import java.util.List;

import org.activiti.engine.impl.persistence.entity.JobEntity;

// TODO: Auto-generated Javadoc
/**
 * The Class JobExecutorContext.
 *
 * @author Daniel Meyer
 */
public class JobExecutorContext {

  /** The current processor job queue. */
  protected List<String> currentProcessorJobQueue = new LinkedList<String>();
  
  /** The current job. */
  protected JobEntity currentJob;
        
  /**
   * Gets the current processor job queue.
   *
   * @return the current processor job queue
   */
  public List<String> getCurrentProcessorJobQueue() {
    return currentProcessorJobQueue;
  }

  /**
   * Checks if is executing exclusive job.
   *
   * @return true, if is executing exclusive job
   */
  public boolean isExecutingExclusiveJob() {
    return currentJob == null ? false : currentJob.isExclusive();
  }
     
  /**
   * Sets the current job.
   *
   * @param currentJob the new current job
   */
  public void setCurrentJob(JobEntity currentJob) {
    this.currentJob = currentJob;
  }
    
  /**
   * Gets the current job.
   *
   * @return the current job
   */
  public JobEntity getCurrentJob() {
    return currentJob;
  }
}
