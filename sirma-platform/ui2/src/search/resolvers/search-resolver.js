/**
 * Abstract class for all search resolvers used for modifying search criteria before performing a search.
 *
 * The <code>resolve</code> function must accept search tree and optionally a context and should return a promise
 * to signal when the resolving is complete.
 */
export class SearchResolver {

  constructor() {
    if (typeof this.resolve !== 'function') {
      throw new TypeError('Must override resolve function');
    }
  }

  /**
   * Checks if the provided rule has another search tree as a value - an embedded search.
   * @param rule - the provided value
   * @returns {boolean} if the rule's value is another search tree or false otherwise
   */
  isRuleValueEmbeddedTree(rule) {
    return !!rule.value && !Array.isArray(rule.value) && !!rule.value.condition;
  }

}