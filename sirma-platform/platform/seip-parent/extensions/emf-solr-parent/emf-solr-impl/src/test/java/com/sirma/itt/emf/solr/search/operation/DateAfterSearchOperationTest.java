package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * Test class for {@link DateAfterSearchOperation}.
 *
 * @author Hristo Lungov
 */
public class DateAfterSearchOperationTest {

    private DateAfterSearchOperation dateAfterSearchOperation = new DateAfterSearchOperation();

    @Test
    public void test_IsApplicable() {
        Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "after", "date");
        Assert.assertTrue(dateAfterSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("dateField", "dateTime", "before", "date");
        Assert.assertFalse(dateAfterSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("dateField", "dateTime", "", "date");
        Assert.assertFalse(dateAfterSearchOperation.isApplicable(rule));
    }

    @Test
    public void test_buildOperation() {
        Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "after", "date");
        StringBuilder builder = new StringBuilder();

        dateAfterSearchOperation.buildOperation(builder, rule);
        Assert.assertEquals("(dateField:([date TO *]))", builder.toString());
    }
}
