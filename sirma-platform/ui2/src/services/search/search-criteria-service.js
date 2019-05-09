import {Injectable, Inject} from 'app/app';
import {ModelsService} from 'services/rest/models-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {CodelistService} from 'services/codelist/codelist-service';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {PropertiesRestService} from 'services/rest/properties-service';
import {QueryBuilder, DynamicTreeWalkListener} from 'search/utils/query-builder';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';
import _ from 'lodash';

export const NLINE = '\r\n';
export const SPACE = '\u0020';
export const COMMA = ',' + SPACE;

export const CONDITION_OR_LABEL = 'search.advanced.criteria.or';
export const CONDITION_AND_LABEL = 'search.advanced.criteria.and';
export const ANY_OBJECT_LABEL = 'search.advanced.value.anyObject';

export const VALUES_COLOR = '#1a9bbc';
export const CONTROLS_COLOR = '#8B008B';
export const OPERATORS_COLOR = '#008000';
export const PROPERTIES_COLOR = '#0000FF';

/**
 * Service which provides common functionality for examining, transforming, modifying and translating a search criteria
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(TranslateService, InstanceRestService, PropertiesRestService, CodelistService, ModelsService, PromiseAdapter)
export class SearchCriteriaService {

  constructor(translateService, instanceRestService, propertiesRestService, codelistService, modelsService, promiseAdapter) {
    this.promiseAdapter = promiseAdapter;

    this.modelsService = modelsService;
    this.codelistService = codelistService;
    this.translateService = translateService;

    this.instanceRestService = instanceRestService;
    this.propertiesRestService = propertiesRestService;

    this.models = {};
    this.controls = {};
    this.instances = {};
    this.operators = {};
    this.properties = {};

    this.assignPromises();
  }

  assignPromises() {
    this.propertiesPromise = this.propertiesRestService.getSearchableProperties();
    this.modelsPromise = this.modelsService.getModels(ModelsService.PURPOSE_SEARCH);
    this.dataPromises = [this.propertiesPromise, this.modelsPromise];
  }

  /**
   * Stringifies and optionally beautifies the provided
   * search criteria in a readable and humane manner
   *
   * @param criteria - criteria to be stringified
   * @param beautify - colorize & format the tree: true | false
   * @param indent - the number of space indents per level: 0 | 1 | N
   */
  stringifySearchCriteria(criteria, beautify = true, indent = 1) {
    criteria = this.modifyCriteria(criteria);
    let stringified = this.stringifyCriteria(criteria, -1, beautify, indent);
    return beautify ? `<pre>${stringified}</pre>` : stringified;
  }

  /**
   * Translates provided search criteria all eligible elements
   * contained inside the criteria are translated
   *
   * @param criteria - criteria to be translated
   */
  translateSearchCriteria(criteria) {
    return this.loadData(() => {
      let instances = SearchCriteriaUtils.getTypeValuesFromCriteria(criteria, 'object');
      // filter out instanced that might already be loaded or that are not eligible for loading e.g. ANY_OBJECT
      let filtered = instances.filter(instance => instance !== SearchCriteriaUtils.ANY_OBJECT && !this.instances[instance]);

      if (filtered && filtered.length) {
        // extract the code lists based on the fetched searchable properties
        return this.instanceRestService.loadBatch(filtered, {params: {properties: ['title']}}).then(response => {
          this.cacheInstances(response);
          return this.translateCriteria(criteria);
        });
      } else {
        return this.translateCriteria(criteria);
      }
    });
  }

  /**
   * Helper method which recursively stringifies
   * a search criteria in a readable and humane manner
   *
   * @param criteria - criteria to be stringified
   * @param level - the level of indentation
   * @param beautify - colorize the tree true | false
   * @param indent - number of space indents per level
   */
  stringifyCriteria(criteria, level, beautify, indent) {
    let text = '';

    for (let key in criteria) {
      let property = criteria[key];

      if (property.field) {
        let values = _.isArray(property.value) ? property.value : [property.value];
        let type = this.beautify(property.field, beautify && PROPERTIES_COLOR);
        let operator = this.beautify(property.operator, beautify && OPERATORS_COLOR);
        values.forEach((element, index, array) => {
          array[index] = this.beautify(element, beautify && VALUES_COLOR);
        });
        values = values.join(COMMA) + NLINE;
        // stringify a single search criteria rule and the values it holds
        text += this.indent([type, operator, values].join(SPACE), level * indent);
      } else if (property.condition) {
        // stringify the artificially inserted condition between rules
        let control = this.beautify(property.condition, beautify && CONTROLS_COLOR);
        text += this.indent(control + NLINE, level * indent);
      }

      if (_.isObject(property)) {
        // stringify all rules for the current condition
        text += this.stringifyCriteria(property, level + 1, beautify, indent);
      }
    }
    return text;
  }

  /**
   * Helper method which recursively translates a provided search criteria
   * every field, operator, value or condition eligible for translation
   *
   * @param - criteria the criteria to be translated
   */
  translateCriteria(criteria) {
    let clonedCriteria = this.prepareCriteria(criteria);

    let translator = new DynamicTreeWalkListener().addOnAny((rule) => {
      if (rule.condition) {
        rule.condition = this.controls[rule.condition].label;
      } else if (rule.field) {
        rule.field = this.properties[rule.field].label;
        rule.operator = this.operators[rule.operator].label;

        if (rule.type === 'object') {
          this.translate(rule.value, this.instances);
        } else if (rule.type === 'codeList') {
          this.translate(rule.value, this.models);
        }
      }
    });
    QueryBuilder.walk(clonedCriteria, translator);
    return clonedCriteria;
  }

  /**
   * Wraps an element in span with a provided text color
   * if no color is provided the element is not wrapped
   *
   * @param element - the element to be wrapped
   * @param color - the color of the element's text
   */
  beautify(element, color) {
    return color ? `<span style=${'color:' + color}>${element}</span>` : element;
  }

  /**
   * Translates array values in place from a given storage
   *
   * @param array - the array to be translated
   * @param storage - the storage of translated labels
   */
  translate(array, storage) {
    array.forEach((element, index, array) => {
      let cached = storage[element];
      if (cached && cached.label) {
        array[index] = cached.label;
      }
    });
  }

  /**
   * Indents a given string with spaces
   * placed on the left side of the string
   *
   * @param str - string to be indented
   * @param length - the number of indents
   */
  indent(str, length) {
    return _.padLeft(str, str.length + length);
  }

  /**
   * Loads and caches relevant data which will remain constant between
   * different invocations of the {@SearchCriteriaService} methods.
   * This method load all searchable properties, models and operators.
   * After the loading is done the provided callback method is invoked.
   *
   * On any subsequent calls to this method the loading is not processed
   * but simply the provided callback is invoked wrapped in a promise
   *
   * @param afterLoaded - callback to be executed after loading is complete
   */
  loadData(afterLoaded) {
    if (this.dataPromises) {

      return this.promiseAdapter.all(this.dataPromises).then((response) => {
        // enforce single time loading
        delete this.dataPromises;

        // cache static data
        this.cacheControls();
        this.cacheModels(response[1]);
        this.cacheProperties(response[0]);
        this.cacheOperators(AdvancedSearchCriteriaOperators);

        // extract the code lists based on the fetched searchable properties
        return this.codelistService.aggregateCodelists(this.codeLists).then(response => {
          this.cacheCodeLists(response);
          return afterLoaded();
        });
      });
    } else {
      // nothing to do, wrap in promise and execute
      return this.promiseAdapter.resolve(afterLoaded());
    }
  }

  cacheControls() {
    //search tree specific controls & conditions
    this.controls[SearchCriteriaUtils.AND_CONDITION] = {
      label: this.translateService.translateInstant(CONDITION_AND_LABEL)
    };
    this.controls[SearchCriteriaUtils.OR_CONDITION] = {
      label: this.translateService.translateInstant(CONDITION_OR_LABEL)
    };
  }

  cacheCodeLists(response) {
    // store code lists with models
    _.forEach(response, (type) => {
      this.models[type.value] = {
        id: type.value,
        label: type.label
      };
    });
  }

  cacheInstances(response) {
    _.forEach(response.data, (instance) => {
      this.instances[instance.id] = {
        id: instance.id,
        label: instance.properties.title
      };
    });
    //manually add any object to list of instances
    this.instances[SearchCriteriaUtils.ANY_OBJECT] = {
      id: SearchCriteriaUtils.ANY_OBJECT,
      label: this.translateService.translateInstant(ANY_OBJECT_LABEL)
    };
  }

  cacheModels(response) {
    _.forEach(response.models, (model) => {
      this.models[model.id] = {
        id: model.id,
        label: model.label
      };
    });
  }

  cacheProperties(response) {
    let set = new Set();
    // collect data for properties & CL
    _.forEach(response, (property) => {
      this.properties[property.id] = {
        id: property.id,
        label: property.text
      };
      if (property.codeLists) {
        _.forEach(property.codeLists, val => set.add(val));
      }
    });
    // list of unique CL identifiers sorted in asc order
    this.codeLists = Array.from(set).sort((a, b) => a - b);
  }

  cacheOperators(operators) {
    Object.keys(operators).forEach((key) => {
      let operator = operators[key];
      this.operators[operator.id] = {
        id: operator.id,
        label: this.translateService.translateInstant(operator.label)
      };
    });
  }

  /**
   * Sanitizes, modifies and validates the provided criteria before processing
   *
   * @param criteria - the criteria to be prepared
   */
  prepareCriteria(criteria) {
    if (!criteria.condition || !criteria.rules) {
      // the root restriction tree is not complete build a complete condition tree
      criteria = SearchCriteriaUtils.buildCondition(SearchCriteriaUtils.AND_CONDITION, [criteria]);
    }
    // sanitize unused fields out from the restriction tree
    return QueryBuilder.sanitizeSearchTree(criteria);
  }

  /**
   * Modifies a provided search criteria in such a way where the conditions
   * are alternated between rules when more than one rule is provided
   *
   * @param criteria - the search criteria or criteria
   */
  modifyCriteria(criteria) {
    if (criteria && criteria.rules) {
      // traverse down all of the criteria levels first - bottom up
      _.forEach(criteria.rules, rule => this.modifyCriteria(rule));

      let rules = [];
      let condition = criteria.condition;
      let last = criteria.rules.length - 1;

      // append the condition between each rule
      _.forEach(criteria.rules, (rule, i) => {
        let result = i < last ? [rule, {condition}] : [rule];
        rules.push(...result);
      });

      // replace old rules
      criteria.rules = rules;
      // unused root condition
      delete criteria.condition;
    }
    return criteria;
  }
}
