package com.sirma.itt.seip.wildfly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.db.DatasourceModel;
import com.sirma.itt.seip.db.DatasourceProvisioner;
import com.sirma.itt.seip.exception.RollbackedException;

/**
 * Test for {@link WildFlyDatasourceProvisioner}
 *
 * @author BBonev
 */
public class WildflyDatasourceProvisioningTest {

	@Mock
	private WildflyControllerService controller;

	@Mock
	private ModelNode node;

	@InjectMocks
	private DatasourceProvisioner datasourceProvisioner = new WildFlyDatasourceProvisioner();

	@Before
	public void beforeMethod() throws RollbackedException {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testForGetDsProperty() throws Exception {
		mockSuccess();
		mockProperty();

		assertEquals("value", datasourceProvisioner.getXaDataSourceDatabase("dsName"));
		assertEquals("value", datasourceProvisioner.getXaDataSourcePort("dsName"));
		assertEquals("value", datasourceProvisioner.getXaDataSourceServerName("dsName"));
	}

	@Test
	public void testForGetDsProperty_fail() throws Exception {
		mockFail();
		assertNull(datasourceProvisioner.getXaDataSourceServerName("dsName"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testForGetDsProperty_failWithError() throws Exception {
		when(controller.execute(any())).thenThrow(RollbackedException.class);
		assertNull(datasourceProvisioner.getXaDataSourceDatabase("dsName"));
	}

	@Test
	public void removeDatasource() throws Exception {
		mockSuccess();
		datasourceProvisioner.removeDatasource("datasource");
	}

	@Test(expected = RollbackedException.class)
	public void removeDatasource_fail() throws Exception {
		mockDatasourceRemoval();
		datasourceProvisioner.removeDatasource("datasource");
	}

	@Test
	public void should_callControllerCreate_forCorrectDatasourceModel() throws RollbackedException {
		mockSuccess();
		ArgumentCaptor<ModelNode> nodeCaptor = ArgumentCaptor.forClass(ModelNode.class);

		DatasourceModel model = new DatasourceModel();
		model.setPoolName("pool");
		model.setUsername("username");
		model.setDriverName("driver");
		model.setJndiName("jndiname");
		model.setPassword("password");
		model.setDatabaseHost("host");
		model.setDatabasePort(1234);
		model.setDatabaseName("dbname");
		model.setDatasourceName("dsname");

		datasourceProvisioner.createXaDatasource(model);
		Mockito.verify(controller, Mockito.atLeastOnce()).execute(nodeCaptor.capture());

		ModelNode steps = nodeCaptor.getValue().get(ClientConstants.STEPS);
		ModelNode request = steps.get(0);

		Assert.assertEquals("pool", request.get("pool-name").asString());
		Assert.assertEquals("username", request.get("user-name").asString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throwException_when_valueIsMissing() throws RollbackedException {
		datasourceProvisioner.createXaDatasource(new DatasourceModel());
	}

	@Test(expected = RollbackedException.class)
	public void should_throwException_when_responseIsError() throws RollbackedException {
		mockFail();

		DatasourceModel model = new DatasourceModel();
		model.setPoolName("pool");
		model.setUsername("username");
		model.setDriverName("driver");
		model.setJndiName("jndiname");
		model.setPassword("password");
		model.setDatabaseHost("host");
		model.setDatabasePort(1234);
		model.setDatabaseName("dbname");
		model.setDatasourceName("dsname");

		datasourceProvisioner.createXaDatasource(model);

	}

	private void mockSuccess() throws RollbackedException {
		ModelNode outcome = mock(ModelNode.class);
		when(outcome.asString()).thenReturn("success");
		when(node.get("outcome")).thenReturn(outcome);
		when(controller.execute(any(ModelNode.class))).thenReturn(node);
	}

	private void mockFail() throws RollbackedException {
		ModelNode outcome = mock(ModelNode.class);
		when(outcome.asString()).thenReturn("failed");
		when(node.get("outcome")).thenReturn(outcome);
		ModelNode description = mock(ModelNode.class);
		when(description.asString()).thenReturn("fail");
		when(node.get("failure-description")).thenReturn(description);
		when(controller.execute(any(ModelNode.class))).thenReturn(node);
	}
	
	private void mockDatasourceRemoval() throws RollbackedException {
		ModelNode successOutcome = mock(ModelNode.class);
		when(successOutcome.asString()).thenReturn("success");
		ModelNode successResponse = mock(ModelNode.class);
		when(successResponse.get("outcome")).thenReturn(successOutcome);

		ModelNode failedOutcome = mock(ModelNode.class);
		when(failedOutcome.asString()).thenReturn("failed");
		ModelNode failedResponse = mock(ModelNode.class);
		when(failedResponse.get("outcome")).thenReturn(failedOutcome);
		ModelNode description = mock(ModelNode.class);
		when(description.asString()).thenReturn("fail");
		when(failedResponse.get("failure-description")).thenReturn(description);

		// First time returns success response for the datasource exists check, then returns failed
		// response.
		when(controller.execute(any(ModelNode.class))).thenReturn(successResponse, failedResponse);
	}

	private void mockProperty() {
		ModelNode value = mock(ModelNode.class);
		when(value.asString()).thenReturn("value");
		ModelNode result = mock(ModelNode.class);
		when(result.get("value")).thenReturn(value);
		when(node.get("result")).thenReturn(result);
	}
}