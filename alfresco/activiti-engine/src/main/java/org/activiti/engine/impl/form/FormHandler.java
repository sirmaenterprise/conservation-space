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

import java.util.Map;

import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.util.xml.Element;


// TODO: Auto-generated Javadoc
/**
 * The Interface FormHandler.
 *
 * @author Tom Baeyens
 */
public interface FormHandler {

  /** The current. */
  ThreadLocal<FormHandler> current = new ThreadLocal<FormHandler>();

  /**
   * Parses the configuration.
   *
   * @param activityElement the activity element
   * @param deployment the deployment
   * @param processDefinition the process definition
   * @param bpmnParse the bpmn parse
   */
  void parseConfiguration(Element activityElement, DeploymentEntity deployment, ProcessDefinitionEntity processDefinition, BpmnParse bpmnParse);

  /**
   * Submit form properties.
   *
   * @param properties the properties
   * @param execution the execution
   */
  void submitFormProperties(Map<String, String> properties, ExecutionEntity execution);
}
