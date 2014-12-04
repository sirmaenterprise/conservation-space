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
package org.activiti.engine.impl.bpmn.webservice;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * An Interface defines a set of operations that are implemented by services
 * external to the process.
 * 
 * @author Joram Barrez
 */
public class BpmnInterface {
  
  /** The id. */
  protected String id;
  
  /** The name. */
  protected String name;
  
  /** The implementation. */
  protected BpmnInterfaceImplementation implementation;
  
  /**
   * Mapping of the operations of this interface.
   * The key of the map is the id of the operation, for easy retrieval.
   */
  protected Map<String, Operation> operations = new HashMap<String, Operation>();
  
  /**
   * Instantiates a new bpmn interface.
   */
  public BpmnInterface() {
    
  }
  
  /**
   * Instantiates a new bpmn interface.
   *
   * @param id the id
   * @param name the name
   */
  public BpmnInterface(String id, String name) {
    setId(id);
    setName(name);
  }

  /**
   * Gets the id.
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

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * Adds the operation.
   *
   * @param operation the operation
   */
  public void addOperation(Operation operation) {
    operations.put(operation.getId(), operation);
  }
  
  /**
   * Gets the operation.
   *
   * @param operationId the operation id
   * @return the operation
   */
  public Operation getOperation(String operationId) {
    return operations.get(operationId);
  }
  
  /**
   * Gets the operations.
   *
   * @return the operations
   */
  public Collection<Operation> getOperations() {
    return operations.values();
  }

  /**
   * Gets the implementation.
   *
   * @return the implementation
   */
  public BpmnInterfaceImplementation getImplementation() {
    return implementation;
  }

  /**
   * Sets the implementation.
   *
   * @param implementation the new implementation
   */
  public void setImplementation(BpmnInterfaceImplementation implementation) {
    this.implementation = implementation;
  }
}
