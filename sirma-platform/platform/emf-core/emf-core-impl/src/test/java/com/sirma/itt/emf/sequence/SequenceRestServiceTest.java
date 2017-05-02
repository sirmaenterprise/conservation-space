package com.sirma.itt.emf.sequence;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.sequence.entity.SequenceEntity;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.rest.RestUtil;

/**
 * Test for SequenceRestService
 *
 * @author BBonev
 */
@Test
public class SequenceRestServiceTest extends BaseRestTest {

	/** The generator service. */
	@Mock
	private SequenceGeneratorService generatorService;

	/** The type converter. */
	@Mock
	private TypeConverter typeConverter;

	/** The rest service. */
	@InjectMocks
	private SequenceRestService restService;

	/**
	 * Before method.
	 */
	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();
		when(typeConverter.convert(eq(JSONObject.class), anyCollectionOf(Sequence.class)))
				.then(new Answer<Collection<JSONObject>>() {

					@SuppressWarnings("unchecked")
					@Override
					public Collection<JSONObject> answer(InvocationOnMock invocation) throws Throwable {
						List<JSONObject> list = new LinkedList<>();
						Collection<? extends JsonRepresentable> object = (Collection<? extends JsonRepresentable>) invocation
								.getArguments()[1];
						for (JsonRepresentable representable : object) {
							list.add(representable.toJSONObject());
						}
						return list;
					}
				});
		when(typeConverter.convert(eq(JSONObject.class), any(Sequence.class))).then(new Answer<JSONObject>() {

			@Override
			public JSONObject answer(InvocationOnMock invocation) throws Throwable {
				JsonRepresentable object = (JsonRepresentable) invocation.getArguments()[1];
				return object.toJSONObject();
			}
		});
		when(typeConverter.convert(eq(Sequence.class), any(JSONObject.class))).then(new Answer<Sequence>() {

			@Override
			public Sequence answer(InvocationOnMock invocation) throws Throwable {
				SequenceEntity entity = new SequenceEntity();
				entity.fromJSONObject((JSONObject) invocation.getArguments()[1]);
				return entity;
			}
		});
	}

	/**
	 * Test list all.
	 */
	@Test
	public void testListAll() {
		Response response = restService.listAll();
		readOkData(response, 0);

		verify(generatorService).listAll();

		SequenceEntity sequence = createSequence("test", 2);
		when(generatorService.listAll()).thenReturn(Collections.<Sequence> singleton(sequence));

		response = restService.listAll();
		JSONArray array = readOkData(response, 1);
		JSONObject object = array.optJSONObject(0);
		sequence = new SequenceEntity();
		sequence.fromJSONObject(object);
		AssertJUnit.assertEquals(sequence.getSequenceId(), "test");
		AssertJUnit.assertEquals(sequence.getSequence(), Long.valueOf(2L));

		verify(generatorService, atLeast(2)).listAll();
	}

	/**
	 * Creates the sequence.
	 *
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 * @return the sequence entity
	 */
	private SequenceEntity createSequence(String name, long value) {
		SequenceEntity sequence = new SequenceEntity();
		sequence.setSequenceId(name);
		sequence.setSequence(value);
		return sequence;
	}

	/**
	 * Test get.
	 */
	@Test
	public void testGet() {
		Response response = restService.get("test");
		readErrorResponse(response, Status.NOT_FOUND);

		SequenceEntity sequence = createSequence("test", 4);
		when(generatorService.getSequence("test")).thenReturn(sequence);

		response = restService.get("test");
		JSONArray data = readOkData(response, 1);
		sequence = new SequenceEntity();
		sequence.fromJSONObject(data.optJSONObject(0));
		AssertJUnit.assertEquals(sequence.getSequenceId(), "test");
		AssertJUnit.assertEquals(sequence.getSequence(), Long.valueOf(4L));

		verify(generatorService, atLeast(2)).getSequence("test");
	}

	/**
	 * Test get next.
	 */
	@Test
	public void testGetNext() {
		SequenceEntity sequence = createSequence("test", 4);
		when(generatorService.incrementSequence("test")).thenReturn(sequence);

		Response response = restService.getNextSequence("test");

		JSONArray data = readOkData(response, 1);
		sequence = new SequenceEntity();
		sequence.fromJSONObject(data.optJSONObject(0));
		AssertJUnit.assertEquals(sequence.getSequenceId(), "test");
		AssertJUnit.assertEquals(sequence.getSequence(), Long.valueOf(4L));

		verify(generatorService).incrementSequence("test");
	}

	@Test
	public void testInitializeSingle() {
		SequenceEntity sequence = createSequence("test", 2);
		Response response = restService.initializeSequence("{}");
		readErrorResponse(response, Status.BAD_REQUEST);

		String requestData = RestUtil
				.buildDataEntity(sequence.toJSONObject(), RestUtil.DEFAULT_DATA_PROPERTY)
					.toString();

		when(generatorService.getCurrentId("test")).thenReturn(0L);
		when(generatorService.getSequence("test")).thenReturn(sequence);

		response = restService.initializeSequence(requestData);
		JSONArray data = readOkData(response, 1);

		sequence.fromJSONObject(data.optJSONObject(0));
		AssertJUnit.assertEquals(sequence.getSequenceId(), "test");
		AssertJUnit.assertEquals(sequence.getSequence(), Long.valueOf(2L));

		verify(generatorService).resetSequenceTo("test", 2L);
	}

	/**
	 * Test initialize multiple.
	 */
	public void testInitializeMultiple() {
		Response response = restService.initializeSequence("{}");
		readErrorResponse(response, Status.BAD_REQUEST);

		SequenceEntity sequence1 = createSequence("test1", 3);
		SequenceEntity sequence2 = createSequence("test2", 5);
		Collection<JSONObject> objects = new ArrayList<>(2);
		objects.add(sequence1.toJSONObject());
		objects.add(sequence2.toJSONObject());
		String requestData = RestUtil
				.buildDataEntity(new JSONArray(objects), RestUtil.DEFAULT_DATA_PROPERTY)
					.toString();

		when(generatorService.getCurrentId(anyString())).thenReturn(0L);
		when(generatorService.getSequence("test1")).thenReturn(sequence1);
		when(generatorService.getSequence("test2")).thenReturn(sequence2);

		response = restService.initializeSequence(requestData);
		JSONArray data = readOkData(response, 2);

		SequenceEntity sequence = new SequenceEntity();
		sequence.fromJSONObject(data.optJSONObject(0));
		AssertJUnit.assertEquals(sequence.getSequenceId(), "test1");
		AssertJUnit.assertEquals(sequence.getSequence(), Long.valueOf(3L));
		sequence.fromJSONObject(data.optJSONObject(1));
		AssertJUnit.assertEquals(sequence.getSequenceId(), "test2");
		AssertJUnit.assertEquals(sequence.getSequence(), Long.valueOf(5L));

		verify(generatorService, atLeast(2)).resetSequenceTo(anyString(), anyLong());
	}

	/**
	 * Test initialize single existing.
	 */
	@Test
	public void testInitializeSingleExisting() {
		SequenceEntity sequence = createSequence("test", 5);
		Response response = restService.initializeSequence("{}");
		readErrorResponse(response, Status.BAD_REQUEST);

		String requestData = RestUtil
				.buildDataEntity(sequence.toJSONObject(), RestUtil.DEFAULT_DATA_PROPERTY)
					.toString();

		when(generatorService.getCurrentId("test")).thenReturn(2L);
		when(generatorService.getSequence("test")).thenReturn(createSequence("test", 2));

		response = restService.initializeSequence(requestData);
		JSONArray data = readOkData(response, 1);

		sequence.fromJSONObject(data.optJSONObject(0));
		AssertJUnit.assertEquals(sequence.getSequenceId(), "test");
		AssertJUnit.assertEquals(sequence.getSequence(), Long.valueOf(2L));

		verify(generatorService, never()).resetSequenceTo(anyString(), anyLong());
	}
}
