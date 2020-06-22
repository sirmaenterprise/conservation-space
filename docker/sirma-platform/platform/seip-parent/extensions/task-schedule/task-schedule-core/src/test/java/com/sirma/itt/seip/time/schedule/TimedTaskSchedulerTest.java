package com.sirma.itt.seip.time.schedule;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TimedTaskScheduler;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.ControlDefintionMock;
import com.sirma.itt.seip.testutil.mocks.ControlParamMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Unit tests for {@link TimedTaskScheduler}
 *
 * @author Valeri Tishev
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TimedTaskSchedulerTest {

	@InjectMocks
	private TimedTaskScheduler CUT;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private SchedulerService schedulerService;

	@Mock
	private TypeConverter typeConverterMock;

	@Mock
	private SystemConfiguration configurations;
	/**
	 * Initialize the class under test (CUT)
	 */
	@Before
	public void init() {
		CUT = new TimedTaskScheduler();
		MockitoAnnotations.initMocks(this);
		Mockito.when(configurations.getTimeZoneID()).thenReturn(new ConfigurationPropertyMock<>(ZoneId.systemDefault()));
		Mockito.when(definitionService.getAllDefinitions(GenericDefinition.class)).thenReturn(mockGenericDefinitions());
		Mockito.when(typeConverterMock.convert(eq(String.class), any(Date.class))).thenReturn("");
		TypeConverterUtil.setTypeConverter(typeConverterMock);
	}

	@Test
	public void scheduleTimedTasks() {
		CUT.scheduleTimedTasks(new DefinitionsChangedEvent());

		// verify scheduling only for ObjectType.TIMER definitions
		// refer to mockGenericDefinitions method documentation
		verify(schedulerService, times(1)).schedule(anyString(), any(), any());
	}

	/**
	 * Creates a {@link List} of {@link GenericDefinition}
	 * containing only one "timer" definition
	 *
	 * @return a mocked {@link List} of {@link GenericDefinition}
	 */
	private List<DefinitionModel> mockGenericDefinitions() {
		List<DefinitionModel> genericDefinitions = new ArrayList<>();

		// mock a "query" generic definition
		DefinitionMock queryDefinition = new DefinitionMock();
		queryDefinition.setType("QUERY");
		genericDefinitions.add(queryDefinition);

		// mock a "dashlet" generic definition
		DefinitionMock dashletDefinition = new DefinitionMock();
		dashletDefinition.setType("DASHLET");
		genericDefinitions.add(dashletDefinition);

		// mock a "timer" generic definition
		DefinitionMock timerDefinition = new DefinitionMock();
		timerDefinition.setType("TIMER");
		timerDefinition.setFields(mockPropertyDefinition());
		genericDefinitions.add(timerDefinition);

		return genericDefinitions;
	}

	/**
	 * Creates a {@link List} of {@link PropertyDefinition}
	 * containing a single property definition of a timer
	 * with only mandatory fields defined
	 *
	 * @return a mocked {@link List} of {@link PropertyDefinition}
	 */
	private static List<PropertyDefinition> mockPropertyDefinition() {
		ControlParamMock controlParamMock = new ControlParamMock();
		controlParamMock.setName("cronExpression");
		controlParamMock.setValue("0 0 0 1/1 * ? *");

		List<ControlParam> controlParams = new ArrayList<>();
		controlParams.add(controlParamMock);

		ControlDefintionMock controlDefintionMock = new ControlDefintionMock();
		controlDefintionMock.setControlParams(controlParams);

		PropertyDefinitionMock propertyDefinitionMock = new PropertyDefinitionMock();
		propertyDefinitionMock.setControlDefinition(controlDefintionMock);

		List<PropertyDefinition> propertyDefinitions = new ArrayList<>(1);
		propertyDefinitions.add(propertyDefinitionMock);
		return propertyDefinitions;
	}
}
