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

package org.activiti.engine.impl.pvm.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.pvm.PvmTransition;


// TODO: Auto-generated Javadoc
/**
 * The Class TransitionImpl.
 *
 * @author Tom Baeyens
 */
public class TransitionImpl extends ProcessElementImpl implements PvmTransition {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The source. */
  protected ActivityImpl source;
  
  /** The destination. */
  protected ActivityImpl destination;
  
  /** The execution listeners. */
  protected List<ExecutionListener> executionListeners;
  
  /** Graphical information: a list of waypoints: x1, y1, x2, y2, x3, y3, .. */
  protected List<Integer> waypoints = new ArrayList<Integer>();

  /**
   * Instantiates a new transition impl.
   *
   * @param id the id
   * @param processDefinition the process definition
   */
  public TransitionImpl(String id, ProcessDefinitionImpl processDefinition) {
    super(id, processDefinition);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.PvmTransition#getSource()
   */
  public ActivityImpl getSource() {
    return source;
  }

  /**
   * Sets the destination.
   *
   * @param destination the new destination
   */
  public void setDestination(ActivityImpl destination) {
    this.destination = destination;
    destination.getIncomingTransitions().add(this);
  }
  
  /**
   * Adds the execution listener.
   *
   * @param executionListener the execution listener
   */
  public void addExecutionListener(ExecutionListener executionListener) {
    if (executionListeners==null) {
      executionListeners = new ArrayList<ExecutionListener>();
    }
    executionListeners.add(executionListener);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "("+source.getId()+")--"+(id!=null?id+"-->(":">(")+destination.getId()+")";
  }

  /**
   * Gets the execution listeners.
   *
   * @return the execution listeners
   */
  @SuppressWarnings("unchecked")
  public List<ExecutionListener> getExecutionListeners() {
    if (executionListeners==null) {
      return Collections.EMPTY_LIST;
    }
    return executionListeners;
  }

  // getters and setters //////////////////////////////////////////////////////

  /**
   * Sets the source.
   *
   * @param source the new source
   */
  protected void setSource(ActivityImpl source) {
    this.source = source;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.PvmTransition#getDestination()
   */
  public ActivityImpl getDestination() {
    return destination;
  }
  
  /**
   * Sets the execution listeners.
   *
   * @param executionListeners the new execution listeners
   */
  public void setExecutionListeners(List<ExecutionListener> executionListeners) {
    this.executionListeners = executionListeners;
  }

  /**
   * Gets the waypoints.
   *
   * @return the waypoints
   */
  public List<Integer> getWaypoints() {
    return waypoints;
  }
  
  /**
   * Sets the waypoints.
   *
   * @param waypoints the new waypoints
   */
  public void setWaypoints(List<Integer> waypoints) {
    this.waypoints = waypoints;
  }
  
}
