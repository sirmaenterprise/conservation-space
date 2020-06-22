@TypeDefs({
		@TypeDef(name = "com.sirma.itt.seip.db.customtype.StringListCustomType", typeClass = StringListCustomType.class),
		@TypeDef(name = "com.sirma.itt.seip.db.customtype.StringSetCustomType", typeClass = StringSetCustomType.class),
		@TypeDef(name = "com.sirma.itt.seip.db.customtype.LongSetCustomType", typeClass = LongSetCustomType.class),
		@TypeDef(name = BooleanCustomType.TYPE_NAME, typeClass = BooleanCustomType.class) })
/**
 * Contains custom db types.
 */
package com.sirma.itt.seip.db.customtype;

import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
