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

import org.activiti.engine.impl.javax.el.ELException;


// TODO: Auto-generated Javadoc
/**
 * Exception type thrown in build phase (scan/parse).
 *
 * @author Christoph Beck
 */
public class TreeBuilderException extends ELException {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The expression. */
	private final String expression;
	
	/** The position. */
	private final int position;
	
	/** The encountered. */
	private final String encountered;
	
	/** The expected. */
	private final String expected;
	
	/**
	 * Instantiates a new tree builder exception.
	 *
	 * @param expression the expression
	 * @param position the position
	 * @param encountered the encountered
	 * @param expected the expected
	 * @param message the message
	 */
	public TreeBuilderException(String expression, int position, String encountered, String expected, String message) {
		super(LocalMessages.get("error.build", expression, message));
		this.expression = expression;
		this.position = position;
		this.encountered = encountered;
		this.expected = expected;
	}
	
	/**
	 * Gets the expression.
	 *
	 * @return the expression string
	 */
	public String getExpression() {
		return expression;
	}
	
	/**
	 * Gets the position.
	 *
	 * @return the error position
	 */
	public int getPosition() {
		return position;
	}
	
	/**
	 * Gets the encountered.
	 *
	 * @return the substring (or description) that has been encountered
	 */
	public String getEncountered() {
		return encountered;
	}
	
	/**
	 * Gets the expected.
	 *
	 * @return the substring (or description) that was expected
	 */
	public String getExpected() {
		return expected;
	}
}
