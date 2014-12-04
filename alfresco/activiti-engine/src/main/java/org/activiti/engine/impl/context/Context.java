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

package org.activiti.engine.impl.context;

import java.util.Stack;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.JobExecutorContext;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;


// TODO: Auto-generated Javadoc
/**
 * The Class Context.
 *
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class Context {

  /** The command context thread local. */
  protected static ThreadLocal<Stack<CommandContext>> commandContextThreadLocal = new ThreadLocal<Stack<CommandContext>>();
  
  /** The process engine configuration stack thread local. */
  protected static ThreadLocal<Stack<ProcessEngineConfigurationImpl>> processEngineConfigurationStackThreadLocal = new ThreadLocal<Stack<ProcessEngineConfigurationImpl>>();
  
  /** The execution context stack thread local. */
  protected static ThreadLocal<Stack<ExecutionContext>> executionContextStackThreadLocal = new ThreadLocal<Stack<ExecutionContext>>();
  
  /** The job executor context thread local. */
  protected static ThreadLocal<JobExecutorContext> jobExecutorContextThreadLocal = new ThreadLocal<JobExecutorContext>();

  /**
   * Gets the command context.
   *
   * @return the command context
   */
  public static CommandContext getCommandContext() {
    Stack<CommandContext> stack = getStack(commandContextThreadLocal);
    if (stack.isEmpty()) {
      return null;
    }
    return stack.peek();
  }

  /**
   * Sets the command context.
   *
   * @param commandContext the new command context
   */
  public static void setCommandContext(CommandContext commandContext) {
    getStack(commandContextThreadLocal).push(commandContext);
  }

  /**
   * Removes the command context.
   */
  public static void removeCommandContext() {
    getStack(commandContextThreadLocal).pop();
  }

  /**
   * Gets the process engine configuration.
   *
   * @return the process engine configuration
   */
  public static ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    Stack<ProcessEngineConfigurationImpl> stack = getStack(processEngineConfigurationStackThreadLocal);
    if (stack.isEmpty()) {
      return null;
    }
    return stack.peek();
  }

  /**
   * Sets the process engine configuration.
   *
   * @param processEngineConfiguration the new process engine configuration
   */
  public static void setProcessEngineConfiguration(ProcessEngineConfigurationImpl processEngineConfiguration) {
    getStack(processEngineConfigurationStackThreadLocal).push(processEngineConfiguration);
  }

  /**
   * Removes the process engine configuration.
   */
  public static void removeProcessEngineConfiguration() {
    getStack(processEngineConfigurationStackThreadLocal).pop();
  }

  /**
   * Gets the execution context.
   *
   * @return the execution context
   */
  public static ExecutionContext getExecutionContext() {
    return getStack(executionContextStackThreadLocal).peek();
  }

  /**
   * Sets the execution context.
   *
   * @param execution the new execution context
   */
  public static void setExecutionContext(InterpretableExecution execution) {
    getStack(executionContextStackThreadLocal).push(new ExecutionContext(execution));
  }

  /**
   * Removes the execution context.
   */
  public static void removeExecutionContext() {
    getStack(executionContextStackThreadLocal).pop();
  }

  /**
   * Gets the stack.
   *
   * @param <T> the generic type
   * @param threadLocal the thread local
   * @return the stack
   */
  protected static <T> Stack<T> getStack(ThreadLocal<Stack<T>> threadLocal) {
    Stack<T> stack = threadLocal.get();
    if (stack==null) {
      stack = new Stack<T>();
      threadLocal.set(stack);
    }
    return stack;
  }
  
  /**
   * Gets the job executor context.
   *
   * @return the job executor context
   */
  public static JobExecutorContext getJobExecutorContext() {
    return jobExecutorContextThreadLocal.get();
  }
  
  /**
   * Sets the job executor context.
   *
   * @param jobExecutorContext the new job executor context
   */
  public static void setJobExecutorContext(JobExecutorContext jobExecutorContext) {
    jobExecutorContextThreadLocal.set(jobExecutorContext);
  }
  
  /**
   * Removes the job executor context.
   */
  public static void removeJobExecutorContext() {
    jobExecutorContextThreadLocal.remove();
  }
}
