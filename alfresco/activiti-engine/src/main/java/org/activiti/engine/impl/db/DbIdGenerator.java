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

package org.activiti.engine.impl.db;

import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cmd.GetNextIdBlockCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;


// TODO: Auto-generated Javadoc
/**
 * The Class DbIdGenerator.
 *
 * @author Tom Baeyens
 */
public class DbIdGenerator implements IdGenerator {

  /** The id block size. */
  protected int idBlockSize;
  
  /** The next id. */
  protected long nextId = 0;
  
  /** The last id. */
  protected long lastId = -1;
  
  /** The command executor. */
  protected CommandExecutor commandExecutor;
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.cfg.IdGenerator#getNextId()
   */
  public synchronized String getNextId() {
    if (lastId<nextId) {
      getNewBlock();
    }
    long _nextId = nextId++;
    return Long.toString(_nextId);
  }

  /**
   * Gets the new block.
   *
   * @return the new block
   */
  protected synchronized void getNewBlock() {
    // TODO http://jira.codehaus.org/browse/ACT-45 use a separate 'requiresNew' command executor
    IdBlock idBlock = commandExecutor.execute(new GetNextIdBlockCmd(idBlockSize));
    this.nextId = idBlock.getNextId();
    this.lastId = idBlock.getLastId();
  }

  /**
   * Gets the id block size.
   *
   * @return the id block size
   */
  public int getIdBlockSize() {
    return idBlockSize;
  }

  /**
   * Sets the id block size.
   *
   * @param idBlockSize the new id block size
   */
  public void setIdBlockSize(int idBlockSize) {
    this.idBlockSize = idBlockSize;
  }
  
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
