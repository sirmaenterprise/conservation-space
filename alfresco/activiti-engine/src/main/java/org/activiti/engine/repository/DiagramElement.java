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

package org.activiti.engine.repository;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * Represents a diagram node.
 *
 * @author Falko Menge
 */
abstract public class DiagramElement implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The id. */
  protected String id = null;

  /**
   * Instantiates a new diagram element.
   */
  public DiagramElement() {
  }

  /**
   * Instantiates a new diagram element.
   *
   * @param id the id
   */
  public DiagramElement(String id) {
    this.id = id;
  }

  /**
   * Id of the diagram element.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "id=" + getId();
  }

  /**
   * Checks if is node.
   *
   * @return true, if is node
   */
  public abstract boolean isNode();
  
  /**
   * Checks if is edge.
   *
   * @return true, if is edge
   */
  public abstract boolean isEdge();

}