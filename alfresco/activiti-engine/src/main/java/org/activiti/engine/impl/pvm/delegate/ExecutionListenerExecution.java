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
package org.activiti.engine.impl.pvm.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.pvm.PvmProcessElement;


// TODO: Auto-generated Javadoc
/**
 * The Interface ExecutionListenerExecution.
 *
 * @author Tom Baeyens
 */
public interface ExecutionListenerExecution extends DelegateExecution {
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateExecution#getEventName()
   */
  String getEventName();

  /**
   * Gets the event source.
   *
   * @return the event source
   */
  PvmProcessElement getEventSource();

  /**
   * Gets the delete reason.
   *
   * @return the delete reason
   */
  String getDeleteReason();
}
