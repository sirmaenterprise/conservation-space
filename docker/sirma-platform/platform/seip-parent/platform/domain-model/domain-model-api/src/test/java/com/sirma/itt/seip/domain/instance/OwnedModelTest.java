package com.sirma.itt.seip.domain.instance;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OwnedModelTest implements OwnedModel<InstanceReference> {

	@Test
	public void testGetOwning() throws Exception {
		OwnedModel<InstanceReference> model = new OwnedModel() {
		};
		assertNull(model.getOwning());
	}

	@Test
	public void testSetOwnedModelValid() throws Exception {
		OwnedModel<InstanceReference> model = mock(OwnedModel.class);
		InstanceReference owning = mock(InstanceReference.class);
		model.setOwning(owning);
		verify(model).setOwning(eq(owning));
		model.setOwning(null);
		verify(model).setOwning(isNull(InstanceReference.class));
		verify(model).setOwning(isNull(InstanceReference.class));
	}
}
