package com.sirma.itt.seip.instance.actions.clone;

import com.google.gson.JsonObject;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.resources.instances.InstanceResourceParser;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link InstanceCloneRequestBodyReader}
 * <p>
 * Created by Ivo Rusev on 20.12.2016 г..
 */
public class InstanceCloneRequestBodyReaderTest {

    private static final String EMF_ID = "emf:id";
    private static final String EMF_TITLE = "this-is-already-cloned-instance";

    @Mock
    private MultivaluedMap<String, String> paramsMap;
    @Mock
    private UriInfo uriInfo;
    @Mock
    private RequestInfo request;
    @Mock
    private InstanceResourceParser parser;

    @InjectMocks
    private InstanceCloneRequestBodyReader reader = new InstanceCloneRequestBodyReader();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(paramsMap.get("id")).thenReturn(Arrays.asList(EMF_ID));
        when(uriInfo.getPathParameters()).thenReturn(paramsMap);
        when(request.getUriInfo()).thenReturn(uriInfo);
    }

    @Test
    public void test_isReadable_false() {
        assertFalse(reader.isReadable(String.class, null, null, null));
    }

    @Test
    public void test_isReadable_true() {
        assertTrue(reader.isReadable(InstanceCloneRequest.class, null, null, null));
    }


    @Test(expected = BadRequestException.class)
    public void test_readFrom_badRequest() {
        BufferedInputStream stream = new BufferedInputStream(
                new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)));
        reader.readFrom(InstanceCloneRequest.class, null, null, null, null, stream);
    }

    @Test
    public void test_readFrom() {
        mockInstanceResourceParser();

        JsonObject json = new JsonObject();
        json.addProperty(JsonKeys.USER_OPERATION, InstanceCloneRequest.OPERATION_NAME);
        json.add(JsonKeys.TARGET_INSTANCE, new JsonObject());

        InputStream stream = new ByteArrayInputStream(json.toString().getBytes(StandardCharsets.UTF_8));

        InstanceCloneRequest request = reader.readFrom(InstanceCloneRequest.class, null, null, null,
                null, stream);
        Instance clonedInstance = request.getClonedInstance();

        assertEquals(clonedInstance.get("id"), EMF_ID);
        assertEquals(clonedInstance.get("title"), EMF_TITLE);
        assertEquals(request.getTargetId(), EMF_ID);
        assertEquals(request.getOperation(), InstanceCloneRequest.OPERATION_NAME);
    }

    private void mockInstanceResourceParser() {
        TypeConverterUtil.setTypeConverter(Mockito.mock(TypeConverter.class));
        Instance instance = new EmfInstance();
        instance.add("title", EMF_TITLE);
        instance.add("id", EMF_ID);
        Mockito.when(parser.toInstance(Matchers.any(javax.json.JsonObject.class), Matchers.anyString()))
                .thenReturn(instance);
    }

}
