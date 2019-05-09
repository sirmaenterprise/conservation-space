package com.sirma.itt.seip.instance.properties.patch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
 * Tests for {@link UpdateSystemUserPropertiesPatch}.
 *
 * @author smustafov
 */
public class UpdateSystemUserPropertiesPatchTest {

	@InjectMocks
	private UpdateSystemUserPropertiesPatch patch;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Mock
	private SecurityConfiguration securityConfiguration;

	@Mock
	private DataSource tenantDataSource;

	@Mock
	private Connection connection;

	@Before
	public void beforeEach() throws SQLException {
		MockitoAnnotations.initMocks(this);
		when(tenantDataSource.getConnection()).thenReturn(connection);
	}

	@Test
	public void should_DoNothing_When_ConfigHasCorrectValue() throws Exception {
		when(securityConfiguration.getSystemUserName())
				.thenReturn(new ConfigurationPropertyMock<>(SecurityContext.SYSTEM_USER_NAME));

		patch.execute(null);

		verify(tenantDataSource, never()).getConnection();
	}

	@Test
	public void should_UpdatePropertiesTables() throws Exception {
		when(securityConfiguration.getSystemUserName()).thenReturn(new ConfigurationPropertyMock<>("system"));

		PreparedStatement propertiesStatement = mockStatement(UpdateSystemUserPropertiesPatch.PROPERTIES_UPDATE_QUERY);
		PreparedStatement propertiesValuesStatement = mockStatement(
				UpdateSystemUserPropertiesPatch.PROPERTY_VALUE_UPDATE_QUERY);

		patch.execute(null);

		verifyStatement(propertiesStatement, "system", SecurityContext.SYSTEM_USER_NAME, "emf:system-%");

		verify(propertiesValuesStatement, times(2)).setString(1, "system");
		verify(propertiesValuesStatement, times(2)).setString(2, SecurityContext.SYSTEM_USER_NAME);
		verify(propertiesValuesStatement).setString(3, "emf:system-%");
		verify(propertiesValuesStatement).setString(3, "system@%");
	}

	private void verifyStatement(PreparedStatement preparedStatement, String... params) throws SQLException {
		for (int i = 0; i < params.length; i++) {
			verify(preparedStatement).setString(i + 1, params[i]);
		}
	}

	private PreparedStatement mockStatement(String query) throws SQLException {
		PreparedStatement preparedStatement = mock(PreparedStatement.class);
		when(connection.prepareStatement(query)).thenReturn(preparedStatement);
		return preparedStatement;
	}

}
