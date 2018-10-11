import uuid from 'common/uuid';
import {JsonUtil} from 'common/json-util';
import _ from 'lodash';

const SEARCH_ARGS_SANITATION_LIST = ['properties'];
const SEARCH_TREE_SANITATION_LIST = ['id', '$$hashKey'];

export class QueryBuilder {

  constructor(tree) {
    this.init(tree);
  }

  init(tree) {
    this.lookup = new Map();
    this.tree = tree || {};
    _.defaultsDeep(this.tree, {condition: 'AND', rules: []});
    let listener = new DynamicTreeWalkListener()
      .addOnAny((node) => {
        if (!node.id) {
          node.id = uuid();
        }
        this.lookup.set(node.id, node);
      });

    QueryBuilder.walk(this.tree, listener);
  }

  add(rule, parentId) {
    if (!rule || !rule.id) {
      throw new Error('Rule with id is required.');
    }

    let parent;
    if (parentId) {
      parent = this.lookup.get(parentId);
      if (!parent) {
        throw new Error('Condition with id ' + parentId + ' does not exist.');
      }
    } else {
      let oldRule = this.lookup.get(rule.id);
      if (oldRule) {
        _.extend(oldRule, rule);
        return;
      }
      parent = this.tree;
    }

    parent.rules.push(rule);

    // register the current rule and any sub-rules in the lookup map
    let lookupSetter = new DynamicTreeWalkListener().addOnAny((node) => {
      if (!node.id) {
        throw new Error('Rule with id is required.');
      }
      this.lookup.set(node.id, node);
    });
    QueryBuilder.walk(rule, lookupSetter);
  }

  remove(criteria) {
    if (!criteria || !criteria.id) {
      throw new Error('Criteria with id is required');
    }

    var lookedUp = this.lookup.get(criteria.id);
    if (!lookedUp) {
      throw new Error('Criteria does not exist in this tree');
    }

    let idRemoveListener = new DynamicTreeWalkListener().addOnAny((node) => {
      this.lookup.delete(node.id);
    });

    let removeListener = new DynamicTreeWalkListener()
      .addOnCondition((condition) => {
        _.remove(condition.rules, (rule) => {
          if (criteria.id === rule.id) {
            this.lookup.delete(criteria.id);
            QueryBuilder.walk(criteria, idRemoveListener);
            return true;
          }
          return false;
        });
      });
    QueryBuilder.walk(this.tree, removeListener);
  }

  static toQueryParams(tree) {
    let query = [];

    let joinValues = (joint, values) => {
      var toJoin = [];
      values.forEach(value => {
        if (value && value.length > 0) {
          value = encodeURIComponent(value);
          toJoin.push(`${joint}=${value}`);
        }
      });
      return toJoin.join('&');
    };

    let listener = new DynamicTreeWalkListener()
      .addOnRule((rule) => {
        if (!rule.field || !rule.value) {
          return;
        }

        let value;
        switch (rule.field) {
          case 'relationship':
            value = joinValues('objectRelationship[]', rule.value);
            break;
          case 'location':
            value = joinValues('location[]', rule.value);
            break;
          case 'types':
            value = joinValues('objectType[]', rule.value);
            break;
          case 'subtypes':
            value = joinValues('subType[]', rule.value);
            break;
          case 'createdBy':
            value = joinValues('createdBy[]', rule.value);
            break;
          case 'metaText':
            value = rule.field + '=' + encodeURIComponent(rule.value);
            break;
          default:
            value = rule.field + '=' + rule.value;
            break;
        }

        query.push(value);
      });

    QueryBuilder.walk(tree, listener);
    return query.join('&');
  }

  static walk(tree, listener) {
    if (listener.onAny) {
      listener.onAny(tree);
    }

    if (!tree.condition) {
      if (listener.onRule) {
        listener.onRule(tree);
      }
      return;
    }

    if (listener.onCondition) {
      listener.onCondition(tree);
    }
    if (tree.rules) {
      tree.rules.forEach((node) => QueryBuilder.walk(node, listener));
    }
  }

  static getRules(tree, field) {
    var fetchedRules = [];
    var walker = new DynamicTreeWalkListener().addOnRule((rule) => {
      if (rule.field === field) {
        fetchedRules.push(rule);
      }
    });
    QueryBuilder.walk(tree, walker);
    return fetchedRules;
  }

  static getFirstRule(tree, field) {
    var rules = QueryBuilder.getRules(tree, field);
    if (rules[0]) {
      return rules[0];
    }
  }

  static updateRules(tree, field, value) {
    QueryBuilder.getRules(tree, field).forEach((rule) => {
      rule.value = value;
    });
  }

  static ruleHasValues(rule) {
    return !!rule && !!rule.value && rule.value.length > 0;
  }

  /**
   * Flattens the search tree to a mapping of every unique field and the corresponding rules.
   * @param tree - the search tree to flatten
   */
  static flattenSearchTree(tree) {
    let flatTree = {};
    let typesWalker = new DynamicTreeWalkListener().addOnRule((rule) => {
      if (!flatTree[rule.field]) {
        flatTree[rule.field] = [];
      }
      flatTree[rule.field].push(rule);
    });
    QueryBuilder.walk(tree, typesWalker);
    return flatTree;
  }

  /**
   * Sanitizes a search tree with a list of predefined properties
   *
   * @param tree the search tree to be sanitized
   */
  static sanitizeSearchTree(tree) {
    let clone = _.cloneDeep(tree);
    return JsonUtil.removeObjectProperties(clone, SEARCH_TREE_SANITATION_LIST);
  }

  /**
   * Sanitizes the search arguments with a list of predefined properties
   *
   * @param args the search arguments to be sanitized
   */
  static sanitizeSearchArguments(args) {
    let clone = _.cloneDeep(args);
    return JsonUtil.removeObjectProperties(clone, SEARCH_ARGS_SANITATION_LIST);
  }

  /**
   * Sanitizes & Encodes to base64 a given search tree
   * The tree is sanitized by a list of predefined properties
   *
   * @param tree the tree to be sanitized & encoded
   */
  static encodeSearchTree(tree) {
    let sanitized = QueryBuilder.sanitizeSearchTree(tree);
    return JsonUtil.encode(sanitized);
  }

  /**
   * Decodes the provided search tree in base64 string format
   *
   * @param tree base64 representation of the search tree
   */
  static decodeSearchTree(tree) {
    return JsonUtil.decode(tree);
  }

  /**
   * Sanitizes & Encodes to base64 the given search arguments
   * The arguments are sanitized by a list of predefined properties
   *
   * @param args the arguments to be sanitized & encoded
   */
  static encodeSearchArguments(args) {
    let sanitized = QueryBuilder.sanitizeSearchArguments(args);
    return JsonUtil.encode(sanitized);
  }

  /**
   * Decodes the provided search arguments in base64 string format
   *
   * @param args base64 representation of the search arguments
   */
  static decodeSearchArguments(args) {
    return JsonUtil.decode(args);
  }

  /**
   * Compares two search trees for equality. The method
   * provides a deep comparison between the two search trees
   *
   * @param treeOne the first tree to be compared
   * @param treeTwo the second tree to be compared
   */
  static isEqual(treeOne, treeTwo) {
    let sanitizedOne = QueryBuilder.sanitizeSearchTree(treeOne);
    let sanitizedTwo = QueryBuilder.sanitizeSearchTree(treeTwo);
    // perform a deep comparison between the sanitized trees
    return _.isEqual(sanitizedOne, sanitizedTwo);
  }
}

export class DynamicTreeWalkListener {
  constructor() {
    this.any = [];
    this.condition = [];
    this.rule = [];
  }

  onAny(node) {
    this.any.forEach((callback) => callback(node));
  }

  onCondition(node) {
    this.condition.forEach((callback) => callback(node));
  }

  onRule(node) {
    this.rule.forEach((callback) => callback(node));
  }

  addOnAny(callback) {
    this.any.push(callback);
    return this;
  }

  addOnCondition(callback) {
    this.condition.push(callback);
    return this;
  }

  addOnRule(callback) {
    this.rule.push(callback);
    return this;
  }
}