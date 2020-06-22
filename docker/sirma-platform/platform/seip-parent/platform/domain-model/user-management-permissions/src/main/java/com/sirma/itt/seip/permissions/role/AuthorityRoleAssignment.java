package com.sirma.itt.seip.permissions.role;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;

/**
 * Holds the special permissions (role mapped to an authority) for a given entity.
 *
 * @author Adrian Mitev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "sep_authority_role_assignment")
@NamedQueries(@NamedQuery(name = AuthorityRoleAssignment.DELETE_ASSIGNMENTS_BY_IDS_KEY, query = AuthorityRoleAssignment.DELETE_ASSIGNMENTS_BY_IDS))
public class AuthorityRoleAssignment implements Serializable {

	public static final String DELETE_ASSIGNMENTS_BY_IDS_KEY = "DELETE_ASSIGNMENTS_BY_IDS";
	static final String DELETE_ASSIGNMENTS_BY_IDS = "delete from AuthorityRoleAssignment where id in (:ids)";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "authority", nullable = false)
	private String authority;

	@Column(name = "role", nullable = false)
	private String role;

	/**
	 * Required no-args/default constructor.
	 */
	public AuthorityRoleAssignment() {
		// used when hibernate creates bean for this class
	}

	/**
	 * Initializes using fields.
	 *
	 * @param authority
	 *            authority for which the role is assigned.
	 * @param role
	 *            the assigned role.
	 */
	public AuthorityRoleAssignment(String authority, String role) {
		this.authority = authority;
		this.role = role;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

}
