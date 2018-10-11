package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Test class for {@link InSearchOperation}.
 *
 * @author Hristo Lungov
 */
public class InSearchOperationTest {

    private InSearchOperation inSearchOperation = new InSearchOperation();

    @Test
    public void test_IsApplicable() {
        Rule rule = SearchOperationUtils.createRule("status", "codelist", "in", Collections.singletonList("1"));
        Assert.assertTrue(inSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("status", "codelist", "", Collections.singletonList("1"));
        Assert.assertFalse(inSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("instanceId", "codelist", "in", Collections.singletonList("1"));
        Assert.assertFalse(inSearchOperation.isApplicable(rule));
    }

    @Test
    public void test_BuildOperationForSingleValue() {
        Rule rule = SearchOperationUtils.createRule("status", "codelist", "in", Collections.singletonList("1"));
        StringBuilder builder = new StringBuilder();

        inSearchOperation.buildOperation(builder, rule);

        Assert.assertEquals("(status:(1))", builder.toString());
    }

    @Test
    public void test_BuildOperationForMultipleValue() {
        Rule rule = SearchOperationUtils.createRule("status", "codelist", "in", Arrays.asList("1", "2"));
        StringBuilder builder = new StringBuilder();

        inSearchOperation.buildOperation(builder, rule);

        Assert.assertEquals("(status:(1) OR status:(2))", builder.toString());
    }
}
