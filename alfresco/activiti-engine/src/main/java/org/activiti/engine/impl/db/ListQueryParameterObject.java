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
 * The Class ListQueryParameterObject.
 *
 * @author Daniel Meyer
 */
public class ListQueryParameterObject {
  
  /** The max results. */
  protected int maxResults = Integer.MAX_VALUE;
  
  /** The first result. */
  protected int firstResult = 0;
  
  /** The parameter. */
  protected Object parameter;
  
  /**
   * Instantiates a new list query parameter object.
   */
  public ListQueryParameterObject() {
  }
  
  /**
   * Instantiates a new list query parameter object.
   *
   * @param parameter the parameter
   * @param firstResult the first result
   * @param maxResults the max results
   */
  public ListQueryParameterObject(Object parameter, int firstResult, int maxResults) {
    this.parameter = parameter;
    this.firstResult = firstResult;
    this.maxResults = maxResults;
  }
  
  /**
   * Gets the first result.
   *
   * @return the first result
   */
  public int getFirstResult() {
    return firstResult;
  }
  
  /**
   * Gets the first row.
   *
   * @return the first row
   */
  public int getFirstRow() {
    return firstResult +1;
  }
  
  /**
   * Gets the last row.
   *
   * @return the last row
   */
  public int getLastRow() {
    if(maxResults == Integer.MAX_VALUE) {
      return maxResults;
    }
    return  firstResult + maxResults + 1;
  }
  
  /**
   * Gets the max results.
   *
   * @return the max results
   */
  public int getMaxResults() {
    return maxResults;
  }
  
  /**
   * Gets the parameter.
   *
   * @return the parameter
   */
  public Object getParameter() {
    return parameter;
  }
    
  /**
   * Sets the first result.
   *
   * @param firstResult the new first result
   */
  public void setFirstResult(int firstResult) {
    this.firstResult = firstResult;
  }
    
  /**
   * Sets the max results.
   *
   * @param maxResults the new max results
   */
  public void setMaxResults(int maxResults) {
    this.maxResults = maxResults;
  }
    
  /**
   * Sets the parameter.
   *
   * @param parameter the new parameter
   */
  public void setParameter(Object parameter) {
    this.parameter = parameter;
  }

}
