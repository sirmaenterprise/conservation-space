package com.sirma.itt.pm.schedule.model;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.TreeNode;
import com.sirma.itt.emf.instance.model.InitializedInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Instance implementation that represents the instance prototype and schedule entry and means to
 * provide the actual instance. The implementation is used to construct the project schedule and a
 * tree with all elements in the schedule
 *
 * @author BBonev
 */
public class ScheduleEntry implements Instance, BidirectionalMapping, OwnedModel,
		TreeNode<Serializable> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -4176680380912549935L;
	/** The id. */
	private Serializable id;
	/** The actual instance definition. */
	private String actualInstanceDefinition;
	/** The actual instance if. Filled when created. */
	private InstanceReference instanceReference;
	/** The actual instance class. */
	private Class<? extends Instance> actualInstanceClass;
	/** Caches the actual instance. Fetched lazily. */
	private Instance actualInstance;
	/** The actual instance id. */
	private Serializable actualInstanceId;
	/**
	 * The content management id. Unique identifier for the particular instance based on the project
	 * id.
	 */
	private String contentManagementId;
	/** The schedule database id. */
	private Long scheduleId;
	/**
	 * The parent prototype instance Id. If <code>null</code> then this is the project prototype
	 * representation
	 */
	private Serializable parentId;
	/** The parent instance. Cached instance of the parent prototype instance */
	private ScheduleEntry parentInstance;
	/** The children. The list of instances that has as a parent the current instance. */
	private List<ScheduleEntry> children;
	/** The properties of the prototype instance. */
	private Map<String, Serializable> properties;

	private boolean leaf = true;
	private String cssClass = "";
	/** The phantom id. */
	private String phantomId;
	/** The parent phantom id. */
	private String parentPhantomId;
	/** The owning instance. */
	private transient Instance owningInstance;
	/**start date for entry. could be actual or planned. */
	private Date startDate;
	/**end date for entry. could be actual or planned. */
	private Date endDate;
	/**system status for entry. */
	private String status;

	private ScheduleInstance schedule;
	/**
	 * Getter method for actualInstanceId.
	 *
	 * @return the actualInstanceId
	 */
	public Serializable getActualInstanceId() {
		return actualInstanceId;
	}

	/**
	 * Setter method for actualInstanceId.
	 *
	 * @param actualInstanceId
	 *            the actualInstanceId to set
	 */
	public void setActualInstanceId(Serializable actualInstanceId) {
		this.actualInstanceId = actualInstanceId;
	}

	@Override
	public void initBidirection() {
		for (ScheduleEntry instance : getChildren()) {
			instance.setParentInstance(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren() {
		return !getChildren().isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getChild(String name) {
		return PathHelper.find(getChildren(), name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Serializable> getProperties() {
		if (properties == null) {
			properties = new LinkedHashMap<String, Serializable>();
		}
		return properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProperties(Map<String, Serializable> properties) {
		this.properties = properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getRevision() {
		return 0l;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PathElement getParentElement() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return getIdentifier();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		return actualInstanceDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIdentifier(String identifier) {
		actualInstanceDefinition = identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setId(Serializable id) {
		this.id = id;
	}

	/**
	 * Getter method for actualInstance.
	 *
	 * @return the actualInstance
	 */
	public Instance getActualInstance() {
		if ((actualInstance == null) && (instanceReference != null)
				&& (instanceReference.getIdentifier() != null)) {
			actualInstance = TypeConverterUtil.getConverter().convert(getActualInstanceClass(),
					this);
		}
		return actualInstance;
	}

	/**
	 * Setter method for actualInstance.
	 *
	 * @param actualInstance
	 *            the actualInstance to set
	 */
	public void setActualInstance(Instance actualInstance) {
		this.actualInstance = actualInstance;
	}

	/**
	 * Getter method for contentManagementId.
	 *
	 * @return the contentManagementId
	 */
	public String getContentManagementId() {
		return contentManagementId;
	}

	/**
	 * Setter method for contentManagementId.
	 *
	 * @param contentManagementId
	 *            the contentManagementId to set
	 */
	public void setContentManagementId(String contentManagementId) {
		this.contentManagementId = contentManagementId;
	}

	/**
	 * Getter method for projectId.
	 *
	 * @return the projectId
	 */
	public Long getScheduleId() {
		return scheduleId;
	}

	/**
	 * Setter method for projectId.
	 *
	 * @param projectId
	 *            the projectId to set
	 */
	public void setScheduleId(Long projectId) {
		scheduleId = projectId;
	}

	/**
	 * Getter method for parentId.
	 *
	 * @return the parentId
	 */
	@Override
	public Serializable getParentId() {
		return parentId;
	}

	/**
	 * Setter method for parentId.
	 *
	 * @param parentId
	 *            the parentId to set
	 */
	@Override
	public void setParentId(Serializable parentId) {
		this.parentId = parentId;
	}

	/**
	 * Getter method for parentInstance.
	 *
	 * @return the parentInstance
	 */
	public ScheduleEntry getParentInstance() {
		return parentInstance;
	}

	/**
	 * Setter method for parentInstance.
	 *
	 * @param parentInstance
	 *            the parentInstance to set
	 */
	public void setParentInstance(ScheduleEntry parentInstance) {
		this.parentInstance = parentInstance;
	}

	/**
	 * Getter method for children.
	 *
	 * @return the children
	 */
	public List<ScheduleEntry> getChildren() {
		if (children == null) {
			children = new LinkedList<ScheduleEntry>();
		}
		return children;
	}

	/**
	 * Setter method for children.
	 *
	 * @param children
	 *            the children to set
	 */
	public void setChildren(List<ScheduleEntry> children) {
		this.children = children;
	}

	/**
	 * Setter method for revision.
	 *
	 * @param revision
	 *            the revision to set
	 */
	@Override
	public void setRevision(Long revision) {
		// no_op
	}

	/**
	 * Getter method for actualInstanceClass.
	 *
	 * @return the actualInstanceClass
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends Instance> getActualInstanceClass() {
		if ((actualInstanceClass == null) && (getInstanceReference() != null)
				&& (getInstanceReference().getReferenceType() != null)) {
			actualInstanceClass = TypeConverterUtil.getConverter().convert(Class.class,
					getInstanceReference().getReferenceType().getJavaClassName());
		}
		return actualInstanceClass;
	}

	/**
	 * Setter method for actualInstanceClass.
	 *
	 * @param actualInstanceClass the actualInstanceClass to set
	 */
	public void setActualInstanceClass(Class<? extends Instance> actualInstanceClass) {
		this.actualInstanceClass = actualInstanceClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScheduleEntry [id=");
		builder.append(id);
		builder.append(", actualInstanceDefinition=");
		builder.append(actualInstanceDefinition);
		builder.append(", actualInstanceClass=");
		builder.append(actualInstanceClass);
		builder.append(", actualInstance=");
		builder.append(actualInstance == null ? "NULL" : "PRESENT");
		builder.append(", contentManagementId=");
		builder.append(contentManagementId);
		builder.append(", projectId=");
		builder.append(scheduleId);
		builder.append(", parentId=");
		builder.append(parentId);
		builder.append(", parentInstance=");
		builder.append(parentInstance == null ? "NULL" : "PRESENT");
		builder.append(", children=");
		builder.append(children == null ? "NULL" : "PRESENT");
		builder.append(", properties=");
		builder.append(properties);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((id == null) ? 0 : id.hashCode());
		result = (prime * result) + ((phantomId == null) ? 0 : phantomId.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ScheduleEntry other = (ScheduleEntry) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (phantomId == null) {
			if (other.phantomId != null) {
				return false;
			}
		} else if (!phantomId.equals(other.phantomId)) {
			return false;
		}
		return true;
	}

	/**
	 * Getter method for instanceReference.
	 *
	 * @return the instanceReference
	 */
	public InstanceReference getInstanceReference() {
		return instanceReference;
	}

	/**
	 * Setter method for instanceReference.
	 *
	 * @param instanceReference the instanceReference to set
	 */
	public void setInstanceReference(InstanceReference instanceReference) {
		this.instanceReference = instanceReference;
	}

	/**
	 * Gets the phantom id.
	 *
	 * @return the phantom id
	 */
	public String getPhantomId() {
		return phantomId;
	}

	/**
	 * Sets the phantom id.
	 *
	 * @param phantomId
	 *            the new phantom id
	 */
	public void setPhantomId(String phantomId) {
		this.phantomId = phantomId;
	}

	/**
	 * Gets the parent phantom id.
	 *
	 * @return the parent phantom id
	 */
	public String getParentPhantomId() {
		return parentPhantomId;
	}

	/**
	 * Sets the parent phantom id.
	 *
	 * @param parentPhantomId
	 *            the new parent phantom id
	 */
	public void setParentPhantomId(String parentPhantomId) {
		this.parentPhantomId = parentPhantomId;
	}

	/**
	 * Checks if is leaf.
	 *
	 * @return true, if is leaf
	 */
	public boolean isLeaf() {
		// if the entry is marked as leaf but has child objects then we mark the entry as not leaf
		if (leaf && hasChildren()) {
			leaf = false;
		}
		return leaf;
	}

	/**
	 * Sets the leaf.
	 *
	 * @param leaf
	 *            the new leaf
	 */
	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	/**
	 * Getter method for cssClass.
	 *
	 * @return the cssClass
	 */
	public String getCssClass() {
		return cssClass;
	}

	/**
	 * Setter method for cssClass.
	 *
	 * @param cssClass
	 *            the cssClass to set
	 */
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	@Override
	public InstanceReference getOwningReference() {
		return getInstanceReference();
	}

	/**
	 * Getter method for owningInstance.
	 *
	 * @return the owningInstance
	 */
	@Override
	public Instance getOwningInstance() {
		if ((owningInstance == null) && (getOwningReference() != null)
				&& StringUtils.isNotNullOrEmpty(getOwningReference().getIdentifier())) {
			owningInstance = TypeConverterUtil.getConverter()
					.convert(InitializedInstance.class, getOwningReference()).getInstance();
		}
		if (owningInstance == null) {
			if (getParentInstance() == null) {
				return getSchedule();
			} 
			return getParentInstance();
		}
		return owningInstance;
	}

	/**
	 * Setter method for owningInstance.
	 *
	 * @param owningInstance
	 *            the owningInstance to set
	 */
	@Override
	public void setOwningInstance(Instance owningInstance) {
		this.owningInstance = owningInstance;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Getter method for schedule.
	 * 
	 * @return the schedule
	 */
	public ScheduleInstance getSchedule() {
		return schedule;
	}

	/**
	 * Setter method for schedule.
	 * 
	 * @param schedule
	 *            the schedule to set
	 */
	public void setSchedule(ScheduleInstance schedule) {
		this.schedule = schedule;
	}

	@Override
	public void setOwningReference(InstanceReference reference) {
		setInstanceReference(reference);
	}

	@Override
	public InstanceReference toReference() {
		// not supported for now
		return null;
	}

}
