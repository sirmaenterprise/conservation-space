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

import org.activiti.engine.delegate.DelegateTask;



// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving task events.
 * The class that is interested in processing a task
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addTaskListener<code> method. When
 * the task event occurs, that object's appropriate
 * method is invoked.
 *
 * @deprecated use org.activiti.engine.delegate.TaskListener instead
 * @author Tom Baeyens
 */
public interface TaskListener {

  /** The eventname create. */
  String EVENTNAME_CREATE = "create";
  
  /** The eventname assignment. */
  String EVENTNAME_ASSIGNMENT = "assignment";
  
  /** The eventname complete. */
  String EVENTNAME_COMPLETE = "complete";
  
  /**
   * Notify.
   *
   * @param delegateTask the delegate task
   */
  void notify(DelegateTask delegateTask);
}
