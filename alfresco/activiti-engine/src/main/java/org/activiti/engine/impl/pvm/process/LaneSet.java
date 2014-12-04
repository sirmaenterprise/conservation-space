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
import java.util.List;


// TODO: Auto-generated Javadoc
/**
 * A BPMN 2.0 LaneSet, containg {@link Lane}s, currently only used for
 * rendering the DI info.
 * 
 * @author Frederik Heremans
 */
public class LaneSet {

  /** The id. */
  protected String id;
  
  /** The lanes. */
  protected List<Lane> lanes;
  
  /** The name. */
  protected String name;
    
  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
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
   * Gets the lanes.
   *
   * @return the lanes
   */
  public List<Lane> getLanes() {
    if(lanes == null) {
      lanes = new ArrayList<Lane>();
    }
    return lanes;
  }
  
  /**
   * Adds the lane.
   *
   * @param laneToAdd the lane to add
   */
  public void addLane(Lane laneToAdd) {
    getLanes().add(laneToAdd);
  }
  
  /**
   * Gets the lane for id.
   *
   * @param id the id
   * @return the lane for id
   */
  public Lane getLaneForId(String id) {
    if(lanes != null && lanes.size() > 0) {
      for(Lane lane : lanes) {
        if(id.equals(lane.getId())) {
          return lane;
        }
      }
    }
    return null;
  }
}