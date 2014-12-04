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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


// TODO: Auto-generated Javadoc
/**
 * The Class AcquiredJobs.
 *
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class AcquiredJobs {

  /** The acquired job batches. */
  protected List<List<String>> acquiredJobBatches = new ArrayList<List<String>>();
  
  /** The acquired jobs. */
  protected Set<String> acquiredJobs = new HashSet<String>();

  /**
   * Gets the job id batches.
   *
   * @return the job id batches
   */
  public List<List<String>> getJobIdBatches() {
    return acquiredJobBatches;
  }

  /**
   * Adds the job id batch.
   *
   * @param jobIds the job ids
   */
  public void addJobIdBatch(List<String> jobIds) {
    acquiredJobBatches.add(jobIds);
    acquiredJobs.addAll(jobIds);
  }
  
  /**
   * Contains.
   *
   * @param jobId the job id
   * @return true, if successful
   */
  public boolean contains(String jobId) {
    return acquiredJobs.contains(jobId);    
  }

  /**
   * Size.
   *
   * @return the int
   */
  public int size() {
    return acquiredJobs.size();
  }
  
  
}
