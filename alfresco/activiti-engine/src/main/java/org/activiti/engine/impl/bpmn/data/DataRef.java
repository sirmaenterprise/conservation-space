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
package org.activiti.engine.impl.bpmn.data;

// TODO: Auto-generated Javadoc
/**
 * Implementation of the BPMN 2.0 'dataInputRef' and 'dataOutputRef'
 * 
 * @author Esteban Robles Luna
 */
public class DataRef {

  /** The id ref. */
  protected String idRef;
  
  /**
   * Instantiates a new data ref.
   *
   * @param idRef the id ref
   */
  public DataRef(String idRef) {
    this.idRef = idRef;
  }
  
  /**
   * Gets the id ref.
   *
   * @return the id ref
   */
  public String getIdRef() {
    return this.idRef;
  }
}
