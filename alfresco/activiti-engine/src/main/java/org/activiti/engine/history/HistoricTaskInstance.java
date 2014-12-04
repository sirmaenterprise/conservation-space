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

package org.activiti.engine.history;

import java.util.Date;


// TODO: Auto-generated Javadoc
/**
 * Represents a historic task instance (waiting, finished or deleted) that is stored permanent for 
 * statistics, audit and other business intelligence purposes.
 * 
 * @author Tom Baeyens
 */
public interface HistoricTaskInstance {

  /**
   * The unique identifier of this historic task instance. This is the same identifier as the
   * runtime Task instance.
   *
   * @return the id
   */
  String getId();

  /**
   * Process definition reference.
   *
   * @return the process definition id
   */
  String getProcessDefinitionId();

  /**
   * Process instance reference.
   *
   * @return the process instance id
   */
  String getProcessInstanceId();

  /**
   * Execution reference.
   *
   * @return the execution id
   */
  String getExecutionId();

  /**
   * The latest name given to this task.
   *
   * @return the name
   */
  String getName();

  /**
   * The latest description given to this task.
   *
   * @return the description
   */
  String getDescription();

  /**
   * The reason why this task was deleted {'completed' | 'deleted' | any other user defined string }.
   *
   * @return the delete reason
   */
  String getDeleteReason();
  
  /**
   * Task owner.
   *
   * @return the owner
   */
  String getOwner();

  /**
   * The latest assignee given to this task.
   *
   * @return the assignee
   */
  String getAssignee();

  /**
   * Time when the task started.
   *
   * @return the start time
   */
  Date getStartTime();

  /**
   * Time when the task was deleted or completed.
   *
   * @return the end time
   */
  Date getEndTime();

  /**
   * Difference between {@link #getEndTime()} and {@link #getStartTime()} in milliseconds.
   *
   * @return the duration in millis
   */
  Long getDurationInMillis();
  
  /**
   * Task definition key.
   *
   * @return the task definition key
   */
  String getTaskDefinitionKey();
  
  /**
   * Task priority *.
   *
   * @return the priority
   */
  int getPriority();
  
  /**
   * Task due date *.
   *
   * @return the due date
   */
  Date getDueDate();
  
  /**
   * The parent task of this task, in case this task was a subtask.
   *
   * @return the parent task id
   */
  String getParentTaskId();

}
