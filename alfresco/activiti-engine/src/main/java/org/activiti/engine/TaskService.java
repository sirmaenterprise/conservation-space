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
package org.activiti.engine;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;

// TODO: Auto-generated Javadoc
/** Service which provides access to {@link Task} and form related operations.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface TaskService {

	/**
	 * Creates a new task that is not related to any process instance.
	 * 
	 * The returned task is transient and must be saved with {@link #saveTask(Task)} 'manually'.
	 *
	 * @return the task
	 */
  Task newTask();
  
  /**
   * create a new task with a user defined task id.
   *
   * @param taskId the task id
   * @return the task
   */
  Task newTask(String taskId);
	
	/**
	 * Saves the given task to the persistent data store. If the task is already
	 * present in the persistent store, it is updated.
	 * After a new task has been saved, the task instance passed into this method
	 * is updated with the id of the newly created task.
	 * @param task the task, cannot be null.
	 */
	void saveTask(Task task);
	
	/**
	 * Deletes the given task.
	 * @param taskId The id of the task that will be deleted, cannot be null. If no task
	 * exists with the given taskId, the operation is ignored.
	 */
	void deleteTask(String taskId);
	
	/**
	 * Deletes all tasks of the given collection.
	 * @param taskIds The id's of the tasks that will be deleted, cannot be null. All
	 * id's in the list that don't have an existing task will be ignored.
	 */
	void deleteTasks(Collection<String> taskIds);
	
  /**
   * Deletes the given task.
   * @param taskId The id of the task that will be deleted, cannot be null. If no task
   * exists with the given taskId, the operation is ignored.
   * @param cascade If cascade is true, also the historic information related to this task is deleted.
   */
  void deleteTask(String taskId, boolean cascade);
  
  /**
   * Deletes all tasks of the given collection.
   * @param taskIds The id's of the tasks that will be deleted, cannot be null. All
   * id's in the list that don't have an existing task will be ignored.
   * @param cascade If cascade is true, also the historic information related to this task is deleted.
   */
  void deleteTasks(Collection<String> taskIds, boolean cascade);
  
	 /**
 	 * Claim responsibility for a task: the given user is made assignee for the task.
 	 * The difference with {@link #setAssignee(String, String)} is that here
 	 * a check is done if the task already has a user assigned to it.
 	 * No check is done whether the user is known by the identity component.
 	 *
 	 * @param taskId task to claim, cannot be null.
 	 * @param userId user that claims the task. When userId is null the task is unclaimed,
 	 * assigned to no one.
 	 */
  void claim(String taskId, String userId);
  
  /**
   * Called when the task is successfully executed.
   *
   * @param taskId the id of the task to complete, cannot be null.
   */
  void complete(String taskId);
  
  /**
   * Delegates the task to another user. This means that the assignee is set
   * and the delegation state is set to {@link DelegationState#PENDING}.
   * If no owner is set on the task, the owner is set to the current assignee
   * of the task.
   *
   * @param taskId The id of the task that will be delegated.
   * @param userId The id of the user that will be set as assignee.
   */
  void delegateTask(String taskId, String userId);
  
  /**
   * Marks that the assignee is done with this task and that it can be send back to the owner.
   * Can only be called when this task is {@link DelegationState#PENDING} delegation.
   * After this method returns, the {@link Task#getDelegationState() delegationState} is set to {@link DelegationState#RESOLVED}.
   *
   * @param taskId the id of the task to resolve, cannot be null.
   */
  void resolveTask(String taskId);

  /**
   * Called when the task is successfully executed,
   * and the required task parameters are given by the end-user.
   *
   * @param taskId the id of the task to complete, cannot be null.
   * @param variables task parameters. May be null or empty.
   */
  void complete(String taskId, Map<String, Object> variables);

  /**
   * Changes the assignee of the given task to the given userId.
   * No check is done whether the user is known by the identity component.
   *
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user to use as assignee.
   */
  void setAssignee(String taskId, String userId);
  
  /**
   * Transfers ownership of this task to another user.
   * No check is done whether the user is known by the identity component.
   *
   * @param taskId id of the task, cannot be null.
   * @param userId of the person that is receiving ownership.
   */
  void setOwner(String taskId, String userId);
  
  /**
   * Retrieves the {@link IdentityLink}s associated with the given task.
   * Such an {@link IdentityLink} informs how a certain identity (eg. group or user)
   * is associated with a certain task (eg. as candidate, assignee, etc.)
   *
   * @param taskId the task id
   * @return the identity links for task
   */
  List<IdentityLink> getIdentityLinksForTask(String taskId);
  
  /**
   * Convenience shorthand for {@link #addUserIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}.
   *
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user to use as candidate, cannot be null.
   */
  void addCandidateUser(String taskId, String userId);
  
  /**
   * Convenience shorthand for {@link #addGroupIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}.
   *
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to use as candidate, cannot be null.
   */
  void addCandidateGroup(String taskId, String groupId);
  
  /**
   * Involves a user with a task. The type of identity link is defined by the
   * given identityLinkType.
   *
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user involve, cannot be null.
   * @param identityLinkType type of identityLink, cannot be null (@see {@link IdentityLinkType}).
   */
  void addUserIdentityLink(String taskId, String userId, String identityLinkType);
  
  /**
   * Involves a group with a task. The type of identityLink is defined by the
   * given identityLink.
   *
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to involve, cannot be null.
   * @param identityLinkType type of identity, cannot be null (@see {@link IdentityLinkType}).
   */
  void addGroupIdentityLink(String taskId, String groupId, String identityLinkType);
  
  /**
   * Convenience shorthand for {@link #deleteUserIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}.
   *
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user to use as candidate, cannot be null.
   */
  void deleteCandidateUser(String taskId, String userId);
  
  /**
   * Convenience shorthand for {@link #deleteGroupIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}.
   *
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to use as candidate, cannot be null.
   */
  void deleteCandidateGroup(String taskId, String groupId);
  
  /**
   * Removes the association between a user and a task for the given identityLinkType.
   *
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user involve, cannot be null.
   * @param identityLinkType type of identityLink, cannot be null (@see {@link IdentityLinkType}).
   */
  void deleteUserIdentityLink(String taskId, String userId, String identityLinkType);
  
  /**
   * Removes the association between a group and a task for the given identityLinkType.
   *
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to involve, cannot be null.
   * @param identityLinkType type of identity, cannot be null (@see {@link IdentityLinkType}).
   */
  void deleteGroupIdentityLink(String taskId, String groupId, String identityLinkType);
  
  /**
   * Changes the priority of the task.
   * 
   * Authorization: actual owner / business admin
   *
   * @param taskId id of the task, cannot be null.
   * @param priority the new priority for the task.
   */
  void setPriority(String taskId, int priority);
  
  /**
   * Returns a new {@link TaskQuery} that can be used to dynamically query tasks.
   *
   * @return the task query
   */
  TaskQuery createTaskQuery();

  /**
   * set variable on a task.  If the variable is not already existing, it will be created in the
   * most outer scope.  This means the process instance in case this task is related to an
   * execution.
   *
   * @param taskId the task id
   * @param variableName the variable name
   * @param value the value
   */
  void setVariable(String taskId, String variableName, Object value);

  /**
   * set variables on a task.  If the variable is not already existing, it will be created in the
   * most outer scope.  This means the process instance in case this task is related to an
   * execution.
   *
   * @param taskId the task id
   * @param variables the variables
   */
  void setVariables(String taskId, Map<String, ? extends Object> variables);

  /**
   * set variable on a task.  If the variable is not already existing, it will be created in the
   * task.
   *
   * @param taskId the task id
   * @param variableName the variable name
   * @param value the value
   */
  void setVariableLocal(String taskId, String variableName, Object value);

  /**
   * set variables on a task.  If the variable is not already existing, it will be created in the
   * task.
   *
   * @param taskId the task id
   * @param variables the variables
   */
  void setVariablesLocal(String taskId, Map<String, ? extends Object> variables);

  /**
   * get a variables and search in the task scope and if available also the execution scopes.
   *
   * @param taskId the task id
   * @param variableName the variable name
   * @return the variable
   */
  Object getVariable(String taskId, String variableName);

  /**
   * get a variables and only search in the task scope.
   *
   * @param taskId the task id
   * @param variableName the variable name
   * @return the variable local
   */
  Object getVariableLocal(String taskId, String variableName);

  /**
   * get all variables and search in the task scope and if available also the execution scopes.
   * If you have many variables and you only need a few, consider using {@link #getVariables(String, Collection)}
   * for better performance.
   *
   * @param taskId the task id
   * @return the variables
   */
  Map<String, Object> getVariables(String taskId);

  /**
   * get all variables and search only in the task scope.
   * If you have many task local variables and you only need a few, consider using {@link #getVariablesLocal(String, Collection)}
   * for better performance.
   *
   * @param taskId the task id
   * @return the variables local
   */
  Map<String, Object> getVariablesLocal(String taskId);

  /**
   * get values for all given variableNames and search only in the task scope.
   *
   * @param taskId the task id
   * @param variableNames the variable names
   * @return the variables
   */
  Map<String, Object> getVariables(String taskId, Collection<String> variableNames);

  /**
   * get a variable on a task.
   *
   * @param taskId the task id
   * @param variableNames the variable names
   * @return the variables local
   */
  Map<String, Object> getVariablesLocal(String taskId, Collection<String> variableNames);
  
  /**
   * Add a comment to a task and/or process instance.
   *
   * @param taskId the task id
   * @param processInstanceId the process instance id
   * @param message the message
   */
  void addComment(String taskId, String processInstanceId, String message);

  /**
   * The comments related to the given task.
   *
   * @param taskId the task id
   * @return the task comments
   */
  List<Comment> getTaskComments(String taskId);

  /**
   * The all events related to the given task.
   *
   * @param taskId the task id
   * @return the task events
   */
  List<Event> getTaskEvents(String taskId);

  /**
   * The comments related to the given process instance.
   *
   * @param processInstanceId the process instance id
   * @return the process instance comments
   */
  List<Comment> getProcessInstanceComments(String processInstanceId);

  /**
   * Add a new attachment to a task and/or a process instance and use an input stream to provide the content.
   *
   * @param attachmentType the attachment type
   * @param taskId the task id
   * @param processInstanceId the process instance id
   * @param attachmentName the attachment name
   * @param attachmentDescription the attachment description
   * @param content the content
   * @return the attachment
   */
  Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, InputStream content);

  /**
   * Add a new attachment to a task and/or a process instance and use an url as the content.
   *
   * @param attachmentType the attachment type
   * @param taskId the task id
   * @param processInstanceId the process instance id
   * @param attachmentName the attachment name
   * @param attachmentDescription the attachment description
   * @param url the url
   * @return the attachment
   */
  Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, String url);
  
  /**
   * Update the name and decription of an attachment.
   *
   * @param attachment the attachment
   */
  void saveAttachment(Attachment attachment);
  
  /**
   * Retrieve a particular attachment.
   *
   * @param attachmentId the attachment id
   * @return the attachment
   */
  Attachment getAttachment(String attachmentId);
  
  /**
   * Retrieve stream content of a particular attachment.
   *
   * @param attachmentId the attachment id
   * @return the attachment content
   */
  InputStream getAttachmentContent(String attachmentId);
  
  /**
   * The list of attachments associated to a task.
   *
   * @param taskId the task id
   * @return the task attachments
   */
  List<Attachment> getTaskAttachments(String taskId);

  /**
   * The list of attachments associated to a process instance.
   *
   * @param processInstanceId the process instance id
   * @return the process instance attachments
   */
  List<Attachment> getProcessInstanceAttachments(String processInstanceId);

  /**
   * Delete an attachment.
   *
   * @param attachmentId the attachment id
   */
  void deleteAttachment(String attachmentId);

  /**
   * The list of subtasks for this parent task.
   *
   * @param parentTaskId the parent task id
   * @return the sub tasks
   */
  List<Task> getSubTasks(String parentTaskId);
}
