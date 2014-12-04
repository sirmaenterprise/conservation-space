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

package org.activiti.engine.task;

import org.activiti.engine.TaskService;



// TODO: Auto-generated Javadoc
/** Any type of content that is be associated with
 * a task or with a process instance.
 * 
 * @author Tom Baeyens
 */
public interface Attachment {

  /**
   * unique id for this attachment.
   *
   * @return the id
   */
  String getId();
  
  /**
   * free user defined short (max 255 chars) name for this attachment.
   *
   * @return the name
   */
  String getName();

  /**
   * free user defined short (max 255 chars) name for this attachment.
   *
   * @param name the new name
   */
  void setName(String name);
  
  /**
   * long (max 255 chars) explanation what this attachment is about in context of the task and/or process instance it's linked to.
   *
   * @return the description
   */
  String getDescription();

  /**
   * long (max 255 chars) explanation what this attachment is about in context of the task and/or process instance it's linked to.
   *
   * @param description the new description
   */
  void setDescription(String description);

  /**
   * indication of the type of content that this attachment refers to. Can be mime type or any other indication.
   *
   * @return the type
   */
  String getType();  
  
  /**
   * reference to the task to which this attachment is associated.
   *
   * @return the task id
   */
  String getTaskId();

  /**
   * reference to the process instance to which this attachment is associated.
   *
   * @return the process instance id
   */
  String getProcessInstanceId();

  /**
   * the remote URL in case this is remote content.  If the attachment content was
   *
   * @return the url
   * {@link TaskService#createAttachment(String, String, String, String, String, java.io.InputStream) uploaded with an input stream},
   * then this method returns null and the content can be fetched with {@link TaskService#getAttachmentContent(String)}.
   */
  String getUrl();
}
