package com.sirma.itt.seip.instance.actions.evaluation;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.sep.instance.actions.group.ActionMenu;
import com.sirma.sep.instance.actions.group.ActionMenuMember;
import com.sirma.sep.instance.actions.group.VisitableMenu;
import com.sirma.sep.instance.actions.group.Visitor;

/**
 * Writer for response from request for instance actions. Converts collection of actions to {@link JsonArray} with the
 * actions as elements.
 *
 * @author T. Dossev
 */
@Provider
@Produces(Versions.V2_JSON)
public class ActionsGroupMessageBodyWriter extends AbstractMessageBodyWriter<ActionMenu> {

	@Override
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		return ActionMenu.class.isAssignableFrom(clazz);
	}

	@Override
	public void writeTo(ActionMenu menu, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> headers, OutputStream stream) throws IOException {
		if (menu != null) {
			stream.write(new ActionMenuJsonGenerator(menu).getJsonMenu().toString().getBytes(StandardCharsets.UTF_8));
		}
	}

	/**
	 * Inner class for transforming {@link ActionMenu} to {@link JsonArray}. Act as {@link ActionMenu} visitor.
	 *
	 * @author T. Dossev
	 */
	private class ActionMenuJsonGenerator implements Visitor {

		private static final String DATA = "data";
		private ActionMenu menu;

		public ActionMenuJsonGenerator(ActionMenu menu) {
			this.menu = menu;
		}

		public JsonArray getJsonMenu() {
			menu.acceptVisitor(this);
			List<ActionMenu> menuMembers = menu.getMenuMembers(this);
			return toJson(menuMembers, Json.createArrayBuilder());
		}

		private JsonArray toJson(List<ActionMenu> groupItems, JsonArrayBuilder builder) {
			for (ActionMenu item : groupItems) {
				visitMenu(item);
				List<ActionMenu> visitMenuMembers = visitMenuMembers(item);
				JsonObjectBuilder visitMenuMemberJson = visitMenuMember(item).toJsonHelper();
				if (visitMenuMembers.isEmpty()) {
					builder.add(visitMenuMemberJson);
				} else {
					builder.add(visitMenuMemberJson.add(DATA, toJson(visitMenuMembers, Json.createArrayBuilder())));
				}
			}
			return builder.build();
		}

		@Override
		public void visitMenu(VisitableMenu menuToVisit) {
			menuToVisit.acceptVisitor(this);
		}

		@Override
		public List<ActionMenu> visitMenuMembers(VisitableMenu menuToVisit) {
			return menuToVisit.getMenuMembers(this);
		}

		@Override
		public ActionMenuMember visitMenuMember(VisitableMenu menuToVisit) {
			return menuToVisit.getMenuMember(this);
		}
	}
}