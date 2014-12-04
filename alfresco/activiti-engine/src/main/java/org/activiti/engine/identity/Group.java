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
package org.activiti.engine.identity;

import java.io.Serializable;

import org.activiti.engine.IdentityService;


// TODO: Auto-generated Javadoc
/**
 * Represents a group, used in {@link IdentityService}.
 * 
 * @author Tom Baeyens
 */
public interface Group extends Serializable {

  /**
   * Gets the id.
   *
   * @return the id
   */
  String getId();
  
  /**
   * Sets the id.
   *
   * @param id the new id
   */
  void setId(String id);
  
  /**
   * Gets the name.
   *
   * @return the name
   */
  String getName();
  
  /**
   * Sets the name.
   *
   * @param name the new name
   */
  void setName(String name);
  
  /**
   * Gets the type.
   *
   * @return the type
   */
  String getType();
  
  /**
   * Sets the type.
   *
   * @param string the new type
   */
  void setType(String string);
}
