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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;


// TODO: Auto-generated Javadoc
/**
 * The Class CommentEntity.
 *
 * @author Tom Baeyens
 */
public class CommentEntity implements Comment, Event, PersistentObject, Serializable {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The Constant TYPE_EVENT. */
  public static final String TYPE_EVENT = "event";
  
  /** The Constant TYPE_COMMENT. */
  public static final String TYPE_COMMENT = "comment";
  
  /** The id. */
  protected String id;
  
  /** The type. */
  protected String type;
  
  /** The user id. */
  protected String userId;
  
  /** The time. */
  protected Date time;
  
  /** The task id. */
  protected String taskId;
  
  /** The process instance id. */
  protected String processInstanceId;
  
  /** The action. */
  protected String action;
  
  /** The message. */
  protected String message;
  
  /** The full message. */
  protected String fullMessage;
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    return CommentEntity.class;
  }

  /**
   * Gets the full message bytes.
   *
   * @return the full message bytes
   */
  public byte[] getFullMessageBytes() {
    return (fullMessage!=null ? fullMessage.getBytes() : null);
  }

  /**
   * Sets the full message bytes.
   *
   * @param fullMessageBytes the new full message bytes
   */
  public void setFullMessageBytes(byte[] fullMessageBytes) {
    fullMessage = (fullMessageBytes!=null ? new String(fullMessageBytes) : null );
  }
  
  /** The message parts marker. */
  public static String MESSAGE_PARTS_MARKER = "_|_";
  
  /**
   * Sets the message.
   *
   * @param messageParts the new message
   */
  public void setMessage(String[] messageParts) {
    StringBuilder stringBuilder = new StringBuilder();
    for (String part: messageParts) {
      if (part!=null) {
        stringBuilder.append(part.replace(MESSAGE_PARTS_MARKER, " | "));
        stringBuilder.append(MESSAGE_PARTS_MARKER);
      } else {
        stringBuilder.append("null");
        stringBuilder.append(MESSAGE_PARTS_MARKER);
      }
    }
    for (int i=0; i<MESSAGE_PARTS_MARKER.length(); i++) {
      stringBuilder.deleteCharAt(stringBuilder.length()-1);
    }
    message = stringBuilder.toString();
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Event#getMessageParts()
   */
  public List<String> getMessageParts() {
    if (message==null) {
      return null;
    }
    List<String> messageParts = new ArrayList<String>();
    StringTokenizer tokenizer = new StringTokenizer(message, MESSAGE_PARTS_MARKER);
    while (tokenizer.hasMoreTokens()) {
      String nextToken = tokenizer.nextToken();
      if ("null".equals(nextToken)) {
        messageParts.add(null);
      } else {
        messageParts.add(nextToken);
      }
    }
    return messageParts;
  }
  
  // getters and setters //////////////////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getId()
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
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Comment#getUserId()
   */
  public String getUserId() {
    return userId;
  }
  
  /**
   * Sets the user id.
   *
   * @param userId the new user id
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Comment#getTaskId()
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
   * @see org.activiti.engine.task.Event#getMessage()
   */
  public String getMessage() {
    return message;
  }
  
  /**
   * Sets the message.
   *
   * @param message the new message
   */
  public void setMessage(String message) {
    this.message = message;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Comment#getTime()
   */
  public Date getTime() {
    return time;
  }
  
  /**
   * Sets the time.
   *
   * @param time the new time
   */
  public void setTime(Date time) {
    this.time = time;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Comment#getProcessInstanceId()
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

  /**
   * Gets the type.
   *
   * @return the type
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
   * @see org.activiti.engine.task.Comment#getFullMessage()
   */
  public String getFullMessage() {
    return fullMessage;
  }

  /**
   * Sets the full message.
   *
   * @param fullMessage the new full message
   */
  public void setFullMessage(String fullMessage) {
    this.fullMessage = fullMessage;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Event#getAction()
   */
  public String getAction() {
    return action;
  }
  
  /**
   * Sets the action.
   *
   * @param action the new action
   */
  public void setAction(String action) {
    this.action = action;
  }
}
