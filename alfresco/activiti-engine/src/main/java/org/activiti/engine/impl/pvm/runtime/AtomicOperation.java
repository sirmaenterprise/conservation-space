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
package org.activiti.engine.impl.pvm.runtime;



// TODO: Auto-generated Javadoc
/**
 * The Interface AtomicOperation.
 *
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public interface AtomicOperation {
  
  /** The process start. */
  AtomicOperation PROCESS_START = new AtomicOperationProcessStart();
  
  /** The process start initial. */
  AtomicOperation PROCESS_START_INITIAL = new AtomicOperationProcessStartInitial();
  
  /** The process end. */
  AtomicOperation PROCESS_END = new AtomicOperationProcessEnd();
  
  /** The activity start. */
  AtomicOperation ACTIVITY_START = new AtomicOperationActivityStart();
  
  /** The activity execute. */
  AtomicOperation ACTIVITY_EXECUTE = new AtomicOperationActivityExecute();
  
  /** The activity end. */
  AtomicOperation ACTIVITY_END = new AtomicOperationActivityEnd();
  
  /** The transition notify listener end. */
  AtomicOperation TRANSITION_NOTIFY_LISTENER_END = new AtomicOperationTransitionNotifyListenerEnd();
  
  /** The transition destroy scope. */
  AtomicOperation TRANSITION_DESTROY_SCOPE = new AtomicOperationTransitionDestroyScope();
  
  /** The transition notify listener take. */
  AtomicOperation TRANSITION_NOTIFY_LISTENER_TAKE = new AtomicOperationTransitionNotifyListenerTake();
  
  /** The transition create scope. */
  AtomicOperation TRANSITION_CREATE_SCOPE = new AtomicOperationTransitionCreateScope();
  
  /** The transition notify listener start. */
  AtomicOperation TRANSITION_NOTIFY_LISTENER_START = new AtomicOperationTransitionNotifyListenerStart();

  /** The delete cascade. */
  AtomicOperation DELETE_CASCADE = new AtomicOperationDeleteCascade();
  
  /** The delete cascade fire activity end. */
  AtomicOperation DELETE_CASCADE_FIRE_ACTIVITY_END = new AtomicOperationDeleteCascadeFireActivityEnd();

  /**
   * Execute.
   *
   * @param execution the execution
   */
  void execute(InterpretableExecution execution);
  
  /**
   * Checks if is async.
   *
   * @param execution the execution
   * @return true, if is async
   */
  boolean isAsync(InterpretableExecution execution);
}
