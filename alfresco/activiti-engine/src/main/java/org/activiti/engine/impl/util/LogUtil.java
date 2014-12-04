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
package org.activiti.engine.impl.util;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import org.activiti.engine.impl.pvm.PvmException;

// TODO: Auto-generated Javadoc
/**
 * The Class LogUtil.
 *
 * @author Tom Baeyens
 */
public class LogUtil {
  
  /**
   * The Enum ThreadLogMode.
   */
  public static enum ThreadLogMode {
    
    /** The none. */
    NONE, 
 /** The indent. */
 INDENT, 
 /** The print id. */
 PRINT_ID;
  }

  /** The Constant LINE_SEPARATOR. */
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  
  /** The thread indents. */
  private static Map<Integer, String> threadIndents = new HashMap<Integer, String>();
  
  /** The thread log mode. */
  private static ThreadLogMode threadLogMode = ThreadLogMode.NONE;
  
  /**
   * Gets the thread log mode.
   *
   * @return the thread log mode
   */
  public static ThreadLogMode getThreadLogMode() {
    return threadLogMode;
  }

  /**
   * Sets the thread log mode.
   *
   * @param threadLogMode the thread log mode
   * @return the thread log mode
   */
  public static ThreadLogMode setThreadLogMode(ThreadLogMode threadLogMode) {
    ThreadLogMode old = LogUtil.threadLogMode;
    LogUtil.threadLogMode = threadLogMode;
    return old;
  }

  /**
   * Read java util logging config from classpath.
   */
  public static void readJavaUtilLoggingConfigFromClasspath() {
    InputStream inputStream = ReflectUtil.getResourceAsStream("logging.properties");
    try {
      if (inputStream != null) {
        LogManager.getLogManager().readConfiguration(inputStream);

        String redirectCommons = LogManager.getLogManager().getProperty("redirect.commons.logging");
        if ((redirectCommons != null) && (!redirectCommons.equalsIgnoreCase("false"))) {
          System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Jdk14Logger");
        }
      }
    } catch (Exception e) {
      throw new PvmException("couldn't initialize logging properly", e);
    } finally {
      IoUtil.closeSilently(inputStream);
    }
  }

  /** The date format. */
  private static Format dateFormat = new SimpleDateFormat("HH:mm:ss,SSS");

  /**
   * The Class LogFormatter.
   */
  public static class LogFormatter extends Formatter {

    /* (non-Javadoc)
     * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
     */
    public String format(LogRecord record) {
      StringBuilder line = new StringBuilder();
      line.append(dateFormat.format(new Date()));
      if (Level.FINE.equals(record.getLevel())) {
        line.append(" FIN ");
      } else if (Level.FINEST.equals(record.getLevel())) {
        line.append(" FST ");
      } else if (Level.INFO.equals(record.getLevel())) {
        line.append(" INF ");
      } else if (Level.SEVERE.equals(record.getLevel())) {
        line.append(" SEV ");
      } else if (Level.WARNING.equals(record.getLevel())) {
        line.append(" WRN ");
      } else if (Level.FINER.equals(record.getLevel())) {
        line.append(" FNR ");
      } else if (Level.CONFIG.equals(record.getLevel())) {
        line.append(" CFG ");
      }

      int threadId = record.getThreadID();
      String threadIndent = getThreadIndent(threadId);

      line.append(threadIndent);
      line.append(" | ");
      line.append(record.getMessage());

      if (record.getThrown() != null) {
        line.append(LINE_SEPARATOR);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        record.getThrown().printStackTrace(printWriter);
        line.append(stringWriter.toString());
      }

      line.append("  [");
      line.append(record.getLoggerName());
      line.append("]");

      line.append(LINE_SEPARATOR);

      return line.toString();
    }

    /**
     * Gets the thread indent.
     *
     * @param threadId the thread id
     * @return the thread indent
     */
    protected static String getThreadIndent(int threadId) {
      Integer threadIdInteger = new Integer(threadId);
      if (threadLogMode==ThreadLogMode.NONE) {
        return "";
      }
      if (threadLogMode==ThreadLogMode.PRINT_ID) {
        return ""+threadId;
      }
      String threadIndent = threadIndents.get(threadIdInteger);
      if (threadIndent == null) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < threadIndents.size(); i++) {
          stringBuilder.append("  ");
        }
        threadIndent = stringBuilder.toString();
        threadIndents.put(threadIdInteger, threadIndent);
      }
      return threadIndent;
    }

  }

  /**
   * Reset thread indents.
   */
  public static void resetThreadIndents() {
    threadIndents = new HashMap<Integer, String>();
  }
}
