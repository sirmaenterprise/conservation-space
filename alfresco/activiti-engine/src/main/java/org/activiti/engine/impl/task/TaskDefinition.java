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
package org.activiti.engine.impl.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.form.TaskFormHandler;

// TODO: Auto-generated Javadoc
/**
 * Container for task definition information gathered at parsing time.
 * 
 * @author Joram Barrez
 */
public class TaskDefinition {

  /** The key. */
  protected String key;
  
  // assignment fields
  /** The name expression. */
  protected Expression nameExpression;
  
  /** The description expression. */
  protected Expression descriptionExpression;
  
  /** The assignee expression. */
  protected Expression assigneeExpression;
  
  /** The candidate user id expressions. */
  protected Set<Expression> candidateUserIdExpressions = new HashSet<Expression>();
  
  /** The candidate group id expressions. */
  protected Set<Expression> candidateGroupIdExpressions = new HashSet<Expression>();
  
  /** The due date expression. */
  protected Expression dueDateExpression;
  
  /** The priority expression. */
  protected Expression priorityExpression;
  
  // form fields
  /** The task form handler. */
  protected TaskFormHandler taskFormHandler;
  
  // task listeners
  /** The task listeners. */
  protected Map<String, List<TaskListener>> taskListeners = new HashMap<String, List<TaskListener>>();
  
  /**
   * Instantiates a new task definition.
   *
   * @param taskFormHandler the task form handler
   */
  public TaskDefinition(TaskFormHandler taskFormHandler) {
    this.taskFormHandler = taskFormHandler;
  }

  // getters and setters //////////////////////////////////////////////////////

  /**
   * Gets the name expression.
   *
   * @return the name expression
   */
  public Expression getNameExpression() {
    return nameExpression;
  }

  /**
   * Sets the name expression.
   *
   * @param nameExpression the new name expression
   */
  public void setNameExpression(Expression nameExpression) {
    this.nameExpression = nameExpression;
  }

  /**
   * Gets the description expression.
   *
   * @return the description expression
   */
  public Expression getDescriptionExpression() {
    return descriptionExpression;
  }

  /**
   * Sets the description expression.
   *
   * @param descriptionExpression the new description expression
   */
  public void setDescriptionExpression(Expression descriptionExpression) {
    this.descriptionExpression = descriptionExpression;
  }

  /**
   * Gets the assignee expression.
   *
   * @return the assignee expression
   */
  public Expression getAssigneeExpression() {
    return assigneeExpression;
  }

  /**
   * Sets the assignee expression.
   *
   * @param assigneeExpression the new assignee expression
   */
  public void setAssigneeExpression(Expression assigneeExpression) {
    this.assigneeExpression = assigneeExpression;
  }

  /**
   * Gets the candidate user id expressions.
   *
   * @return the candidate user id expressions
   */
  public Set<Expression> getCandidateUserIdExpressions() {
    return candidateUserIdExpressions;
  }

  /**
   * Adds the candidate user id expression.
   *
   * @param userId the user id
   */
  public void addCandidateUserIdExpression(Expression userId) {
    candidateUserIdExpressions.add(userId);
  }

  /**
   * Gets the candidate group id expressions.
   *
   * @return the candidate group id expressions
   */
  public Set<Expression> getCandidateGroupIdExpressions() {
    return candidateGroupIdExpressions;
  }

  /**
   * Adds the candidate group id expression.
   *
   * @param groupId the group id
   */
  public void addCandidateGroupIdExpression(Expression groupId) {
    candidateGroupIdExpressions.add(groupId);
  }

  /**
   * Gets the priority expression.
   *
   * @return the priority expression
   */
  public Expression getPriorityExpression() {
    return priorityExpression;
  }

  /**
   * Sets the priority expression.
   *
   * @param priorityExpression the new priority expression
   */
  public void setPriorityExpression(Expression priorityExpression) {
    this.priorityExpression = priorityExpression;
  }

  /**
   * Gets the task form handler.
   *
   * @return the task form handler
   */
  public TaskFormHandler getTaskFormHandler() {
    return taskFormHandler;
  }

  /**
   * Sets the task form handler.
   *
   * @param taskFormHandler the new task form handler
   */
  public void setTaskFormHandler(TaskFormHandler taskFormHandler) {
    this.taskFormHandler = taskFormHandler;
  }

  /**
   * Gets the key.
   *
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * Sets the key.
   *
   * @param key the new key
   */
  public void setKey(String key) {
    this.key = key;
  }
  
  /**
   * Gets the due date expression.
   *
   * @return the due date expression
   */
  public Expression getDueDateExpression() {
    return dueDateExpression;
  }
  
  /**
   * Sets the due date expression.
   *
   * @param dueDateExpression the new due date expression
   */
  public void setDueDateExpression(Expression dueDateExpression) {
    this.dueDateExpression = dueDateExpression;
  }

  /**
   * Gets the task listeners.
   *
   * @return the task listeners
   */
  public Map<String, List<TaskListener>> getTaskListeners() {
    return taskListeners;
  }

  /**
   * Sets the task listeners.
   *
   * @param taskListeners the task listeners
   */
  public void setTaskListeners(Map<String, List<TaskListener>> taskListeners) {
    this.taskListeners = taskListeners;
  }
  
  /**
   * Gets the task listener.
   *
   * @param eventName the event name
   * @return the task listener
   */
  public List<TaskListener> getTaskListener(String eventName) {
    return taskListeners.get(eventName);
  }
  
  /**
   * Adds the task listener.
   *
   * @param eventName the event name
   * @param taskListener the task listener
   */
  public void addTaskListener(String eventName, TaskListener taskListener) {
    List<TaskListener> taskEventListeners = taskListeners.get(eventName);
    if (taskEventListeners == null) {
      taskEventListeners = new ArrayList<TaskListener>();
      taskListeners.put(eventName, taskEventListeners);
    }
    taskEventListeners.add(taskListener);
  }
  
}
