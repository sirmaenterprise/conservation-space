package com.sirma.itt.seip.instance.lock;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.LOCKED_BY;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Predicate;

import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.Converter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.rest.handlers.writers.InstanceToJsonSerializer;
import com.sirma.itt.seip.rest.handlers.writers.PropertiesFilterBuilder;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.seip.time.ISO8601DateFormat;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test for {@link LockInfoConverterProvider}
 *
 * @author BBonev
 */
public class LockInfoConverterProviderTest {

	@InjectMocks
	private LockInfoConverterProvider converterProvider;
	@Mock
	private ResourceService resourceService;
	@Mock
	private TypeConverter typeConverter;
	@Mock
	private InstanceToJsonSerializer instanceSerializer;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(resourceService.findResource("emf:user")).thenReturn(new EmfUser("user"));
		when(typeConverter.convert(eq(String.class), any(Date.class)))
				.then(a -> ISO8601DateFormat.format(a.getArgumentAt(1, Date.class)));

		// for json conversion
		doAnswer(a -> {
			Instance instance = a.getArgumentAt(0, Instance.class);
			DefinitionMock mock = new DefinitionMock();
			PropertyDefinitionMock prop = new PropertyDefinitionMock();
			prop.setName("userId");
			mock.getFields().add(prop);
			Predicate<String> filter = a.getArgumentAt(1, PropertiesFilterBuilder.class).buildFilter(mock);
			JsonGenerator generator = a.getArgumentAt(2, JsonGenerator.class);
			generator.writeStartObject().writeStartObject("properties");
			instance.getOrCreateProperties().entrySet().stream().filter(entry -> filter.test(entry.getKey())).forEach(
					entry -> generator.write(entry.getKey(), entry.getValue().toString()));
			generator.writeEnd().writeEnd();
			return null;
		}).when(instanceSerializer).serialize(any(Instance.class), any(PropertiesFilterBuilder.class),
				any(JsonGenerator.class));

		// for string conversion
		doAnswer(a -> {
			Instance instance = a.getArgumentAt(1, Instance.class);
			DefinitionMock mock = new DefinitionMock();
			PropertyDefinitionMock prop = new PropertyDefinitionMock();
			prop.setName("userId");
			mock.getFields().add(prop);
			Predicate<String> filter = a.getArgumentAt(2, PropertiesFilterBuilder.class).buildFilter(mock);
			JsonGenerator generator = a.getArgumentAt(3, JsonGenerator.class);
			generator.writeStartObject(a.getArgumentAt(0, String.class)).writeStartObject("properties");
			instance.getOrCreateProperties().entrySet().stream().filter(entry -> filter.test(entry.getKey())).forEach(
					entry -> generator.write(entry.getKey(), entry.getValue().toString()));
			generator.writeEnd().writeEnd();
			return null;
		}).when(instanceSerializer).serialize(eq(LOCKED_BY), any(Instance.class), any(PropertiesFilterBuilder.class),
				any(JsonGenerator.class));
	}

	@Test
	@SuppressWarnings({"unchecked" })
	public void testConvertToString() throws Exception {
		converterProvider.register(typeConverter);
		ArgumentCaptor<Converter> captor = ArgumentCaptor.forClass(Converter.class);
		verify(typeConverter).addConverter(eq(LockInfo.class), eq(String.class), captor.capture());
		//setting required timeZone
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		Calendar calendar = Calendar.getInstance();
		calendar.set(2016, Calendar.AUGUST, 1, 13, 5, 50);
		calendar.set(Calendar.MILLISECOND, 0);
		Object result = captor
				.getValue()
					.convert(new LockInfo(null, "emf:user", calendar.getTime(), "test lock", id -> true));
		JsonAssert.assertJsonEquals(ResourceLoadUtil.loadResource(getClass(), "lock-info-locked.json"), result);
	}

	@Test
	@SuppressWarnings({ "unchecked" })
	public void testConvertToJson() throws Exception {
		converterProvider.register(typeConverter);
		ArgumentCaptor<Converter> captor = ArgumentCaptor.forClass(Converter.class);
		verify(typeConverter).addConverter(eq(LockInfo.class), eq(JsonValue.class), captor.capture());
		Object result = captor.getValue().convert(new LockInfo());
		JsonAssert.assertJsonEquals(ResourceLoadUtil.loadResource(getClass(), "lock-info-not-locked.json"),
				result.toString());
	}

}
