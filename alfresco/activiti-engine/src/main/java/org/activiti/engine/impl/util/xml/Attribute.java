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


// TODO: Auto-generated Javadoc
/**
 * The Class Attribute.
 *
 * @author Joram Barrez
 */
public class Attribute {
  
  /** The name. */
  protected String name;
  
  /** The value. */
  protected String value;

  /** The uri. */
  protected String uri;
  
  /**
   * Instantiates a new attribute.
   *
   * @param name the name
   * @param value the value
   */
  public Attribute(String name, String value) {
    this.name = name;
    this.value = value;
  }
  
  /**
   * Instantiates a new attribute.
   *
   * @param name the name
   * @param value the value
   * @param uri the uri
   */
  public Attribute(String name, String value, String uri) {
    this(name, value);
    this.uri = uri;
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
   * Gets the value.
   *
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets the value.
   *
   * @param value the new value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Gets the uri.
   *
   * @return the uri
   */
  public String getUri() {
    return uri;
  }

  /**
   * Sets the uri.
   *
   * @param uri the new uri
   */
  public void setUri(String uri) {
    this.uri = uri;
  }
  
}
