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
package org.activiti.engine.impl.util.xml;

import org.xml.sax.SAXParseException;


// TODO: Auto-generated Javadoc
/**
 * The Class Problem.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class Problem {

  /** The error message. */
  protected String errorMessage;
  
  /** The resource. */
  protected String resource;
  
  /** The line. */
  protected int line;
  
  /** The column. */
  protected int column;

  /**
   * Instantiates a new problem.
   *
   * @param e the e
   * @param resource the resource
   */
  public Problem(SAXParseException e, String resource) {
    Throwable exception = e;
    while (exception!=null) {
      if (this.errorMessage==null) {
        this.errorMessage = exception.getMessage(); 
      } else {
        this.errorMessage += ": "+exception.getMessage();
      }
      exception = exception.getCause();
    }
    this.resource = resource;
    this.line = e.getLineNumber();
    this.column = e.getColumnNumber();
  }
  
  /**
   * Instantiates a new problem.
   *
   * @param errorMessage the error message
   * @param resourceName the resource name
   * @param element the element
   */
  public Problem(String errorMessage, String resourceName, Element element) {
    this.errorMessage = errorMessage;
    this.resource = resourceName;
    if (element!=null) {
      this.line = element.getLine();
      this.column = element.getColumn();
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return errorMessage+(resource!=null ? " | "+resource : "")+" | line "+line+" | column "+column;
  }
}
