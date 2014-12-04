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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.form.FormData;
import org.activiti.engine.form.FormProperty;


// TODO: Auto-generated Javadoc
/**
 * The Class FormDataImpl.
 *
 * @author Tom Baeyens
 */
public abstract class FormDataImpl implements FormData, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The form key. */
  protected String formKey;
  
  /** The deployment id. */
  protected String deploymentId;
  
  /** The form properties. */
  protected List<FormProperty> formProperties = new ArrayList<FormProperty>();
  
  // getters and setters //////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.form.FormData#getFormKey()
   */
  public String getFormKey() {
    return formKey;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.form.FormData#getDeploymentId()
   */
  public String getDeploymentId() {
    return deploymentId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.form.FormData#getFormProperties()
   */
  public List<FormProperty> getFormProperties() {
    return formProperties;
  }
  
  /**
   * Sets the form key.
   *
   * @param formKey the new form key
   */
  public void setFormKey(String formKey) {
    this.formKey = formKey;
  }
  
  /**
   * Sets the deployment id.
   *
   * @param deploymentId the new deployment id
   */
  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }
  
  /**
   * Sets the form properties.
   *
   * @param formProperties the new form properties
   */
  public void setFormProperties(List<FormProperty> formProperties) {
    this.formProperties = formProperties;
  }

}
