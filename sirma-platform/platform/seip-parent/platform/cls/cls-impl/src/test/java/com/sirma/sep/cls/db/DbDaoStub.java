package com.sirma.sep.cls.db;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.sep.cls.db.entity.CodeListEntity;
import com.sirma.sep.cls.db.entity.CodeValueEntity;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper utility to stub the wrapped {@link org.mockito.Mock} of {@link DbDao} with {@link CodeListEntity} and {@link CodeValueEntity}.
 *
 * @author Mihail Radkov
 */
public class DbDaoStub {

	private final DbDao dbDaoMock;

	private final Map<String, Object> lists = new HashMap<>();

	private final Map<String, List<Object>> values = new HashMap<>();

	/**
	 * Constructs the stub utility with the provided {@link org.mockito.Mock} for {@link DbDao}.
	 *
	 * @param dbDaoMock
	 * 		the DAO to stub
	 */
	public DbDaoStub(DbDao dbDaoMock) {
		this.dbDaoMock = dbDaoMock;
	}

	public CodeListEntity withListEntity(String value, String... descriptions) {
		CodeListEntity listEntity = CodeEntityTestUtils.getListEntity(value, descriptions);
		withListEntity(listEntity);
		return listEntity;
	}

	public CodeValueEntity withValueEntityForList(String listValue, String value, String... descriptions) {
		CodeValueEntity valueEntity = CodeEntityTestUtils.getValueEntity(listValue, value, descriptions);
		withValueEntityForList(valueEntity);
		return valueEntity;
	}

	public void withListEntity(CodeListEntity... listEntities) {
		Arrays.stream(listEntities).forEach(listEntity -> {
			lists.put(listEntity.getValue(), listEntity);
			// Stub get all
			Mockito.when(dbDaoMock.fetchWithNamed(Matchers.eq(CodeListEntity.QUERY_ALL_CODELISTS_KEY), Matchers.any()))
				   .thenReturn(new ArrayList<>(lists.values()));
			// Stub get by value
			Mockito.when(dbDaoMock.fetchWithNamed(Matchers.eq(CodeListEntity.QUERY_CODELIST_BY_VALUE_KEY), Matchers.any()))
				   .thenAnswer(invocation -> {
					   List queryParameters = invocation.getArgumentAt(1, List.class);
					   Pair codeListIdParam = (Pair) queryParameters.get(0);
					   Object loadedList = lists.get(codeListIdParam.getSecond().toString());
					   if (loadedList == null) {
						   return Collections.emptyList();
					   }
					   return Collections.singletonList(loadedList);
				   });
		});
	}

	public void withValueEntityForList(CodeValueEntity... valueEntities) {
		Arrays.stream(valueEntities).forEach(valueEntity -> {
			values.computeIfAbsent(valueEntity.getCodeListId(), k -> new ArrayList<>()).add(valueEntity);
			// Stub get all from specific list
			Mockito.when(dbDaoMock.fetchWithNamed(Matchers.eq(CodeValueEntity.QUERY_VALUES_BY_CL_ID_KEY), Matchers.any()))
				   .thenAnswer(invocation -> {
					   List queryParameters = invocation.getArgumentAt(1, List.class);
					   Pair codeListIdParam = (Pair) queryParameters.get(0);
					   return values.getOrDefault(codeListIdParam.getSecond().toString(), new ArrayList<>());
				   });
			// Stub get specific value
			Mockito.when(dbDaoMock.fetchWithNamed(Matchers.eq(CodeValueEntity.QUERY_VALUE_BY_VALUE_AND_CL_ID_KEY), Matchers.any()))
				   .thenAnswer(invocation -> {
					   List queryParameters = invocation.getArgumentAt(1, List.class);
					   Pair codeListIdParam = (Pair) queryParameters.get(0);
					   Pair codeValueParam = (Pair) queryParameters.get(1);
					   return values.getOrDefault(codeListIdParam.getSecond().toString(), new ArrayList<>())
									.stream()
									.filter(ve -> codeValueParam.getSecond().toString().equals(((CodeValueEntity) ve).getValue()))
									.collect(Collectors.toList());
				   });
		});
	}
}
