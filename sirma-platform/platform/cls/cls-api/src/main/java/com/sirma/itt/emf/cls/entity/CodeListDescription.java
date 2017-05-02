package com.sirma.itt.emf.cls.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * Entity POJO representing a code list description. Extends {@link Description} where the common attributes are
 * defined. This class is used only to split the Descriptions table into two tables - one for code lists and one for
 * code values. Both tables have the same fields.<br>
 * <b>NOTE</b>: The DB ID is not serialized to JSON.<br>
 * <b>NOTE</b>: Attributes with null values are not serialized to JSON.
 *
 * @author Mihail Radkov
 * @author Nikolay Velkov
 */
@Entity
@Table(name = "CLS_CODELISTDESCRIPTION")
@JsonIgnoreProperties(value = { "id" })
@JsonSerialize(include = Inclusion.NON_NULL)
public class CodeListDescription extends Description {

	/** Auto generated serial version UID. */
	private static final long serialVersionUID = 6863023338136456467L;

}
