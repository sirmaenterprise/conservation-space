package com.sirma.sep.model.management.exception.mappers;

import com.sirma.sep.model.management.DeploymentValidationReport;
import com.sirma.sep.model.management.exception.UpdateModelFailed;
import net.javacrumbs.jsonunit.JsonAssert;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for {@link UpdateModelFailedMapper}
 *
 * @author Radoslav Dimitrov
 */
public class UpdateModelFailedMapperTest {

	@Test
	public void should_Handle_Exception_With_DeploymentValidationReport() {
		DeploymentValidationReport report = new DeploymentValidationReport();
		report.failedDeploymentValidationFor("invalid_node", Collections.singletonList("Error!"));
		Response response = new UpdateModelFailedMapper().toResponse(new UpdateModelFailed(report));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		JsonAssert.assertJsonEquals(
				"{\"genericErrors\":[],\"nodes\":[{\"id\":\"invalid_node\",\"messages\":[{\"severity\":\"ERROR\",\"message\":\"Error!\"}],\"valid\":false}],\"version\":0,\"valid\":false}",
				response.getEntity());
	}
}
