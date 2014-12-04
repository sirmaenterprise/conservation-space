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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.task.Attachment;


// TODO: Auto-generated Javadoc
/**
 * The Class AttachmentEntity.
 *
 * @author Tom Baeyens
 */
public class AttachmentEntity implements Attachment, PersistentObject, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The id. */
  protected String id;
  
  /** The revision. */
  protected int revision;
  
  /** The name. */
  protected String name;
  
  /** The description. */
  protected String description;
  
  /** The type. */
  protected String type;
  
  /** The task id. */
  protected String taskId;
  
  /** The process instance id. */
  protected String processInstanceId;
  
  /** The url. */
  protected String url;
  
  /** The content id. */
  protected String contentId;
  
  /** The content. */
  protected ByteArrayEntity content;

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("name", name);
    persistentState.put("description", description);
    return persistentState;
  }

  /**
   * Gets the revision next.
   *
   * @return the revision next
   */
  public int getRevisionNext() {
    return revision+1;
  }
  
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Attachment#getId()
   */
  public String getId() {
    return id;
  }

  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#setId(java.lang.String)
   */
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   * Gets the revision.
   *
   * @return the revision
   */
  public int getRevision() {
    return revision;
  }

  
  /**
   * Sets the revision.
   *
   * @param revision the new revision
   */
  public void setRevision(int revision) {
    this.revision = revision;
  }

  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Attachment#getName()
   */
  public String getName() {
    return name;
  }

  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Attachment#setName(java.lang.String)
   */
  public void setName(String name) {
    this.name = name;
  }

  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Attachment#getDescription()
   */
  public String getDescription() {
    return description;
  }

  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Attachment#setDescription(java.lang.String)
   */
  public void setDescription(String description) {
    this.description = description;
  }

  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Attachment#getType()
   */
  public String getType() {
    return type;
  }

  
  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(String type) {
    this.type = type;
  }

  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Attachment#getTaskId()
   */
  public String getTaskId() {
    return taskId;
  }

  
  /**
   * Sets the task id.
   *
   * @param taskId the new task id
   */
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Attachment#getProcessInstanceId()
   */
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  
  /**
   * Sets the process instance id.
   *
   * @param processInstanceId the new process instance id
   */
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Attachment#getUrl()
   */
  public String getUrl() {
    return url;
  }

  
  /**
   * Sets the url.
   *
   * @param url the new url
   */
  public void setUrl(String url) {
    this.url = url;
  }

  
  /**
   * Gets the content id.
   *
   * @return the content id
   */
  public String getContentId() {
    return contentId;
  }

  /**
   * Sets the content id.
   *
   * @param contentId the new content id
   */
  public void setContentId(String contentId) {
    this.contentId = contentId;
  }
  
  /**
   * Gets the content.
   *
   * @return the content
   */
  public ByteArrayEntity getContent() {
    return content;
  }

  /**
   * Sets the content.
   *
   * @param content the new content
   */
  public void setContent(ByteArrayEntity content) {
    this.content = content;
  }
}
