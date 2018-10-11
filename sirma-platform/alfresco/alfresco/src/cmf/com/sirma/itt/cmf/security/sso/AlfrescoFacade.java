package com.sirma.itt.cmf.security.sso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.User;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * The Class AlfrescoFacade.
 */
public class AlfrescoFacade {

	/** The transaction service. */
	private TransactionService transactionService;

	/** The node service. */
	private NodeService nodeService;

	/** The auth component. */
	private AuthenticationComponent authComponent;

	/** The auth service. */
	private MutableAuthenticationService authService;

	/** The person service. */
	private PersonService personService;

	/** The permission service. */
	private PermissionService permissionService;

	/** The authentication service. */
	private MutableAuthenticationService authenticationService;

	/** The transactional helper. */
	private TransactionalHelper transactionalHelper;

	/** The authority service. */
	private AuthorityService authorityService;

	/** The servlet context. */
	private final ServletContext servletContext;

	/** The ticket component. */
	private TicketComponent ticketComponent;

	/**
	 * Instantiates a new alfresco facade.
	 * 
	 * @param servletContext
	 *            the servlet context
	 */
	public AlfrescoFacade(ServletContext servletContext) {
		this.servletContext = servletContext;
		WebApplicationContext ctx = WebApplicationContextUtils
				.getRequiredWebApplicationContext(servletContext);
		ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
		transactionService = serviceRegistry.getTransactionService();
		nodeService = serviceRegistry.getNodeService();
		authComponent = ((AuthenticationComponent) ctx.getBean("AuthenticationComponent"));
		authService = ((MutableAuthenticationService) ctx.getBean("AuthenticationService"));
		personService = ((PersonService) ctx.getBean("personService"));
		permissionService = ((PermissionService) ctx.getBean("permissionService"));
		authenticationService = ((MutableAuthenticationService) ctx
				.getBean("authenticationService"));

		authorityService = ((AuthorityService) ctx.getBean("authorityService"));
		transactionalHelper = new TransactionalHelper(transactionService);
		ticketComponent = ((TicketComponent) ctx.getBean("ticketComponent"));
	}

	/**
	 * Sets the authenticated user if <code>populateSession</code> set to true,
	 * or authenticated user is not the same as the requested by
	 * <code>userName</code
	 * 
	 * @param request
	 *            the http request
	 * @param response
	 *            the http response
	 * @param httpSession
	 *            the http session
	 * @param userName
	 *            the user name
	 * 
	 * @param populateSession
	 *            whether to force session updated
	 */
	protected void setAuthenticatedUser(HttpServletRequest request, HttpServletResponse response,
			HttpSession httpSession, String userName, boolean populateSession) {
		if (populateSession || !userName.equals(AuthenticationUtil.getFullyAuthenticatedUser())) {
			AuthenticationUtil.clearCurrentSecurityContext();
			AuthenticationUtil.setFullyAuthenticatedUser(userName);
			authComponent.setCurrentUser(userName);
			ticketComponent.clearCurrentTicket();
			transactionalHelper.doInTransaction(new LoginTransaction(userName, httpSession,
					request, response));
		}
	}

	/**
	 * Populate session.
	 * 
	 * @param httpSess
	 *            the http sess
	 * @param user
	 *            the user
	 */
	protected void populateSession(HttpSession httpSess, User user) {
		httpSess.setAttribute("_alfAuthTicket", user);
		httpSess.setAttribute("_alfExternalAuth", Boolean.TRUE);
	}

	/**
	 * Creates the user.
	 * 
	 * @param username
	 *            the username
	 * @param email
	 *            the email
	 * @param firstName
	 *            the first name
	 * @param lastName
	 *            the last name
	 */
	public void createUser(String username, String email, String firstName, String lastName) {
		transactionalHelper.doInTransaction(new Trans4(username, firstName, lastName, email));
	}

	/**
	 * Exist user.
	 * 
	 * @param username
	 *            the username
	 * @return the boolean
	 */
	public Boolean existUser(String username) {
		return (Boolean) transactionalHelper.doInTransaction(new Trans5(username));
	}

	/**
	 * Gets the user groups.
	 * 
	 * @param username
	 *            the username
	 * @return the user groups
	 */
	public ArrayList<String> getUserGroups(String username) {
		throw new NotImplementedException("Not implemented");
	}

	/**
	 * Creates the or update groups.
	 * 
	 * @param principal
	 *            the principal
	 * @param groups
	 *            the groups
	 */
	public void createOrUpdateGroups(String principal, List<String> groups) {
		if ((groups == null) || (groups.size() == 0)) {
			return;
		}
		transactionalHelper.doInTransaction(new Trans2(principal, groups));
	}

	/**
	 * Authenticate as guest.
	 * 
	 * @param session
	 *            the session
	 */
	public void authenticateAsGuest(final HttpSession session) {
		transactionalHelper.doInTransaction(new Trans1(session));
	}

	/**
	 * The Class Trans2.
	 */
	private class Trans2 implements Transactionable {

		/** The principal. */
		private String principal;

		/** The groups. */
		private List<String> groups;

		/**
		 * Instantiates a new trans2.
		 * 
		 * @param principal
		 *            the principal
		 * @param groups
		 *            the groups
		 */
		public Trans2(String principal, List<String> groups) {
			super();
			this.principal = principal;
			this.groups = groups;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sourcesense.alfresco.transaction.Transactionable#execute()
		 */
		@Override
		public Object execute() {
			Set<String> authoritiesForUser = authorityService.getAuthoritiesForUser(principal);
			for (String authority : authoritiesForUser) {
				if (authority.startsWith("GROUP_")) {
					String groupName = authority.substring("GROUP_".length());
					if ((!groups.contains(groupName)) && (!groupName.equals("EVERYONE"))) {
						authorityService.removeAuthority(authority, principal);
					}
				}
			}
			for (String group : groups) {
				String authority = "GROUP_".concat(group);
				if (!authorityService.authorityExists(authority)) {
					authority = authorityService.createAuthority(AuthorityType.GROUP, group);
				}
				authorityService.addAuthority(authority, principal);
			}
			return null;
		}
	}

	/**
	 * The Class Trans1.
	 */
	private class Trans1 implements Transactionable {

		/** The session. */
		private HttpSession session;

		/**
		 * Instantiates a new trans1.
		 * 
		 * @param session
		 *            the session
		 */
		public Trans1(HttpSession session) {
			this.session = session;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sourcesense.alfresco.transaction.Transactionable#execute()
		 */
		@Override
		public Object execute() {
			authService.authenticateAsGuest();
			if (!personService.personExists("guest")) {
				throw new IllegalArgumentException("No guest account!");
			}
			NodeRef guestRef = personService.getPerson("guest");
			User user = new User("guest", authService.getCurrentTicket(), guestRef);
			NodeRef guestHomeRef = (NodeRef) nodeService.getProperty(guestRef,
					ContentModel.PROP_HOMEFOLDER);
			user.setHomeSpaceId(guestHomeRef.getId());
			session.setAttribute("_alfAuthTicket", user);
			return null;
		}
	}

	/**
	 * The LoginTransaction to authenticate the user.
	 */
	private class LoginTransaction implements Transactionable {

		/** The user name. */
		private String userName;

		/** The http sess. */
		private HttpSession httpSess;

		/** The req. */
		private HttpServletRequest req;

		/** The res. */
		private HttpServletResponse res;

		/**
		 * Instantiates a new trans3.
		 * 
		 * @param userName
		 *            the user name
		 * @param httpSess
		 *            the http sess
		 * @param req
		 *            the req
		 * @param res
		 *            the res
		 */
		public LoginTransaction(String userName, HttpSession httpSess, HttpServletRequest req,
				HttpServletResponse res) {
			this.userName = userName;
			this.httpSess = httpSess;
			this.req = req;
			this.res = res;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sourcesense.alfresco.transaction.Transactionable#execute()
		 */
		@Override
		public Object execute() {
			NodeRef homeSpaceRef = null;
			if (!personService.personExists(userName)) {
				throw new IllegalArgumentException("The requested user " + userName
						+ " does not exits");
			}
			NodeRef person = personService.getPerson(userName);
			User user = new User(userName, authService.getCurrentTicket(), person);
			homeSpaceRef = (NodeRef) nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER);
			user.setHomeSpaceId(homeSpaceRef.getId());
			populateSession(httpSess, user);
			FacesHelper.getFacesContext(req, res, servletContext);
			FacesContext fc = FacesContext.getCurrentInstance();
			Map<?, ?> session = fc.getExternalContext().getSessionMap();
			session.remove("_alfSessionInvalid");

			return null;
		}
	}

	/**
	 * The Class Trans5.
	 */
	private class Trans5 implements Transactionable {

		/** The username. */
		private String username;

		/**
		 * Instantiates a new trans5.
		 * 
		 * @param username
		 *            the username
		 */
		public Trans5(String username) {
			super();
			this.username = username;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sourcesense.alfresco.transaction.Transactionable#execute()
		 */
		@Override
		public Object execute() {
			return Boolean.valueOf(personService.personExists(username));
		}
	}

	/**
	 * The Class Trans4.
	 */
	private class Trans4 implements Transactionable {

		/** The username. */
		private String username;

		/** The first name. */
		private String firstName;

		/** The last name. */
		private String lastName;

		/** The email. */
		private String email;

		/**
		 * Instantiates a new trans4.
		 * 
		 * @param username
		 *            the username
		 * @param firstName
		 *            the first name
		 * @param lastName
		 *            the last name
		 * @param email
		 *            the email
		 */
		public Trans4(String username, String firstName, String lastName, String email) {
			this.username = username;
			this.firstName = firstName;
			this.lastName = lastName;
			this.email = email;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sourcesense.alfresco.transaction.Transactionable#execute()
		 */
		@Override
		public Object execute() {
			authenticationService.createAuthentication(username, username.toCharArray());
			HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_USERNAME, username);
			properties.put(ContentModel.PROP_FIRSTNAME, firstName);
			properties.put(ContentModel.PROP_LASTNAME, lastName);
			properties.put(ContentModel.PROP_EMAIL, getNullSafe(email));
			NodeRef newPerson = personService.createPerson(properties);
			permissionService.setPermission(newPerson, username,
					permissionService.getAllPermission(), true);
			authenticationService.setAuthenticationEnabled(username, true);
			return null;
		}

		/**
		 * Gets the null safe.
		 * 
		 * @param email
		 *            the email
		 * @return the null safe
		 */
		private String getNullSafe(String email) {
			return (email == null) || (email.isEmpty()) ? username.concat("@") : email;
		}
	}
}

/*
 * Location:
 * W:\CMF\test\WebContent\WEB-INF\lib\alfresco-opensso-webclient-0.8.jar
 * Qualified Name: com.sourcesense.alfresco.opensso.AlfrescoFacade JD-Core
 * Version: 0.6.0
 */