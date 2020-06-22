package com.sirma.itt.emf.rule;

import com.sirma.itt.seip.Applicable;
import com.sirma.itt.seip.AsyncSupportable;
import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.context.Configurable;
import com.sirma.itt.seip.plugin.SupportablePlugin;

/**
 * Interface that provides means to check an instance if is applicable for execution of an {@link InstanceRule} before
 * the rule to be triggered. This step is intended to limit the rule activations by checking if some data is present or
 * not in the instance or related instances before running the rule with the complete set of data.
 * <p>
 * The preconditions will be checked twice. The first time are checked before rule activation in the same thread that
 * called the trigger method. This invocation should be as fast as possible. The rules invoked are the one that return
 * {@link #isAsyncSupported()} <code>false</code> For example no queries should be performed in this step. <br>
 * The second invocation happens when the rule is run in a separate thread but before the rule execution. This checks
 * are allowed to perform more slow operations like checking for existing relations in the database or running custom
 * queries to check for data. For this invocations are selected preconditions that returns <code>true</code> from the
 * method {@link #isAsyncSupported()} method.
 * <p>
 * If there are more then one precondition attached to a rule the first condition that fails will stop the execution of
 * the rule and no other preconditions will be checked. Preconditions are checked in order of appearance.
 *
 * @author BBonev
 */
public interface RulePrecondition
		extends Named, SupportablePlugin<String>, Configurable, DynamicSupportable, AsyncSupportable, Applicable {

	/**
	 * Configuration property of type collection that defines the fields that need to be checked/searched in
	 */
	String CHECK_FOR_PROPERTIES = "checkForProperties";

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
	 * Configuration group to define relations existence filtering.
	 */
	String RELATIONS = "relations";

	/**
	 * Match the information located in the processing context with the given instance.
	 *
	 * @param processingContext
	 *            the processing context (shared with all processing threads). Modification to this context may cause a
	 *            concurrent modification exception.
	 * @return true, if matched successfully
	 */
	boolean checkPreconditions(RuleContext processingContext);

}
