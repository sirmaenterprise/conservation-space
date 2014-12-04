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
package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.ActivitiException;

// TODO: Auto-generated Javadoc
/**
 * Contains a predefined set of states for process definitions and process instances.
 *
 * @author Daniel Meyer
 */
public interface SuspensionState {
  
  /** The active. */
  SuspensionState ACTIVE = new SuspensionStateImpl(1, "active");
  
  /** The suspended. */
  SuspensionState SUSPENDED = new SuspensionStateImpl(2, "suspended");
  
  /**
   * Gets the state code.
   *
   * @return the state code
   */
  int getStateCode();
  
  ///////////////////////////////////////////////////// default implementation 
  
  /**
   * The Class SuspensionStateImpl.
   */
  static class SuspensionStateImpl implements SuspensionState {

    /** The state code. */
    public final int stateCode;
    
    /** The name. */
    protected final String name;   

    /**
     * Instantiates a new suspension state impl.
     *
     * @param suspensionCode the suspension code
     * @param string the string
     */
    public SuspensionStateImpl(int suspensionCode, String string) {
      this.stateCode = suspensionCode;
      this.name = string;
    }    
   
    /* (non-Javadoc)
     * @see org.activiti.engine.impl.persistence.entity.SuspensionState#getStateCode()
     */
    public int getStateCode() {     
      return stateCode;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + stateCode;
      return result;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      SuspensionStateImpl other = (SuspensionStateImpl) obj;
      if (stateCode != other.stateCode)
        return false;
      return true;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return name;
    }
  }
  
  /////////////////////////////////////////// helper class
  
  /**
   * The Class SuspensionStateUtil.
   */
  public static class SuspensionStateUtil{
    
    /**
     * Sets the suspension state.
     *
     * @param processDefinitionEntity the process definition entity
     * @param state the state
     */
    public static void setSuspensionState(ProcessDefinitionEntity processDefinitionEntity, SuspensionState state) {
      if(processDefinitionEntity.getSuspensionState() == state.getStateCode()) {
        throw new ActivitiException("Cannot set suspension state '"+state+"' for "+processDefinitionEntity+"': already in state '"+state+"'.");
      }
      processDefinitionEntity.setSuspensionState(state.getStateCode());
    }   
    
    /**
     * Sets the suspension state.
     *
     * @param executionEntity the execution entity
     * @param state the state
     */
    public static void setSuspensionState(ExecutionEntity executionEntity, SuspensionState state) {
      if(executionEntity.getSuspensionState() == state.getStateCode()) {
        throw new ActivitiException("Cannot set suspension state '"+state+"' for "+executionEntity+"': already in state '"+state+"'.");
      }
      executionEntity.setSuspensionState(state.getStateCode());
    }   
  }
  
}
