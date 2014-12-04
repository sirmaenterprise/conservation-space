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

import org.activiti.engine.impl.interceptor.CommandExecutor;



// TODO: Auto-generated Javadoc
/**
 * The Class ServiceImpl.
 *
 * @author Tom Baeyens
 */
public class ServiceImpl {

  /** The command executor. */
  protected CommandExecutor commandExecutor;
  
  /**
   * Gets the command executor.
   *
   * @return the command executor
   */
  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  /**
   * Sets the command executor.
   *
   * @param commandExecutor the new command executor
   */
  public void setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }
}
