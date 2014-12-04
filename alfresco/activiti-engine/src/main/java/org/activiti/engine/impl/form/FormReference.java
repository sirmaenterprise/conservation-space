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
 * The Class FormReference.
 *
 * @author Tom Baeyens
 */
public class FormReference {

  /** The form. */
  protected String form;
  
  /** The language. */
  protected String language;

  /**
   * Instantiates a new form reference.
   *
   * @param form the form
   * @param language the language
   */
  public FormReference(String form, String language) {
    this.form = form;
    this.language = language;
  }
  
  /**
   * Gets the form.
   *
   * @return the form
   */
  public String getForm() {
    return form;
  }
  
  /**
   * Gets the language.
   *
   * @return the language
   */
  public String getLanguage() {
    return language;
  }
}
