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

package org.activiti.engine.impl.interceptor;


import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;

// TODO: Auto-generated Javadoc
/**
 * The Class CommandContextInterceptor.
 *
 * @author Tom Baeyens
 */
public class CommandContextInterceptor extends CommandInterceptor {

  /** The command context factory. */
  protected CommandContextFactory commandContextFactory;
  
  /** The process engine configuration. */
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  /**
   * Instantiates a new command context interceptor.
   */
  public CommandContextInterceptor() {
  }

  /**
   * Instantiates a new command context interceptor.
   *
   * @param commandContextFactory the command context factory
   * @param processEngineConfiguration the process engine configuration
   */
  public CommandContextInterceptor(CommandContextFactory commandContextFactory, ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.commandContextFactory = commandContextFactory;
    this.processEngineConfiguration = processEngineConfiguration;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.CommandExecutor#execute(org.activiti.engine.impl.interceptor.Command)
   */
  public <T> T execute(Command<T> command) {
    CommandContext context = commandContextFactory.createCommandContext(command);

    try {
      Context.setCommandContext(context);
      Context.setProcessEngineConfiguration(processEngineConfiguration);
      return next.execute(command);
      
    } catch (Exception e) {
      context.exception(e);
      
    } finally {
      try {
        context.close();
      } finally {
        Context.removeCommandContext();
        Context.removeProcessEngineConfiguration();
      }
    }
    
    return null;
  }
  
  /**
   * Gets the command context factory.
   *
   * @return the command context factory
   */
  public CommandContextFactory getCommandContextFactory() {
    return commandContextFactory;
  }
  
  /**
   * Sets the command context factory.
   *
   * @param commandContextFactory the new command context factory
   */
  public void setCommandContextFactory(CommandContextFactory commandContextFactory) {
    this.commandContextFactory = commandContextFactory;
  }

  /**
   * Gets the process engine configuration.
   *
   * @return the process engine configuration
   */
  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  /**
   * Sets the process engine context.
   *
   * @param processEngineContext the new process engine context
   */
  public void setProcessEngineContext(ProcessEngineConfigurationImpl processEngineContext) {
    this.processEngineConfiguration = processEngineContext;
  }
}
