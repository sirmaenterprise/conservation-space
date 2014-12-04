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



// TODO: Auto-generated Javadoc
/**
 * The Class LongFormType.
 *
 * @author Tom Baeyens
 */
public class LongFormType extends AbstractFormType {

  /* (non-Javadoc)
   * @see org.activiti.engine.form.FormType#getName()
   */
  public String getName() {
    return "long";
  }

  /**
   * Gets the mime type.
   *
   * @return the mime type
   */
  public String getMimeType() {
    return "plain/text";
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.form.AbstractFormType#convertFormValueToModelValue(java.lang.String)
   */
  public Object convertFormValueToModelValue(String propertyValue) {
    if (propertyValue==null || "".equals(propertyValue)) {
      return null;
    }
    return new Long(propertyValue);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.form.AbstractFormType#convertModelValueToFormValue(java.lang.Object)
   */
  public String convertModelValueToFormValue(Object modelValue) {
    if (modelValue==null) {
      return null;
    }
    return modelValue.toString();
  }
}
