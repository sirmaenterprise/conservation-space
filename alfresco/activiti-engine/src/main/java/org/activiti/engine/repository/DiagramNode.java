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

package org.activiti.engine.repository;

// TODO: Auto-generated Javadoc
/**
 * Stores position and dimensions of a diagram node.
 *
 * @author Falko Menge
 */
public class DiagramNode extends DiagramElement {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The x. */
  private Double x = null;
  
  /** The y. */
  private Double y = null;
  
  /** The width. */
  private Double width = null;
  
  /** The height. */
  private Double height = null;

  /**
   * Instantiates a new diagram node.
   */
  public DiagramNode() {
    super();
  }
  
  /**
   * Instantiates a new diagram node.
   *
   * @param id the id
   */
  public DiagramNode(String id) {
    super(id);
  }

  /**
   * Instantiates a new diagram node.
   *
   * @param id the id
   * @param x the x
   * @param y the y
   * @param width the width
   * @param height the height
   */
  public DiagramNode(String id, Double x, Double y, Double width, Double height) {
    super(id);
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  /**
   * Gets the x.
   *
   * @return the x
   */
  public Double getX() {
    return x;
  }

  /**
   * Sets the x.
   *
   * @param x the new x
   */
  public void setX(Double x) {
    this.x = x;
  }

  /**
   * Gets the y.
   *
   * @return the y
   */
  public Double getY() {
    return y;
  }

  /**
   * Sets the y.
   *
   * @param y the new y
   */
  public void setY(Double y) {
    this.y = y;
  }

  /**
   * Gets the width.
   *
   * @return the width
   */
  public Double getWidth() {
    return width;
  }

  /**
   * Sets the width.
   *
   * @param width the new width
   */
  public void setWidth(Double width) {
    this.width = width;
  }

  /**
   * Gets the height.
   *
   * @return the height
   */
  public Double getHeight() {
    return height;
  }

  /**
   * Sets the height.
   *
   * @param height the new height
   */
  public void setHeight(Double height) {
    this.height = height;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.DiagramElement#toString()
   */
  @Override
  public String toString() {
    return super.toString() + ", x=" + getX() + ", y=" + getY() + ", width=" + getWidth() + ", height=" + getHeight();
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.DiagramElement#isNode()
   */
  @Override
  public boolean isNode() {
    return true;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.DiagramElement#isEdge()
   */
  @Override
  public boolean isEdge() {
    return false;
  }

}
