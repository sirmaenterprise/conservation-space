package com.sirma.itt.emf.instance;

import static org.testng.Assert.assertEquals;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.util.EmfTest;

/**
 * Test for InstanceRestService.
 *
 * @author svelikov
 */
@Test
public class InstanceRestServiceTest extends EmfTest {

	private final InstanceRestService service;
	private final TypeConverter typeConverter;

	/** The link service. */
	private LinkService linkService;

	/**
	 * Instantiates a new instance rest service test.
	 */
	@Test
	public InstanceRestServiceTest() {
		service = new InstanceRestService() {
			//
		};

		linkService = Mockito.mock(LinkService.class);
		ReflectionUtils.setField(service, "linkService", linkService);

		typeConverter = createTypeConverter();

		typeConverter.addConverter(String.class, InstanceReference.class,
				new Converter<String, InstanceReference>() {

					@Override
					public InstanceReference convert(String source) {
						DataType type = new DataType();
						if (source.contains(".")) {
							type.setJavaClassName(source);
							type.setName(type.getJavaClass().getSimpleName().toLowerCase());
						} else {
							type.setName(source);
						}
						return new LinkSourceId(null, type);
					}
				});

		ReflectionUtils.setField(service, "typeConverter", typeConverter);
	}

	/**
	 * Detach test.
	 */
	public void detachTest() {
		Response response = service.detach(null);
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());

		String request = "{targetId:'1',targetType:'sectioninstance',linked:[{instanceId:'1',instanceType:'documentinstance'}]}";

		// response = service.detach(request);
	}

	/** Tests removeSemanticLinks when the id is empty. */
	public void removeSemanticLinks_emptyId_returnBadRequest() {
		Response response = service.removeSemanticLinks("", "projectinstance");
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/** Tests removeSemanticLinks when the type is empty. */
	public void removeSemanticLinks_emptyType_returnBadRequest() {
		Response response = service.removeSemanticLinks("testIdentifier", "");
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/** Tests removeSemanticLinks when the id is empty. */
	public void removeSemanticLinks_nullId_returnBadRequest() {
		Response response = service.removeSemanticLinks(null, "projectinstance");
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/** Tests removeSemanticLinks when the type is empty. */
	public void removeSemanticLinks_nullType_returnBadRequest() {
		Response response = service.removeSemanticLinks("testIdentifier", null);
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/**
	 * Tests removeSemanticLinks when the id is persisted.
	 * <p>
	 * <b>!!!</b> The id is not really persisted. The method isIdPersisted(Serializable id) in
	 * InstanceUtils will return <b>true</b> if the id is not null or not tracked. }
	 */
	public void removeSemanticLinks_persistedId_returnOK() {
		Response response = service.removeSemanticLinks("persistedIdentifier", "projectinstance");
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
	}

	/** Tests removeSemanticLinks when the id is not persisted and haven't links. */
	public void removeSemanticLinks_notPersistedNoneLinks_returnOK() {
		SequenceEntityGenerator.registerId("emf:8b98033d-f590-46b5-9df2-549d937bbc12");
		Response response = service.removeSemanticLinks("emf:8b98033d-f590-46b5-9df2-549d937bbc12",
				"projectinstance");
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
	}

	/** Tests removeSemanticLinks when the id is not persisted and have links. */
	public void removeSemanticLinks_notPersistedHaveLinks_returnOK() {
		SequenceEntityGenerator.registerId("testIdentifierInstance1");
		InstanceReference reference1 = Mockito.mock(InstanceReference.class);
		reference1.setIdentifier("testIdentifierInstance1");
		InstanceReference reference2 = Mockito.mock(InstanceReference.class);
		reference2.setIdentifier("testIdentifierInstance2");
		linkService.linkSimple(reference1, reference2, LinkConstants.TREE_PARENT_TO_CHILD,
				LinkConstants.TREE_CHILD_TO_PARENT);
		Response response = service.removeSemanticLinks("testIdentifierInstance1",
				"projectinstance");
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
	}

}
