/*
 * Copyright (C) 2005-2010 Alfresco Software Limited. This file is part of Alfresco Alfresco is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version. Alfresco is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General License for more details. You should have received a
 * copy of the GNU Lesser General License along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sirma.itt.seip.domain.definition;

import java.io.Serializable;
import java.util.Set;

import com.sirma.itt.seip.Entity;

/**
 * Read-only definition of a Data Type.
 */
public interface DataTypeDefinition extends Entity<Long>, Serializable {

	//
	// Built-in Property Types
	//
	/** The text. */
	String TEXT = "text";
	/** The int. */
	String INT = "int";
	/** The long. */
	String LONG = "long";
	/** The float. */
	String FLOAT = "float";
	/** The double. */
	String DOUBLE = "double";
	/** The date. */
	String DATE = "date";
	/** The datetime. */
	String DATETIME = "datetime";
	/** The boolean. */
	String BOOLEAN = "boolean";
	/** The any. */
	String ANY = "any";

	/** The instance. */
	String INSTANCE = "instance";

	/** The uri. */
	String URI = "uri";

	/**
	 * Gets the name.
	 *
	 * @return the qualified name of the data type
	 */
	String getName();

	/**
	 * Gets the title.
	 *
	 * @return the human-readable class title
	 */
	String getTitle();

	/**
	 * Gets the description.
	 *
	 * @return the human-readable class description
	 */
	String getDescription();

	/**
	 * Gets the java class name.
	 *
	 * @return the equivalent java class name (or null, if not mapped)
	 */
	String getJavaClassName();

	/**
	 * Gets the java class represented by the java class name returned by.
	 *
	 * @return the java class {@link #getJavaClassName()}.
	 */
	Class<?> getJavaClass();

	/**
	 * Gets the Semantic URI identifier. If are defined more then one the first will be returned!.
	 *
	 * @return the uri
	 */
	String getFirstUri();

	/**
	 * Gets all defined URIs for the given type in order of definition.
	 *
	 * @return the uries
	 */
	Set<String> getUries();

}
