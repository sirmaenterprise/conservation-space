/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.history;

import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * Represents one execution of an activity and it's stored permanent for statistics, audit and other business intelligence purposes.
 * 
 * @author Christian Stettler
 */
public interface HistoricActivityInstance {

  /**
   * The unique identifier of this historic activity instance.
   *
   * @return the id
   */
  String getId();

  /**
   * The unique identifier of the activity in the process.
   *
   * @return the activity id
   */
  String getActivityId();

  /**
   * The display name for the activity.
   *
   * @return the activity name
   */
  String getActivityName();

  /**
   * The XML tag of the activity as in the process file.
   *
   * @return the activity type
   */
  String getActivityType();

  /**
   * Process definition reference.
   *
   * @return the process definition id
   */
  String getProcessDefinitionId();

  /**
   * Process instance reference.
   *
   * @return the process instance id
   */
  String getProcessInstanceId();

  /**
   * Execution reference.
   *
   * @return the execution id
   */
  String getExecutionId();

  /**
   * Assignee in case of user task activity.
   *
   * @return the assignee
   */
  String getAssignee();

  /**
   * Time when the activity instance started.
   *
   * @return the start time
   */
  Date getStartTime();

  /**
   * Time when the activity instance ended.
   *
   * @return the end time
   */
  Date getEndTime();

  /**
   * Difference between {@link #getEndTime()} and {@link #getStartTime()}.
   *
   * @return the duration in millis
   */
  Long getDurationInMillis();
}
