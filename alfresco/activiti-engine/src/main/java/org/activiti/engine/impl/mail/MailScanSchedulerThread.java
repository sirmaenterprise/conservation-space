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

package org.activiti.engine.impl.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.interceptor.CommandExecutor;


// TODO: Auto-generated Javadoc
/**
 * The Class MailScanSchedulerThread.
 *
 * @author Tom Baeyens
 */
public class MailScanSchedulerThread extends Thread {
  
  /** The log. */
  private static Logger log = Logger.getLogger(MailScanSchedulerThread.class.getName());

  /** The is active. */
  protected boolean isActive = false;
  
  /** The idle wait in millis. */
  protected int idleWaitInMillis = 10000;
  
  /** The mail scanner. */
  protected MailScanner mailScanner;
  
  /** The command executor. */
  protected CommandExecutor commandExecutor;
  
  /** The all mail scans cmds. */
  protected Map<String, MailScanCmd> allMailScansCmds = Collections.synchronizedMap(new HashMap<String, MailScanCmd>());
  
  /** The next mail scan cmds. */
  protected List<MailScanCmd> nextMailScanCmds = Collections.synchronizedList(new ArrayList<MailScanCmd>());

  /**
   * Instantiates a new mail scan scheduler thread.
   *
   * @param mailScanner the mail scanner
   */
  public MailScanSchedulerThread(MailScanner mailScanner) {
    this.mailScanner = mailScanner;
    this.commandExecutor = mailScanner.getCommandExecutor();
  }

  /**
   * Adds the user.
   *
   * @param userId the user id
   * @param userPassword the user password
   */
  public synchronized void addUser(String userId, String userPassword) {
    MailScanCmd mailScanCmd = commandExecutor.execute(new CreateMailScanCmd(userId, userPassword));
    if (mailScanCmd!=null) {
      allMailScansCmds.put(userId, mailScanCmd);
    }
    nextMailScanCmds.add(0, mailScanCmd);
  }

  /**
   * Removes the user.
   *
   * @param userId the user id
   */
  public synchronized void removeUser(String userId) {
    allMailScansCmds.remove(userId);
  }

  /* (non-Javadoc)
   * @see java.lang.Thread#run()
   */
  public void run() {
    isActive = true;
    log.fine(getClass().getName()+" is started");
    while (isActive) {
      MailScanCmd mailScanCmd = getNextMailScanCmd();
      if (mailScanCmd != null) {
        try {
          commandExecutor.execute(mailScanCmd);
        } catch (Exception e) {
          // users need to logout and login if they want to re-enable mail scanning after a failure
          String userId = mailScanCmd.getUserId();
          // allMailScansCmds.remove(userId);
          log.log(Level.SEVERE, "couldn't check todo mail for "+userId+": "+e.getMessage(), e);
        }
      }
      
      try {
        Thread.sleep(5*1000);
      } catch (InterruptedException e1) {
        e1.printStackTrace();
      }
    }
    log.fine(getClass().getName()+" is stopping");
  }

  /**
   * Gets the next mail scan cmd.
   *
   * @return the next mail scan cmd
   */
  protected MailScanCmd getNextMailScanCmd() {
    MailScanCmd nextMailScanCmd = null;
    while (nextMailScanCmd == null) {
      while (allMailScansCmds.isEmpty() && nextMailScanCmds.isEmpty()) {
        try {
          log.fine("sleeping for "+idleWaitInMillis+" millis");
          Thread.sleep(idleWaitInMillis);
        } catch (InterruptedException e) {
          log.fine("sleep got interrupted");
          return null;
        }
      }
      if (nextMailScanCmds.isEmpty()) {
        log.fine("scheduling mailscans for users "+allMailScansCmds.keySet());
        nextMailScanCmds.addAll(allMailScansCmds.values());
      }
      synchronized (this) {
        if (!nextMailScanCmds.isEmpty()) {
          nextMailScanCmd = nextMailScanCmds.remove(0);
        }
      }
    }
    return nextMailScanCmd;
  }

  /**
   * Shutdown.
   */
  public void shutdown() {
    if (isActive) {
      log.info(getName() + " is shutting down");
      isActive = false;
      interrupt();
      try {
        join();
      } catch (InterruptedException e) {
        log.log(Level.WARNING, "Interruption while shutting down " + this.getClass().getName(), e);
      }
    }
  }
}
