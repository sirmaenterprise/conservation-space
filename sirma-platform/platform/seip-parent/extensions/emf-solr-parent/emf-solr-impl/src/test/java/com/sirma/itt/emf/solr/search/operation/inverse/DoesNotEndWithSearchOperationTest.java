package com.sirma.itt.emf.solr.search.operation.inverse;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Test class for {@link DoesNotEndWithSearchOperation}.
 *
 * @author Hristo Lungov
 */
public class DoesNotEndWithSearchOperationTest {

    private DoesNotEndWithSearchOperation doesNotEndWithSearchOperation = new DoesNotEndWithSearchOperation();

    @Test
    public void test_IsApplicable() {
        Rule rule = SearchOperationUtils.createRule("title", "string", "does_not_end_with", "123");
        Assert.assertTrue(doesNotEndWithSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("title", "string", "", "123");
        Assert.assertFalse(doesNotEndWithSearchOperation.isApplicable(rule));
    }

    @Test
    public void test_BuildOperationForSingleValue() {
        Rule rule = SearchOperationUtils.createRule("title", "string", "does_not_end_with", "123");
        StringBuilder builder = new StringBuilder();

        doesNotEndWithSearchOperation.buildOperation(builder, rule);

        Assert.assertEquals("-(title:(*123))", builder.toString());
    }

    @Test
    public void test_BuildOperationForMultipleValue() {
        Rule rule = SearchOperationUtils.createRule("title", "string", "does_not_end_with", Arrays.asList("123", "abc"));
        StringBuilder builder = new StringBuilder();

        doesNotEndWithSearchOperation.buildOperation(builder, rule);

        Assert.assertEquals("-(title:(*123) OR title:(*abc))", builder.toString());
    }
}
