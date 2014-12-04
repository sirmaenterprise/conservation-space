/*
 * Based on JUEL 2.2.1 code, 2006-2009 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.juel;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELResolver;
import org.activiti.engine.impl.javax.el.FunctionMapper;
import org.activiti.engine.impl.javax.el.ValueExpression;
import org.activiti.engine.impl.javax.el.VariableMapper;

// TODO: Auto-generated Javadoc
/**
 * Simple context implementation.
 * 
 * @author Christoph Beck
 */
public class SimpleContext extends ELContext {
	
	/**
	 * The Class Functions.
	 */
	static class Functions extends FunctionMapper {
		
		/** The map. */
		Map<String, Method> map = Collections.emptyMap();

		/* (non-Javadoc)
		 * @see org.activiti.engine.impl.javax.el.FunctionMapper#resolveFunction(java.lang.String, java.lang.String)
		 */
		@Override
		public Method resolveFunction(String prefix, String localName) {
			return map.get(prefix + ":" + localName);
		}

		/**
		 * Sets the function.
		 *
		 * @param prefix the prefix
		 * @param localName the local name
		 * @param method the method
		 */
		public void setFunction(String prefix, String localName, Method method) {
			if (map.isEmpty()) {
				map = new HashMap<String, Method>();
			}
			map.put(prefix + ":" + localName, method);
		}
	}

	/**
	 * The Class Variables.
	 */
	static class Variables extends VariableMapper {
		
		/** The map. */
		Map<String, ValueExpression> map = Collections.emptyMap();

		/* (non-Javadoc)
		 * @see org.activiti.engine.impl.javax.el.VariableMapper#resolveVariable(java.lang.String)
		 */
		@Override
		public ValueExpression resolveVariable(String variable) {
			return map.get(variable);
		}

		/* (non-Javadoc)
		 * @see org.activiti.engine.impl.javax.el.VariableMapper#setVariable(java.lang.String, org.activiti.engine.impl.javax.el.ValueExpression)
		 */
		@Override
		public ValueExpression setVariable(String variable, ValueExpression expression) {
			if (map.isEmpty()) {
				map = new HashMap<String, ValueExpression>();
			}
			return map.put(variable, expression);
		}
	}

	/** The functions. */
	private Functions functions;
	
	/** The variables. */
	private Variables variables;
	
	/** The resolver. */
	private ELResolver resolver;

	/**
	 * Create a context.
	 */
	public SimpleContext() {
		this(null);
	}

	/**
	 * Create a context, use the specified resolver.
	 *
	 * @param resolver the resolver
	 */
	public SimpleContext(ELResolver resolver) {
		this.resolver = resolver;
	}

	/**
	 * Define a function.
	 *
	 * @param prefix the prefix
	 * @param localName the local name
	 * @param method the method
	 */
	public void setFunction(String prefix, String localName, Method method) {
		if (functions == null) {
			functions = new Functions();
		}
		functions.setFunction(prefix, localName, method);
	}

	/**
	 * Define a variable.
	 *
	 * @param name the name
	 * @param expression the expression
	 * @return the value expression
	 */
	public ValueExpression setVariable(String name, ValueExpression expression) {
		if (variables == null) {
			variables = new Variables();
		}
		return variables.setVariable(name, expression);
	}

	/**
	 * Get our function mapper.
	 *
	 * @return the function mapper
	 */
	@Override
	public FunctionMapper getFunctionMapper() {
		if (functions == null) {
			functions = new Functions();
		}
		return functions;
	}

	/**
	 * Get our variable mapper.
	 *
	 * @return the variable mapper
	 */
	@Override
	public VariableMapper getVariableMapper() {
		if (variables == null) {
			variables = new Variables();
		}
		return variables;
	}

	/**
	 * Get our resolver. Lazy initialize to a {@link SimpleResolver} if necessary.
	 *
	 * @return the eL resolver
	 */
	@Override
	public ELResolver getELResolver() {
		if (resolver == null) {
			resolver = new SimpleResolver();
		}
		return resolver;
	}

	/**
	 * Set our resolver.
	 *
	 * @param resolver the new eL resolver
	 */
	public void setELResolver(ELResolver resolver) {
		this.resolver = resolver;
	}
}
