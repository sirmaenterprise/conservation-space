package com.sirma.itt.seip.rest.secirity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;

/**
 * Represents jwt, saml, sessionIndex and other session related data.
 *
 * @author smustafov
 */
@PersistenceUnitBinding(PersistenceUnits.CORE)
@javax.persistence.Entity(name = "sep_active_user_session")
@Table(name = "sep_active_user_session")
@NamedQueries({ @NamedQuery(name = ActiveUserSession.QUERY_GET_ALL_KEY, query = ActiveUserSession.QUERY_GET_ALL),
		@NamedQuery(name = ActiveUserSession.QUERY_GET_BY_JWT_KEY, query = ActiveUserSession.QUERY_GET_BY_JWT),
		@NamedQuery(name = ActiveUserSession.QUERY_GET_BY_SAML_KEY, query = ActiveUserSession.QUERY_GET_BY_SAML),
		@NamedQuery(name = ActiveUserSession.QUERY_GET_BY_SESSION_INDEX_KEY, query = ActiveUserSession.QUERY_GET_BY_SESSION_INDEX),
		@NamedQuery(name = ActiveUserSession.QUERY_DELETE_BY_SESSION_INDEX_KEY, query = ActiveUserSession.QUERY_DELETE_BY_SESSION_INDEX) })
public class ActiveUserSession implements Entity<Long>, Serializable {

	private static final long serialVersionUID = 1284399513697779347L;

	public static final String QUERY_GET_ALL_KEY = "QUERY_GET_ALL";
	static final String QUERY_GET_ALL = "from com.sirma.itt.seip.rest.secirity.ActiveUserSession t";

	public static final String QUERY_GET_BY_JWT_KEY = "QUERY_GET_BY_JWT";
	static final String QUERY_GET_BY_JWT = "from com.sirma.itt.seip.rest.secirity.ActiveUserSession t where t.jwt = :jwt";

	public static final String QUERY_GET_BY_SAML_KEY = "QUERY_GET_BY_SAML";
	static final String QUERY_GET_BY_SAML = "from com.sirma.itt.seip.rest.secirity.ActiveUserSession t where t.saml = :saml";

	public static final String QUERY_GET_BY_SESSION_INDEX_KEY = "QUERY_GET_BY_SESSION_INDEX";
	static final String QUERY_GET_BY_SESSION_INDEX = "from com.sirma.itt.seip.rest.secirity.ActiveUserSession t where t.sessionIndex = :sessionIndex";

	public static final String QUERY_DELETE_BY_SESSION_INDEX_KEY = "QUERY_DELETE_BY_SESSION_INDEX";
	static final String QUERY_DELETE_BY_SESSION_INDEX = "delete from com.sirma.itt.seip.rest.secirity.ActiveUserSession t where t.sessionIndex = :sessionIndex";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "jwt", nullable = false, unique = true, length = 1024)
	private String jwt;

	@Column(name = "saml", nullable = false, unique = true, length = 4096)
	private String saml;

	@Column(name = "session_index", nullable = false, length = 36)
	private String sessionIndex;

	@Column(name = "identity_id", nullable = false, length = 256)
	private String identityId;

	@Column(name = "identity_properties", nullable = false, length = 4096)
	private String identityProperties;

	@Column(name = "loggedin_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date loggedInDate;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Getter method for jwt.
	 *
	 * @return the jwt
	 */
	public String getJwt() {
		return jwt;
	}

	/**
	 * Setter method for jwt.
	 *
	 * @param jwt
	 *            the jwt to set
	 */
	public void setJwt(String jwt) {
		this.jwt = jwt;
	}

	/**
	 * Getter method for saml.
	 *
	 * @return the saml
	 */
	public String getSaml() {
		return saml;
	}

	/**
	 * Setter method for saml.
	 *
	 * @param saml
	 *            the saml to set
	 */
	public void setSaml(String saml) {
		this.saml = saml;
	}

	/**
	 * Getter method for sessionIndex.
	 *
	 * @return the sessionIndex
	 */
	public String getSessionIndex() {
		return sessionIndex;
	}

	/**
	 * Setter method for sessionIndex.
	 *
	 * @param sessionIndex
	 *            the sessionIndex to set
	 */
	public void setSessionIndex(String sessionIndex) {
		this.sessionIndex = sessionIndex;
	}

	/**
	 * Getter method for identityId.
	 *
	 * @return the identityId
	 */
	public String getIdentityId() {
		return identityId;
	}

	/**
	 * Setter method for identityId.
	 *
	 * @param identityId
	 *            the identityId to set
	 */
	public void setIdentityId(String identityId) {
		this.identityId = identityId;
	}

	/**
	 * Getter method for identityProperties.
	 *
	 * @return the identityProperties
	 */
	public String getIdentityProperties() {
		return identityProperties;
	}

	/**
	 * Setter method for identityProperties.
	 *
	 * @param identityProperties
	 *            the identityProperties to set
	 */
	public void setIdentityProperties(String identityProperties) {
		this.identityProperties = identityProperties;
	}

	/**
	 * Getter method for loggedInDate.
	 *
	 * @return the loggedInDate
	 */
	public Date getLoggedInDate() {
		return loggedInDate;
	}

	/**
	 * Setter method for loggedInDate.
	 *
	 * @param loggedInDate
	 *            the loggedInDate to set
	 */
	public void setLoggedInDate(Date loggedInDate) {
		this.loggedInDate = loggedInDate;
	}

}
