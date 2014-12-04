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
package org.activiti.engine.impl;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cmd.AddCommentCmd;
import org.activiti.engine.impl.cmd.AddIdentityLinkCmd;
import org.activiti.engine.impl.cmd.ClaimTaskCmd;
import org.activiti.engine.impl.cmd.CompleteTaskCmd;
import org.activiti.engine.impl.cmd.CreateAttachmentCmd;
import org.activiti.engine.impl.cmd.DelegateTaskCmd;
import org.activiti.engine.impl.cmd.DeleteAttachmentCmd;
import org.activiti.engine.impl.cmd.DeleteIdentityLinkCmd;
import org.activiti.engine.impl.cmd.DeleteTaskCmd;
import org.activiti.engine.impl.cmd.GetAttachmentCmd;
import org.activiti.engine.impl.cmd.GetAttachmentContentCmd;
import org.activiti.engine.impl.cmd.GetIdentityLinksForTaskCmd;
import org.activiti.engine.impl.cmd.GetProcessInstanceAttachmentsCmd;
import org.activiti.engine.impl.cmd.GetProcessInstanceCommentsCmd;
import org.activiti.engine.impl.cmd.GetSubTasksCmd;
import org.activiti.engine.impl.cmd.GetTaskAttachmentsCmd;
import org.activiti.engine.impl.cmd.GetTaskCommentsCmd;
import org.activiti.engine.impl.cmd.GetTaskEventsCmd;
import org.activiti.engine.impl.cmd.GetTaskVariableCmd;
import org.activiti.engine.impl.cmd.GetTaskVariablesCmd;
import org.activiti.engine.impl.cmd.ResolveTaskCmd;
import org.activiti.engine.impl.cmd.SaveAttachmentCmd;
import org.activiti.engine.impl.cmd.SaveTaskCmd;
import org.activiti.engine.impl.cmd.SetTaskPriorityCmd;
import org.activiti.engine.impl.cmd.SetTaskVariablesCmd;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;


// TODO: Auto-generated Javadoc
/**
 * The Class TaskServiceImpl.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class TaskServiceImpl extends ServiceImpl implements TaskService {

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#newTask()
   */
  public Task newTask() {
    return newTask(null);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#newTask(java.lang.String)
   */
  public Task newTask(String taskId) {
    TaskEntity task = TaskEntity.create();
    task.setId(taskId);
    return task;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#saveTask(org.activiti.engine.task.Task)
   */
  public void saveTask(Task task) {
    commandExecutor.execute(new SaveTaskCmd(task));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#deleteTask(java.lang.String)
   */
  public void deleteTask(String taskId) {
    commandExecutor.execute(new DeleteTaskCmd(taskId, false));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#deleteTasks(java.util.Collection)
   */
  public void deleteTasks(Collection<String> taskIds) {
    commandExecutor.execute(new DeleteTaskCmd(taskIds, false));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#deleteTask(java.lang.String, boolean)
   */
  public void deleteTask(String taskId, boolean cascade) {
    commandExecutor.execute(new DeleteTaskCmd(taskId, cascade));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#deleteTasks(java.util.Collection, boolean)
   */
  public void deleteTasks(Collection<String> taskIds, boolean cascade) {
    commandExecutor.execute(new DeleteTaskCmd(taskIds, cascade));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#setAssignee(java.lang.String, java.lang.String)
   */
  public void setAssignee(String taskId, String userId) {
    commandExecutor.execute(new AddIdentityLinkCmd(taskId, userId, null, IdentityLinkType.ASSIGNEE));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#setOwner(java.lang.String, java.lang.String)
   */
  public void setOwner(String taskId, String userId) {
    commandExecutor.execute(new AddIdentityLinkCmd(taskId, userId, null, IdentityLinkType.OWNER));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#addCandidateUser(java.lang.String, java.lang.String)
   */
  public void addCandidateUser(String taskId, String userId) {
    commandExecutor.execute(new AddIdentityLinkCmd(taskId, userId, null, IdentityLinkType.CANDIDATE));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#addCandidateGroup(java.lang.String, java.lang.String)
   */
  public void addCandidateGroup(String taskId, String groupId) {
    commandExecutor.execute(new AddIdentityLinkCmd(taskId, null, groupId, IdentityLinkType.CANDIDATE));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#addUserIdentityLink(java.lang.String, java.lang.String, java.lang.String)
   */
  public void addUserIdentityLink(String taskId, String userId, String identityLinkType) {
    commandExecutor.execute(new AddIdentityLinkCmd(taskId, userId, null, identityLinkType));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#addGroupIdentityLink(java.lang.String, java.lang.String, java.lang.String)
   */
  public void addGroupIdentityLink(String taskId, String groupId, String identityLinkType) {
    commandExecutor.execute(new AddIdentityLinkCmd(taskId, null, groupId, identityLinkType));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#deleteCandidateGroup(java.lang.String, java.lang.String)
   */
  public void deleteCandidateGroup(String taskId, String groupId) {
    commandExecutor.execute(new DeleteIdentityLinkCmd(taskId, null, groupId, IdentityLinkType.CANDIDATE));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#deleteCandidateUser(java.lang.String, java.lang.String)
   */
  public void deleteCandidateUser(String taskId, String userId) {
    commandExecutor.execute(new DeleteIdentityLinkCmd(taskId, userId, null, IdentityLinkType.CANDIDATE));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#deleteGroupIdentityLink(java.lang.String, java.lang.String, java.lang.String)
   */
  public void deleteGroupIdentityLink(String taskId, String groupId, String identityLinkType) {
    commandExecutor.execute(new DeleteIdentityLinkCmd(taskId, null, groupId, identityLinkType));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#deleteUserIdentityLink(java.lang.String, java.lang.String, java.lang.String)
   */
  public void deleteUserIdentityLink(String taskId, String userId, String identityLinkType) {
    commandExecutor.execute(new DeleteIdentityLinkCmd(taskId, userId, null, identityLinkType));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#getIdentityLinksForTask(java.lang.String)
   */
  public List<IdentityLink> getIdentityLinksForTask(String taskId) {
    return commandExecutor.execute(new GetIdentityLinksForTaskCmd(taskId));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#claim(java.lang.String, java.lang.String)
   */
  public void claim(String taskId, String userId) {
    ClaimTaskCmd cmd = new ClaimTaskCmd(taskId, userId);
    commandExecutor.execute(cmd);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#complete(java.lang.String)
   */
  public void complete(String taskId) {
    commandExecutor.execute(new CompleteTaskCmd(taskId, null));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#complete(java.lang.String, java.util.Map)
   */
  public void complete(String taskId, Map<String, Object> variables) {
    commandExecutor.execute(new CompleteTaskCmd(taskId, variables));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#delegateTask(java.lang.String, java.lang.String)
   */
  public void delegateTask(String taskId, String userId) {
    commandExecutor.execute(new DelegateTaskCmd(taskId, userId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#resolveTask(java.lang.String)
   */
  public void resolveTask(String taskId) {
    commandExecutor.execute(new ResolveTaskCmd(taskId, null));
  }

  /**
   * Resolve.
   *
   * @param taskId the task id
   * @param variables the variables
   */
  public void resolve(String taskId, Map<String, Object> variables) {
    commandExecutor.execute(new ResolveTaskCmd(taskId, variables));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#setPriority(java.lang.String, int)
   */
  public void setPriority(String taskId, int priority) {
    commandExecutor.execute(new SetTaskPriorityCmd(taskId, priority) );
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#createTaskQuery()
   */
  public TaskQuery createTaskQuery() {
    return new TaskQueryImpl(commandExecutor);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#getVariables(java.lang.String)
   */
  public Map<String, Object> getVariables(String executionId) {
    return commandExecutor.execute(new GetTaskVariablesCmd(executionId, null, false));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#getVariablesLocal(java.lang.String)
   */
  public Map<String, Object> getVariablesLocal(String executionId) {
    return commandExecutor.execute(new GetTaskVariablesCmd(executionId, null, true));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#getVariables(java.lang.String, java.util.Collection)
   */
  public Map<String, Object> getVariables(String executionId, Collection<String> variableNames) {
    return commandExecutor.execute(new GetTaskVariablesCmd(executionId, variableNames, false));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#getVariablesLocal(java.lang.String, java.util.Collection)
   */
  public Map<String, Object> getVariablesLocal(String executionId, Collection<String> variableNames) {
    return commandExecutor.execute(new GetTaskVariablesCmd(executionId, variableNames, true));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#getVariable(java.lang.String, java.lang.String)
   */
  public Object getVariable(String executionId, String variableName) {
    return commandExecutor.execute(new GetTaskVariableCmd(executionId, variableName, false));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#getVariableLocal(java.lang.String, java.lang.String)
   */
  public Object getVariableLocal(String executionId, String variableName) {
    return commandExecutor.execute(new GetTaskVariableCmd(executionId, variableName, true));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#setVariable(java.lang.String, java.lang.String, java.lang.Object)
   */
  public void setVariable(String executionId, String variableName, Object value) {
    if(variableName == null) {
      throw new ActivitiException("variableName is null");
    }
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(variableName, value);
    commandExecutor.execute(new SetTaskVariablesCmd(executionId, variables, false));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#setVariableLocal(java.lang.String, java.lang.String, java.lang.Object)
   */
  public void setVariableLocal(String executionId, String variableName, Object value) {
    if(variableName == null) {
      throw new ActivitiException("variableName is null");
    }
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(variableName, value);
    commandExecutor.execute(new SetTaskVariablesCmd(executionId, variables, true));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#setVariables(java.lang.String, java.util.Map)
   */
  public void setVariables(String executionId, Map<String, ? extends Object> variables) {
    commandExecutor.execute(new SetTaskVariablesCmd(executionId, variables, false));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#setVariablesLocal(java.lang.String, java.util.Map)
   */
  public void setVariablesLocal(String executionId, Map<String, ? extends Object> variables) {
    commandExecutor.execute(new SetTaskVariablesCmd(executionId, variables, true));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#addComment(java.lang.String, java.lang.String, java.lang.String)
   */
  public void addComment(String taskId, String processInstance, String message) {
    commandExecutor.execute(new AddCommentCmd(taskId, processInstance, message));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#getTaskComments(java.lang.String)
   */
  public List<Comment> getTaskComments(String taskId) {
    return commandExecutor.execute(new GetTaskCommentsCmd(taskId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#getTaskEvents(java.lang.String)
   */
  public List<Event> getTaskEvents(String taskId) {
    return commandExecutor.execute(new GetTaskEventsCmd(taskId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#getProcessInstanceComments(java.lang.String)
   */
  public List<Comment> getProcessInstanceComments(String processInstanceId) {
    return commandExecutor.execute(new GetProcessInstanceCommentsCmd(processInstanceId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#createAttachment(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.io.InputStream)
   */
  public Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, InputStream content) {
    return commandExecutor.execute(new CreateAttachmentCmd(attachmentType, taskId, processInstanceId, attachmentName, attachmentDescription, content, null));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#createAttachment(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  public Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, String url) {
    return commandExecutor.execute(new CreateAttachmentCmd(attachmentType, taskId, processInstanceId, attachmentName, attachmentDescription, null, url));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#getAttachmentContent(java.lang.String)
   */
  public InputStream getAttachmentContent(String attachmentId) {
    return commandExecutor.execute(new GetAttachmentContentCmd(attachmentId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#deleteAttachment(java.lang.String)
   */
  public void deleteAttachment(String attachmentId) {
    commandExecutor.execute(new DeleteAttachmentCmd(attachmentId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#getAttachment(java.lang.String)
   */
  public Attachment getAttachment(String attachmentId) {
    return commandExecutor.execute(new GetAttachmentCmd(attachmentId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#getTaskAttachments(java.lang.String)
   */
  public List<Attachment> getTaskAttachments(String taskId) {
    return commandExecutor.execute(new GetTaskAttachmentsCmd(taskId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#getProcessInstanceAttachments(java.lang.String)
   */
  public List<Attachment> getProcessInstanceAttachments(String processInstanceId) {
    return commandExecutor.execute(new GetProcessInstanceAttachmentsCmd(processInstanceId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#saveAttachment(org.activiti.engine.task.Attachment)
   */
  public void saveAttachment(Attachment attachment) {
    commandExecutor.execute(new SaveAttachmentCmd(attachment));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.TaskService#getSubTasks(java.lang.String)
   */
  public List<Task> getSubTasks(String parentTaskId) {
    return commandExecutor.execute(new GetSubTasksCmd(parentTaskId));
  }
}
