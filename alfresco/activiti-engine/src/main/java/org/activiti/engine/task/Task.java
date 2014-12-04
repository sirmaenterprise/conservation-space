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
package org.activiti.engine.task;

import java.util.Date;



// TODO: Auto-generated Javadoc
/** Represents one task for a human user.
 * 
 * @author Joram Barrez
 */
public interface Task {
  
  /** The priority minium. */
  int PRIORITY_MINIUM = 0; 
  
  /** The priority normal. */
  int PRIORITY_NORMAL = 50;
  
  /** The priority maximum. */
  int PRIORITY_MAXIMUM = 100;
	
  /**
   * DB id of the task.
   *
   * @return the id
   */
	String getId();
	
  /**
   * Name or title of the task.
   *
   * @return the name
   */
	String getName();

  /**
   * Name or title of the task.
   *
   * @param name the new name
   */
	void setName(String name);
	
  /**
   * Free text description of the task.
   *
   * @return the description
   */
	String getDescription();
	
  /**
   * Change the description of the task.
   *
   * @param description the new description
   */
	void setDescription(String description);
	
	/**
	 * indication of how important/urgent this task is with a number between
	 * 0 and 100 where higher values mean a higher priority and lower values mean
	 * lower priority: [0..19] lowest, [20..39] low, [40..59] normal, [60..79] high
	 * [80..100] highest
	 *
	 * @return the priority
	 */
	int getPriority();
	
  /**
   * indication of how important/urgent this task is with a number between
   * 0 and 100 where higher values mean a higher priority and lower values mean
   * lower priority: [0..19] lowest, [20..39] low, [40..59] normal, [60..79] high
   * [80..100] highest
   *
   * @param priority the new priority
   */
	void setPriority(int priority);
	
  /**
   * The {@link User.getId() userId} of the person that is responsible for this task.
   *
   * @return the owner
   */
  String getOwner();
  
  /**
   * The {@link User.getId() userId} of the person that is responsible for this task.
   *
   * @param owner the new owner
   */
  void setOwner(String owner);
  
  /**
   * The {@link User.getId() userId} of the person to which this task is delegated.
   *
   * @return the assignee
   */
	String getAssignee();
	
	/**
	 * The {@link User.getId() userId} of the person to which this task is delegated.
	 *
	 * @param assignee the new assignee
	 */
	void setAssignee(String assignee);
	
	/**
	 * The current {@link DelegationState} for this task.
	 *
	 * @return the delegation state
	 */ 
  DelegationState getDelegationState();
  
  /**
   * The current {@link DelegationState} for this task.
   *
   * @param delegationState the new delegation state
   */ 
  void setDelegationState(DelegationState delegationState);
	
  /**
   * Reference to the process instance or null if it is not related to a process instance.
   *
   * @return the process instance id
   */
	String getProcessInstanceId();
	
  /**
   * Reference to the path of execution or null if it is not related to a process instance.
   *
   * @return the execution id
   */
	String getExecutionId();
	
  /**
   * Reference to the process definition or null if it is not related to a process.
   *
   * @return the process definition id
   */
	String getProcessDefinitionId();

	/**
	 * The date/time when this task was created.
	 *
	 * @return the creates the time
	 */
	Date getCreateTime();
	
	/**
	 * The id of the activity in the process defining this task or null if this is not related to a process.
	 *
	 * @return the task definition key
	 */
	String getTaskDefinitionKey();
	
	/**
	 * Due date of the task.
	 *
	 * @return the due date
	 */
	Date getDueDate();
	
	/**
	 * Change due date of the task.
	 *
	 * @param dueDate the new due date
	 */
	void setDueDate(Date dueDate);

	/**
	 * delegates this task to the given user and sets the {@link #getDelegationState() delegationState} to {@link DelegationState#PENDING}.
	 * If no owner is set on the task, the owner is set to the current assignee of the task.
	 *
	 * @param userId the user id
	 */
  void delegate(String userId);
  
  /**
   * the parent task for which this task is a subtask.
   *
   * @param parentTaskId the new parent task id
   */
  void setParentTaskId(String parentTaskId);

  /**
   * the parent task for which this task is a subtask.
   *
   * @return the parent task id
   */
  String getParentTaskId();
}
