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

package org.activiti.engine.impl.test;

import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.activiti.engine.impl.util.ClassNameUtil;
import org.activiti.engine.impl.util.LogUtil;
import org.activiti.engine.impl.util.LogUtil.ThreadLogMode;


// TODO: Auto-generated Javadoc
/**
 * The Class PvmTestCase.
 *
 * @author Tom Baeyens
 */
public class PvmTestCase extends TestCase {

  /** The Constant EMPTY_LINE. */
  protected static final String EMPTY_LINE = "                                                                                           ";

  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }
  
  /** The log. */
  private static Logger log = Logger.getLogger(PvmTestCase.class.getName());

  /** The Constant DEFAULT_THREAD_LOG_MODE. */
  protected static final ThreadLogMode DEFAULT_THREAD_LOG_MODE = ThreadLogMode.INDENT;
  
  /** The thread rendering mode. */
  protected ThreadLogMode threadRenderingMode;
  
  /** The is empty lines enabled. */
  protected boolean isEmptyLinesEnabled = true;

  /**
   * Instantiates a new pvm test case.
   */
  public PvmTestCase() {
    this(DEFAULT_THREAD_LOG_MODE);
  }
  
  /**
   * Instantiates a new pvm test case.
   *
   * @param threadRenderingMode the thread rendering mode
   */
  public PvmTestCase(ThreadLogMode threadRenderingMode) {
    this.threadRenderingMode = threadRenderingMode;
  }
  
  /**
   * Assert text present.
   *
   * @param expected the expected
   * @param actual the actual
   */
  public void assertTextPresent(String expected, String actual) {
    if ( (actual==null)
         || (actual.indexOf(expected)==-1)
       ) {
      throw new AssertionFailedError("expected presence of ["+expected+"], but was ["+actual+"]");
    }
  }
  
  /* (non-Javadoc)
   * @see junit.framework.TestCase#runTest()
   */
  @Override
  protected void runTest() throws Throwable {
    LogUtil.resetThreadIndents();
    ThreadLogMode oldThreadRenderingMode = LogUtil.setThreadLogMode(threadRenderingMode);
    
    if (log.isLoggable(Level.FINE)) {
      if (isEmptyLinesEnabled) {
        log.fine(EMPTY_LINE);
      }
      log.fine("#### START "+ClassNameUtil.getClassNameWithoutPackage(this)+"."+getName()+" ###########################################################");
    }

    try {
      
      super.runTest();

    }  catch (AssertionFailedError e) {
      log.severe(EMPTY_LINE);
      log.log(Level.SEVERE, "ASSERTION FAILED: "+e, e);
      throw e;
      
    } catch (Throwable e) {
      log.severe(EMPTY_LINE);
      log.log(Level.SEVERE, "EXCEPTION: "+e, e);
      throw e;
      
    } finally {
      log.fine("#### END "+ClassNameUtil.getClassNameWithoutPackage(this)+"."+getName()+" #############################################################");
      LogUtil.setThreadLogMode(oldThreadRenderingMode);
    }
  }

}
