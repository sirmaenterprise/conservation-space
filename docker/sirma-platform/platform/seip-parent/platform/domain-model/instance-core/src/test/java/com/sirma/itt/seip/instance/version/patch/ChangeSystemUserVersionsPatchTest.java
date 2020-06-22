package com.sirma.itt.seip.instance.version.patch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Tests for {@link ChangeSystemUserVersionsPatch}.
 *
 * @author smustafov
 */
public class ChangeSystemUserVersionsPatchTest {

	private static final String CURRENT_USER_NAME = "system";
	private static final String TENANT_ID = "cia.test";

	@InjectMocks
	private ChangeSystemUserVersionsPatch patch;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Mock
	private SecurityConfiguration securityConfiguration;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private DataSource tenantDataSource;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		when(securityConfiguration.getSystemUserName())
				.thenReturn(new ConfigurationPropertyMock<String>(CURRENT_USER_NAME));
		when(securityContext.getCurrentTenantId()).thenReturn(TENANT_ID);
	}

	@Test
	public void should_DoNothing_When_ConfigHasCorrectValue() throws Exception {
		when(securityConfiguration.getSystemUserName())
				.thenReturn(new ConfigurationPropertyMock<String>(SecurityContext.SYSTEM_USER_NAME));

		patch.execute(null);

		verify(tenantDataSource, never()).getConnection();
	}

	@Test
	public void should_UpdateVersions() throws Exception {
		PreparedStatement preparedStatement = mockDataSource();

		patch.execute(null);

		verify(preparedStatement).setString(1, EMF.PREFIX + ":" + SecurityContext.SYSTEM_USER_NAME + "-" + TENANT_ID);
		verify(preparedStatement).setString(2, EMF.PREFIX + ":" + CURRENT_USER_NAME + "-" + TENANT_ID);
		verify(preparedStatement).executeUpdate();
	}

	private PreparedStatement mockDataSource() throws Exception {
		PreparedStatement preparedStatement = mock(PreparedStatement.class);
		Connection connection = mock(Connection.class);

		when(tenantDataSource.getConnection()).thenReturn(connection);
		when(connection.prepareStatement(Matchers.anyString())).thenReturn(preparedStatement);
		return preparedStatement;
	}

}
