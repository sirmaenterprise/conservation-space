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
package org.activiti.engine.impl.javax.el;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * This class encapsulates a base model object and one of its properties.
 * 
 * @since 2.2
 */
public class ValueReference implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The base. */
	private Object base;
	
	/** The property. */
	private Object property;

	/**
	 * Instantiates a new value reference.
	 *
	 * @param base the base
	 * @param property the property
	 */
	public ValueReference(Object base, Object property) {
		this.base = base;
		this.property = property;
	}

	/**
	 * Gets the base.
	 *
	 * @return the base
	 */
	public Object getBase() {
		return base;
	}

	/**
	 * Gets the property.
	 *
	 * @return the property
	 */
	public Object getProperty() {
		return property;
	}
}