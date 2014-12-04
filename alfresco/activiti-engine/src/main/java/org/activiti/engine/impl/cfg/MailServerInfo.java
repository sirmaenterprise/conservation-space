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


// TODO: Auto-generated Javadoc
/**
 * The Class MailServerInfo.
 *
 * @author Tom Baeyens
 */
public class MailServerInfo {

  /** The mail server default from. */
  protected String mailServerDefaultFrom;
  
  /** The mail server host. */
  protected String mailServerHost;
  
  /** The mail server port. */
  protected int mailServerPort;
  
  /** The mail server username. */
  protected String mailServerUsername;
  
  /** The mail server password. */
  protected String mailServerPassword;
  
  /**
   * Gets the mail server default from.
   *
   * @return the mail server default from
   */
  public String getMailServerDefaultFrom() {
    return mailServerDefaultFrom;
  }
  
  /**
   * Sets the mail server default from.
   *
   * @param mailServerDefaultFrom the new mail server default from
   */
  public void setMailServerDefaultFrom(String mailServerDefaultFrom) {
    this.mailServerDefaultFrom = mailServerDefaultFrom;
  }
  
  /**
   * Gets the mail server host.
   *
   * @return the mail server host
   */
  public String getMailServerHost() {
    return mailServerHost;
  }
  
  /**
   * Sets the mail server host.
   *
   * @param mailServerHost the new mail server host
   */
  public void setMailServerHost(String mailServerHost) {
    this.mailServerHost = mailServerHost;
  }
  
  /**
   * Gets the mail server port.
   *
   * @return the mail server port
   */
  public int getMailServerPort() {
    return mailServerPort;
  }
  
  /**
   * Sets the mail server port.
   *
   * @param mailServerPort the new mail server port
   */
  public void setMailServerPort(int mailServerPort) {
    this.mailServerPort = mailServerPort;
  }
  
  /**
   * Gets the mail server username.
   *
   * @return the mail server username
   */
  public String getMailServerUsername() {
    return mailServerUsername;
  }
  
  /**
   * Sets the mail server username.
   *
   * @param mailServerUsername the new mail server username
   */
  public void setMailServerUsername(String mailServerUsername) {
    this.mailServerUsername = mailServerUsername;
  }
  
  /**
   * Gets the mail server password.
   *
   * @return the mail server password
   */
  public String getMailServerPassword() {
    return mailServerPassword;
  }
  
  /**
   * Sets the mail server password.
   *
   * @param mailServerPassword the new mail server password
   */
  public void setMailServerPassword(String mailServerPassword) {
    this.mailServerPassword = mailServerPassword;
  }
}
