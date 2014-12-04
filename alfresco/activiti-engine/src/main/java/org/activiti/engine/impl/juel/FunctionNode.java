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

// TODO: Auto-generated Javadoc
/**
 * Function node interface.
 *
 * @author Christoph Beck
 */
public interface FunctionNode extends Node {
	
	/**
	 * Get the full function name.
	 *
	 * @return the name
	 */
	public String getName();

	/**
	 * Get the unique index of this identifier in the expression (e.g. preorder index)
	 *
	 * @return the index
	 */
	public int getIndex();

	/**
	 * Get the number of parameters for this function.
	 *
	 * @return the param count
	 */
	public int getParamCount();
	
	/**
	 * Checks if is var args.
	 *
	 * @return <code>true</code> if this node supports varargs.
	 */
	public boolean isVarArgs();
}
