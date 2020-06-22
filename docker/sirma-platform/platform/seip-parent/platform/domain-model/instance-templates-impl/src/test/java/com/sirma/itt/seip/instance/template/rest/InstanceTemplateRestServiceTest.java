package com.sirma.itt.seip.instance.template.rest;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.instance.template.InstanceTemplateService;

public class InstanceTemplateRestServiceTest {

	@InjectMocks
	private InstanceTemplateRestService instanceTemplateRestService;

	@Mock
	private InstanceTemplateService instanceTemplateService;

	@Test
	public void should_InitTemplateUpdateProcess_ProvidingTheTemplateInstanceId() {
		final String TEMPLATE_INSTANCE_ID = "template1";

		InstanceTemplateUpdateRequest request = new InstanceTemplateUpdateRequest();
		request.setTemplateInstance(TEMPLATE_INSTANCE_ID);

		instanceTemplateRestService.initiateInstanceTemplateUpdateOperation(request);

		verify(instanceTemplateService, times(1)).updateInstanceViews(TEMPLATE_INSTANCE_ID);
	}

	@Test
	public void should_InitSingleTemplateUpdateProcess_ProvidingTheInstanceId() {
		final String INSTANCE_ID = "instance1";

		InstanceTemplateUpdateRequest request = new InstanceTemplateUpdateRequest();
		request.setInstance(INSTANCE_ID);

		instanceTemplateRestService.initiateSingleInstanceTemplateUpdateOperation(request);

		verify(instanceTemplateService, times(1)).updateInstanceView(INSTANCE_ID);
	}

	@Test
	public void should_InitTemplateVersionGetterProcess_ProvidingTheInstanceId() {
		final String INSTANCE_ID = "instance1";

		InstanceTemplateUpdateRequest request = new InstanceTemplateUpdateRequest();
		request.setInstance(INSTANCE_ID);

		instanceTemplateRestService.getInstanceTemplateVersion(request);

		verify(instanceTemplateService, times(1)).getInstanceTemplateVersion(INSTANCE_ID);
	}

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
}
