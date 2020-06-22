package com.sirma.itt.seip.tasks.entity;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.BaseEntity;
import com.sirma.itt.seip.model.SerializableValue;
import com.sirma.itt.seip.serialization.SerializationHelper;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntry;
import com.sirma.itt.seip.tasks.SchedulerEntryStatus;
import com.sirma.itt.seip.tasks.SchedulerEntryType;

/**
 * Represents a single scheduled entry to be executed based on time or event trigger.
 *
 * @author BBonev
 */
@PersistenceUnitBinding({ PersistenceUnits.CORE, PersistenceUnits.PRIMARY})
@Entity
@Table(name = "emf_schedulerentity", indexes = { @Index(name = "idx_sche_tsn", columnList = "nextscheduletime"),
		@Index(name = "idx_sche_tsett", columnList = "eventclassid,targetclass,targetid,operation,user_operation") })
@NamedQueries({
		@NamedQuery(name = SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_STATUS_KEY, query = SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_STATUS),
		@NamedQuery(name = SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_KEY, query = SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER),
		@NamedQuery(name = SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_KEY, query = SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP),
		@NamedQuery(name = SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_USER_OP_KEY, query = SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_USER_OP),
		@NamedQuery(name = SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_AND_USER_OP_KEY, query = SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_AND_USER_OP),
		@NamedQuery(name = SchedulerEntity.QUERY_SCHEDULER_ENTRY_BY_UID_KEY, query = SchedulerEntity.QUERY_SCHEDULER_ENTRY_BY_UID),
		@NamedQuery(name = SchedulerEntity.QUERY_NEXT_EXECUTION_TIME_KEY, query = SchedulerEntity.QUERY_NEXT_EXECUTION_TIME),
		@NamedQuery(name = SchedulerEntity.QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION_KEY, query = SchedulerEntity.QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION),
		@NamedQuery(name = SchedulerEntity.QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS_KEY, query = SchedulerEntity.QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS),
})
public class SchedulerEntity extends BaseEntity {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/*
	 * SCHEDULER ENTITY QUERIES
	 */
	/** The Constant QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS_KEY. */
	public static final String QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS_KEY = "QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS";
	/** The Constant QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS. */
	static final String QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS = "select e from SchedulerEntity e where e.id in (:ids)";

	/** The Constant QUERY_SCHEDULER_ENTRY_BY_UID_KEY. */
	public static final String QUERY_SCHEDULER_ENTRY_BY_UID_KEY = "QUERY_SCHEDULER_ENTRY_BY_UID";
	/** The Constant QUERY_SCHEDULER_ENTRY_BY_UID. */
	static final String QUERY_SCHEDULER_ENTRY_BY_UID = "select e from SchedulerEntity e where e.identifier=:identifier";

	/** The Constant QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_KEY. */
	public static final String QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_KEY = "QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER";
	/** The Constant QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER. */
	/** The Constant QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER. */
	static final String QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER = "select e.id from SchedulerEntity e where e.eventTrigger.eventClassId=:eventClassId AND e.eventTrigger.targetSemanticClass=:targetSemanticClass AND e.eventTrigger.targetId=:targetId";

	/** The Constant QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_KEY. */
	public static final String QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_KEY = "QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP";
	/** The Constant QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP. */
	static final String QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP = "select e.id from SchedulerEntity e where e.eventTrigger.eventClassId=:eventClassId AND e.eventTrigger.targetSemanticClass=:targetSemanticClass AND e.eventTrigger.targetId=:targetId AND e.eventTrigger.serverOperation=:serverOperation";

	/** The Constant QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_AND_USER_OP_KEY. */
	public static final String QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_AND_USER_OP_KEY = "QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_AND_USER_OP";
	/** The Constant QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_AND_USER_OP. */
	static final String QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_AND_USER_OP = "select e.id from SchedulerEntity e where e.eventTrigger.eventClassId=:eventClassId AND e.eventTrigger.targetSemanticClass=:targetSemanticClass AND e.eventTrigger.targetId=:targetId AND e.eventTrigger.serverOperation=:serverOperation AND e.eventTrigger.userOperation=:userOperation";


	/** The Constant QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_USER_OP_KEY. */
	public static final String QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_USER_OP_KEY = "QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_USER_OP";
	/** The Constant QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_USER_OP. */
	static final String QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_USER_OP = "select e.id from SchedulerEntity e where e.eventTrigger.eventClassId=:eventClassId AND e.eventTrigger.targetSemanticClass=:targetSemanticClass AND e.eventTrigger.targetId=:targetId AND e.eventTrigger.userOperation=:userOperation";

	/** The Constant QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION_KEY. */
	public static final String QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION_KEY = "QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION";
	/** The Constant QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION. */
	static final String QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION = "select e.id from SchedulerEntity e where e.nextScheduleTime is not null AND e.nextScheduleTime <= :next AND e.status in (:status) AND e.type in (:type) order by e.nextScheduleTime asc";

	/** The Constant QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION_KEY. */
	public static final String QUERY_NEXT_EXECUTION_TIME_KEY = "QUERY_NEXT_EXECUTION_TIME";
	/** The Constant QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION. */
	static final String QUERY_NEXT_EXECUTION_TIME = "select e.nextScheduleTime from SchedulerEntity e where e.nextScheduleTime is not null AND e.nextScheduleTime > :next AND e.status in (:status) AND e.type in (:type) order by e.nextScheduleTime asc";

	/** The Constant QUERY_SCHEDULER_ENTRY_ID_BY_STATUS_KEY. */
	public static final String QUERY_SCHEDULER_ENTRY_ID_BY_STATUS_KEY = "QUERY_SCHEDULER_ENTRY_ID_BY_STATUS";
	/** The Constant QUERY_SCHEDULER_ENTRY_ID_BY_STATUS. */
	static final String QUERY_SCHEDULER_ENTRY_ID_BY_STATUS = "select e.id from SchedulerEntity e where e.status in (:status)";

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -8075928904335418825L;

	/** The type of the entry. */
	@Column(name = "type", nullable = false)
	private SchedulerEntryType type;

	/** The run status of the entry. */
	@Column(name = "status", nullable = false)
	private SchedulerEntryStatus status = SchedulerEntryStatus.NOT_RUN;

	/** The next schedule time for timed and cron events. */
	@Column(name = "nextScheduleTime")
	@Temporal(TemporalType.TIMESTAMP)
	private Date nextScheduleTime;

	@Column(name = "actionClassId")
	private Integer actionClassId;

	/** The action name. */
	@Column(name = "actionName", length = 150)
	private String actionName;

	/** The number of retries the operation is tried to run. */
	@Column(name = "retries")
	private Integer retries;

	/** The event trigger. */
	@Embedded
	private EventTriggerEntity eventTrigger;

	/** The context data. */
	@JoinColumn(name = "contextdata_id")
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private SerializableValue contextData;
	/** The identifier. */
	@Column(name = "identifier", length = 100, unique = true, nullable = false)
	private String identifier;

	/**
	 * Id to specify what is the source database for checks and synchronizations
	 */
	@Transient
	private String tenantId;

	/**
	 * Gets the tenant id.
	 *
	 * @return the tenant id
	 */
	public String getTenantId() {
		return tenantId;
	}

	/**
	 * Sets the tenant id.
	 *
	 * @param tenantId
	 *            the new tenant id
	 */
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * Getter method for type.
	 *
	 * @return the type
	 */
	public SchedulerEntryType getType() {
		return type;
	}

	/**
	 * Setter method for type.
	 *
	 * @param type
	 *            the type to set
	 */
	public void setType(SchedulerEntryType type) {
		this.type = type;
	}

	/**
	 * Getter method for status.
	 *
	 * @return the status
	 */
	public SchedulerEntryStatus getStatus() {
		return status;
	}

	/**
	 * Setter method for status.
	 *
	 * @param status
	 *            the status to set
	 */
	public void setStatus(SchedulerEntryStatus status) {
		this.status = status;
	}

	/**
	 * Getter method for nextScheduleTime.
	 *
	 * @return the nextScheduleTime
	 */
	public Date getNextScheduleTime() {
		return nextScheduleTime;
	}

	/**
	 * Setter method for nextScheduleTime.
	 *
	 * @param nextScheduleTime
	 *            the nextScheduleTime to set
	 */
	public void setNextScheduleTime(Date nextScheduleTime) {
		this.nextScheduleTime = nextScheduleTime;
	}

	/**
	 * Getter method for retries.
	 *
	 * @return the retries
	 */
	public Integer getRetries() {
		return retries;
	}

	/**
	 * Setter method for retries.
	 *
	 * @param retries
	 *            the retries to set
	 */
	public void setRetries(Integer retries) {
		this.retries = retries;
	}

	/**
	 * Getter method for contextData.
	 *
	 * @return the contextData
	 */
	public SerializableValue getContextData() {
		return contextData;
	}

	/**
	 * Setter method for contextData.
	 *
	 * @param contextData
	 *            the contextData to set
	 */
	public void setContextData(SerializableValue contextData) {
		this.contextData = contextData;
	}

	/**
	 * Getter method for actionClassId.
	 *
	 * @return the actionClassId
	 */
	public Integer getActionClassId() {
		return actionClassId;
	}

	/**
	 * Setter method for actionClassId.
	 *
	 * @param actionClassId
	 *            the actionClassId to set
	 */
	public void setActionClassId(Integer actionClassId) {
		this.actionClassId = actionClassId;
	}

	/**
	 * Getter method for actionName.
	 *
	 * @return the actionName
	 */
	public String getActionName() {
		return actionName;
	}

	/**
	 * Setter method for actionName.
	 *
	 * @param actionName
	 *            the actionName to set
	 */
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SchedulerEntity [id=");
		builder.append(getId());
		builder.append(", type=");
		builder.append(type);
		builder.append(", status=");
		builder.append(status);
		builder.append(", nextScheduleTime=");
		builder.append(nextScheduleTime);
		builder.append(", actionClassId=");
		builder.append(actionClassId);
		builder.append(", actionName=");
		builder.append(actionName);
		builder.append(", retries=");
		builder.append(retries);
		builder.append(", eventTrigger=");
		builder.append(getEventTrigger());
		builder.append(", contextData=");
		builder.append(contextData == null ? "NULL" : "BYTE_DATA");
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for eventTrigger.
	 *
	 * @return the eventTrigger
	 */
	public EventTriggerEntity getEventTrigger() {
		return eventTrigger;
	}

	/**
	 * Setter method for eventTrigger.
	 *
	 * @param eventTrigger
	 *            the eventTrigger to set
	 */
	public void setEventTrigger(EventTriggerEntity eventTrigger) {
		this.eventTrigger = eventTrigger;
	}

	/**
	 * Getter method for identifier.
	 *
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Setter method for identifier.
	 *
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + (identifier == null ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof SchedulerEntity)) {
			return false;
		}
		SchedulerEntity other = (SchedulerEntity) obj;
		return nullSafeEquals(identifier, other.getIdentifier());
	}

	/**
	 * Convert to schedule entry and instantiate the action if requested.
	 *
	 * @param serializationHelper
	 *            the serialization helper to be used when deserializing context data
	 * @return the scheduler entry
	 */
	public SchedulerEntry toSchedulerEntry(SerializationHelper serializationHelper) {
		SchedulerData data = extractSchedulerData(serializationHelper);
		if (data == null) {
			// no configuration -- probably good idea to throw an exception
			return null;
		}
		SchedulerEntry entry = new SchedulerEntry();
		entry.setConfiguration(data.configuration);
		entry.setContext(data.context);
		entry.setId(getId());
		entry.setStatus(getStatus());
		entry.setIdentifier(getIdentifier());
		entry.setContainer(getTenantId());
		entry.setExpectedExecutionTime(getNextScheduleTime());
		entry.setActionName(getActionName());
		return entry;
	}

	/**
	 * Sets the context and configuration data to the given entity.
	 *
	 * @param serializationHelper
	 *            the serialization helper to be used to serialize the given configuration and context
	 * @param config
	 *            the configuration to set
	 * @param context
	 *            the context to set
	 */
	public void setContextData(SerializationHelper serializationHelper, SchedulerConfiguration config,
			SchedulerContext context) {
		SchedulerData data = new SchedulerData(config, context);
		Serializable serializableValue = serializationHelper.serialize(data);
		// reuse the context object entry
		if (getContextData() == null) {
			setContextData(new SerializableValue(serializableValue));
		} else {
			getContextData().setSerializable(serializableValue);
		}
	}

	/**
	 * Extract and deserialize scheduler data.
	 *
	 * @param serializationHelper
	 *            the {@link SerializationHelper} to be used when deserializing context data
	 * @return the scheduler data
	 */
	private SchedulerData extractSchedulerData(SerializationHelper serializationHelper) {
		SerializableValue data = getContextData();
		Serializable serializable = data.getSerializable();
		try {
			Object object = serializationHelper.deserialize(serializable);
			if (object instanceof SchedulerData) {
				return (SchedulerData) object;
			}
		} catch (KryoException e) {
			// if this happens probably the configuration is broken or something is not right
			// but it's not that fatal. Some of the code will just reschedule new entry for it
			LOGGER.warn("Could not read configuration data for {}. Scheduler entry ignored", getIdentifier(), e);
		}
		return null;
	}

	/**
	 * Wrapper object to store the action data.
	 *
	 * @author BBonev
	 */
	static class SchedulerData implements Serializable {
		private static final long serialVersionUID = -3963891439477250899L;

		/** Kryo engine index. */
		static final int CLASS_INDEX = 222;

		@Tag(1)
		protected SchedulerConfiguration configuration;
		@Tag(2)
		protected SchedulerContext context;

		/**
		 * Instantiates a new scheduler data.
		 */
		public SchedulerData() {
			// default constructor
		}

		/**
		 * Instantiates a new scheduler data.
		 *
		 * @param configuration
		 *            the configuration
		 * @param context
		 *            the context
		 */
		public SchedulerData(SchedulerConfiguration configuration, SchedulerContext context) {
			this.configuration = configuration;
			this.context = context;
		}
	}

}
