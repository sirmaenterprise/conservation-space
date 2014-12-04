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

package org.activiti.engine.impl.cmd;

import java.io.Serializable;

import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.task.Event;


// TODO: Auto-generated Javadoc
/**
 * The Class AddCommentCmd.
 *
 * @author Tom Baeyens
 */
public class AddCommentCmd implements Command<Object>, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The task id. */
  protected String taskId;
  
  /** The process instance id. */
  protected String processInstanceId;
  
  /** The message. */
  protected String message;
  
  /**
   * Instantiates a new adds the comment cmd.
   *
   * @param taskId the task id
   * @param processInstanceId the process instance id
   * @param message the message
   */
  public AddCommentCmd(String taskId, String processInstanceId, String message) {
    this.taskId = taskId;
    this.processInstanceId = processInstanceId;
    this.message = message;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public Object execute(CommandContext commandContext) {
    String userId = Authentication.getAuthenticatedUserId();
    CommentEntity comment = new CommentEntity();
    comment.setUserId(userId);
    comment.setType(CommentEntity.TYPE_COMMENT);
    comment.setTime(ClockUtil.getCurrentTime());
    comment.setTaskId(taskId);
    comment.setProcessInstanceId(processInstanceId);
    comment.setAction(Event.ACTION_ADD_COMMENT);
    
    String eventMessage = message.replaceAll("\\s+", " ");
    if (eventMessage.length()>163) {
      eventMessage = eventMessage.substring(0, 160)+"...";
    }
    comment.setMessage(eventMessage);
    
    comment.setFullMessage(message);
    
    commandContext
      .getCommentManager()
      .insert(comment);
    
    return null;
  }
}
