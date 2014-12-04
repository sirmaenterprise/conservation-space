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

package org.activiti.engine.impl.bpmn.helper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ServiceTaskJavaDelegateActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ExecutionListenerInvocation;
import org.activiti.engine.impl.delegate.TaskListenerInvocation;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.SignallableActivityBehavior;
import org.activiti.engine.impl.util.ReflectUtil;


// TODO: Auto-generated Javadoc
/**
 * Helper class for bpmn constructs that allow class delegation.
 *
 * This class will lazily instantiate the referenced classes when needed at runtime.
 * 
 * @author Joram Barrez
 * @author Falko Menge
 */
public class ClassDelegate extends AbstractBpmnActivityBehavior implements TaskListener, ExecutionListener {
  
  /** The class name. */
  protected String className;
  
  /** The field declarations. */
  protected List<FieldDeclaration> fieldDeclarations;
  
  /** The execution listener instance. */
  protected ExecutionListener executionListenerInstance;
  
  /** The task listener instance. */
  protected TaskListener taskListenerInstance;
  
  /** The activity behavior instance. */
  protected ActivityBehavior activityBehaviorInstance;
  
  /**
   * Instantiates a new class delegate.
   *
   * @param className the class name
   * @param fieldDeclarations the field declarations
   */
  public ClassDelegate(String className, List<FieldDeclaration> fieldDeclarations) {
    this.className = className;
    this.fieldDeclarations = fieldDeclarations;
  }
  
  /**
   * Instantiates a new class delegate.
   *
   * @param clazz the clazz
   * @param fieldDeclarations the field declarations
   */
  public ClassDelegate(Class<?> clazz, List<FieldDeclaration> fieldDeclarations) {
    this(clazz.getName(), fieldDeclarations);
  }

  // Execution listener
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.ExecutionListener#notify(org.activiti.engine.delegate.DelegateExecution)
   */
  public void notify(DelegateExecution execution) throws Exception {
    if (executionListenerInstance == null) {
      executionListenerInstance = getExecutionListenerInstance();
    }
    Context.getProcessEngineConfiguration()
      .getDelegateInterceptor()
      .handleInvocation(new ExecutionListenerInvocation(executionListenerInstance, execution));
  }

  /**
   * Gets the execution listener instance.
   *
   * @return the execution listener instance
   */
  protected ExecutionListener getExecutionListenerInstance() {
    Object delegateInstance = instantiateDelegate(className, fieldDeclarations);
    if (delegateInstance instanceof ExecutionListener) {
      return (ExecutionListener) delegateInstance; 
    } else if (delegateInstance instanceof JavaDelegate) {
      return new ServiceTaskJavaDelegateActivityBehavior((JavaDelegate) delegateInstance);
    } else {
      throw new ActivitiException(delegateInstance.getClass().getName()+" doesn't implement "+ExecutionListener.class+" nor "+JavaDelegate.class);
    }
  }
  
  // Task listener
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.TaskListener#notify(org.activiti.engine.delegate.DelegateTask)
   */
  public void notify(DelegateTask delegateTask) {
    if (taskListenerInstance == null) {
      taskListenerInstance = getTaskListenerInstance();
    }
    try {
      Context.getProcessEngineConfiguration()
        .getDelegateInterceptor()
        .handleInvocation(new TaskListenerInvocation(taskListenerInstance, delegateTask));
    }catch (Exception e) {
      throw new ActivitiException("Exception while invoking TaskListener: "+e.getMessage(), e);
    }
  }
  
  /**
   * Gets the task listener instance.
   *
   * @return the task listener instance
   */
  protected TaskListener getTaskListenerInstance() {
    Object delegateInstance = instantiateDelegate(className, fieldDeclarations);
    if (delegateInstance instanceof TaskListener) {
      return (TaskListener) delegateInstance; 
    } else {
      throw new ActivitiException(delegateInstance.getClass().getName()+" doesn't implement "+TaskListener.class);
    }
  }

  // Activity Behavior
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.behavior.FlowNodeActivityBehavior#execute(org.activiti.engine.impl.pvm.delegate.ActivityExecution)
   */
  public void execute(ActivityExecution execution) throws Exception {
    if (activityBehaviorInstance == null) {
      activityBehaviorInstance = getActivityBehaviorInstance(execution);
    }
    try {
      activityBehaviorInstance.execute(execution);
    } catch (BpmnError error) {
      ErrorPropagation.propagateError(error, execution);
    }
  }

  // Signallable activity behavior
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior#signal(org.activiti.engine.impl.pvm.delegate.ActivityExecution, java.lang.String, java.lang.Object)
   */
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    if (activityBehaviorInstance == null) {
      activityBehaviorInstance = getActivityBehaviorInstance(execution);
    }
    
    if (activityBehaviorInstance instanceof SignallableActivityBehavior) {
      ((SignallableActivityBehavior) activityBehaviorInstance).signal(execution, signalName, signalData);
    } else {
      throw new ActivitiException("signal() can only be called on a " + SignallableActivityBehavior.class.getName() + " instance");
    }
  }

  /**
   * Gets the activity behavior instance.
   *
   * @param execution the execution
   * @return the activity behavior instance
   */
  protected ActivityBehavior getActivityBehaviorInstance(ActivityExecution execution) {
    Object delegateInstance = instantiateDelegate(className, fieldDeclarations);
    
    if (delegateInstance instanceof ActivityBehavior) {
      return determineBehaviour((ActivityBehavior) delegateInstance, execution);
    } else if (delegateInstance instanceof JavaDelegate) {
      return determineBehaviour(new ServiceTaskJavaDelegateActivityBehavior((JavaDelegate) delegateInstance), execution);
    } else {
      throw new ActivitiException(delegateInstance.getClass().getName()+" doesn't implement "+JavaDelegate.class.getName()+" nor "+ActivityBehavior.class.getName());
    }
  }
  
  // Adds properties to the given delegation instance (eg multi instance) if needed
  /**
   * Determine behaviour.
   *
   * @param delegateInstance the delegate instance
   * @param execution the execution
   * @return the activity behavior
   */
  protected ActivityBehavior determineBehaviour(ActivityBehavior delegateInstance, ActivityExecution execution) {
    if (hasMultiInstanceCharacteristics()) {
      multiInstanceActivityBehavior.setInnerActivityBehavior((AbstractBpmnActivityBehavior) delegateInstance);
      return multiInstanceActivityBehavior;
    }
    return delegateInstance;
  }
  
  // --HELPER METHODS (also usable by external classes) ----------------------------------------
  
  /**
   * Instantiate delegate.
   *
   * @param clazz the clazz
   * @param fieldDeclarations the field declarations
   * @return the object
   */
  public static Object instantiateDelegate(Class<?> clazz, List<FieldDeclaration> fieldDeclarations) {
    return instantiateDelegate(clazz.getName(), fieldDeclarations);
  }
  
  /**
   * Instantiate delegate.
   *
   * @param className the class name
   * @param fieldDeclarations the field declarations
   * @return the object
   */
  public static Object instantiateDelegate(String className, List<FieldDeclaration> fieldDeclarations) {
    Object object = ReflectUtil.instantiate(className);
    if(fieldDeclarations != null) {
      for(FieldDeclaration declaration : fieldDeclarations) {
        applyFieldDeclaration(declaration, object);
      }
    }
    return object;
  }
  
  /**
   * Apply field declaration.
   *
   * @param declaration the declaration
   * @param target the target
   */
  private static void applyFieldDeclaration(FieldDeclaration declaration, Object target) {
    Method setterMethod = ReflectUtil.getSetter(declaration.getName(), 
      target.getClass(), declaration.getValue().getClass());
    
    if(setterMethod != null) {
      try {
        setterMethod.invoke(target, declaration.getValue());
      } catch (IllegalArgumentException e) {
        throw new ActivitiException("Error while invoking '" + declaration.getName() + "' on class " + target.getClass().getName(), e);
      } catch (IllegalAccessException e) {
        throw new ActivitiException("Illegal acces when calling '" + declaration.getName() + "' on class " + target.getClass().getName(), e);
      } catch (InvocationTargetException e) {
        throw new ActivitiException("Exception while invoking '" + declaration.getName() + "' on class " + target.getClass().getName(), e);
      }
    } else {
      Field field = ReflectUtil.getField(declaration.getName(), target);
      if(field == null) {
        throw new ActivitiException("Field definition uses unexisting field '" + declaration.getName() + "' on class " + target.getClass().getName());
      }
      // Check if the delegate field's type is correct
     if(!fieldTypeCompatible(declaration, field)) {
       throw new ActivitiException("Incompatible type set on field declaration '" + declaration.getName() 
          + "' for class " + target.getClass().getName() 
          + ". Declared value has type " + declaration.getValue().getClass().getName() 
          + ", while expecting " + field.getType().getName());
     }
     ReflectUtil.setField(field, target, declaration.getValue());
    }
  }
  
  /**
   * Field type compatible.
   *
   * @param declaration the declaration
   * @param field the field
   * @return true, if successful
   */
  public static boolean fieldTypeCompatible(FieldDeclaration declaration, Field field) {
    if(declaration.getValue() != null) {
      return field.getType().isAssignableFrom(declaration.getValue().getClass());
    } else {      
      // Null can be set any field type
      return true;
    }
  }

  /**
   * returns the class name this {@link ClassDelegate} is configured to. Comes in handy if you want to
   * check which delegates you already have e.g. in a list of listeners
   *
   * @return the class name
   */
  public String getClassName() {
    return className;
  }

}
