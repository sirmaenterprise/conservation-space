package com.sirma.itt.seip.eai.model.mapper;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.sirma.itt.seip.eai.model.request.query.QueryEntry;
import com.sirma.itt.seip.eai.model.request.query.RawQuery;
import com.sirma.itt.seip.eai.model.request.query.RawQueryEntry;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.mapper.MapperExtension;

/**
 * Custom mapper for {@link RawQuery} to serialize and deserialize
 * 
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = MapperExtension.NAME, order = 50)
public class RawQueryMapperExtension implements MapperExtension {

	@Override
	public void extend(ObjectMapper mapper) {
		SimpleModule module = new SimpleModule();
		module.addDeserializer(RawQuery.class, new RawQueryDeserializer());
		module.addSerializer(RawQuery.class, new RawQuerySerializer());
		mapper.registerModule(module);
	}

	static class RawQuerySerializer extends JsonSerializer<RawQuery> {

		@Override
		public void serialize(RawQuery value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeStartArray();
			for (QueryEntry element : value.getEntries()) {
				gen.writeObject(element);
			}
			gen.writeEndArray();
		}

	}

	static class RawQueryDeserializer extends JsonDeserializer<RawQuery> {

		@Override
		public RawQuery deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
			RawQuery naturalQuery = new RawQuery();
			ObjectCodec codec = jp.getCodec();

			JsonNode node = jp.getCodec().readTree(jp);
			if (node.getNodeType() == JsonNodeType.ARRAY) {
				for (JsonNode jsonNode : node) {
					parse(jsonNode, naturalQuery, codec);
				}
			}
			return naturalQuery;

		}

		private void parse(JsonNode node, RawQuery naturalQuery, ObjectCodec codec) throws IOException {
			if (node.getNodeType() == JsonNodeType.ARRAY) {
				RawQuery nextGroup = new RawQuery();
				for (JsonNode jsonNode : node) {
					parse(jsonNode, nextGroup, codec);
				}
				naturalQuery.addEntry(nextGroup);
			} else if (node.getNodeType() == JsonNodeType.OBJECT) {
				try (JsonParser traverse = node.traverse()) {
					traverse.setCodec(codec);
					RawQueryEntry entry = traverse.readValueAs(RawQueryEntry.class);
					naturalQuery.addEntry(entry);
				}
			}

		}

	}
}
