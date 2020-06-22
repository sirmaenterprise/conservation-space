package com.sirma.itt.seip.template.observers;

import static com.sirma.itt.seip.domain.ObjectTypes.TEMPLATE;
import static com.sirma.itt.seip.template.TemplateProperties.FOR_OBJECT_TYPE;
import static com.sirma.itt.seip.template.TemplateProperties.IS_PRIMARY_TEMPLATE;
import static com.sirma.itt.seip.template.TemplateProperties.TEMPLATE_PURPOSE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.event.BeforeInstancePersistEvent;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.db.TemplateDao;
import com.sirma.itt.seip.testutil.fakes.InstanceTypeFake;

/**
 * Tests the functionality of {@link TemplateCreateObserver}.
 *
 * @author Vilizar Tsonev
 */
public class TemplateCreateObserverTest {

	@InjectMocks
	private TemplateCreateObserver templateCreateObserver;

	@Mock
	private TemplateDao templateDao;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_Not_Try_To_Set_Primary_If_Not_Template_Type() {
		InstanceType instanceType = InstanceTypeFake.buildForCategory("randomNonTemplateType");
		EmfInstance instance = new EmfInstance();
		instance.setType(instanceType);
		BeforeInstancePersistEvent<?, ?> event = new BeforeInstancePersistEvent(instance);

		templateCreateObserver.onBeforeTemplateCreated(event);
		verify(templateDao, never()).findExistingPrimaryTemplate(anyString(), anyString(), any(), any());
	}

	@Test
	public void should_Set_New_Template_Primary_When_No_Primary_Exists() {
		Instance instance = mockNewlyCreatedTemplate();
		BeforeInstancePersistEvent<?, ?> event = new BeforeInstancePersistEvent(instance);
		mockExistingPrimaryTemplate(false);

		templateCreateObserver.onBeforeTemplateCreated(event);

		verify(templateDao).findExistingPrimaryTemplate(eq("sampleType"), eq("creatable"), any(), any());

		verify(instance).add(eq(IS_PRIMARY_TEMPLATE), eq(Boolean.TRUE));
	}

	@Test
	public void should_Not_Set_New_Template_Primary_When_Primary_Exists() {
		Instance instance = mockNewlyCreatedTemplate();
		BeforeInstancePersistEvent<?, ?> event = new BeforeInstancePersistEvent(instance);
		mockExistingPrimaryTemplate(true);

		templateCreateObserver.onBeforeTemplateCreated(event);

		verify(templateDao).findExistingPrimaryTemplate(eq("sampleType"), eq("creatable"), any(), any());

		verify(instance, never()).add(anyString(), any());
	}

	private static Instance mockNewlyCreatedTemplate() {
		InstanceType instanceType = InstanceTypeFake.buildForCategory(TEMPLATE);
		Instance instance = mock(Instance.class);
		when(instance.getAsString(eq(FOR_OBJECT_TYPE))).thenReturn("sampleType");
		when(instance.getAsString(eq(TEMPLATE_PURPOSE))).thenReturn("creatable");
		when(instance.getBoolean(eq(IS_PRIMARY_TEMPLATE))).thenReturn(Boolean.FALSE);
		when(instance.type()).thenReturn(instanceType);
		return instance;
	}

	private void mockExistingPrimaryTemplate(boolean exists) {
		if (exists) {
			Template primaryTemplate = new Template();
			primaryTemplate.setId("existingPrimaryTemplateId");
			when(templateDao.findExistingPrimaryTemplate(anyString(), anyString(), any(), any()))
					.thenReturn(Optional.of(primaryTemplate));
		} else {
			when(templateDao.findExistingPrimaryTemplate(anyString(), anyString(), any(), any()))
					.thenReturn(Optional.empty());
		}
	}

}
