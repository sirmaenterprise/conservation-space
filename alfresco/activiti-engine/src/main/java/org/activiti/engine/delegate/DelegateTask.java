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
package org.activiti.engine.delegate;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;

// TODO: Auto-generated Javadoc
/**
 * The Interface DelegateTask.
 *
 * @author Joram Barrez
 */
public interface DelegateTask extends VariableScope {

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
   * Change the name of the task.
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
   * Returns the execution currently at the task.
   *
   * @return the execution
   */
  DelegateExecution getExecution();
  
  /**
   * Returns the event name which triggered the task listener to fire for this task.
   *
   * @return the event name
   */
  String getEventName();
  
  /**
   * Adds the given user as a candidate user to this task.
   *
   * @param userId the user id
   */
  void addCandidateUser(String userId);
  
  /**
   * Adds multiple users as candidate user to this task.
   *
   * @param candidateUsers the candidate users
   */
  void addCandidateUsers(Collection<String> candidateUsers);
  
  /**
   * Adds the given group as candidate group to this task.
   *
   * @param groupId the group id
   */
  void addCandidateGroup(String groupId);
  
  /**
   * Adds multiple groups as candidate group to this task.
   *
   * @param candidateGroups the candidate groups
   */
  void addCandidateGroups(Collection<String> candidateGroups);

  /**
   * The {@link User.getId() userId} of the person responsible for this task.
   *
   * @return the owner
   */
  String getOwner();
  
  /**
   * The {@link User.getId() userId} of the person responsible for this task.
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
   * Involves a user with a task. The type of identity link is defined by the given identityLinkType.
   *
   * @param userId id of the user involve, cannot be null.
   * @param identityLinkType type of identityLink, cannot be null (@see {@link IdentityLinkType}).
   */
  void addUserIdentityLink(String userId, String identityLinkType);
  
  /**
   * Involves a group with group task. The type of identityLink is defined by the given identityLink.
   *
   * @param groupId id of the group to involve, cannot be null.
   * @param identityLinkType type of identity, cannot be null (@see {@link IdentityLinkType}).
   */
  void addGroupIdentityLink(String groupId, String identityLinkType);
  
  /**
   * Convenience shorthand for {@link #deleteUserIdentityLink(String, String)}; with type {@link IdentityLinkType#CANDIDATE}.
   *
   * @param userId id of the user to use as candidate, cannot be null.
   */
  void deleteCandidateUser(String userId);
  
  /**
   * Convenience shorthand for {@link #deleteGroupIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}.
   *
   * @param groupId id of the group to use as candidate, cannot be null.
   */
  void deleteCandidateGroup(String groupId);
  
  /**
   * Removes the association between a user and a task for the given identityLinkType.
   *
   * @param userId id of the user involve, cannot be null.
   * @param identityLinkType type of identityLink, cannot be null (@see {@link IdentityLinkType}).
   */
  void deleteUserIdentityLink(String userId, String identityLinkType);
  
  /**
   * Removes the association between a group and a task for the given identityLinkType.
   *
   * @param groupId id of the group to involve, cannot be null.
   * @param identityLinkType type of identity, cannot be null (@see {@link IdentityLinkType}).
   */
  void deleteGroupIdentityLink(String groupId, String identityLinkType);

  /**
   * Retrieves the candidate users and groups associated with the task.
   * @return set of {@link IdentityLink}s of type {@link IdentityLinkType#CANDIDATE}.
   */
  Set<IdentityLink> getCandidates();
}
