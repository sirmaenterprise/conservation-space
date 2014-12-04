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

import org.activiti.engine.form.FormType;


// TODO: Auto-generated Javadoc
/**
 * The Class AbstractFormType.
 *
 * @author Tom Baeyens
 */
public abstract class AbstractFormType implements FormType {

  /**
   * Convert form value to model value.
   *
   * @param propertyValue the property value
   * @return the object
   */
  public abstract Object convertFormValueToModelValue(String propertyValue);

  /**
   * Convert model value to form value.
   *
   * @param modelValue the model value
   * @return the string
   */
  public abstract String convertModelValueToFormValue(Object modelValue);

  /* (non-Javadoc)
   * @see org.activiti.engine.form.FormType#getInformation(java.lang.String)
   */
  public Object getInformation(String key) {
    return null;
  }

}
