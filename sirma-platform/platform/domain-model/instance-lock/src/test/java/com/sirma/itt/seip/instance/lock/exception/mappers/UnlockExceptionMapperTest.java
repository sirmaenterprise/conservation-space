package com.sirma.itt.seip.instance.lock.exception.mappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.exception.UnlockException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link UnlockExceptionMapper}.
 *
 * @author A. Kunchev
 */
public class UnlockExceptionMapperTest {

	private UnlockExceptionMapper mapper;

	@Before
	public void setup() {
		mapper = new UnlockExceptionMapper();
	}

	@Test
	public void toResponse_withoutExceptionMessage() {
		LockInfo info = new LockInfo(new InstanceReferenceMock(), "Batman", new Date(), "info", x -> true);
		Response response = mapper.toResponse(new UnlockException(info, ""));
		Object entity = response.getEntity();
		assertNotNull(entity);
		JsonObject json = JSON.readObject(entity.toString(), Function.identity());
		assertEquals("You cannot unlock this resource, it is locked by Batman", json.getString("message"));
	}

	@Test
	public void toResponse_withExceptionMessage() {
		Response response = mapper.toResponse(new UnlockException(new LockInfo(), "Unlock-exception-msg"));
		Object entity = response.getEntity();
		assertNotNull(entity);
		JsonObject json = JSON.readObject(entity.toString(), Function.identity());
		assertEquals("Unlock-exception-msg", json.getString("message"));
	}

}
