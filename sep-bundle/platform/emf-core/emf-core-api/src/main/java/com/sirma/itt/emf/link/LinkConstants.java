package com.sirma.itt.emf.link;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Interface that links some of the default links identifiers.
 * 
 * @author BBonev
 */
public interface LinkConstants {

	/** Relation used to define tree parent to child link. */
	String TREE_PARENT_TO_CHILD = "emf:parentOf";
	/** Relation used to define tree child to parent link. */
	String TREE_CHILD_TO_PARENT = "emf:hasParent";

	String PART_OF_URI = "ptop:partOf";

	String REFERENCES_URI = "emf:references";

	/** The has child uri. */
	String HAS_CHILD_URI = "emf:hasChild";

	/** The outgoing documents from a task link id. */
	String OUTGOING_DOCUMENTS_LINK_ID = "emf:outgoingDocuments";

	/** The incoming documents to a task link id. */
	String INCOMING_DOCUMENTS_LINK_ID = "emf:incomingDocuments";

	/** The manual case to case link id. */
	String MANUAL_CASE_TO_CASE_LINK_ID = REFERENCES_URI;

	/** The link description. */
	String LINK_DESCRIPTION = "emf:linkDescription";

	/** The parent to child. */
	String PARENT_TO_CHILD = HAS_CHILD_URI;

	/** The child to parent. */
	String CHILD_TO_PARENT = PART_OF_URI;

	/** The parent to child. */
	String ROOT_TO_CHILD = HAS_CHILD_URI;

	/** The child to parent. */
	String CHILD_TO_ROOT = PART_OF_URI;

	/** mail to attachment. */
	String LINK_MAIL_ATTACHMENT = "emf:mail_attachment";

	/** attachment to mail. */
	String LINK_ATTACHMENT_MAIL = "emf:attachment_mail";

	/** link type for an object that has a primary image */
	String HAS_PRIMARY_IMAGE = "emf:hasPrimaryImage";

	/** link type for an image that is associated to an object */
	String IS_PRIMARY_IMAGE_OF = "emf:isPrimaryImageOf";

	/** link type for an object that has a thumbnail image */
	String HAS_THUMBNAIL = "emf:hasThumbnail";

	/** link type for an image that is associated to an object */
	String IS_THUMBNAIL_OF = "emf:isThumbnailOf";

	/** The default link properties. In the map it added the system user as creator. */
	// NOTE: generally this will not work due to the fact we are in interface and have a code that
	// is taken from a class initialized lazy from outside the class loader. But we have the same
	// reference of the system user that will be initialized later (after being added to the map)
	// it should still work.
	Map<String, Serializable> DEFAULT_SYSTEM_PROPERTIES = Collections
			.unmodifiableMap(CollectionUtils.addToMap(null, new Pair<String, Serializable>(
					DefaultProperties.CREATED_BY, SecurityContextManager.getSystemUser())));

	/** Property that point if relation is deleted or not is deleted. */
	String IS_ACTIVE = "emf:isActive";

	String IS_ATTACHED = "isAttached";
	/** The processes. */
	String PROCESSES = "emf:processes";
	/** The processed by. */
	String PROCESSED_BY = "emf:processedBy";
	String PRIMARY_PARENT = "emf:primaryParent";
}
