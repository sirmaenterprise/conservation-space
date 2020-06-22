package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test class for {@link DateRangeSearchOperation}.
 *
 * @author Hristo Lungov
 */
public class DateRangeSearchOperationTest {

    private DateRangeSearchOperation dateRangeSearchOperation = new DateRangeSearchOperation();

    @Test
    public void test_IsApplicable() {
        Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "between", Arrays.asList("date", "date"));
        Assert.assertTrue(dateRangeSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("dateField", "dateTime", "is", Arrays.asList("date", "date"));
        Assert.assertTrue(dateRangeSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("dateField", "dateTime", "within", Arrays.asList("date", "date"));
        Assert.assertTrue(dateRangeSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("dateField", "date", "is", Arrays.asList("date", "date"));
        Assert.assertTrue(dateRangeSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("dateField", "string", "is", Arrays.asList("date", "date"));
        Assert.assertFalse(dateRangeSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("dateField", "dateTime", "", Arrays.asList("date", "date"));
        Assert.assertFalse(dateRangeSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("dateField", "dateTime", "is", "date");
        Assert.assertFalse(dateRangeSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("dateField", "dateTime", "is", Collections.emptyList());
        Assert.assertFalse(dateRangeSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("dateField", "dateTime", "is");
        Assert.assertFalse(dateRangeSearchOperation.isApplicable(rule));
    }

    @Test
    public void test_DateRange() {
        List<String> dates = Arrays.asList("from", "to");
        Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "is", dates);
        StringBuilder builder = new StringBuilder();
        dateRangeSearchOperation.buildOperation(builder, rule);
        Assert.assertEquals("dateField:[from TO to]", builder.toString());
    }

    @Test
    public void test_DateRangeWithEmptyDate() {
        // To is empty
        List<String> dates = Arrays.asList("from", "");
        Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "within", dates);
        StringBuilder builder = new StringBuilder();

        dateRangeSearchOperation.buildOperation(builder, rule);

        Assert.assertEquals("dateField:[from TO *]", builder.toString());

        // From is empty
        builder = new StringBuilder();
        dates = Arrays.asList("", "to");
        rule = SearchOperationUtils.createRule("dateField", "dateTime", "within", dates);

        dateRangeSearchOperation.buildOperation(builder, rule);

        Assert.assertEquals("dateField:[* TO to]", builder.toString());
    }
}
