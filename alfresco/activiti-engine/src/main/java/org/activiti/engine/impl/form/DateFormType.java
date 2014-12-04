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

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.activiti.engine.ActivitiException;


// TODO: Auto-generated Javadoc
/**
 * The Class DateFormType.
 *
 * @author Tom Baeyens
 */
public class DateFormType extends AbstractFormType {
  
  /** The date pattern. */
  protected String datePattern; 
  
  /** The date format. */
  protected Format dateFormat; 

  /**
   * Instantiates a new date form type.
   *
   * @param datePattern the date pattern
   */
  public DateFormType(String datePattern) {
    this.datePattern = datePattern;
    this.dateFormat = new SimpleDateFormat(datePattern);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.form.FormType#getName()
   */
  public String getName() {
    return "date";
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.form.AbstractFormType#getInformation(java.lang.String)
   */
  public Object getInformation(String key) {
    if ("datePattern".equals(key)) {
      return datePattern;
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.form.AbstractFormType#convertFormValueToModelValue(java.lang.String)
   */
  public Object convertFormValueToModelValue(String propertyValue) {
    if (propertyValue==null || "".equals(propertyValue)) {
      return null;
    }
    try {
      return dateFormat.parseObject(propertyValue);
    } catch (ParseException e) {
      throw new ActivitiException("invalid date value "+propertyValue);
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.form.AbstractFormType#convertModelValueToFormValue(java.lang.Object)
   */
  public String convertModelValueToFormValue(Object modelValue) {
    if (modelValue==null) {
      return null;
    }
    return dateFormat.format(modelValue);
  }
}
