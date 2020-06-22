import {Component, View, Inject} from 'app/app';
import {SearchCriteriaComponent} from 'search/components/common/search-criteria-component';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {OPEN_SAVED_SEARCH_EVENT} from 'search/components/saved/saved-search-select/saved-search-select';
import {EVENT_BEFORE_SEARCH, EVENT_SEARCH} from 'search/search-mediator';
import {FTS_CHANGE_EVENT} from 'search/components/search';
import {IdocContext} from 'idoc/idoc-context';
import {ContextualObjectsFactory, CURRENT_OBJECT} from 'services/context/contextual-objects-factory';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {InstanceRestService} from 'services/rest/instance-service';
import {SavedSearchesLoader} from 'search/components/saved/saved-searches-loader';
import 'search/components/search-bar/search-bar';
import 'search/components/advanced/advanced-search';
import 'external-search/external-search';
import 'components/help/contextual-help';
import _ from 'lodash';

import './mixed-search-criteria.css!css';
import template from 'search/components/common/mixed-search-criteria.html!text';

export const SEARCH_BAR_FIELDS = [SearchCriteriaUtils.CRITERIA_TYPES_RULE_FIELD, SearchCriteriaUtils.CRITERIA_FTS_RULE_FIELD, SearchCriteriaUtils.ANY_RELATION];

/**
 * Component wrapping & configuring search bar and the advanced search criteria components.
 *
 * It allows switching between the two when:
 * 1) the search bar fires its onSearchModeSelected component event
 * 2) when the advanced search is closed
 *
 * Provided configuration and context properties are passed down to the currently rendered search component.
 * Providing a search mediator in the configuration is mandatory!
 *
 * There are several decisions on which criteria to be rendered in the following order:
 * 1) Search bar - If the provided criteria tree is empty, a default one is assigned and the search bar is rendered
 * 2) Advanced search - If the provided search mode is advanced
 * 3) Search bar - If the provided mode is basic and the criteria tree can be rendered in the search bar model
 *
 * A criteria tree can be rendered in the search bar if:
 * 1) All of the rule fields are part of SEARCH_BAR_FIELDS, if one is not supported it will render the advanced search
 * 2) If there are only one rule of each SEARCH_BAR_FIELDS in the criteria tree
 * 3) If the context rule field has only one value or none at all
 *
 * When switching from search bar to the advanced search, the search bar criteria tree is copied and given to the advanced search.
 *
 * If a context component property is provided, this component will try to resolve the current context which can be
 * assigned as the search bar context if none is previously provided. It can resolve to the following:
 * 1) root context - if config.useRootContext is true then it resolves to the top most instance in the current context's
 *                   path to which the user has read access. But if the current instance is actually the root context,
 *                   then no context is resolved for usage.
 * 2) CURRENT_OBJECT - if config.useRootContext is false
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-mixed-search-criteria',
  properties: {
    config: 'config',
    context: 'context'
  }
})
@View({template})
@Inject(PromiseAdapter, InstanceRestService, ContextualObjectsFactory)
export class MixedSearchCriteria extends SearchCriteriaComponent {

  constructor(promiseAdapter, instanceRestService, contextualObjectsFactory) {
    super({
      disabled: false,
      searchMode: SearchCriteriaUtils.BASIC_MODE,
      advancedOnly: false,
      predefinedTypes: []
    });

    this.promiseAdapter = promiseAdapter;
    this.instanceRestService = instanceRestService;
    this.contextualObjectsFactory = contextualObjectsFactory;
  }

  ngOnInit() {
    this.initialize();
  }

  initialize() {
    this.searchMediator = this.config.searchMediator;

    this.resolveContext().then((resolvedContext) => {
      this.resolvedContext = resolvedContext;

      this.setupCriteria();
      this.configureSearchBar();
      this.registerSearchLoadListener();

      this.searchMediator.searchMode = this.config.searchMode;
    });
  }

  resolveContext() {
    return this.promiseAdapter.promise((resolve) => {
      if (this.context) {
        if (this.config.useRootContext) {
          this.context.getCurrentObject().then((currentObject) => {
            resolve(this.getRootContext(currentObject));
          });
        } else {
          resolve(this.getContextualItem());
        }
      } else {
        resolve();
      }
    });
  }

  getRootContext(currentObject) {
    // Current context path of the instanceObject
    let contextPath = currentObject.getContextPath();

    if (contextPath) {
      // The current object is the last object in the context path
      let currentNode = contextPath[contextPath.length - 1];
      let parentNode = IdocContext.getRootContextWithReadAccess(contextPath);

      // Assign root id only if the root is not the current object
      if (parentNode && parentNode.id !== currentNode.id) {
        return parentNode;
      }
    }
  }

  getContextualItem() {
    return this.contextualObjectsFactory.getCurrentObject();
  }

  setupCriteria() {
    if (this.isExternal()) {
      // Don't setup anything in case of external search
      return;
    }

    delete this.basicSearchQueryBuilder;
    let initialQueryBuilder = this.searchMediator.queryBuilder;

    // If the tree is empty -> simply setup the search bar
    if (initialQueryBuilder.tree.rules.length < 1) {
      initialQueryBuilder.init(this.getDefaultSearchBarTree());
      this.setupSearchBarTree(initialQueryBuilder);
      this.afterInit();
      return;
    }

    // Find out which criteria form to use
    let fieldsMap = QueryBuilder.flattenSearchTree(initialQueryBuilder.tree);
    if (this.shouldUseAdvanced(fieldsMap)) {
      this.config.searchMode = SearchCriteriaUtils.ADVANCED_MODE;
    } else {
      this.setupSearchBarTree(initialQueryBuilder);
      this.afterInit();
    }
  }

  setupSearchBarTree(queryBuilder) {
    this.basicSearchQueryBuilder = queryBuilder;
    let tree = this.basicSearchQueryBuilder.tree;

    let innerCondition = tree.rules[0].rules && tree.rules[0].rules[1];
    // In case the tree is incomplete
    if (!innerCondition) {
      innerCondition = SearchCriteriaUtils.buildCondition();
      if (!tree.rules[0].rules) {
        tree.rules[0].rules = [];
      }
      tree.rules[0].rules.splice(1, 0, innerCondition);
    }

    let freeTextRule = QueryBuilder.getFirstRule(innerCondition, SearchCriteriaUtils.CRITERIA_FTS_RULE_FIELD);
    if (!freeTextRule) {
      freeTextRule = SearchCriteriaUtils.getDefaultFreeTextRule();
      innerCondition.rules.push(freeTextRule);
    }

    let anyRelationRule = QueryBuilder.getFirstRule(innerCondition, SearchCriteriaUtils.ANY_RELATION);
    if (!anyRelationRule) {
      anyRelationRule = SearchCriteriaUtils.getDefaultAnyRelationRule();
      innerCondition.rules.push(anyRelationRule);
      if (this.resolvedContext) {
        anyRelationRule.value.push(this.resolvedContext.id);
      }
    }

    // Advanced search can render free text criteria only as the first rule so they need to be swapped if necessary
    if (innerCondition.rules[0].field === SearchCriteriaUtils.ANY_RELATION) {
      let anyRule = innerCondition.rules[0];
      innerCondition.rules[0] = innerCondition.rules[1];
      innerCondition.rules[1] = anyRule;
    }

    if (!this.config.advancedOnly) {
      // Setup the bar model only if we do render it.
      let typeRule = QueryBuilder.getFirstRule(tree, SearchCriteriaUtils.CRITERIA_TYPES_RULE_FIELD);
      this.setupSearchBarModel(typeRule, freeTextRule, anyRelationRule);
    }
  }

  setupSearchBarModel(typeRule, freeTextRule, anyRelationRule) {
    // Map the rules for the search bar
    this.searchBarModel = {
      objectType: typeRule,
      freeText: freeTextRule
    };

    if (anyRelationRule.value.length > 0 && anyRelationRule.value.indexOf(CURRENT_OBJECT) > -1) {
      this.searchBarModel.context = this.getContextualItem();
    } else if (anyRelationRule.value.length > 0) {
      // Need to load the instance to get its headers
      this.instanceRestService.load(anyRelationRule.value[0]).then((response) => {
        this.searchBarModel.context = response.data;
      });
    }
  }

  registerSearchLoadListener() {
    this.searchMediator.registerListener(OPEN_SAVED_SEARCH_EVENT, (savedSearch) => {
      this.config.searchMode = savedSearch.searchMode;
      this.setupCriteria();
    });
  }

  configureSearchBar() {
    if (this.config.advancedOnly) {
      // No need to configure the bar if we do not render it
      return;
    }

    this.searchBarConfig = {
      disabled: this.config.disabled,
      multiple: true,
      enableCurrentObject: !!this.resolvedContext,
      predefinedTypes: this.config.predefinedTypes
    };

    this.searchMediator.registerListener(EVENT_BEFORE_SEARCH, () => this.searchBarConfig.disabled = true);
    this.searchMediator.registerListener(EVENT_SEARCH, () => this.searchBarConfig.disabled = false);
  }

  onSwitch(queryBuilder) {
    this.searchMediator.searchMode = this.config.searchMode;
    this.searchMediator.queryBuilder = queryBuilder;
    this.clearResults();
  }

  onFreeTextChange(freeText) {
    this.config.searchMediator.trigger(FTS_CHANGE_EVENT, freeText);
  }

  onContextChange(context) {
    let currentTree = this.searchMediator.queryBuilder.tree;
    let anyRelationRule = QueryBuilder.getFirstRule(currentTree, SearchCriteriaUtils.ANY_RELATION);
    anyRelationRule.value = context ? [context.id] : [];
  }

  closeAdvancedSearch() {
    this.openSearchBar();
  }

  closeExternalSearch() {
    this.openSearchBar();
  }

  openSearchBar() {
    this.config.searchMode = SearchCriteriaUtils.BASIC_MODE;

    if (this.config.advancedOnly) {
      return;
    }

    if (!this.basicSearchQueryBuilder) {
      this.setupSearchBarTree(new QueryBuilder(this.getDefaultSearchBarTree()));
    }

    this.onSwitch(this.basicSearchQueryBuilder);
    this.onFreeTextChange(this.searchBarModel.freeText.value);
  }

  onSearch() {
    this.search();
  }

  loadSavedSearch(savedSearch) {
    this.config.searchMediator.trigger(OPEN_SAVED_SEARCH_EVENT, SavedSearchesLoader.convertSavedSearch(savedSearch));
  }

  /**
   * Executed when the mode is changed via the search bar options.
   */
  changeMode(mode) {
    if (mode === SearchCriteriaUtils.EXTERNAL_MODE) {
      this.config.searchMode = SearchCriteriaUtils.EXTERNAL_MODE;
      this.searchMediator.searchMode = this.config.searchMode;
      this.clearResults();
    } else {
      this.config.searchMode = SearchCriteriaUtils.ADVANCED_MODE;

      // Transfer criteria from search bar to advanced
      let clonedTree = _.cloneDeep(this.searchMediator.queryBuilder.tree);

      // Advanced search still uses the anyObject wildcard thus we need to provide it
      let typeRule = QueryBuilder.getFirstRule(clonedTree, SearchCriteriaUtils.CRITERIA_TYPES_RULE_FIELD);
      SearchCriteriaUtils.defaultValue(typeRule, [SearchCriteriaUtils.ANY_OBJECT]);

      this.onSwitch(new QueryBuilder(clonedTree));
    }
  }

  /**
   * Determines if the search criteria should be rendered in the advanced search or can be visualized in the search bar.
   *
   * A criteria can be rendered in the search bar if all of the fields are amongst SEARCH_BAR_FIELDS only once and they
   * should one only one value with the exception of the SearchCriteriaUtils.CRITERIA_TYPES_RULE_FIELD.
   *
   * @param fieldsMap - map of the criteria tree fields
   * @returns {boolean} true if it should be in the advanced search or false in case for the search bar
   */
  shouldUseAdvanced(fieldsMap) {
    if (this.config.searchMode === SearchCriteriaUtils.ADVANCED_MODE) {
      return true;
    }

    let fieldNames = Object.keys(fieldsMap);

    let notSupportedFields = fieldNames.filter((field) => {
      return SEARCH_BAR_FIELDS.indexOf(field) === -1;
    });
    if (notSupportedFields.length > 0) {
      return true;
    }

    let type = fieldsMap[SearchCriteriaUtils.CRITERIA_TYPES_RULE_FIELD];
    let fts = fieldsMap[SearchCriteriaUtils.CRITERIA_FTS_RULE_FIELD];
    let context = fieldsMap[SearchCriteriaUtils.ANY_RELATION];

    let hasMultipleRules = [type, fts, context].some((field) => {
      return field && field.length > 1;
    });

    return hasMultipleRules || context && context[0].value.length > 1;
  }

  getDefaultSearchBarTree() {
    let context = this.resolvedContext ? [this.resolvedContext.id] : [];
    // re-construct the search tree from stored parameters for search bar
    return SearchCriteriaUtils.getSearchTree({
      objectType: this.config.predefinedTypes,
      restrictions: this.config.restrictions,
      context,
      freeText: ''
    });
  }

  isExternal() {
    return this.config.searchMode === SearchCriteriaUtils.EXTERNAL_MODE;
  }
}
