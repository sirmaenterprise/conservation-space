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

package org.activiti.engine.impl;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.task.TaskQuery;



// TODO: Auto-generated Javadoc
/**
 * Contains the possible properties that can be used in a {@link TaskQuery}.
 * 
 * @author Joram Barrez
 */
public class TaskQueryProperty implements QueryProperty {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The Constant properties. */
  private static final Map<String, TaskQueryProperty> properties = new HashMap<String, TaskQueryProperty>();

  /** The Constant TASK_ID. */
  public static final TaskQueryProperty TASK_ID = new TaskQueryProperty("T.ID_");
  
  /** The Constant NAME. */
  public static final TaskQueryProperty NAME = new TaskQueryProperty("T.NAME_");
  
  /** The Constant DESCRIPTION. */
  public static final TaskQueryProperty DESCRIPTION = new TaskQueryProperty("T.DESCRIPTION_");
  
  /** The Constant PRIORITY. */
  public static final TaskQueryProperty PRIORITY = new TaskQueryProperty("T.PRIORITY_");
  
  /** The Constant ASSIGNEE. */
  public static final TaskQueryProperty ASSIGNEE = new TaskQueryProperty("T.ASSIGNEE_");
  
  /** The Constant CREATE_TIME. */
  public static final TaskQueryProperty CREATE_TIME = new TaskQueryProperty("T.CREATE_TIME_");
  
  /** The Constant PROCESS_INSTANCE_ID. */
  public static final TaskQueryProperty PROCESS_INSTANCE_ID = new TaskQueryProperty("T.PROC_INST_ID_");
  
  /** The Constant EXECUTION_ID. */
  public static final TaskQueryProperty EXECUTION_ID = new TaskQueryProperty("T.EXECUTION_ID_");
  
  /** The Constant DUE_DATE. */
  public static final TaskQueryProperty DUE_DATE = new TaskQueryProperty("T.DUE_DATE_");
  
  /** The name. */
  private String name;

  /**
   * Instantiates a new task query property.
   *
   * @param name the name
   */
  public TaskQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.query.QueryProperty#getName()
   */
  public String getName() {
    return name;
  }
  
  /**
   * Find by name.
   *
   * @param propertyName the property name
   * @return the task query property
   */
  public static TaskQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

}
