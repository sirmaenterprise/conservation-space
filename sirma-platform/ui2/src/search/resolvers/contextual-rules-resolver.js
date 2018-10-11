import {Injectable, Inject} from 'app/app';
import {SearchResolver} from 'search/resolvers/search-resolver';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {QueryBuilder, DynamicTreeWalkListener} from 'search/utils/query-builder';
import {CURRENT_OBJECT} from 'services/context/contextual-objects-factory';
import uuid from 'common/uuid';

/**
 * Resolves contextual rules located in a query tree with their real instance IDs.
 *
 * Providing a context to <code>resolve</code> is essential for resolving them.
 *
 * @author Mihail Radkov
 */
@Injectable()
@Inject(PromiseAdapter)
export class ContextualRulesResolver extends SearchResolver {

  constructor(promiseAdapter) {
    super();
    this.promiseAdapter = promiseAdapter;
  }

  resolve(tree, context) {
    return this.resolveCurrentObject(tree, context);
  }

  /**
   * Walks every rule in the query tree to replace {@link CURRENT_OBJECT} with it's real instance ID.
   *
   * @returns {Promise}
   */
  resolveCurrentObject(tree, context) {
    return this.promiseAdapter.promise((resolve) => {
      if (context) {
        context.getCurrentObject().then((currentObject) => {
          var treeWalker = new DynamicTreeWalkListener().addOnRule((rule)=> {
            if (this.isRuleValueEmbeddedTree(rule)) {
              QueryBuilder.walk(rule.value, treeWalker);
            } else {
              this.resolveCurrentObjectRule(rule, currentObject);
            }
          });
          QueryBuilder.walk(tree, treeWalker);
          resolve(tree);
        });
      } else {
        resolve(tree);
      }
    });
  }

  /**
   * Replace all rule's values that are equal to CURRENT_OBJECT with the object's real instance ID. If a value
   * is the temporary CURRENT_OBJECT_TEMP_ID then it's skipped from the rule's value.
   *
   * @param rule - the current query tree rule
   * @param object - the contextual object with instance ID
   */
  resolveCurrentObjectRule(rule, object) {
    if (Array.isArray(rule.value)) {
      var values = [];
      rule.value.forEach((value)=> {
        if (value === CURRENT_OBJECT) {
          value = this.getCurrentObjectId(object);
          if (value) {
            values.push(value);
          }
        } else {
          values.push(value);
        }
      });
      rule.value = values;
    } else if (rule.value === CURRENT_OBJECT) {
      rule.value = this.getCurrentObjectId(object);
    }
  }

  getCurrentObjectId(currentObject) {
    if (currentObject.isPersisted()) {
      return currentObject.getId();
    } else {
      // No searches can be done with CURRENT_OBJECT_TEMP_ID so a fake one is placed instead so it will perform a
      // search for an object that does not exist and return 0 results
      return `emf:${uuid()}`;
    }
  }

}