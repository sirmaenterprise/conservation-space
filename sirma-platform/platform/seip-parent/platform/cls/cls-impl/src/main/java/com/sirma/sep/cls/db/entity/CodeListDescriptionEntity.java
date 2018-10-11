package com.sirma.sep.cls.db.entity;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Database entity for storing {@link com.sirma.sep.cls.model.CodeDescription} of {@link com.sirma.sep.cls.model.CodeList}. All of the
 * needed columns are declared in {@link CodeDescriptionEntity}, this is needed just for forcing separate database table.
 *
 * @author Mihail Radkov
 */
@Entity
@Table(name = "CLS_CODELISTDESCRIPTION")
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
public class CodeListDescriptionEntity extends CodeDescriptionEntity {
	// Nothing to add
}
