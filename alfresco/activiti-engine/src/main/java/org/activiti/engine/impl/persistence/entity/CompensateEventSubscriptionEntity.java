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

package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.impl.event.CompensationEventHandler;



// TODO: Auto-generated Javadoc
/**
 * The Class CompensateEventSubscriptionEntity.
 *
 * @author Daniel Meyer
 */
public class CompensateEventSubscriptionEntity extends EventSubscriptionEntity {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /**
   * Instantiates a new compensate event subscription entity.
   */
  @SuppressWarnings("unused") // used by mybatis
  private CompensateEventSubscriptionEntity() {
  }

  /**
   * Instantiates a new compensate event subscription entity.
   *
   * @param executionEntity the execution entity
   */
  private CompensateEventSubscriptionEntity(ExecutionEntity executionEntity) {
    super(executionEntity);
    eventType=CompensationEventHandler.EVENT_HANDLER_TYPE;    
  }
  
  /**
   * Creates the and insert.
   *
   * @param executionEntity the execution entity
   * @return the compensate event subscription entity
   */
  public static CompensateEventSubscriptionEntity createAndInsert(ExecutionEntity executionEntity) {
    CompensateEventSubscriptionEntity eventSubscription = new CompensateEventSubscriptionEntity(executionEntity);    
    eventSubscription.insert();
    return eventSubscription;
  }
  
  // custom processing behavior //////////////////////////////////////////////////////////////////////////////  
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity#processEventSync(java.lang.Object)
   */
  @Override
  protected void processEventSync(Object payload) {
    delete();  
    super.processEventSync(payload);
  }

  /**
   * Move under.
   *
   * @param newExecution the new execution
   * @return the compensate event subscription entity
   */
  public CompensateEventSubscriptionEntity moveUnder(ExecutionEntity newExecution) {    
    
    delete();    
    
    CompensateEventSubscriptionEntity newSubscription = createAndInsert(newExecution);
    newSubscription.setActivity(getActivity());
    newSubscription.setConfiguration(configuration);    
    // use the original date
    newSubscription.setCreated(created);   
    
    return newSubscription;    
  }
      
}
