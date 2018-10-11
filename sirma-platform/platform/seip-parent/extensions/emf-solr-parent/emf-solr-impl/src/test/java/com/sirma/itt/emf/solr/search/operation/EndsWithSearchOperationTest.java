package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Test class for {@link EndsWithSearchOperation}.
 *
 * @author Hristo Lungov
 */
public class EndsWithSearchOperationTest {

    private EndsWithSearchOperation endsWithSearchOperation = new EndsWithSearchOperation();

    @Test
    public void test_IsApplicable() {
        Rule rule = SearchOperationUtils.createRule("title", "string", "ends_with", Collections.singletonList("123"));
        Assert.assertTrue(endsWithSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("title", "string", "", Collections.singletonList("123"));
        Assert.assertFalse(endsWithSearchOperation.isApplicable(rule));
    }

    @Test
    public void test_BuildOperationForSingleValue() {
        Rule rule = SearchOperationUtils.createRule("title", "string", "ends_with", Collections.singletonList("123"));
        StringBuilder builder = new StringBuilder();

        endsWithSearchOperation.buildOperation(builder, rule);

        Assert.assertEquals("(title:(*123))", builder.toString());
    }

    @Test
    public void test_BuildOperationForMultipleValue() {
        Rule rule = SearchOperationUtils.createRule("title", "string", "ends_with", Arrays.asList("123", "abc"));
        StringBuilder builder = new StringBuilder();

        endsWithSearchOperation.buildOperation(builder, rule);

        Assert.assertEquals("(title:(*123) OR title:(*abc))", builder.toString());
    }
}
