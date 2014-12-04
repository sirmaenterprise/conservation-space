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
package org.activiti.engine.impl;

import java.io.Serializable;

import org.activiti.engine.ProcessEngineInfo;


// TODO: Auto-generated Javadoc
/**
 * The Class ProcessEngineInfoImpl.
 *
 * @author Tom Baeyens
 */
public class ProcessEngineInfoImpl implements Serializable, ProcessEngineInfo {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The name. */
  String name;
  
  /** The resource url. */
  String resourceUrl;
  
  /** The exception. */
  String exception;

  /**
   * Instantiates a new process engine info impl.
   *
   * @param name the name
   * @param resourceUrl the resource url
   * @param exception the exception
   */
  public ProcessEngineInfoImpl(String name, String resourceUrl, String exception) {
    this.name = name;
    this.resourceUrl = resourceUrl;
    this.exception = exception;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineInfo#getName()
   */
  public String getName() {
    return name;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineInfo#getResourceUrl()
   */
  public String getResourceUrl() {
    return resourceUrl;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineInfo#getException()
   */
  public String getException() {
    return exception;
  }
}
