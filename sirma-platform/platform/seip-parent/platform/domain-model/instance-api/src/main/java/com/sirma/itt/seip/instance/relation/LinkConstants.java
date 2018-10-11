package com.sirma.itt.seip.instance.relation;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tasks.TransactionMode;

/**
 * Interface that links some of the default links identifiers.
 *
 * @author BBonev
 */
// suppress warning for class constructor - because the class is extended
@SuppressWarnings("squid:S1118")
public class LinkConstants {

	/** Relation used to define instance created by template link  */
	public static final String HAS_TEMPLATE = "emf:hasTemplate";
 

	public static final String REFERENCES_URI = "emf:references";

	/** The outgoing documents from a task link id. */
	public static final String OUTGOING_DOCUMENTS_LINK_ID = "emf:outgoingDocuments";

	/** The incoming documents to a task link id. */
	public static final String INCOMING_DOCUMENTS_LINK_ID = "emf:incomingDocuments";

	/** The manual case to case link id. */
	public static final String MANUAL_CASE_TO_CASE_LINK_ID = REFERENCES_URI;

	/** The link description. */
	public static final String LINK_DESCRIPTION = "emf:linkDescription";

	/** mail to attachment. */
	public static final String LINK_MAIL_ATTACHMENT = "emf:hasMailAttachment";

	/** attachment to mail. */
	public static final String LINK_ATTACHMENT_MAIL = "emf:isAttachmentTo";

	/** link type for an object that has a primary image */
	public static final String HAS_PRIMARY_IMAGE = "emf:hasPrimaryImage";

	/** link type for an image that is associated to an object */
	public static final String IS_PRIMARY_IMAGE_OF = "emf:isPrimaryImageOf";

	/** link type for an object that has a thumbnail image */
	public static final String HAS_THUMBNAIL = "emf:hasThumbnail";

	/** link type for an image that is associated to an object */
	public static final String IS_THUMBNAIL_OF = "emf:isThumbnailOf";

	/**
	 * The default link properties. In the map it added the system user as creator.
	 */
	// NOTE: generally this will not work due to the fact we are in interface
	// and have a code that
	// is taken from a class initialized lazy from outside the class loader. But
	// we have the same
	// reference of the system user that will be initialized later (after being
	// added to the map)
	// it should still work.
	private static ContextualMap<String, Serializable> defaultSystemProperties;

	/** Property that point if relation is deleted or not is deleted. */
	public static final String IS_ACTIVE = "emf:isActive";

	public static final String IS_ATTACHED = "isAttached";
	/** The processes. */
	public static final String PROCESSES = "emf:processes";
	/** The processed by. */
	public static final String PROCESSED_BY = "emf:processedBy";
 
	public static final String HAS_REVISION = "emf:hasRevision";
	public static final String IS_REVISION_OF = "emf:isRevisionOf";
	public static final String NEXT_REVISION = "emf:nextRevision";
	public static final String PREVIOUS_REVISION = "emf:previousRevision";

	/**
	 * Defines a relation between 2 instances and indicates that the second instance is attached to the first one.
	 * Inverse of {@link #IS_ATTACHED_TO}
	 */
	public static final String HAS_ATTACHMENT = "emf:hasAttachment";
	/**
	 * Defines a relation between 2 instances and indicates that the first instance is attached to the second one.
	 * Inverse of {@link #HAS_ATTACHMENT}
	 */
	public static final String IS_ATTACHED_TO = "emf:isAttachedTo";

	/**
	 * Initialize some properties that come from configurations.
	 *
	 * @param securityContextManager
	 *            the security configuration
	 * @param mapInstance
	 *            contextual map instance to write the default system properties to
	 */
	@Startup(phase = StartupPhase.BEFORE_APP_START, transactionMode = TransactionMode.NOT_SUPPORTED)
	public static void init(SecurityContextManager securityContextManager,
			ContextualMap<String, Serializable> mapInstance) {
		defaultSystemProperties = mapInstance;
		defaultSystemProperties.initializeWith(() -> Collections.unmodifiableMap(CollectionUtils.addToMap(null,
				new Pair<>(DefaultProperties.CREATED_BY, securityContextManager.getSystemUser()))));
	}

	/**
	 * Gets the default system link properties.
	 *
	 * @return the default system properties
	 */
	public static Map<String, Serializable> getDefaultSystemProperties() {
		return defaultSystemProperties.getContextValue();
	}

	/**
	 * Defines a relation between user and instance, indicates that the second instance is marked as favorite for the
	 * user.
	 */
	public static final String HAS_FAVOURITE = "emf:hasFavourite";

	/**
	 * Defines a relation between user and instance, indicates that the second instance is marked for download for the
	 * user.
	 */
	public static final String MARKED_FOR_DOWNLOAD = "emf:markedForDownload";

	/**
	 * Defines a relation between instance and definition, indicates that the first instance is default location for
	 * instances created with the definition.
	 */
	public static final String IS_DEFAULT_LOCATION = "emf:isDefaultLocation";
}
