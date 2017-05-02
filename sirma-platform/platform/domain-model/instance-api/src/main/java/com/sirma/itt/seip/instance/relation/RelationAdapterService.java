/**
 *
 */
package com.sirma.itt.seip.instance.relation;

import com.sirma.itt.seip.domain.instance.DMSInstance;

/**
 * Adapter service that provides operations for adding and removing associations between DMS nodes.
 *
 * @author BBonev
 */
public interface RelationAdapterService {
	/**
	 * Links a instance to another with the parent/child assoc type. The assocName could customize the association name.
	 * The method allows duplicate definitions and should be paid attention for association request leading to
	 * duplicates.
	 *
	 * @param parent
	 *            is instance with the parent role
	 * @param child
	 *            is instance with the child role
	 * @param parentToUnlink
	 *            is optional param to remove old association to this parent
	 * @param assocName
	 *            customized association name. could be null
	 * @return true on success
	 */
	boolean linkAsChild(DMSInstance parent, DMSInstance child, DMSInstance parentToUnlink, String assocName);

	/**
	 * Removes a link between instance to another with the parent/child association type. The method removes all
	 * associations with the provided name if any. If no name is provided all associations will be removed between the
	 * two instances.
	 *
	 * @param parent
	 *            is instance with the parent role
	 * @param child
	 *            is instance with the child role
	 * @param assocName
	 *            customized association name. could be null
	 * @return true on success
	 */
	boolean removeLinkAsChild(DMSInstance parent, DMSInstance child, String assocName);
}
