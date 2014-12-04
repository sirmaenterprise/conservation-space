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

import java.util.HashMap;
import java.util.Map;


// TODO: Auto-generated Javadoc
/**
 * The Class Direction.
 *
 * @author Tom Baeyens
 */
public class Direction {

  /** The Constant directions. */
  private static final Map<String, Direction> directions = new HashMap<String, Direction>();

  /** The Constant ASCENDING. */
  public static final Direction ASCENDING = new Direction("asc");
  
  /** The Constant DESCENDING. */
  public static final Direction DESCENDING = new Direction("desc");
  
  /** The name. */
  private String name;
  
  /**
   * Instantiates a new direction.
   *
   * @param name the name
   */
  public Direction(String name) {
    this.name = name;
    directions.put(name, this);
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
   * Find by name.
   *
   * @param directionName the direction name
   * @return the direction
   */
  public static Direction findByName(String directionName) {
    return directions.get(directionName);
  }
}
