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
package org.activiti.engine.impl.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.activiti.engine.impl.util.IoUtil;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


// TODO: Auto-generated Javadoc
/**
 * The Class LaunchThread.
 *
 * @author Tom Baeyens
 */
public class LaunchThread extends Thread {

  /** The task. */
  Task task;
  
  /** The cmd. */
  String[] cmd;
  
  /** The dir. */
  File dir;
  
  /** The msg. */
  String msg;
  
  /**
   * Instantiates a new launch thread.
   *
   * @param task the task
   * @param cmd the cmd
   * @param dir the dir
   * @param msg the msg
   */
  public LaunchThread(Task task, String[] cmd, File dir, String msg) {
    this.task = task;
    this.cmd = cmd;
    this.dir = dir;
    this.msg = msg;
  }

  /**
   * Launch.
   *
   * @param task the task
   * @param cmd the cmd
   * @param dir the dir
   * @param launchCompleteText the launch complete text
   */
  public static void launch(Task task, String[] cmd, File dir, String launchCompleteText) {
    if (cmd==null) {
      throw new BuildException("cmd is null");
    }
    try {
      LaunchThread launchThread = new LaunchThread(task, cmd, dir, launchCompleteText);
      launchThread.start();
      launchThread.join();
    } catch (Exception e) {
      throw new BuildException("couldn't launch cmd: "+cmdString(cmd), e);
    }
  }
  
  /**
   * Cmd string.
   *
   * @param cmd the cmd
   * @return the string
   */
  private static String cmdString(String[] cmd) {
    StringBuilder cmdText = new  StringBuilder();
    for(String cmdPart: cmd) {
      cmdText.append(cmdPart);
      cmdText.append(" ");
    }
    return cmdText.toString();
  }

  /* (non-Javadoc)
   * @see java.lang.Thread#run()
   */
  public void run() {
    task.log("launching cmd '"+cmdString(cmd)+"' in dir '"+dir+"'");
    if (msg!=null) {
      task.log("waiting for launch completion msg '"+msg+"'...");
    } else {
      task.log("not waiting for a launch completion msg.");
    }
    ProcessBuilder processBuilder = new ProcessBuilder(cmd)
      .redirectErrorStream(true)
      .directory(dir);
    
    InputStream consoleStream = null;
    try {
      Process process = processBuilder.start();
      
      consoleStream = process.getInputStream();
      BufferedReader consoleReader = new BufferedReader(new InputStreamReader(consoleStream));
      String consoleLine = "";
      while ( (consoleLine!=null)
              && (msg==null || consoleLine.indexOf(msg)==-1)
            ) {
        consoleLine = consoleReader.readLine();
        
        if (consoleLine!=null) {
          task.log("  " + consoleLine);
        } else {
          task.log("launched process completed");
        }
      }
    } catch (Exception e) {
      throw new BuildException("couldn't launch "+cmdString(cmd), e);
    } finally {
      IoUtil.closeSilently(consoleStream);
    }
  }
}
