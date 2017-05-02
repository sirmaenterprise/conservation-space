package com.sirma.itt.seip.wildfly.cli.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jboss.dmr.ModelNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.wildfly.WildflyControllerService;

/**
 * Test for {@link WildFlyDatasourceProvisioning}
 *
 * @author BBonev
 */
public class WildFlyDatasourceProvisioningTest {

	@Mock
	private WildflyControllerService controller;
	@Mock
	private ModelNode node;

	@Before
	public void beforeMethod() throws RollbackedException {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testForGetDsProperty() throws Exception {
		mockSuccess();
		mockProperty();

		assertEquals("value", WildFlyDatasourceProvisioning.getXaDataSourceProperty(controller, "dsName", "property"));
		assertEquals("value", WildFlyDatasourceProvisioning.getXaDataSourceDatabase(controller, "dsName"));
		assertEquals("value", WildFlyDatasourceProvisioning.getXaDataSourcePort(controller, "dsName"));
		assertEquals("value", WildFlyDatasourceProvisioning.getXaDataSourceServerName(controller, "dsName"));
	}

	@Test
	public void testForGetDsProperty_fail() throws Exception {
		mockFail();

		assertNull(WildFlyDatasourceProvisioning.getXaDataSourceProperty(controller, "dsName", "property"));
	}

	@Test
	public void testForGetDsProperty_failWithError() throws Exception {
		when(controller.execute(any())).thenThrow(RollbackedException.class);

		assertNull(WildFlyDatasourceProvisioning.getXaDataSourceProperty(controller, "dsName", "property"));
	}

	@Test
	public void addUserAndPassword() throws Exception {
		Map<String, Serializable> model = new HashMap<>();
		WildFlyDatasourceProvisioning.addUserAndPassword(model, "user", "pass");
		assertFalse(model.isEmpty());
	}

	@Test
	public void nullSafeValue() throws Exception {
		assertNull(WildFlyDatasourceProvisioning.nullSafeValue(null, null));
		assertEquals("default", WildFlyDatasourceProvisioning.nullSafeValue(null, "default"));
		assertEquals("2", WildFlyDatasourceProvisioning.nullSafeValue(2, "default"));
	}

	@Test
	public void removeDatasource() throws Exception {
		mockSuccess();
		WildFlyDatasourceProvisioning.removeDatasource(controller, "datasource");
	}

	@Test(expected = RollbackedException.class)
	public void removeDatasource_fail() throws Exception {
		mockFail();
		WildFlyDatasourceProvisioning.removeDatasource(controller, "datasource");
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

	private void mockProperty() {
		ModelNode value = mock(ModelNode.class);
		when(value.asString()).thenReturn("value");
		ModelNode result = mock(ModelNode.class);
		when(result.get("value")).thenReturn(value);
		when(node.get("result")).thenReturn(result);
	}
}
