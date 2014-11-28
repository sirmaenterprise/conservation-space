package example;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.sirma.itt.seip.rest.EntityType;
import com.sirma.itt.seip.rest.IdentityRestClient;
import com.sirma.itt.seip.rest.Instance;
import com.sirma.itt.seip.rest.JsonUtil;
import com.sirma.itt.seip.rest.OperationExecutor;
import com.sirma.itt.seip.rest.Rest;

public class CreateProject {

	public static void main(String[] args) {
		String applicationUrl = "https://cspace-test.sirmaplatform.com:8443/emf";

		IdentityRestClient identityService = new IdentityRestClient();
		String sessionCookie = identityService.login(applicationUrl, "a.mitev", "mitev");

		Map<String, String> properties = new HashMap<>();
		properties.put("title", "Imported project");

		// create Exhibition project (definition id: PRJ10001)
		JsonObject result = OperationExecutor.build(applicationUrl, sessionCookie)
				.createProject("PRJ10001", properties).execute();

		JsonObject response = result.getAsJsonArray("operations").get(0).getAsJsonObject()
				.getAsJsonObject("response");

		Instance project = JsonUtil.toInstace(response);

		// you have to know the template id before fetching it
		// the application performs an ajax request for fetching the available templates
		final String templateId = "14";

		JsonObject template = Rest.get(
				applicationUrl + "/service/document-template/content/" + templateId, sessionCookie)
				.getJson();
		String content = template.get("content").getAsString();

		properties = new HashMap<>();
		properties.put("title", "Imported Book object");

		// create Book domain object (definition id: EO007005);
		// Here are the definition fields -
		// https://cspace-test.sirmaplatform.com:8443/emf/service/administration/definition?type=objectinstance&id=EO007005
		OperationExecutor
				.build(applicationUrl, sessionCookie)
				.createDomainObject(EntityType.PROJECT, project.getId(), "EO007005", content,
						properties).execute();
	}
}
