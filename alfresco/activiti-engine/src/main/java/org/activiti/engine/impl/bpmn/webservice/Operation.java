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

// TODO: Auto-generated Javadoc
/**
 * An Operation is part of an {@link BpmnInterface} and it defines Messages that are consumed and
 * (optionally) produced when the Operation is called.
 * 
 * @author Joram Barrez
 */
public class Operation {
  
  /** The id. */
  protected String id;
  
  /** The name. */
  protected String name;
  
  /** The in message. */
  protected MessageDefinition inMessage;
  
  /** The out message. */
  protected MessageDefinition outMessage;
  
  /** The implementation. */
  protected OperationImplementation implementation;
  
  /** The interface to which this operations belongs. */
  protected BpmnInterface bpmnInterface;
  
  /**
   * Instantiates a new operation.
   */
  public Operation() {
    
  }
  
  /**
   * Instantiates a new operation.
   *
   * @param id the id
   * @param name the name
   * @param bpmnInterface the bpmn interface
   * @param inMessage the in message
   */
  public Operation(String id, String name, BpmnInterface bpmnInterface, MessageDefinition inMessage) {
    setId(id);
    setName(name);
    setInterface(bpmnInterface);
    setInMessage(inMessage);
  }
  
  /**
   * Send message.
   *
   * @param message the message
   * @return the message instance
   */
  public MessageInstance sendMessage(MessageInstance message) {
    return this.implementation.sendFor(message, this);
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
   * Gets the interface.
   *
   * @return the interface
   */
  public BpmnInterface getInterface() {
    return bpmnInterface;
  }

  /**
   * Sets the interface.
   *
   * @param bpmnInterface the new interface
   */
  public void setInterface(BpmnInterface bpmnInterface) {
    this.bpmnInterface = bpmnInterface;
  }

  /**
   * Gets the in message.
   *
   * @return the in message
   */
  public MessageDefinition getInMessage() {
    return inMessage;
  }

  /**
   * Sets the in message.
   *
   * @param inMessage the new in message
   */
  public void setInMessage(MessageDefinition inMessage) {
    this.inMessage = inMessage;
  }

  /**
   * Gets the out message.
   *
   * @return the out message
   */
  public MessageDefinition getOutMessage() {
    return outMessage;
  }

  /**
   * Sets the out message.
   *
   * @param outMessage the new out message
   */
  public void setOutMessage(MessageDefinition outMessage) {
    this.outMessage = outMessage;
  }

  /**
   * Gets the implementation.
   *
   * @return the implementation
   */
  public OperationImplementation getImplementation() {
    return implementation;
  }

  /**
   * Sets the implementation.
   *
   * @param implementation the new implementation
   */
  public void setImplementation(OperationImplementation implementation) {
    this.implementation = implementation;
  }
}
