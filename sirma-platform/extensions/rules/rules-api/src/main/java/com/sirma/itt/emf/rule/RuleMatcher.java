package com.sirma.itt.emf.rule;

import com.sirma.itt.seip.Applicable;
import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.context.Configurable;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Interface that provides means to match/compare instance in complex {@link InstanceRule}s. The processing (fixed)
 * instance is located in the content with other information based on the rule. The instance that is passed to the match
 * method is the instance that should be matched against. For a single rule activation this instance will change with
 * every call. The last argument is an execution context, unique for the thread that processes the provided instances.
 * Could be used for a cache between calls of the same matcher or between matchers
 *
 * @author BBonev
 */
public interface RuleMatcher extends Named, Configurable, DynamicSupportable, Applicable {

	/**
	 * Configuration property of type collection that defines the fields that need to be checked/searched in
	 */
	String CHECK_FOR_PROPERTIES = "checkForProperties";

	/** Configuration property of type collection that defines the fields that need to be searched */
	String SEARCH_IN_PROPERTIES = "searchInProperties";

	/** Boolean property to activate or deactivate case matching. */
	String IGNORE_CASE = "ignoreCase";
	/**
	 * Boolean property to control if the searching should be exact compare or partial. For properties only.
	 */
	String CONTAINS = "contains";

	/**
	 * Boolean property to control of the searched words should be treated exactly as they are or some non work
	 * characters are allowed between them. <br>
	 * <table border="1">
	 * <thead>
	 * <tr>
	 * <th>Input</th>
	 * <th>Match against</th>
	 * <th><strong>exactMatch</strong></th>
	 * <th>Expected result</th>
	 * </tr>
	 * </thead><tbody>
	 * <tr>
	 * <td>test value</td>
	 * <td>test value</td>
	 * <td>true</td>
	 * <td>true</td>
	 * </tr>
	 * <tr>
	 * <td>test value</td>
	 * <td>test value</td>
	 * <td>false</td>
	 * <td>true</td>
	 * </tr>
	 * <tr>
	 * <td>test value</td>
	 * <td>test <span style="color: rgb(255,0,0);">-</span> value</td>
	 * <td>true</td>
	 * <td>false</td>
	 * </tr>
	 * <tr>
	 * <td>test value</td>
	 * <td>test <span style="color: rgb(255,0,0);">-</span><span style="color: rgb(255,0,0);"
	 * >{}{}{^&amp;%$%^&amp;*()</span> value</td>
	 * <td>false</td>
	 * <td>true</td>
	 * </tr>
	 * <tr>
	 * <td>test value</td>
	 * <td>test<span style="color: rgb(255,0,0);">s</span> - value</td>
	 * <td>true</td>
	 * <td>false</td>
	 * </tr>
	 * <tr>
	 * <td>test</td>
	 * <td>test value</td>
	 * <td>true</td>
	 * <td>true</td>
	 * </tr>
	 * <tr>
	 * <td>test</td>
	 * <td>test<span style="color: rgb(255,0,0);">s</span> value</td>
	 * <td>true</td>
	 * <td>false</td>
	 * </tr>
	 * <tr>
	 * <td>test</td>
	 * <td>test<span style="color: rgb(255,0,0);">s</span> value</td>
	 * <td>false</td>
	 * <td>false</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 */
	String EXACT_MATCH = "exactMatch";

	/**
	 * Boolean configuration that instructs that the matching should be inverted. For example the general case is when
	 * comparing properties the property in the current instance is search in the instance coming via query provider.
	 * The inverted case is when searching properties of instances returned by the query provider in the current
	 * instance. Default value is <code>false</code>.
	 */
	String INVERT_MATCHING = "invertMatching";

	/**
	 * Integer property to configure a minimal length for the searched property. If less than or equal to 0 the property
	 * length will be ignored. Otherwise properties with length less than configured will be ignored.
	 */
	String MINIMAL_LENGTH = "minimalLength";

	/**
	 * The invert the matcher result. This is useful to implement a check if a matcher does not match now. Default value
	 * is <code>false</code> and the result will NOT be inverted.
	 */
	String INVERT = "invert";

	/**
	 * Match the information located in the processing context with the given instance.
	 *
	 * @param processingContext
	 *            the processing context (shared with all processing threads). Modification to this context may cause a
	 *            concurrent modification exception.
	 * @param instanceToMatch
	 *            the instance to match
	 * @param context
	 *            the context that is unique for the current processing thread.
	 * @return true, if matched successfully
	 */
	boolean match(Context<String, Object> processingContext, Instance instanceToMatch, Context<String, Object> context);

}
