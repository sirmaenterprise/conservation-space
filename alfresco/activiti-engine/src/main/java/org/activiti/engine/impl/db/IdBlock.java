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

// TODO: Auto-generated Javadoc
/**
 * The Class IdBlock.
 *
 * @author Tom Baeyens
 */
public class IdBlock {

  /** The next id. */
  long nextId;
  
  /** The last id. */
  long lastId;

  /**
   * Instantiates a new id block.
   *
   * @param nextId the next id
   * @param lastId the last id
   */
  public IdBlock(long nextId, long lastId) {
    this.nextId = nextId;
    this.lastId = lastId;
  }

  /**
   * Gets the next id.
   *
   * @return the next id
   */
  public long getNextId() {
    return nextId;
  }
  
  /**
   * Gets the last id.
   *
   * @return the last id
   */
  public long getLastId() {
    return lastId;
  }
}
