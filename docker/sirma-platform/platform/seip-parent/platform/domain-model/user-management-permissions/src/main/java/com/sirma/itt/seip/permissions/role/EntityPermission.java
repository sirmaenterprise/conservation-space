package com.sirma.itt.seip.permissions.role;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.customtype.BooleanCustomType;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.BaseEntity;

/**
 * Holds information about the way to retrieve the assigned permissions for a give entity (target).
 *
 * @author Adrian Mitev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "sep_entity_permission")
@NamedQueries({
		@NamedQuery(name = EntityPermission.QUERY_LOAD_BY_TARGET_ID_KEY, query = EntityPermission.QUERY_LOAD_BY_TARGET_ID),
		@NamedQuery(name = EntityPermission.QUERY_LOAD_BY_TARGET_ID_WITH_ROLE_ASSIGNMENTS_KEY, query = EntityPermission.QUERY_LOAD_BY_TARGET_ID_WITH_ROLE_ASSIGNMENTS),
		@NamedQuery(name = EntityPermission.QUERY_DELETE_ASSIGNMENTS_KEY, query = EntityPermission.QUERY_DELETE_ASSIGNMENTS),
		@NamedQuery(name = EntityPermission.QUERY_UPDATE_LIBRARY_FOR_TARGET_KEY, query = EntityPermission.QUERY_UPDATE_LIBRARY_FOR_TARGET),
		@NamedQuery(name = EntityPermission.QUERY_UPDATE_PARENT_INHERITANCE_FOR_TARGET_KEY, query = EntityPermission.QUERY_UPDATE_PARENT_INHERITANCE_FOR_TARGET),
		@NamedQuery(name = EntityPermission.QUERY_UPDATE_PARENT_FOR_TARGET_KEY, query = EntityPermission.QUERY_UPDATE_PARENT_FOR_TARGET) })

@NamedNativeQueries({
		@NamedNativeQuery(name = EntityPermission.QUERY_LOAD_ENTITY_PERMISSIONS_FOR_HIERARCHY_KEY, query = EntityPermission.QUERY_LOAD_ENTITY_PERMISSIONS_FOR_HIERARCHY),
		@NamedNativeQuery(name = EntityPermission.QUERY_LOAD_ASSIGNMENTS_FOR_HIERARCHY_KEY, query = EntityPermission.QUERY_LOAD_ASSIGNMENTS_FOR_HIERARCHY),
		@NamedNativeQuery(name = EntityPermission.QUERY_GET_DESCENDANTS_KEY, query = EntityPermission.QUERY_GET_DESCENDANTS) })
public class EntityPermission extends BaseEntity {

	public static final String QUERY_LOAD_BY_TARGET_ID_KEY = "QUERY_LOAD_BY_TARGET_ID";
	static final String QUERY_LOAD_BY_TARGET_ID = "from EntityPermission where targetId = :targetId";

	public static final String QUERY_LOAD_BY_TARGET_ID_WITH_ROLE_ASSIGNMENTS_KEY = "QUERY_LOAD_BY_TARGET_ID_WITH_ROLE_ASSIGNMENTS";
	static final String QUERY_LOAD_BY_TARGET_ID_WITH_ROLE_ASSIGNMENTS = "from EntityPermission ep left join fetch ep.library left join fetch ep.parent left join fetch ep.assignments where ep.targetId = :targetId";

	private static final String QUERY_FETCH_HIERARCHY = "with hierarchy_ids as (" + "with recursive tree as ("
			+ "select id, parent_id, array[id] as visited_parents from sep_entity_permission where target_id in (:targetIds) "
			+ "union all select ep.id, ep.parent_id, tree.visited_parents||ep.id from sep_entity_permission ep, tree where ep.id = tree.parent_id and ep.id <> all (visited_parents)"
			+ ")" + "select id from tree" + ")";

	public static final String QUERY_LOAD_ENTITY_PERMISSIONS_FOR_HIERARCHY_KEY = "QUERY_LOAD_ENTITY_PERMISSIONS_FOR_HIERARCHY";
	static final String QUERY_LOAD_ENTITY_PERMISSIONS_FOR_HIERARCHY = QUERY_FETCH_HIERARCHY + "select target_id,"
			+ "(select target_id from sep_entity_permission where id = ep.parent_id) as parent,"
			+ "inherit_from_parent,"
			+ "(select target_id from sep_entity_permission where id = ep.library_id) as library,"
			+ "inherit_from_library, is_library from sep_entity_permission ep where id in ("
			+ "select id from hierarchy_ids "
			+ "union select distinct(library_id) from sep_entity_permission where id in (select id from hierarchy_ids)"
			+ ")";

	public static final String QUERY_LOAD_ASSIGNMENTS_FOR_HIERARCHY_KEY = "QUERY_LOAD_ASSIGNMENTS_FOR_HIERARCHY";
	static final String QUERY_LOAD_ASSIGNMENTS_FOR_HIERARCHY = QUERY_FETCH_HIERARCHY
			+ "select p.target_id, ara.authority, ara.role from sep_entity_permission p inner join sep_authority_role_assignment ara on ara.permission_id = p.id where p.id in ("
			+ "select id from hierarchy_ids "
			+ "union select distinct(library_id) from sep_entity_permission where id in (select id from hierarchy_ids)"
			+ ")";

	public static final String QUERY_GET_DESCENDANTS_KEY = "QUERY_GET_DESCENDANTS";
	static final String QUERY_GET_DESCENDANTS = "with hierarchy_ids as (with recursive tree as ("
			+ "select id,target_id,array[id] as visited_parents from sep_entity_permission where target_id = :targetId "
			+ "union all select ep.id, ep.target_id, tree.visited_parents||ep.id from sep_entity_permission ep, tree where ep.parent_id = tree.id and ep.id <> all (visited_parents)"
			+ ") select target_id from tree) " + "select target_id from hierarchy_ids";

	public static final String QUERY_DELETE_ASSIGNMENTS_KEY = "QUERY_DELETE_ASSIGNMENTS";
	static final String QUERY_DELETE_ASSIGNMENTS = "delete AuthorityRoleAssignment where permission_id = :permissionId";

	public static final String QUERY_UPDATE_LIBRARY_FOR_TARGET_KEY = "QUERY_UPDATE_LIBRARY_FOR_TARGET";
	static final String QUERY_UPDATE_LIBRARY_FOR_TARGET = "update EntityPermission set library = (from EntityPermission where targetId = :libraryId) where targetId = :targetId and exists (from EntityPermission where targetId = :libraryId)";

	public static final String QUERY_UPDATE_PARENT_FOR_TARGET_KEY = "QUERY_UPDATE_PARENT_FOR_TARGET";
	static final String QUERY_UPDATE_PARENT_FOR_TARGET = "update EntityPermission set parent = (from EntityPermission where targetId = :parentId) where targetId in (:targetId) and exists (from EntityPermission where targetId = :parentId)";

	public static final String QUERY_UPDATE_PARENT_INHERITANCE_FOR_TARGET_KEY = "QUERY_UPDATE_PARENT_INHERITANCE_FOR_TARGET";
	static final String QUERY_UPDATE_PARENT_INHERITANCE_FOR_TARGET = "update EntityPermission set inheritFromParent = 2 where targetId in (:targetId) and exists (from EntityPermission where targetId = :parentId)";

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private EntityPermission parent;

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "library_id")
	private EntityPermission library;

	@Column(name = "target_id", nullable = false)
	private String targetId;

	@Column(name = "inherit_from_parent", nullable = false)
	@Type(type = BooleanCustomType.TYPE_NAME)
	private boolean inheritFromParent;

	@Column(name = "inherit_from_library", nullable = false)
	@Type(type = BooleanCustomType.TYPE_NAME)
	private boolean inheritFromLibrary;

	@Column(name = "is_library", nullable = false)
	@Type(type = BooleanCustomType.TYPE_NAME)
	private boolean isLibrary;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "permission_id", referencedColumnName = "id", nullable = false)
	private Set<AuthorityRoleAssignment> assignments = new HashSet<>();

	public EntityPermission getParent() {
		return parent;
	}

	/**
	 * Get the parent identifier if any. This returns the {@link EntityPermission#getTargetId()} of the parent entity
	 *
	 * @return the parent id
	 */
	public String getParentId() {
		if (parent == null) {
			return null;
		}
		return parent.getTargetId();
	}

	public EntityPermission setParent(EntityPermission parent) {
		this.parent = parent;
		return this;
	}

	public boolean getInheritFromParent() {
		return inheritFromParent;
	}

	public EntityPermission setInheritFromParent(boolean inheritFromParent) {
		this.inheritFromParent = inheritFromParent;
		return this;
	}

	public Set<AuthorityRoleAssignment> getAssignments() {
		return assignments;
	}

	public String getTargetId() {
		return targetId;
	}

	public EntityPermission setTargetId(String targetId) {
		this.targetId = targetId;
		return this;
	}

	public EntityPermission getLibrary() {
		return library;
	}

	/**
	 * Get the library identifier if any. This returns the {@link EntityPermission#getTargetId()} of the library entity
	 *
	 * @return the library id
	 */
	public String getLibraryId() {
		if (library == null) {
			return null;
		}
		return library.getTargetId();
	}

	public EntityPermission setLibrary(EntityPermission library) {
		this.library = library;
		return this;
	}

	public boolean getInheritFromLibrary() {
		return inheritFromLibrary;
	}

	public EntityPermission setInheritFromLibrary(boolean inheritFromLibrary) {
		this.inheritFromLibrary = inheritFromLibrary;
		return this;
	}

	public boolean isLibrary() {
		return isLibrary;
	}

	public EntityPermission setIsLibrary(boolean isLibrary) {
		this.isLibrary = isLibrary;
		return this;
	}

}
