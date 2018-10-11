package com.sirma.itt.seip.instance.actions.evaluation;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

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
public class ActionsGroupMessageBodyWriter implements MessageBodyWriter<ActionMenu> {

	@Override
	public long getSize(ActionMenu arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		return ActionMenu.class.isAssignableFrom(clazz);
	}

	@Override
	public void writeTo(ActionMenu menu, Class<?> clazz, Type type, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> headers, OutputStream stream) throws IOException {
		if (menu != null) {
			ActionMenuJsonGenerator generatedMenu = new ActionMenuJsonGenerator(menu);
			stream.write(generatedMenu.getJsonMenu().toString().getBytes());
		}
	}

	/**
	 * Inner class for transforming {@link ActionMenu} to {@link JsonValue}. Act as {@link ActionMenu} visitor.
	 *
	 * @author T. Dossev
	 */
	private class ActionMenuJsonGenerator implements Visitor {

		private JsonBuilderFactory factoryBuilder;
		private ActionMenu menu;

		public ActionMenuJsonGenerator(ActionMenu menu) {
			this.menu = menu;
			this.factoryBuilder = Json.createBuilderFactory(null);
		}

		@SuppressWarnings("unchecked")
		private JsonValue toJson(List<ActionMenu> groupItems, JsonArrayBuilder builder) {
			for(ActionMenu item : groupItems) {
				visitMenu(item);
				if (visitMenuMembers(item).isEmpty()) {
					builder.add((JsonValue) visitMenuMember(item).toJsonHelper());
				} else {
					builder.add(((JsonObjectBuilder) visitMenuMember(item).toJsonHelper())
							.add("data",
									toJson(visitMenuMembers(item), factoryBuilder.createArrayBuilder())));
				}
			}
			return builder.build();
		}

		public JsonValue getJsonMenu() {
			menu.acceptVisitor(this);
			List<ActionMenu> menuMembers = menu.getMenuMembers(this);
			return toJson(menuMembers, factoryBuilder.createArrayBuilder());
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
