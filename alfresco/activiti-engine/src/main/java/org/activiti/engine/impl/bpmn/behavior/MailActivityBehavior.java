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

package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;

// TODO: Auto-generated Javadoc
/**
 * The Class MailActivityBehavior.
 *
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class MailActivityBehavior extends AbstractBpmnActivityBehavior {

  /** The to. */
  protected Expression to;
  
  /** The from. */
  protected Expression from;
  
  /** The cc. */
  protected Expression cc;
  
  /** The bcc. */
  protected Expression bcc;
  
  /** The subject. */
  protected Expression subject;
  
  /** The text. */
  protected Expression text;
  
  /** The html. */
  protected Expression html;
  
  /** The charset. */
  protected Expression charset;

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.behavior.FlowNodeActivityBehavior#execute(org.activiti.engine.impl.pvm.delegate.ActivityExecution)
   */
  public void execute(ActivityExecution execution) {
    String toStr = getStringFromField(to, execution);
    String fromStr = getStringFromField(from, execution);
    String ccStr = getStringFromField(cc, execution);
    String bccStr = getStringFromField(bcc, execution);
    String subjectStr = getStringFromField(subject, execution);
    String textStr = getStringFromField(text, execution);
    String htmlStr = getStringFromField(html, execution);
    String charSetStr = getStringFromField(charset, execution);

    Email email = createEmail(textStr, htmlStr);

    addTo(email, toStr);
    setFrom(email, fromStr);
    addCc(email, ccStr);
    addBcc(email, bccStr);
    setSubject(email, subjectStr);
    setMailServerProperties(email);
    setCharset(email, charSetStr);

    try {
      email.send();
    } catch (EmailException e) {
      throw new ActivitiException("Could not send e-mail", e);
    }
    leave(execution);
  }

  /**
   * Creates the email.
   *
   * @param text the text
   * @param html the html
   * @return the email
   */
  protected Email createEmail(String text, String html) {
    if (html != null) {
      return createHtmlEmail(text, html);
    } else if (text != null) {
      return createTextOnlyEmail(text);
    } else {
      throw new ActivitiException("'html' or 'text' is required to be defined when using the mail activity");
    }
  }

  /**
   * Creates the html email.
   *
   * @param text the text
   * @param html the html
   * @return the html email
   */
  protected HtmlEmail createHtmlEmail(String text, String html) {
    HtmlEmail email = new HtmlEmail();
    try {
      email.setHtmlMsg(html);
      if (text != null) { // for email clients that don't support html
        email.setTextMsg(text);
      }
      return email;
    } catch (EmailException e) {
      throw new ActivitiException("Could not create HTML email", e);
    }
  }

  /**
   * Creates the text only email.
   *
   * @param text the text
   * @return the simple email
   */
  protected SimpleEmail createTextOnlyEmail(String text) {
    SimpleEmail email = new SimpleEmail();
    try {
      email.setMsg(text);
      return email;
    } catch (EmailException e) {
      throw new ActivitiException("Could not create text-only email", e);
    }
  }

  /**
   * Adds the to.
   *
   * @param email the email
   * @param to the to
   */
  protected void addTo(Email email, String to) {
    String[] tos = splitAndTrim(to);
    if (tos != null) {
      for (String t : tos) {
        try {
          email.addTo(t);
        } catch (EmailException e) {
          throw new ActivitiException("Could not add " + t + " as recipient", e);
        }
      }
    } else {
      throw new ActivitiException("No recipient could be found for sending email");
    }
  }

  /**
   * Sets the from.
   *
   * @param email the email
   * @param from the from
   */
  protected void setFrom(Email email, String from) {
    String fromAddres = null;

    if (from != null) {
      fromAddres = from;
    } else { // use default configured from address in process engine config
      fromAddres = Context.getProcessEngineConfiguration().getMailServerDefaultFrom();
    }

    try {
      email.setFrom(fromAddres);
    } catch (EmailException e) {
      throw new ActivitiException("Could not set " + from + " as from address in email", e);
    }
  }

  /**
   * Adds the cc.
   *
   * @param email the email
   * @param cc the cc
   */
  protected void addCc(Email email, String cc) {
    String[] ccs = splitAndTrim(cc);
    if (ccs != null) {
      for (String c : ccs) {
        try {
          email.addCc(c);
        } catch (EmailException e) {
          throw new ActivitiException("Could not add " + c + " as cc recipient", e);
        }
      }
    }
  }

  /**
   * Adds the bcc.
   *
   * @param email the email
   * @param bcc the bcc
   */
  protected void addBcc(Email email, String bcc) {
    String[] bccs = splitAndTrim(bcc);
    if (bccs != null) {
      for (String b : bccs) {
        try {
          email.addBcc(b);
        } catch (EmailException e) {
          throw new ActivitiException("Could not add " + b + " as bcc recipient", e);
        }
      }
    }
  }

  /**
   * Sets the subject.
   *
   * @param email the email
   * @param subject the subject
   */
  protected void setSubject(Email email, String subject) {
    email.setSubject(subject != null ? subject : "");
  }

  /**
   * Sets the mail server properties.
   *
   * @param email the new mail server properties
   */
  protected void setMailServerProperties(Email email) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    String host = processEngineConfiguration.getMailServerHost();
    if (host == null) {
      throw new ActivitiException("Could not send email: no SMTP host is configured");
    }
    email.setHostName(host);

    int port = processEngineConfiguration.getMailServerPort();
    email.setSmtpPort(port);

    email.setTLS(processEngineConfiguration.getMailServerUseTLS());

    String user = processEngineConfiguration.getMailServerUsername();
    String password = processEngineConfiguration.getMailServerPassword();
    if (user != null && password != null) {
      email.setAuthentication(user, password);
    }
  }
  
  /**
   * Sets the charset.
   *
   * @param email the email
   * @param charSetStr the char set str
   */
  protected void setCharset(Email email, String charSetStr) {
    if (charset != null) {
      email.setCharset(charSetStr);
    }
  }
  
  /**
   * Split and trim.
   *
   * @param str the str
   * @return the string[]
   */
  protected String[] splitAndTrim(String str) {
    if (str != null) {
      String[] splittedStrings = str.split(",");
      for (int i = 0; i < splittedStrings.length; i++) {
        splittedStrings[i] = splittedStrings[i].trim();
      }
      return splittedStrings;
    }
    return null;
  }

  /**
   * Gets the string from field.
   *
   * @param expression the expression
   * @param execution the execution
   * @return the string from field
   */
  protected String getStringFromField(Expression expression, DelegateExecution execution) {
    if(expression != null) {
      Object value = expression.getValue(execution);
      if(value != null) {
        return value.toString();
      }
    }
    return null;
  }

}
