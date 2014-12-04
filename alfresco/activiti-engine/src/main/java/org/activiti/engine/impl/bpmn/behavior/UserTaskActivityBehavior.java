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
package org.activiti.engine.impl.bpmn.behavior;

import java.util.Collection;
import java.util.Date;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.task.TaskDefinition;

// TODO: Auto-generated Javadoc
/**
 * activity implementation for the user task.
 * 
 * @author Joram Barrez
 */
public class UserTaskActivityBehavior extends TaskActivityBehavior {

  /** The task definition. */
  protected TaskDefinition taskDefinition;
  
  /** The expression manager. */
  protected ExpressionManager expressionManager;

  /**
   * Instantiates a new user task activity behavior.
   *
   * @param expressionManager the expression manager
   * @param taskDefinition the task definition
   */
  public UserTaskActivityBehavior(ExpressionManager expressionManager, TaskDefinition taskDefinition) {
    this.expressionManager = expressionManager;
    this.taskDefinition = taskDefinition;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.behavior.FlowNodeActivityBehavior#execute(org.activiti.engine.impl.pvm.delegate.ActivityExecution)
   */
  public void execute(ActivityExecution execution) throws Exception {
    TaskEntity task = TaskEntity.createAndInsert(execution);
    task.setExecution(execution);
    task.setTaskDefinition(taskDefinition);

    if (taskDefinition.getNameExpression() != null) {
      String name = (String) taskDefinition.getNameExpression().getValue(execution);
      task.setName(name);
    }

    if (taskDefinition.getDescriptionExpression() != null) {
      String description = (String) taskDefinition.getDescriptionExpression().getValue(execution);
      task.setDescription(description);
    }
    
    if(taskDefinition.getDueDateExpression() != null) {
      Object dueDate = taskDefinition.getDueDateExpression().getValue(execution);
      if(dueDate != null) {
        if(!(dueDate instanceof Date)) {
          throw new ActivitiException("Due date expression does not resolve to a Date: " + 
                  taskDefinition.getDueDateExpression().getExpressionText());
        }
        task.setDueDate((Date) dueDate);
      }
    }

    if (taskDefinition.getPriorityExpression() != null) {
      final Object priority = taskDefinition.getPriorityExpression().getValue(execution);
      if (priority != null) {
        if (priority instanceof String) {
          try {
            task.setPriority(Integer.valueOf((String) priority));
          } catch (NumberFormatException e) {
            throw new ActivitiException("Priority does not resolve to a number: " + priority, e);
          }
        } else if (priority instanceof Number) {
          task.setPriority(((Number) priority).intValue());
        } else {
          throw new ActivitiException("Priority expression does not resolve to a number: " + 
                  taskDefinition.getPriorityExpression().getExpressionText());
        }
      }
    }
    
    handleAssignments(task, execution);
   
    // All properties set, now firing 'create' event
    task.fireEvent(TaskListener.EVENTNAME_CREATE);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior#signal(org.activiti.engine.impl.pvm.delegate.ActivityExecution, java.lang.String, java.lang.Object)
   */
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    leave(execution);
  }

  /**
   * Handle assignments.
   *
   * @param task the task
   * @param execution the execution
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void handleAssignments(TaskEntity task, ActivityExecution execution) {
    if (taskDefinition.getAssigneeExpression() != null) {
      task.setAssignee((String) taskDefinition.getAssigneeExpression().getValue(execution));
    }

    if (!taskDefinition.getCandidateGroupIdExpressions().isEmpty()) {
      for (Expression groupIdExpr : taskDefinition.getCandidateGroupIdExpressions()) {
        Object value = groupIdExpr.getValue(execution);
        if (value instanceof String) {
          task.addCandidateGroup((String) value);
        } else if (value instanceof Collection) {
          task.addCandidateGroups((Collection) value);
        } else {
          throw new ActivitiException("Expression did not resolve to a string or collection of strings");
        }
      }
    }

    if (!taskDefinition.getCandidateUserIdExpressions().isEmpty()) {
      for (Expression userIdExpr : taskDefinition.getCandidateUserIdExpressions()) {
        Object value = userIdExpr.getValue(execution);
        if (value instanceof String) {
          task.addCandidateUser((String) value);
        } else if (value instanceof Collection) {
          task.addCandidateUsers((Collection) value);
        } else {
          throw new ActivitiException("Expression did not resolve to a string or collection of strings");
        }
      }
    }
  }

  // getters and setters //////////////////////////////////////////////////////
  
  /**
   * Gets the task definition.
   *
   * @return the task definition
   */
  public TaskDefinition getTaskDefinition() {
    return taskDefinition;
  }
  
  /**
   * Gets the expression manager.
   *
   * @return the expression manager
   */
  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }

}
