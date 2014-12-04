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

package org.activiti.engine.impl.scripting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

// TODO: Auto-generated Javadoc
/**
 * Factory to create {@link JuelScriptEngine}s.
 * 
 * @author Frederik Heremans
 */
public class JuelScriptEngineFactory implements ScriptEngineFactory {

  /** The names. */
  private static List<String> names;
  
  /** The extensions. */
  private static List<String> extensions;
  
  /** The mime types. */
  private static List<String> mimeTypes;

  static {
    names = Collections.unmodifiableList(Arrays.asList("juel"));
    extensions = names;
    mimeTypes = Collections.unmodifiableList(new ArrayList<String>(0));
  }

  /* (non-Javadoc)
   * @see javax.script.ScriptEngineFactory#getEngineName()
   */
  public String getEngineName() {
    return "juel";
  }

  /* (non-Javadoc)
   * @see javax.script.ScriptEngineFactory#getEngineVersion()
   */
  public String getEngineVersion() {
    return "1.0";
  }

  /* (non-Javadoc)
   * @see javax.script.ScriptEngineFactory#getExtensions()
   */
  public List<String> getExtensions() {
    return extensions;
  }

  /* (non-Javadoc)
   * @see javax.script.ScriptEngineFactory#getLanguageName()
   */
  public String getLanguageName() {
    return "JSP 2.1 EL";
  }

  /* (non-Javadoc)
   * @see javax.script.ScriptEngineFactory#getLanguageVersion()
   */
  public String getLanguageVersion() {
    return "2.1";
  }

  /* (non-Javadoc)
   * @see javax.script.ScriptEngineFactory#getMethodCallSyntax(java.lang.String, java.lang.String, java.lang.String[])
   */
  public String getMethodCallSyntax(String obj, String method, String... arguments) {
    throw new UnsupportedOperationException("Method getMethodCallSyntax is not supported");
  }

  /* (non-Javadoc)
   * @see javax.script.ScriptEngineFactory#getMimeTypes()
   */
  public List<String> getMimeTypes() {
    return mimeTypes;
  }

  /* (non-Javadoc)
   * @see javax.script.ScriptEngineFactory#getNames()
   */
  public List<String> getNames() {
    return names;
  }

  /* (non-Javadoc)
   * @see javax.script.ScriptEngineFactory#getOutputStatement(java.lang.String)
   */
  public String getOutputStatement(String toDisplay) {
    // We will use out:print function to output statements
    StringBuilder stringBuffer = new StringBuilder();
    stringBuffer.append("out:print(\"");
    
    int length = toDisplay.length();
    for (int i = 0; i < length; i++) {
      char c = toDisplay.charAt(i);
      switch (c) {
      case '"':
        stringBuffer.append("\\\"");
        break;
      case '\\':
        stringBuffer.append("\\\\");
        break;
      default:
        stringBuffer.append(c);
        break;
      }
    }
    stringBuffer.append("\")");
    return stringBuffer.toString();
  }

  /* (non-Javadoc)
   * @see javax.script.ScriptEngineFactory#getParameter(java.lang.String)
   */
  public String getParameter(String key) {
    if (key.equals(ScriptEngine.NAME)) {
      return getLanguageName();
    } else if (key.equals(ScriptEngine.ENGINE)) {
      return getEngineName();
    } else if (key.equals(ScriptEngine.ENGINE_VERSION)) {
      return getEngineVersion();
    } else if (key.equals(ScriptEngine.LANGUAGE)) {
      return getLanguageName();
    } else if (key.equals(ScriptEngine.LANGUAGE_VERSION)) {
      return getLanguageVersion();
    } else if (key.equals("THREADING")) {
      return "MULTITHREADED";
    } else {
      return null;
    }
  }

  /* (non-Javadoc)
   * @see javax.script.ScriptEngineFactory#getProgram(java.lang.String[])
   */
  public String getProgram(String... statements) {
    // Each statement is wrapped in '${}' to comply with EL
    StringBuilder buf = new StringBuilder();
    if (statements.length != 0) {
      for (int i = 0; i < statements.length; i++) {
        buf.append("${");
        buf.append(statements[i]);
        buf.append("} ");
      }
    }
    return buf.toString();
  }

  /* (non-Javadoc)
   * @see javax.script.ScriptEngineFactory#getScriptEngine()
   */
  public ScriptEngine getScriptEngine() {
    return new JuelScriptEngine(this);
  }

}
