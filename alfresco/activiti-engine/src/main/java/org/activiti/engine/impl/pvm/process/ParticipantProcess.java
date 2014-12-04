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


// TODO: Auto-generated Javadoc
/**
 * Object indicating that a {@link ProcessDefinitionImpl} is a participant in a collaboration (pool). 
 * Currently only used to store graphical information and the pool name.
 * 
 * @author Frederik Heremans
 */
public class ParticipantProcess implements HasDIBounds {

  /** The id. */
  protected String id;
  
  /** The name. */
  protected String name;
  
  /** The x. */
  protected int x = -1;
  
  /** The y. */
  protected int y = -1;
  
  /** The width. */
  protected int width = -1;
  
  /** The height. */
  protected int height = -1;
  
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
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.HasDIBounds#getX()
   */
  public int getX() {
    return x;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.HasDIBounds#setX(int)
   */
  public void setX(int x) {
    this.x = x;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.HasDIBounds#getY()
   */
  public int getY() {
    return y;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.HasDIBounds#setY(int)
   */
  public void setY(int y) {
    this.y = y;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.HasDIBounds#getWidth()
   */
  public int getWidth() {
    return width;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.HasDIBounds#setWidth(int)
   */
  public void setWidth(int width) {
    this.width = width;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.HasDIBounds#getHeight()
   */
  public int getHeight() {
    return height;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.HasDIBounds#setHeight(int)
   */
  public void setHeight(int height) {
    this.height = height;
  }
  
  
}
