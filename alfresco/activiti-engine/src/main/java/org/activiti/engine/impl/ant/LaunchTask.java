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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


// TODO: Auto-generated Javadoc
/**
 * The Class LaunchTask.
 *
 * @author Tom Baeyens
 */
public class LaunchTask extends Task {

  /** The Constant FILESEPARATOR. */
  private static final String FILESEPARATOR = System.getProperty("file.separator");
  
  /** The dir. */
  File dir;
  
  /** The script. */
  String script;
  
  /** The msg. */
  String msg;
  
  /** The args. */
  String args;
  
  /* (non-Javadoc)
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() throws BuildException {
    if (dir==null) {
      throw new BuildException("dir attribute is required with the launch task");
    }    
    if (script==null) {
      throw new BuildException("script attribute is required with the launch task");
    }    
    
    String[] cmd = null;
    String executable = getExecutable();
    if (args!=null) {
      List<String> pieces = new ArrayList<String>();
      pieces.add(executable);
      StringTokenizer tokenizer = new StringTokenizer("args", " ");
      while (tokenizer.hasMoreTokens()) {
        pieces.add(tokenizer.nextToken());
      }
      cmd = pieces.toArray(new String[pieces.size()]);
      
    } else {
      cmd = new String[]{executable};
    }
    
    LaunchThread.launch(this,cmd,dir,msg);
  }

  /**
   * Gets the executable.
   *
   * @return the executable
   */
  public String getExecutable() {
    String os = System.getProperty("os.name").toLowerCase();
    String dirPath = dir.getAbsolutePath();
    String base = dirPath+FILESEPARATOR+script;
    if (exists(base)) {
      return base;
    }
    
    if (os.indexOf("windows")!=-1) {
      if (exists(base+".exe")) {
        return base+".exe";
      }
      if (exists(base+".bat")) {
        return base+".bat";
      }
    }
      
    if (os.indexOf("linux")!=-1 || os.indexOf("mac")!=-1) {
      if (exists(base+".sh")) {
        return base+".sh";
      }
    }
  
    throw new BuildException("couldn't find executable for script "+base);
  }

  /**
   * Exists.
   *
   * @param path the path
   * @return true, if successful
   */
  public boolean exists(String path) {
    File file = new File(path);
    return (file.exists());
  }

  /**
   * Sets the dir.
   *
   * @param dir the new dir
   */
  public void setDir(File dir) {
    this.dir = dir;
  }
  
  /**
   * Sets the script.
   *
   * @param script the new script
   */
  public void setScript(String script) {
    this.script = script;
  }
  
  /**
   * Sets the msg.
   *
   * @param msg the new msg
   */
  public void setMsg(String msg) {
    this.msg = msg;
  }
  
  /**
   * Sets the args.
   *
   * @param args the new args
   */
  public void setArgs(String args) {
    this.args = args;
  }
}
