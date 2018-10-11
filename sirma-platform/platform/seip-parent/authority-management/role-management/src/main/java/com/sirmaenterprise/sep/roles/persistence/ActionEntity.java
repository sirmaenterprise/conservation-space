package com.sirmaenterprise.sep.roles.persistence;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.customtype.BooleanCustomType;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;

/**
 * Entity that represent an user action in the system
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@javax.persistence.Entity(name = "sep_action")
@Table(name = "sep_action")
@NamedQueries({ @NamedQuery(name = ActionEntity.QUERY_ACTIONS_BY_IDS_KEY, query = ActionEntity.QUERY_ACTIONS_BY_IDS),
		@NamedQuery(name = ActionEntity.QUERY_ALL_ACTIONS_KEY, query = ActionEntity.QUERY_ALL_ACTIONS) })
public class ActionEntity implements Entity<String>, Serializable {

	private static final long serialVersionUID = 2308410813265017802L;
	/**
	 * Select all defined actions ordered by id in ascending order
	 */
	public static final String QUERY_ALL_ACTIONS_KEY = "QUERY_ALL_ACTIONS";
	static final String QUERY_ALL_ACTIONS = "select a from com.sirmaenterprise.sep.roles.persistence.ActionEntity a order by a.id ASC";

	/**
	 * Select actions that match the given identifiers (ids) in ascending order
	 */
	public static final String QUERY_ACTIONS_BY_IDS_KEY = "QUERY_ACTIONS_BY_IDS";
	static final String QUERY_ACTIONS_BY_IDS = "select a from com.sirmaenterprise.sep.roles.persistence.ActionEntity a where a.id in (:ids) order by a.id ASC";

	@Id
	@Column(name = "id", length = 100, nullable = false, unique = true, updatable = false)
	private String id;

	@Column(name = "action_type", length = 100, nullable = false)
	private String actionType;

	@Column(name = "is_enabled", nullable = false)
	@Type(type = BooleanCustomType.TYPE_NAME)
	private boolean enabled;

	@Column(name = "is_user_defined", nullable = false)
	@Type(type = BooleanCustomType.TYPE_NAME)
	private boolean userDefined;

	@Column(name = "is_immediate")
	@Type(type = BooleanCustomType.TYPE_NAME)
	private boolean immediate;

	@Column(name = "is_visible")
	@Type(type = BooleanCustomType.TYPE_NAME)
	private boolean visible = true;

	@Column(name = "image_path", length = 512)
	private String imagePath;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public boolean isUserDefined() {
		return userDefined;
	}

	public void setUserDefined(boolean userDefined) {
		this.userDefined = userDefined;
	}

	public boolean isImmediate() {
		return immediate;
	}

	public void setImmediate(boolean immediate) {
		this.immediate = immediate;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	@Override
	public int hashCode() {
		return 31 + ((id == null) ? 0 : id.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ActionEntity)) {
			return false;
		}
		ActionEntity other = (ActionEntity) obj;
		return nullSafeEquals(id, other.id);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(256);
		builder
				.append("ActionEntity [id=")
					.append(id)
					.append(", actionType=")
					.append(actionType)
					.append(", enabled=")
					.append(enabled)
					.append(", userDefined=")
					.append(userDefined)
					.append(", immediate=")
					.append(immediate)
					.append(", visible=")
					.append(visible)
					.append(", imagePath=")
					.append(imagePath)
					.append("]");
		return builder.toString();
	}

}
