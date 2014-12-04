/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.interceptor;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;


// TODO: Auto-generated Javadoc
/**
 * A factory for creating CommandContext objects.
 *
 * @author Tom Baeyens
 */
public class CommandContextFactory {

  /** The process engine configuration. */
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  
  /**
   * Creates a new CommandContext object.
   *
   * @param cmd the cmd
   * @return the command context
   */
  public CommandContext createCommandContext(Command<?> cmd) {
    return new CommandContext(cmd, processEngineConfiguration);
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  /**
   * Gets the process engine configuration.
   *
   * @return the process engine configuration
   */
  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  /**
   * Sets the process engine configuration.
   *
   * @param processEngineConfiguration the new process engine configuration
   */
  public void setProcessEngineConfiguration(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
  }
}