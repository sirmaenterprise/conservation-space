package com.sirma.itt.emf.audit.patch;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Tests for {@link ChangeSystemUserAuditPatch}.
 *
 * @author smustafov
 */
public class ChangeSystemUserAuditPatchTest {

	private static final String CURRENT_USER_NAME = "system";
	private static final String TENANT_ID = "cia.test";

	@InjectMocks
	private ChangeSystemUserAuditPatch patch;

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
	public void should_UpdateAudit() throws Exception {
		PreparedStatement preparedStatement = mockDataSource();

		patch.execute(null);

		verify(preparedStatement, times(9)).setString(anyInt(), anyString());
		verify(preparedStatement, times(3)).executeUpdate();
	}

	private PreparedStatement mockDataSource() throws Exception {
		PreparedStatement preparedStatement = mock(PreparedStatement.class);
		Connection connection = mock(Connection.class);

		when(tenantDataSource.getConnection()).thenReturn(connection);
		when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
		return preparedStatement;
	}

}
