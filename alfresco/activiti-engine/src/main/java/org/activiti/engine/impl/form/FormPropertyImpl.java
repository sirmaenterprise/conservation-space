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

package org.activiti.engine.impl.form;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.FormType;


// TODO: Auto-generated Javadoc
/**
 * The Class FormPropertyImpl.
 *
 * @author Tom Baeyens
 */
public class FormPropertyImpl implements FormProperty {
  
  /** The id. */
  protected String id;
  
  /** The name. */
  protected String name;
  
  /** The type. */
  protected FormType type;
  
  /** The is required. */
  protected boolean isRequired;
  
  /** The is readable. */
  protected boolean isReadable;
  
  /** The is writable. */
  protected boolean isWritable;

  /** The value. */
  protected String value;

  /**
   * Instantiates a new form property impl.
   *
   * @param formPropertyHandler the form property handler
   */
  public FormPropertyImpl(FormPropertyHandler formPropertyHandler) {
    this.id = formPropertyHandler.getId();
    this.name = formPropertyHandler.getName();
    this.type = formPropertyHandler.getType();
    this.isRequired = formPropertyHandler.isRequired();
    this.isReadable = formPropertyHandler.isReadable();
    this.isWritable = formPropertyHandler.isWritable();
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.form.FormProperty#getId()
   */
  public String getId() {
    return id;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.form.FormProperty#getName()
   */
  public String getName() {
    return name;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.form.FormProperty#getType()
   */
  public FormType getType() {
    return type;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.form.FormProperty#getValue()
   */
  public String getValue() {
    return value;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.form.FormProperty#isRequired()
   */
  public boolean isRequired() {
    return isRequired;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.form.FormProperty#isReadable()
   */
  public boolean isReadable() {
    return isReadable;
  }
  
  /**
   * Sets the value.
   *
   * @param value the new value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.form.FormProperty#isWritable()
   */
  public boolean isWritable() {
    return isWritable;
  }
}
