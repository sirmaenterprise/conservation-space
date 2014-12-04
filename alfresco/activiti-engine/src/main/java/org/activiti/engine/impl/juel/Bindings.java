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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.activiti.engine.impl.javax.el.ELException;
import org.activiti.engine.impl.javax.el.ValueExpression;


// TODO: Auto-generated Javadoc
/**
 * Bindings, usually created by a {@link org.activiti.engine.impl.juel.Tree}.
 *
 * @author Christoph Beck
 */
public class Bindings implements TypeConverter {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant NO_FUNCTIONS. */
	private static final Method[] NO_FUNCTIONS = new Method[0];
	
	/** The Constant NO_VARIABLES. */
	private static final ValueExpression[] NO_VARIABLES = new ValueExpression[0];

	/**
	 * Wrap a {@link Method} for serialization.
	 */
	private static class MethodWrapper implements Serializable {
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/** The method. */
		private transient Method method;
		
		/**
		 * Instantiates a new method wrapper.
		 *
		 * @param method the method
		 */
		private MethodWrapper(Method method) {
			this.method = method;
		}
		
		/**
		 * Write object.
		 *
		 * @param out the out
		 * @throws IOException Signals that an I/O exception has occurred.
		 * @throws ClassNotFoundException the class not found exception
		 */
		private void writeObject(ObjectOutputStream out) throws IOException, ClassNotFoundException {
			out.defaultWriteObject();
			out.writeObject(method.getDeclaringClass());
			out.writeObject(method.getName());
			out.writeObject(method.getParameterTypes());
		}
		
		/**
		 * Read object.
		 *
		 * @param in the in
		 * @throws IOException Signals that an I/O exception has occurred.
		 * @throws ClassNotFoundException the class not found exception
		 */
		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
			Class<?> type = (Class<?>)in.readObject();
			String name = (String)in.readObject();
			Class<?>[] args = (Class<?>[])in.readObject();
			try {
				method = type.getDeclaredMethod(name, args);
			} catch (NoSuchMethodException e) {
				throw new IOException(e.getMessage());
			}
		}	
	}

	/** The functions. */
	private transient Method[] functions;
	
	/** The variables. */
	private final ValueExpression[] variables;
	
	/** The converter. */
	private final TypeConverter converter;

	/**
	 * Constructor.
	 *
	 * @param functions the functions
	 * @param variables the variables
	 */
	public Bindings(Method[] functions, ValueExpression[] variables) {
		this(functions, variables, TypeConverter.DEFAULT);
	}

	/**
	 * Constructor.
	 *
	 * @param functions the functions
	 * @param variables the variables
	 * @param converter the converter
	 */
	public Bindings(Method[] functions, ValueExpression[] variables, TypeConverter converter) {
		super();

		this.functions = functions == null || functions.length == 0 ? NO_FUNCTIONS : functions;
		this.variables = variables == null || variables.length == 0 ? NO_VARIABLES : variables;
		this.converter = converter == null ? TypeConverter.DEFAULT : converter;
	}
	
	/**
	 * Get function by index.
	 * @param index function index
	 * @return method
	 */
	public Method getFunction(int index) {
		return functions[index];
	}
	
	/**
	 * Test if given index is bound to a function.
	 * This method performs an index check.
	 * @param index identifier index
	 * @return <code>true</code> if the given index is bound to a function
	 */
	public boolean isFunctionBound(int index) {
		return index >= 0 && index < functions.length;
	}
	
	/**
	 * Get variable by index.
	 * @param index identifier index
	 * @return value expression
	 */
	public ValueExpression getVariable(int index) {
		return variables[index];
	}

	/**
	 * Test if given index is bound to a variable.
	 * This method performs an index check.
	 * @param index identifier index
	 * @return <code>true</code> if the given index is bound to a variable
	 */
	public boolean isVariableBound(int index) {
		return index >= 0 && index < variables.length && variables[index] != null;
	}
	
	/**
	 * Apply type conversion.
	 *
	 * @param <T> the generic type
	 * @param value value to convert
	 * @param type target type
	 * @return converted value
	 */
	public <T> T convert(Object value, Class<T> type) {
		return converter.convert(value, type);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Bindings) {
			Bindings other = (Bindings)obj;
			return Arrays.equals(functions, other.functions)
				&& Arrays.equals(variables, other.variables)
				&& converter.equals(other.converter);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(functions) ^ Arrays.hashCode(variables) ^ converter.hashCode();
	}

	/**
	 * Write object.
	 *
	 * @param out the out
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void writeObject(ObjectOutputStream out) throws IOException, ClassNotFoundException {
		out.defaultWriteObject();
		MethodWrapper[] wrappers = new MethodWrapper[functions.length];
		for (int i = 0; i < wrappers.length; i++) {
			wrappers[i] = new MethodWrapper(functions[i]);
		}
		out.writeObject(wrappers);
	}

	/**
	 * Read object.
	 *
	 * @param in the in
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		MethodWrapper[] wrappers = (MethodWrapper[])in.readObject();
		if (wrappers.length == 0) {
			functions = NO_FUNCTIONS;
		} else {
			functions = new Method[wrappers.length];
			for (int i = 0; i < functions.length; i++) {
				functions[i] = wrappers[i].method;
			}
		}
	}	
}
