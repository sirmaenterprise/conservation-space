package com.sirma.itt.emf.semantic;

import static org.mockito.Mockito.when;

import java.rmi.RemoteException;

import javax.ejb.EJBException;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openrdf.model.Statement;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.semantic.ConnectionFactory;

/**
 * The Class TransactionalRepositoryConnectionImplTest.
 *
 * @author bbonev
 */
@Test
public class TransactionalRepositoryConnectionImplTest {

	@InjectMocks
	private TransactionalRepositoryConnectionImpl impl;

	@Mock
	private ConnectionFactory connectionFactory;
	@Mock
	private RepositoryConnection connection;
	@Mock
	private RepositoryConnection rOConnection;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private RepositoryConnectionMonitor connectionMonitor;

	@BeforeMethod
	public void initialize() {
		MockitoAnnotations.initMocks(this);

		when(connectionFactory.produceConnection()).thenReturn(connection);
		when(connectionFactory.produceReadOnlyConnection()).thenReturn(rOConnection);

		when(securityContext.getCurrentTenantId()).thenReturn("tenant.com");
	}

	/**
	 * Test no transaction.
	 *
	 * @throws RepositoryException
	 *             the repository exception
	 * @throws MalformedQueryException
	 *             the malformed query exception
	 */
	public void testNoTransaction() throws RepositoryException, MalformedQueryException {
		impl.prepareGraphQuery(null, null);
		impl.close();

		Mockito.verify(connectionFactory, Mockito.never()).produceConnection();
		Mockito.verify(connectionFactory, Mockito.atLeastOnce()).produceReadOnlyConnection();
		Mockito.verify(rOConnection).prepareGraphQuery(null, null);
		Mockito.verify(connectionFactory, Mockito.atLeastOnce()).disposeConnection(rOConnection);
	}

	/**
	 * Test with transaction.
	 *
	 * @throws EJBException
	 *             the EJB exception
	 * @throws RemoteException
	 *             the remote exception
	 * @throws RepositoryException
	 *             the repository exception
	 * @throws MalformedQueryException
	 *             the malformed query exception
	 */
	public void testWithTransaction()
			throws EJBException, RemoteException, RepositoryException, MalformedQueryException {
		impl.afterBegin();
		impl.add((Iterable<Statement>) null);
		impl.beforeCompletion();

		Mockito.verify(connectionFactory, Mockito.atLeastOnce()).produceConnection();
		Mockito.verify(connectionFactory, Mockito.never()).produceReadOnlyConnection();
		Mockito.verify(connectionFactory, Mockito.atLeastOnce()).disposeConnection(connection);
		Mockito.verify(connection).add((Iterable<Statement>) null);
	}

	/**
	 * Test mixed_no connection.
	 *
	 * @throws RepositoryException
	 *             the repository exception
	 * @throws MalformedQueryException
	 *             the malformed query exception
	 * @throws EJBException
	 *             the EJB exception
	 * @throws RemoteException
	 *             the remote exception
	 */
	public void testMixed_noConnection()
			throws RepositoryException, MalformedQueryException, EJBException, RemoteException {

		impl.prepareGraphQuery(null, null);
		impl.afterBegin();
		impl.add((Iterable<Statement>) null);
		impl.beforeCompletion();

		Mockito.verify(connectionFactory, Mockito.atLeastOnce()).produceReadOnlyConnection();
		Mockito.verify(connectionFactory, Mockito.atLeastOnce()).produceConnection();
		Mockito.verify(rOConnection, Mockito.never()).begin();
		Mockito.verify(connectionFactory, Mockito.atLeastOnce()).disposeConnection(connection);
		Mockito.verify(connectionFactory, Mockito.never()).disposeConnection(rOConnection);
	}

	/**
	 * Test mixed_open connection_no tx.
	 *
	 * @throws RepositoryException
	 *             the repository exception
	 * @throws MalformedQueryException
	 *             the malformed query exception
	 * @throws EJBException
	 *             the EJB exception
	 * @throws RemoteException
	 *             the remote exception
	 */
	public void testMixed_openConnection_noTx()
			throws RepositoryException, MalformedQueryException, EJBException, RemoteException {

		Mockito.when(rOConnection.isOpen()).thenReturn(true);

		impl.prepareGraphQuery(null, null);
		impl.afterBegin();
		impl.add((Iterable<Statement>) null);
		impl.beforeCompletion();

		Mockito.verify(connectionFactory, Mockito.never()).produceConnection();
		Mockito.verify(connectionFactory, Mockito.atLeastOnce()).produceReadOnlyConnection();
		Mockito.verify(rOConnection, Mockito.atLeastOnce()).begin();
		Mockito.verify(connectionFactory, Mockito.atLeastOnce()).disposeConnection(rOConnection);
		Mockito.verify(connectionFactory, Mockito.never()).disposeConnection(connection);
	}

	/**
	 * Test mixed_open connection_w tx.
	 *
	 * @throws RepositoryException
	 *             the repository exception
	 * @throws MalformedQueryException
	 *             the malformed query exception
	 * @throws EJBException
	 *             the EJB exception
	 * @throws RemoteException
	 *             the remote exception
	 */
	public void testMixed_openConnection_wTx()
			throws RepositoryException, MalformedQueryException, EJBException, RemoteException {

		Mockito.when(rOConnection.isOpen()).thenReturn(true);
		Mockito.when(rOConnection.isActive()).thenReturn(true);

		impl.prepareGraphQuery(null, null);
		impl.afterBegin();
		impl.add((Iterable<Statement>) null);
		impl.beforeCompletion();

		Mockito.verify(connectionFactory, Mockito.never()).produceConnection();
		Mockito.verify(connectionFactory, Mockito.atLeastOnce()).produceReadOnlyConnection();
		Mockito.verify(rOConnection, Mockito.never()).begin();
		Mockito.verify(connectionFactory, Mockito.atLeastOnce()).disposeConnection(rOConnection);
		Mockito.verify(connectionFactory, Mockito.never()).disposeConnection(connection);
	}

}
