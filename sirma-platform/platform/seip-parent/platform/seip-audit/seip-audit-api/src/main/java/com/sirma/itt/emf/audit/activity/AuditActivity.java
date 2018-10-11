package com.sirma.itt.emf.audit.activity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

/**
 * POJO entity describing an audit activity. It is designed to be serialized with Kryo.
 *
 * @author Mihail Radkov
 */
@PersistenceUnitBinding("seip-auditlog")
@Entity
@Table(name = "emf_events", indexes = @Index(name = "idx_datereceived", columnList = "datereceived"))
@NamedQueries({ @NamedQuery(name = AuditActivity.AUDIT_SELECT_KEY, query = AuditActivity.AUDIT_SELECT),
		@NamedQuery(name = AuditActivity.GET_ACTIVITIES_AFTER_KEY, query = AuditActivity.GET_ACTIVITIES_AFTER) })
public class AuditActivity implements Serializable, com.sirma.itt.seip.Entity<Long> {

	/**
	 * Relation operation status for addition of a relation/data.
	 */
	public static final String STATUS_ADDED = "add";
	/**
	 * Relation operation status for removal of a relation/data.
	 */
	public static final String STATUS_REMOVED = "remove";
	/**
	 * Relation status that combines the add and remove. This is special and used when the entry is imported in solr
	 */
	public static final String STATUS_CHANGE = "change";

	private static final long serialVersionUID = -1147254095159096654L;

	/**
	 * Query for selecting activities by their IDs.
	 */
	public static final String AUDIT_SELECT_KEY = "AUDIT_SELECT";
	static final String AUDIT_SELECT = "select aa from AuditActivity aa where aa.id in (:ids)";
	/**
	 * Query activities that are inserted after the given event date. The query accepts:
	 * <ol>
	 * <li>{@code lastKnownDate} - a date that will be used for fetching recored that are newer than the given date
	 * <li>{@code requestIdLimit} - a limit to use for the returned unique request ids. Suggested value is 50.
	 * </ol>
	 * The data is fetched via sub query so that all entries for the selected requests is fetched. <br>
	 * The query does not return entries without request identifier
	 */
	public static final String GET_ACTIVITIES_AFTER_KEY = "GET_ACTIVITIES_AFTER";
	static final String GET_ACTIVITIES_AFTER = "select distinct aa from AuditActivity aa where "
			+ "aa.eventDate > :lastProcessedDate AND "
			+ "aa.requestId in ("
			+ "select distinct a.requestId from com.sirma.itt.emf.audit.activity.ActivitiesView a where a.dateReceived > :lastKnownDate group by a.requestId having (count (a.requestId) <= :requestIdLimit)"
			+ ") order by aa.dateReceived ASC";

	/**
	 * Instantiates a new audit activity. Used to copy {@link AuditActivity} objects.
	 *
	 * @param activity
	 *            the activity
	 */
	public AuditActivity(AuditActivity activity) {
		eventDate = activity.getEventDate();
		userName = activity.getUserName();
		actionID = activity.getActionID();
		objectType = activity.getObjectType();
		objectSubType = activity.getObjectSubType();
		objectState = activity.getObjectState();
		objectPreviousState = activity.getObjectPreviousState();
		objectTitle = activity.getObjectTitle();
		objectID = activity.getObjectID();
		objectSystemID = activity.getObjectSystemID();
		objectURL = activity.getObjectURL();
		context = activity.getContext();
		dateReceived = activity.getDateReceived();
		requestId = activity.getRequestId();
		relationId = activity.getRelationId();
		targetProperties = activity.getTargetProperties();
		relationStatus = activity.getRelationStatus();
	}

	/**
	 * Instantiates a new audit activity.
	 */
	public AuditActivity() {
	}

	/** The id. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Tag(9000)
	private Long id;

	/** The event date. */
	@Column(name = "eventdate")
	@Tag(9001)
	private Date eventDate;

	/** The user name. */
	@Column(name = "username", columnDefinition = "TEXT")
	@Tag(9002)
	private String userName;

	/** The action id. */
	@Column(name = "actionid", columnDefinition = "TEXT")
	@Tag(9004)
	private String actionID;

	/** The object type. */
	@Column(name = "objecttype", columnDefinition = "TEXT")
	@Tag(9005)
	private String objectType;

	/** The object sub type. */
	@Column(name = "objectsubtype", columnDefinition = "TEXT")
	@Tag(9006)
	private String objectSubType;

	/** The object primary state. */
	@Column(name = "objectstate", columnDefinition = "TEXT")
	@Tag(9007)
	private String objectState;

	/** The object previous state. */
	@Column(name = "objectpreviousstate", columnDefinition = "TEXT")
	@Tag(9009)
	private String objectPreviousState;

	/** The object title. */
	@Column(name = "objecttitle", columnDefinition = "TEXT")
	@Tag(9010)
	private String objectTitle;

	/** The object id. */
	@Column(name = "objectid", columnDefinition = "TEXT")
	@Tag(9011)
	private String objectID;

	/** The object system id. */
	@Column(name = "objectsystemid", columnDefinition = "TEXT")
	@Tag(9012)
	private String objectSystemID;

	/** The object url. */
	@Column(name = "objecturl", columnDefinition = "TEXT")
	@Tag(9013)
	private String objectURL;

	// Note: Once retrieved from the database, this field will be repopulated from the
	// AuditConverter.
	/** The context. */
	@Column(name = "context", columnDefinition = "TEXT")
	@Tag(9014)
	private String context;

	/** The date received. */
	@Column(name = "datereceived", insertable = false)
	@Tag(9015)
	private Date dateReceived;

	/** The request id. */
	@Column(name = "requestid", columnDefinition = "TEXT")
	@Tag(9016)
	private String requestId;

	/** The relation id. */
	@Column(name = "relationid", columnDefinition = "TEXT")
	@Tag(9017)
	private String relationId;

	/** The target properties. */
	@Column(name = "targetproperties", columnDefinition = "TEXT")
	@Tag(9018)
	private String targetProperties;

	/** The relation status. */
	@Column(name = "relationstatus", columnDefinition = "TEXT")
	@Tag(9019)
	private String relationStatus;

	/** The relation status. */
	@Column(name = "userid", columnDefinition = "TEXT")
	@Tag(9020)
	private String userId;

	/** The object type label. */
	@Transient
	private String objectTypeLabel;

	/** The object sub type label. */
	@Transient
	private String objectSubTypeLabel;

	/** The object instance type. */
	@Transient
	private String objectInstanceType;

	/** The user display name. */
	@Transient
	private String userDisplayName;

	/** The action. */
	@Transient
	private String action;

	/** The object state label. */
	@Transient
	private String objectStateLabel;

	/** The previous state label. */
	@Transient
	private String objectPreviousStateLabel;

	/**
	 * Getter method for id.
	 *
	 * @return the id
	 */
	@Override
	public Long getId() {
		return id;
	}

	/**
	 * Setter method for id.
	 *
	 * @param id
	 *            the id to set
	 */
	@Override
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Getter method for eventDate.
	 *
	 * @return the eventDate
	 */
	public Date getEventDate() {
		return eventDate;
	}

	/**
	 * Setter method for eventDate.
	 *
	 * @param eventDate
	 *            the eventDate to set
	 */
	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}

	/**
	 * Getter method for userName.
	 *
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Setter method for userName.
	 *
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Getter method for actionID.
	 *
	 * @return the actionID
	 */
	public String getActionID() {
		return actionID;
	}

	/**
	 * Setter method for actionID.
	 *
	 * @param actionID
	 *            the actionID to set
	 */
	public void setActionID(String actionID) {
		this.actionID = actionID;
	}

	/**
	 * Getter method for objectType.
	 *
	 * @return the objectType
	 */
	public String getObjectType() {
		return objectType;
	}

	/**
	 * Setter method for objectType.
	 *
	 * @param objectType
	 *            the objectType to set
	 */
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	/**
	 * Getter method for objectSubType.
	 *
	 * @return the objectSubType
	 */
	public String getObjectSubType() {
		return objectSubType;
	}

	/**
	 * Setter method for objectSubType.
	 *
	 * @param objectSubType
	 *            the objectSubType to set
	 */
	public void setObjectSubType(String objectSubType) {
		this.objectSubType = objectSubType;
	}

	/**
	 * Getter method for objectPrimaryState.
	 *
	 * @return the objectPrimaryState
	 */
	public String getObjectState() {
		return objectState;
	}

	/**
	 * Setter method for objectPrimaryState.
	 *
	 * @param objectState
	 *            the new object state
	 */
	public void setObjectState(String objectState) {
		this.objectState = objectState;
	}

	/**
	 * Getter method for objectPreviousState.
	 *
	 * @return the objectPreviousState
	 */
	public String getObjectPreviousState() {
		return objectPreviousState;
	}

	/**
	 * Setter method for objectPreviousState.
	 *
	 * @param objectPreviousState
	 *            the objectPreviousState to set
	 */
	public void setObjectPreviousState(String objectPreviousState) {
		this.objectPreviousState = objectPreviousState;
	}

	/**
	 * Getter method for objectTitle.
	 *
	 * @return the objectTitle
	 */
	public String getObjectTitle() {
		return objectTitle;
	}

	/**
	 * Setter method for objectTitle.
	 *
	 * @param objectTitle
	 *            the objectTitle to set
	 */
	public void setObjectTitle(String objectTitle) {
		this.objectTitle = objectTitle;
	}

	/**
	 * Getter method for objectID.
	 *
	 * @return the objectID
	 */
	public String getObjectID() {
		return objectID;
	}

	/**
	 * Setter method for objectID.
	 *
	 * @param objectID
	 *            the objectID to set
	 */
	public void setObjectID(String objectID) {
		this.objectID = objectID;
	}

	/**
	 * Getter method for objectSystemID.
	 *
	 * @return the objectSystemID
	 */
	public String getObjectSystemID() {
		return objectSystemID;
	}

	/**
	 * Setter method for objectSystemID.
	 *
	 * @param objectSystemID
	 *            the objectSystemID to set
	 */
	public void setObjectSystemID(String objectSystemID) {
		this.objectSystemID = objectSystemID;
	}

	/**
	 * Getter method for objectURL.
	 *
	 * @return the objectURL
	 */
	public String getObjectURL() {
		return objectURL;
	}

	/**
	 * Setter method for objectURL.
	 *
	 * @param objectURL
	 *            the objectURL to set
	 */
	public void setObjectURL(String objectURL) {
		this.objectURL = objectURL;
	}

	/**
	 * Getter method for context.
	 *
	 * @return the context
	 */
	public String getContext() {
		return context;
	}

	/**
	 * Setter method for context.
	 *
	 * @param context
	 *            the context to set
	 */
	public void setContext(String context) {
		this.context = context;
	}

	/**
	 * Getter method for dateReceived.
	 *
	 * @return the dateReceived
	 */
	public Date getDateReceived() {
		return dateReceived;
	}

	/**
	 * Setter method for dateReceived.
	 *
	 * @param dateReceived
	 *            the dateReceived to set
	 */
	public void setDateReceived(Date dateReceived) {
		this.dateReceived = dateReceived;
	}

	/**
	 * Gets the object type label.
	 *
	 * @return the object type label
	 */
	public String getObjectTypeLabel() {
		return objectTypeLabel;
	}

	/**
	 * Sets the object type label.
	 *
	 * @param objectTypeLabel
	 *            the new object type label
	 */
	public void setObjectTypeLabel(String objectTypeLabel) {
		this.objectTypeLabel = objectTypeLabel;
	}

	/**
	 * Gets the object sub type label.
	 *
	 * @return the object sub type label
	 */
	public String getObjectSubTypeLabel() {
		return objectSubTypeLabel;
	}

	/**
	 * Sets the object sub type label.
	 *
	 * @param objectSubTypeLabel
	 *            the new object sub type label
	 */
	public void setObjectSubTypeLabel(String objectSubTypeLabel) {
		this.objectSubTypeLabel = objectSubTypeLabel;
	}

	/**
	 * Gets the object instance type.
	 *
	 * @return the object instance type
	 */
	public String getObjectInstanceType() {
		return objectInstanceType;
	}

	/**
	 * Sets the object instance type.
	 *
	 * @param objectInstanceType
	 *            the new object instance type
	 */
	public void setObjectInstanceType(String objectInstanceType) {
		this.objectInstanceType = objectInstanceType;
	}

	/**
	 * Gets the user display name.
	 *
	 * @return the user display name
	 */
	public String getUserDisplayName() {
		return userDisplayName;
	}

	/**
	 * Sets the user display name.
	 *
	 * @param userDisplayName
	 *            the new user display name
	 */
	public void setUserDisplayName(String userDisplayName) {
		this.userDisplayName = userDisplayName;
	}

	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * Sets the action.
	 *
	 * @param action
	 *            the new action
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * Gets the object state label.
	 *
	 * @return the object state label
	 */
	public String getObjectStateLabel() {
		return objectStateLabel;
	}

	/**
	 * Sets the object state label.
	 *
	 * @param objectStateLabel
	 *            the new object state label
	 */
	public void setObjectStateLabel(String objectStateLabel) {
		this.objectStateLabel = objectStateLabel;
	}

	/**
	 * Gets the serialversionuid.
	 *
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * Gets the previous state label.
	 *
	 * @return the previous state label
	 */
	public String getObjectPreviousStateLabel() {
		return objectPreviousStateLabel;
	}

	/**
	 * Sets the previous state label.
	 *
	 * @param previousStateLabel
	 *            the new previous state label
	 */
	public void setObjectPreviousStateLabel(String previousStateLabel) {
		objectPreviousStateLabel = previousStateLabel;
	}

	/**
	 * Getter method for requestId.
	 *
	 * @return the requestId
	 */
	public String getRequestId() {
		return requestId;
	}

	/**
	 * Setter method for requestId.
	 *
	 * @param requestId
	 *            the requestId to set
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	/**
	 * Getter method for relationId.
	 *
	 * @return the relationId
	 */
	public String getRelationId() {
		return relationId;
	}

	/**
	 * Setter method for relationId.
	 *
	 * @param relationId
	 *            the relationId to set
	 */
	public void setRelationId(String relationId) {
		this.relationId = relationId;
	}

	/**
	 * Getter method for targetProperties.
	 *
	 * @return the targetProperties
	 */
	public String getTargetProperties() {
		return targetProperties;
	}

	/**
	 * Setter method for targetProperties.
	 *
	 * @param targetProperties
	 *            the targetProperties to set
	 */
	public void setTargetProperties(String targetProperties) {
		this.targetProperties = targetProperties;
	}

	/**
	 * Getter method for relationStatus.
	 *
	 * @return the relationStatus
	 */
	public String getRelationStatus() {
		return relationStatus;
	}

	/**
	 * Setter method for relationStatus.
	 *
	 * @param relationStatus
	 *            the relationStatus to set
	 */
	public void setRelationStatus(String relationStatus) {
		this.relationStatus = relationStatus;
	}

	/**
	 * Getter method for userId.
	 *
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Setter method for userId.
	 *
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
}
