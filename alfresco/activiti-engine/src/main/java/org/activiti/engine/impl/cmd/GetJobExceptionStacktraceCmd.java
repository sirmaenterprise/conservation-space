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

package org.activiti.engine.impl.cmd;

import java.io.Serializable;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;


// TODO: Auto-generated Javadoc
/**
 * The Class GetJobExceptionStacktraceCmd.
 *
 * @author Frederik Heremans
 */
public class GetJobExceptionStacktraceCmd implements Command<String>, Serializable{

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The job id. */
  private String jobId;
    
  /**
   * Instantiates a new gets the job exception stacktrace cmd.
   *
   * @param jobId the job id
   */
  public GetJobExceptionStacktraceCmd(String jobId) {
    this.jobId = jobId;
  }


  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public String execute(CommandContext commandContext) {
    if(jobId == null) {
      throw new ActivitiException("jobId is null");
    }
    
    JobEntity job = commandContext
      .getJobManager()
      .findJobById(jobId);
    
    if(job == null) {
      throw new ActivitiException("No job found with id " + jobId);
    }
    
    return job.getExceptionStacktrace();
  }

  
}
