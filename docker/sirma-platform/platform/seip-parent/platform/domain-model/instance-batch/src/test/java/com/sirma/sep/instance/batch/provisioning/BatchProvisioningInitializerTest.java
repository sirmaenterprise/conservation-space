package com.sirma.sep.instance.batch.provisioning;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.sep.instance.batch.config.BatchConfigurationModel;

/**
 * Test the batch provisioning initializer
 *
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class BatchProvisioningInitializerTest {

	@Mock
	private BatchConfigurationModel configurationModel;

	@Mock
	private BatchConfigurationProvisioner provisioner;

	private Instance<BatchConfigurationProvisioner> provisionerInstance;

	@InjectMocks
	private BatchProvisioningInitializer initializer;

	/**
	 * Init the mocks.
	 */
	@Before
	public void init() {
		provisionerInstance = mock(Instance.class);
		when(provisionerInstance.isUnsatisfied()).thenReturn(false);
		when(provisionerInstance.get()).thenReturn(provisioner);
		ReflectionUtils.setFieldValue(initializer, "provisionerInstance", provisionerInstance);
	}

	@Test
	public void should_provisionBatchSubsystem() throws RollbackedException, BatchProvisioningException {
		initializer.provisionBatchSubsystem();
		verify(provisioner).provision(configurationModel);
	}

	@Test
	public void should_doNothing_whenNoBatchProvisioner() throws RollbackedException {
		when(provisionerInstance.isUnsatisfied()).thenReturn(true);
		initializer.provisionBatchSubsystem();
		verifyZeroInteractions(provisioner);
	}

	@Test(expected = RollbackedException.class)
	public void should_throwRollbackedException_onError() throws BatchProvisioningException, RollbackedException {
		doThrow(new BatchProvisioningException()).when(provisioner).provision(configurationModel);
		initializer.provisionBatchSubsystem();
	}
}
