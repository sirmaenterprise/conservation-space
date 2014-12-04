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
package org.activiti.engine.impl.cfg;

import org.activiti.engine.impl.interceptor.CommandContext;


// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving transaction events.
 * The class that is interested in processing a transaction
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addTransactionListener<code> method. When
 * the transaction event occurs, that object's appropriate
 * method is invoked.
 *
 * @author Tom Baeyens
 */
public interface TransactionListener {

  /**
   * Execute.
   *
   * @param commandContext the command context
   */
  void execute(CommandContext commandContext);
}
