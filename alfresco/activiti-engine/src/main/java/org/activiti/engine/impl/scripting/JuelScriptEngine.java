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

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.data.ItemInstance;
import org.activiti.engine.impl.el.ExpressionFactoryResolver;
import org.activiti.engine.impl.javax.el.ArrayELResolver;
import org.activiti.engine.impl.javax.el.BeanELResolver;
import org.activiti.engine.impl.javax.el.CompositeELResolver;
import org.activiti.engine.impl.javax.el.DynamicBeanPropertyELResolver;
import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELException;
import org.activiti.engine.impl.javax.el.ELResolver;
import org.activiti.engine.impl.javax.el.ExpressionFactory;
import org.activiti.engine.impl.javax.el.FunctionMapper;
import org.activiti.engine.impl.javax.el.ListELResolver;
import org.activiti.engine.impl.javax.el.MapELResolver;
import org.activiti.engine.impl.javax.el.ResourceBundleELResolver;
import org.activiti.engine.impl.javax.el.ValueExpression;
import org.activiti.engine.impl.javax.el.VariableMapper;
import org.activiti.engine.impl.juel.SimpleResolver;
import org.activiti.engine.impl.util.ReflectUtil;


// TODO: Auto-generated Javadoc
/**
 * ScriptEngine that used JUEL for script evaluation and compilation (JSR-223).
 * 
 * Uses EL 1.1 if available, to resolve expressions. Otherwise it reverts to EL
 * 1.0, using {@link ExpressionFactoryResolver}.
 * 
 * @author Frederik Heremans
 */
public class JuelScriptEngine extends AbstractScriptEngine implements Compilable {

  /** The script engine factory. */
  private ScriptEngineFactory scriptEngineFactory;
  
  /** The expression factory. */
  private ExpressionFactory expressionFactory;

  /**
   * Instantiates a new juel script engine.
   *
   * @param scriptEngineFactory the script engine factory
   */
  public JuelScriptEngine(ScriptEngineFactory scriptEngineFactory) {
    this.scriptEngineFactory = scriptEngineFactory;
    // Resolve the ExpressionFactory
    expressionFactory = ExpressionFactoryResolver.resolveExpressionFactory();
  }

  /**
   * Instantiates a new juel script engine.
   */
  public JuelScriptEngine() {
    this(null);
  }

  /* (non-Javadoc)
   * @see javax.script.Compilable#compile(java.lang.String)
   */
  public CompiledScript compile(String script) throws ScriptException {
    ValueExpression expr = parse(script, context);
    return new JuelCompiledScript(expr);
  }

  /* (non-Javadoc)
   * @see javax.script.Compilable#compile(java.io.Reader)
   */
  public CompiledScript compile(Reader reader) throws ScriptException {
    // Create a String based on the reader and complile it
    return compile(readFully(reader));
  }

  /* (non-Javadoc)
   * @see javax.script.ScriptEngine#eval(java.lang.String, javax.script.ScriptContext)
   */
  public Object eval(String script, ScriptContext scriptContext) throws ScriptException {
    ValueExpression expr = parse(script, scriptContext);
    return evaluateExpression(expr, scriptContext);
  }

  /* (non-Javadoc)
   * @see javax.script.ScriptEngine#eval(java.io.Reader, javax.script.ScriptContext)
   */
  public Object eval(Reader reader, ScriptContext scriptContext) throws ScriptException {
    return eval(readFully(reader), scriptContext);
  }

  /* (non-Javadoc)
   * @see javax.script.ScriptEngine#getFactory()
   */
  public ScriptEngineFactory getFactory() {
    synchronized (this) {
      if (scriptEngineFactory == null) {
        scriptEngineFactory = new JuelScriptEngineFactory();
      }
    }
    return scriptEngineFactory;
  }

  /* (non-Javadoc)
   * @see javax.script.ScriptEngine#createBindings()
   */
  public Bindings createBindings() {
    return new SimpleBindings();
  }

  /**
   * Evaluate expression.
   *
   * @param expr the expr
   * @param ctx the ctx
   * @return the object
   * @throws ScriptException the script exception
   */
  private Object evaluateExpression(ValueExpression expr, ScriptContext ctx) throws ScriptException {
    try {
      return expr.getValue(createElContext(ctx));
    } catch (ELException elexp) {
      throw new ScriptException(elexp);
    }
  }

  /**
   * Creates the el resolver.
   *
   * @return the eL resolver
   */
  private ELResolver createElResolver() {
    CompositeELResolver compositeResolver = new CompositeELResolver();
    compositeResolver.add(new ArrayELResolver());
    compositeResolver.add(new ListELResolver());
    compositeResolver.add(new MapELResolver());
    compositeResolver.add(new ResourceBundleELResolver());
    compositeResolver.add(new DynamicBeanPropertyELResolver(ItemInstance.class, "getFieldValue", "setFieldValue"));
    compositeResolver.add(new BeanELResolver());
    return new SimpleResolver(compositeResolver);
  }

  /**
   * Read fully.
   *
   * @param reader the reader
   * @return the string
   * @throws ScriptException the script exception
   */
  private String readFully(Reader reader) throws ScriptException {
    char[] array = new char[8192];
    StringBuilder strBuffer = new StringBuilder();
    int count;
    try {
      while ((count = reader.read(array, 0, array.length)) > 0) {
        strBuffer.append(array, 0, count);
      }
    } catch (IOException exp) {
      throw new ScriptException(exp);
    }
    return strBuffer.toString();
  }

  /**
   * Parses the.
   *
   * @param script the script
   * @param scriptContext the script context
   * @return the value expression
   * @throws ScriptException the script exception
   */
  private ValueExpression parse(String script, ScriptContext scriptContext) throws ScriptException {
    try {
      return expressionFactory.createValueExpression(createElContext(scriptContext), script, Object.class);
    } catch (ELException ele) {
      throw new ScriptException(ele);
    }
  }

  /**
   * Creates the el context.
   *
   * @param scriptCtx the script ctx
   * @return the eL context
   */
  private ELContext createElContext(final ScriptContext scriptCtx) {
    // Check if the ELContext is already stored on the ScriptContext
    Object existingELCtx = scriptCtx.getAttribute("elcontext");
    if (existingELCtx instanceof ELContext) {
      return (ELContext) existingELCtx;
    }

    scriptCtx.setAttribute("context", scriptCtx, ScriptContext.ENGINE_SCOPE);

    // Built-in function are added to ScriptCtx
    scriptCtx.setAttribute("out:print", getPrintMethod(), ScriptContext.ENGINE_SCOPE);

    SecurityManager securityManager = System.getSecurityManager();
    if (securityManager == null) {
      scriptCtx.setAttribute("lang:import", getImportMethod(), ScriptContext.ENGINE_SCOPE);
    }

    ELContext elContext = new ELContext() {

      ELResolver resolver = createElResolver();
      VariableMapper varMapper = new ScriptContextVariableMapper(scriptCtx);
      FunctionMapper funcMapper = new ScriptContextFunctionMapper(scriptCtx);

      @Override
      public ELResolver getELResolver() {
        return resolver;
      }

      @Override
      public VariableMapper getVariableMapper() {
        return varMapper;
      }

      @Override
      public FunctionMapper getFunctionMapper() {
        return funcMapper;
      }
    };
    // Store the elcontext in the scriptContext to be able to reuse
    scriptCtx.setAttribute("elcontext", elContext, ScriptContext.ENGINE_SCOPE);
    return elContext;
  }

  /**
   * Gets the prints the method.
   *
   * @return the prints the method
   */
  private static Method getPrintMethod() {
    try {
      return JuelScriptEngine.class.getMethod("print", new Class[] { Object.class });
    } catch (Exception exp) {
      // Will never occur
      return null;
    }
  }

  /**
   * Prints the.
   *
   * @param object the object
   */
  public static void print(Object object) {
    System.out.print(object);
  }

  /**
   * Gets the import method.
   *
   * @return the import method
   */
  private static Method getImportMethod() {
    try {
      return JuelScriptEngine.class.getMethod("importFunctions", new Class[] { ScriptContext.class, String.class, Object.class });
    } catch (Exception exp) {
      // Will never occur
      return null;
    }
  }

  /**
   * Import functions.
   *
   * @param ctx the ctx
   * @param namespace the namespace
   * @param obj the obj
   */
  public static void importFunctions(ScriptContext ctx, String namespace, Object obj) {
    Class< ? > clazz = null;
    if (obj instanceof Class) {
      clazz = (Class< ? >) obj;
    } else if (obj instanceof String) {
      try {
        clazz = ReflectUtil.loadClass((String) obj);
      } catch (ActivitiException ae) {
        throw new ELException(ae);
      }
    } else {
      throw new ELException("Class or class name is missing");
    }
    Method[] methods = clazz.getMethods();
    for (Method m : methods) {
      int mod = m.getModifiers();
      if (Modifier.isStatic(mod) && Modifier.isPublic(mod)) {
        String name = namespace + ":" + m.getName();
        ctx.setAttribute(name, m, ScriptContext.ENGINE_SCOPE);
      }
    }
  }

  /**
   * Class representing a compiled script using JUEL.
   * 
   * @author Frederik Heremans
   */
  private class JuelCompiledScript extends CompiledScript {

    /** The value expression. */
    private ValueExpression valueExpression;

    /**
     * Instantiates a new juel compiled script.
     *
     * @param valueExpression the value expression
     */
    JuelCompiledScript(ValueExpression valueExpression) {
      this.valueExpression = valueExpression;
    }

    /* (non-Javadoc)
     * @see javax.script.CompiledScript#getEngine()
     */
    public ScriptEngine getEngine() {
      // Return outer class instance
      return JuelScriptEngine.this;
    }

    /* (non-Javadoc)
     * @see javax.script.CompiledScript#eval(javax.script.ScriptContext)
     */
    public Object eval(ScriptContext ctx) throws ScriptException {
      return evaluateExpression(valueExpression, ctx);
    }
  }

  /**
   * ValueMapper that uses the ScriptContext to get variable values or value
   * expressions.
   * 
   * @author Frederik Heremans
   */
  private class ScriptContextVariableMapper extends VariableMapper {

    /** The script context. */
    private ScriptContext scriptContext;

    /**
     * Instantiates a new script context variable mapper.
     *
     * @param scriptCtx the script ctx
     */
    ScriptContextVariableMapper(ScriptContext scriptCtx) {
      this.scriptContext = scriptCtx;
    }

    /* (non-Javadoc)
     * @see org.activiti.engine.impl.javax.el.VariableMapper#resolveVariable(java.lang.String)
     */
    @Override
    public ValueExpression resolveVariable(String variableName) {
      int scope = scriptContext.getAttributesScope(variableName);
      if (scope != -1) {
        Object value = scriptContext.getAttribute(variableName, scope);
        if (value instanceof ValueExpression) {
          // Just return the existing ValueExpression
          return (ValueExpression) value;
        } else {
          // Create a new ValueExpression based on the variable value
          return expressionFactory.createValueExpression(value, Object.class);
        }
      }
      return null;
    }

    /* (non-Javadoc)
     * @see org.activiti.engine.impl.javax.el.VariableMapper#setVariable(java.lang.String, org.activiti.engine.impl.javax.el.ValueExpression)
     */
    @Override
    public ValueExpression setVariable(String name, ValueExpression value) {
      ValueExpression previousValue = resolveVariable(name);
      scriptContext.setAttribute(name, value, ScriptContext.ENGINE_SCOPE);
      return previousValue;
    }
  }

  /**
   * FunctionMapper that uses the ScriptContext to resolve functions in EL.
   * 
   * @author Frederik Heremans
   */
  private class ScriptContextFunctionMapper extends FunctionMapper {

    /** The script context. */
    private ScriptContext scriptContext;

    /**
     * Instantiates a new script context function mapper.
     *
     * @param ctx the ctx
     */
    ScriptContextFunctionMapper(ScriptContext ctx) {
      this.scriptContext = ctx;
    }

    /**
     * Gets the full function name.
     *
     * @param prefix the prefix
     * @param localName the local name
     * @return the full function name
     */
    private String getFullFunctionName(String prefix, String localName) {
      return prefix + ":" + localName;
    }

    /* (non-Javadoc)
     * @see org.activiti.engine.impl.javax.el.FunctionMapper#resolveFunction(java.lang.String, java.lang.String)
     */
    @Override
    public Method resolveFunction(String prefix, String localName) {
      String functionName = getFullFunctionName(prefix, localName);
      int scope = scriptContext.getAttributesScope(functionName);
      if (scope != -1) {
        // Methods are added as variables in the ScriptScope
        Object attributeValue = scriptContext.getAttribute(functionName);
        return (attributeValue instanceof Method) ? (Method) attributeValue : null;
      } else {
        return null;
      }
    }
  }

}
