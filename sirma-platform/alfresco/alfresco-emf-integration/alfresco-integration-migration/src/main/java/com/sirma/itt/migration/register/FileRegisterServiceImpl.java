package com.sirma.itt.migration.register;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.sirma.itt.migration.constants.MigrationStatus;
import com.sirma.itt.migration.dto.FileRegisterSearchDTO;
import com.sirma.itt.migration.dto.FileRegistryEntry;
import com.sirma.itt.utils.ObjectUtil;

/**
 * Implementation of the {@link FileRegisterService}
 *
 * @author BBonev
 */
public class FileRegisterServiceImpl implements FileRegisterService {

	private static final String BATCH_GET_BY_CRC_QUERY = "batch_get_by_crc";
	private static final String FIND_BY_CRC_QUERY = "find_by_crc";
	private static final String FIND_BY_NODE_REF_QUERY = "find_by_node_id";
	private static final String CHANGE_STATUS_QUERY = "change_status";
	private static final String FILE_DELETED_QUERY = "file_deleted";
	private static final String FILE_MOVED_QUERY = "file_moved";
	private static final String FOLDER_MOVED_QUERY = "folder_moved";
	private SessionFactory sessionFactory;
	private AuthenticationService authenticationService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E> E save(E e) {
		Pair<Session, Boolean> sessionPair = getSession(true);
		Session session = sessionPair.getFirst();
		Pair<Transaction, Boolean> pair = null;
		try {
			// update the specific properties
			if (e instanceof FileRegistryEntry) {
				FileRegistryEntry entry = (FileRegistryEntry) e;
				entry.setModifiedBy(authenticationService.getCurrentUserName());
				entry.setModifiedDate(new Date());
			}

			pair = beginTransaction(session);
			// if (session.contains(e)) {
				session.saveOrUpdate(e);
			// } else {
			// session.persist(e);
			// }
			commitTransaction(pair.getFirst(), pair.getSecond());
		} catch (Exception ex) {
			ex.printStackTrace();
			if (pair != null) {
				pair.getFirst().rollback();
			}
			return null;
		} finally {
			if (sessionPair != null) {
				closeSession(session, sessionPair.getSecond());
			}
		}
		return e;
	}

	/**
	 * Returns a session. If the argument is <code>true</code> new session will
	 * be returned, if <code>false</code> the current session will be returned
	 * or if not new will be opened. If the current session is not connected
	 * will connect it
	 *
	 * @param openNew
	 *            if to open a new session or to try to use the current one
	 * @return a pair with the opened session and if it's allowed to close it at
	 *         the end of the call
	 */
	private Pair<Session, Boolean> getSession(boolean openNew) {
		if (openNew) {
			return new Pair<Session, Boolean>(sessionFactory.openSession(),
					Boolean.TRUE);
		}
		Session currentSession = sessionFactory.getCurrentSession();
		Boolean closeIt = Boolean.FALSE;
		if (!currentSession.isOpen() || !currentSession.isConnected()) {
			currentSession = sessionFactory.openSession();
			closeIt = Boolean.TRUE;
		}
		return new Pair<Session, Boolean>(currentSession, closeIt);
	}

	/**
	 * Commits the given transaction if allowed and if not rolledback
	 *
	 * @param transaction
	 *            is the transaction to commit
	 * @param commitIt
	 *            if it's allowed to commit the transaction
	 */
	private void commitTransaction(Transaction transaction, Boolean commitIt) {
		if ((commitIt == Boolean.TRUE) && !transaction.wasRolledBack()) {
			transaction.commit();
		}
	}

	/**
	 * Closes the given session if allowed
	 *
	 * @param session
	 *            is the session to close
	 * @param closeIt
	 *            if to close the session or not
	 */
	private void closeSession(Session session, Boolean closeIt) {
		if (closeIt == Boolean.TRUE) {
			session.close();
		}
	}

	/**
	 * Begin new transaction if not in a transaction
	 *
	 * @param session
	 *            is the current session to use
	 * @return a pair with the transaction and if it's allowed to commit it or
	 *         not
	 */
	private Pair<Transaction, Boolean> beginTransaction(Session session) {
		Transaction transaction = session.getTransaction();
		Boolean closeIt = Boolean.FALSE;
		if (transaction == null) {
			transaction = session.beginTransaction();
			transaction.begin();
			closeIt = Boolean.TRUE;
		} else if (!transaction.isActive()) {
			transaction.begin();
			closeIt = Boolean.TRUE;
		}
		return new Pair<Transaction, Boolean>(transaction, closeIt);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public FileRegisterSearchDTO search(FileRegisterSearchDTO args) {
		Pair<Session, Boolean> sessionPair = getSession(false);
		Criteria criteria = sessionPair.getFirst().createCriteria(
				FileRegistryEntry.class, "fr");
		// check for modified by
		if (!StringUtils.isEmpty(args.getModifiedBy())) {
			criteria.add(Restrictions.eq("fr.modifiedBy", args.getModifiedBy()));
		}
		// check for source path
		if (!StringUtils.isEmpty(args.getSource())) {
			criteria.add(Restrictions.ilike("fr.sourcePath", args.getSource(),
					MatchMode.ANYWHERE));
		}
		// check for status
		if (args.getStatus() != null) {
			criteria.add(Restrictions.eq("fr.status", args.getStatus()
					.getStatusCode()));
		}
		// date range check
		if (ObjectUtil.isValid(args.getModifiedFrom(), args.getModifiedTo())) {
			if ((args.getModifiedFrom() != null)
					&& (args.getModifiedTo() == null)) {
				criteria.add(Restrictions.ge("fr.modifiedDate",
						args.getModifiedFrom()));
			} else if ((args.getModifiedFrom() == null)
					&& (args.getModifiedTo() != null)) {
				criteria.add(Restrictions.le("fr.modifiedDate",
						args.getModifiedTo()));
			} else {
				criteria.add(Restrictions.between("fr.modifiedDate",
						args.getModifiedFrom(), args.getModifiedTo()));
			}
		}
		// filename check
		if (!StringUtils.isEmpty(args.getNameFilter())) {
			Disjunction or = Restrictions.disjunction();
			or.add(Restrictions.ilike("fr.fileName", args.getNameFilter(),
					MatchMode.ANYWHERE));
			or.add(Restrictions.ilike("fr.destFileName", args.getNameFilter(),
					MatchMode.ANYWHERE));

			criteria.add(or);
		}
		if (!StringUtils.isEmpty(args.getTarget())) {
			criteria.add(Restrictions.ilike("fr.targetPath", args.getTarget(),
					MatchMode.ANYWHERE));
		}
		if (!args.getInclude().isEmpty() || !args.getExclude().isEmpty()) {
			Disjunction or = Restrictions.disjunction();
			// apply same patterns to both name fields
			or.add(constructNameCriterion("fr.fileName", args.getInclude(),
					args.getExclude()));

			or.add(constructNameCriterion("fr.destFileName", args.getInclude(),
					args.getExclude()));
			criteria.add(or);
		}
		// do counting of the results
		if (args.getTotalCount() == 0) {
			criteria.setProjection(Projections.count("fr.crc"));
			Integer result = (Integer) criteria.uniqueResult();
			args.setTotalCount(result);
			if (result == 0) {
				args.setResult(Collections.EMPTY_LIST);
				return args;
			}
			criteria.setProjection(null);
		}

		criteria.addOrder(Order.desc("fr.modifiedDate"));

		criteria.setFirstResult(args.getSkipCount());
		criteria.setMaxResults(args.getPageSize());
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		List<FileRegistryEntry> list = criteria.list();
		args.setResult(list);
		return args;
	}

	/**
	 * Construct name criterion.
	 *
	 * @param columnName
	 *            the column name
	 * @param include
	 *            the list with include patterns
	 * @param exclude
	 *            the list with exclude patterns
	 * @return the criterion
	 */
	private Criterion constructNameCriterion(String columnName,
			List<String> include, List<String> exclude) {
		Conjunction and = Restrictions.conjunction();
		if (!include.isEmpty()) {
			Disjunction or = Restrictions.disjunction();
			for (String rule : include) {
				String value = rule.replace("*", "");
				or.add(Restrictions
						.ilike(columnName, value, getMatchMode(rule)));
			}
			and.add(or);
		}
		if (!exclude.isEmpty()) {
			for (String rule : exclude) {
				String value = rule.replace("*", "");
				and.add(Restrictions.not(Restrictions.ilike(columnName, value,
						getMatchMode(rule))));
			}
		}
		return and;
	}

	/**
	 * Construct the match mode based on the rule
	 *
	 * @param rule
	 *            is the rule to parse
	 * @return the match mode
	 */
	private MatchMode getMatchMode(String rule) {
		MatchMode matchMode = MatchMode.EXACT;
		boolean startsWith = rule.startsWith("*");
		boolean endsWith = rule.endsWith("*");
		if (startsWith && !endsWith) {
			matchMode = MatchMode.END;
		} else if (!startsWith && endsWith) {
			matchMode = MatchMode.START;
		} else if (startsWith && endsWith) {
			matchMode = MatchMode.ANYWHERE;
		}
		return matchMode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fileMoved(String src, String target) {
		Pair<Session, Boolean> sessionPair = getSession(true);
		Pair<Transaction, Boolean> transactionPair = beginTransaction(sessionPair
				.getFirst());
		try {
			Query query = sessionPair.getFirst().getNamedQuery(
					FOLDER_MOVED_QUERY);
			query.setParameter("destination_path", target);
			query.setParameter("modified_date", new Date());
			query.setParameter("modified_by",
					authenticationService.getCurrentUserName());
			query.setParameter("old_destination_path", src);
			query.setParameter("old_dest_path", src + "%");
			boolean updated = query.executeUpdate() != 0;
			commitTransaction(transactionPair.getFirst(),
					transactionPair.getSecond());
			return updated;
		} catch (Exception e) {
			e.printStackTrace();
			transactionPair.getFirst().rollback();
		} finally {
			if (sessionPair != null) {
				closeSession(sessionPair.getFirst(), true);
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean changeStatus(String crc, MigrationStatus status) {
		Pair<Session, Boolean> sessionPair = getSession(true);
		Pair<Transaction, Boolean> transactionPair = beginTransaction(sessionPair
				.getFirst());
		try {
			Query query = sessionPair.getFirst().getNamedQuery(
					CHANGE_STATUS_QUERY);
			query.setParameter("crc", crc);
			query.setParameter("status", status.getStatusCode());
			query.setParameter("modified_date", new Date());
			query.setParameter("modified_by",
					authenticationService.getCurrentUserName());
			boolean updated = query.executeUpdate() != 0;
			commitTransaction(transactionPair.getFirst(),
					transactionPair.getSecond());
			return updated;
		} catch (Exception e) {
			e.printStackTrace();
			transactionPair.getFirst().rollback();
		} finally {
			if (sessionPair != null) {
				closeSession(sessionPair.getFirst(), true);
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileRegistryEntry findByCrc(String crc) {
		Pair<Session, Boolean> sessionPair = getSession(false);
		try {
			Query query = sessionPair.getFirst().getNamedQuery(
					FIND_BY_CRC_QUERY);
			query.setParameter("crc", crc);
			List<?> list = query.list();
			if ((list != null) && !list.isEmpty()) {
				return (FileRegistryEntry) list.get(0);
			}
			return null;
		} finally {
			closeSession(sessionPair.getFirst(), sessionPair.getSecond());
		}
	}

	/**
	 * Setter method for sessionFactory.
	 *
	 * @param sessionFactory
	 *            the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Getter method for sessionFactory.
	 *
	 * @return the sessionFactory
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * Getter method for authenticationService.
	 *
	 * @return the authenticationService
	 */
	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	/**
	 * Setter method for authenticationService.
	 *
	 * @param authenticationService
	 *            the authenticationService to set
	 */
	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<FileRegistryEntry> fetchEntries(List<String> crcs) {
		Pair<Session, Boolean> sessionPair = getSession(false);
		try {
			Query query = sessionPair.getFirst().getNamedQuery(
					BATCH_GET_BY_CRC_QUERY);
			query.setParameterList("crc", crcs);
			List<?> list = query.list();
			if (list != null) {
				return (List<FileRegistryEntry>) list;
			}
			return Collections.emptyList();
		} finally {
			closeSession(sessionPair.getFirst(), sessionPair.getSecond());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fileDeleted(NodeRef nodeRef) {
		Pair<Session, Boolean> sessionPair = getSession(true);
		Pair<Transaction, Boolean> transactionPair = beginTransaction(sessionPair
				.getFirst());
		try {
			Query query = sessionPair.getFirst().getNamedQuery(
					FILE_DELETED_QUERY);
			query.setParameter("status",
					MigrationStatus.NOT_MIGRATED.getStatusCode());
			query.setParameter("modified_date", new Date());
			query.setParameter("modified_by",
					authenticationService.getCurrentUserName());
			query.setParameter("old_node_id", nodeRef.getId());
			boolean updated = query.executeUpdate() != 0;
			commitTransaction(transactionPair.getFirst(),
					transactionPair.getSecond());
			return updated;
		} catch (Exception e) {
			e.printStackTrace();
			transactionPair.getFirst().rollback();
		} finally {
			if (sessionPair != null) {
				closeSession(sessionPair.getFirst(), true);
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fileMoved(NodeRef oldRef, String destPath, String newName) {
		Pair<Session, Boolean> sessionPair = getSession(true);
		Pair<Transaction, Boolean> transactionPair = beginTransaction(sessionPair
				.getFirst());
		try {
			Query query = sessionPair.getFirst()
					.getNamedQuery(FILE_MOVED_QUERY);
			query.setParameter("destination_path", destPath);
			query.setParameter("modified_date", new Date());
			query.setParameter("modified_by",
					authenticationService.getCurrentUserName());
			query.setParameter("old_node_id", oldRef.getId());
			query.setParameter("destFileName", newName);
			boolean updated = query.executeUpdate() != 0;
			commitTransaction(transactionPair.getFirst(),
					transactionPair.getSecond());
			return updated;
		} catch (Exception e) {
			e.printStackTrace();
			transactionPair.getFirst().rollback();
		} finally {
			if (sessionPair != null) {
				closeSession(sessionPair.getFirst(), true);
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean existsNodeRefEntry(NodeRef nodeRef) {
		Pair<Session, Boolean> sessionPair = getSession(false);
		try {
			Query query = sessionPair.getFirst().getNamedQuery(
					FIND_BY_NODE_REF_QUERY);
			query.setParameter("node_id", nodeRef.getId());
			List<?> list = query.list();
			return (list != null) && !list.isEmpty();
		} finally {
			closeSession(sessionPair.getFirst(), sessionPair.getSecond());
		}
	}

}
