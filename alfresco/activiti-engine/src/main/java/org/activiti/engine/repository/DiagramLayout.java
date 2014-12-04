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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


// TODO: Auto-generated Javadoc
/**
 * Stores a two-dimensional graph layout.
 *
 * @author Falko Menge
 */
public class DiagramLayout implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The elements. */
  private Map<String, DiagramElement> elements;

  /**
   * Instantiates a new diagram layout.
   *
   * @param elements the elements
   */
  public DiagramLayout(Map<String, DiagramElement> elements) {
    this.setElements(elements);
  }

  /**
   * Gets the node.
   *
   * @param id the id
   * @return the node
   */
  public DiagramNode getNode(String id) {
    DiagramElement element = getElements().get(id);
    if (element instanceof DiagramNode) {
      return (DiagramNode) element;
    } else {
      return null;
    }
  }
  
  /**
   * Gets the edge.
   *
   * @param id the id
   * @return the edge
   */
  public DiagramEdge getEdge(String id) {
    DiagramElement element = getElements().get(id);
    if (element instanceof DiagramEdge) {
      return (DiagramEdge) element;
    } else {
      return null;
    }
  }
  
  /**
   * Gets the elements.
   *
   * @return the elements
   */
  public Map<String, DiagramElement> getElements() {
    return elements;
  }
  
  /**
   * Sets the elements.
   *
   * @param elements the elements
   */
  public void setElements(Map<String, DiagramElement> elements) {
    this.elements = elements;
  }

  /**
   * Gets the nodes.
   *
   * @return the nodes
   */
  public List<DiagramNode> getNodes() {
    List<DiagramNode> nodes = new ArrayList<DiagramNode>();
    for (Entry<String, DiagramElement> entry : getElements().entrySet()) {
      DiagramElement element = entry.getValue();
      if (element instanceof DiagramNode) {
        nodes.add((DiagramNode) element);
      }
    }
    return nodes;
  }
  
}
