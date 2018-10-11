package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * Test class for {@link DateBeforeSearchOperation}.
 *
 * @author Hristo Lungov
 */
public class DateBeforeSearchOperationTest {

    private DateBeforeSearchOperation dateBeforeSearchOperation = new DateBeforeSearchOperation();

    @Test
    public void test_IsApplicable() {
        Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "before", "date");
        Assert.assertTrue(dateBeforeSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("dateField", "dateTime", "after", "date");
        Assert.assertFalse(dateBeforeSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("dateField", "dateTime", "", "date");
        Assert.assertFalse(dateBeforeSearchOperation.isApplicable(rule));
    }

    @Test
    public void test_buildOperation() {
        Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "before", "date");
        StringBuilder builder = new StringBuilder();

        dateBeforeSearchOperation.buildOperation(builder, rule);
        Assert.assertEquals("(dateField:([* TO date]))", builder.toString());
    }
}
