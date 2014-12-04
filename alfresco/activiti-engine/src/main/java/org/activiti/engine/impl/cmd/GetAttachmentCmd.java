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

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.AttachmentEntity;
import org.activiti.engine.task.Attachment;


// TODO: Auto-generated Javadoc
/**
 * The Class GetAttachmentCmd.
 *
 * @author Tom Baeyens
 */
public class GetAttachmentCmd implements Command<Attachment>, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The attachment id. */
  protected String attachmentId;
  
  /**
   * Instantiates a new gets the attachment cmd.
   *
   * @param attachmentId the attachment id
   */
  public GetAttachmentCmd(String attachmentId) {
    this.attachmentId = attachmentId;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public Attachment execute(CommandContext commandContext) {
    return commandContext
      .getDbSqlSession()
      .selectById(AttachmentEntity.class, attachmentId);
  }

}
