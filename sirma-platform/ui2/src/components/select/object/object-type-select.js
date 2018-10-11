import {Select} from 'components/select/select';
import {Configurable} from 'components/configurable';
import {View, Component, Inject, NgElement, NgScope} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {ModelsService} from 'services/rest/models-service';

import _ from 'lodash';
import './object-type-select.css!';
import selectTemplate from './object-type-select.html!text';

/**
 * Reusable select component visualizing available object types. Wraps the {@link Select} component and provides it
 * with the configuration object.
 *
 * The component allows external configuration override of the default select configuration. If the component is used
 * with ng-model, it will create two way binding with the wrapped select.
 *
 * Additional configurations for this component (so it could fetch different types hierarchy) are:
 * config = {
 *    classFilter - for example emf:Document will display only the documents
 *    dataLoader - if this is provided, it will be used to fetch the models instead of the default models service
 *    disableTypesWithoutDefinition - when true, it will disable options that don't have a definition. Default: false
 * }
 *
 * Important: This component accepts only full URIs because the ModelsService returns full URIs.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-object-type-select',
  properties: {'config': 'config'}
})
@View({
  template: selectTemplate
})
@Inject(NgElement, NgScope, PromiseAdapter, ModelsService, TranslateService)
export class ObjectTypeSelect extends Configurable {

  constructor($element, $scope, promiseAdapter, modelsService, translateService) {
    super({
      preferDefinitionType: false,
      disableTypesWithoutDefinition: false
    });
    this.$scope = $scope;
    this.ngModel = $element.controller('ngModel');
    this.promiseAdapter = promiseAdapter;
    this.modelsService = modelsService;
    this.translateService = translateService;
  }

  ngOnInit() {
    this.createSelectConfig();
    this.bindToModel();
  }

  createSelectConfig() {
    let promises = [];
    promises.push(this.translateService.translate('select.objecttype.placeholder'));
    promises.push(this.loadData().then(response => this.convertData(response)));

    return this.promiseAdapter.all(promises).then(responses => {
      let types = responses[1];

      if (this.config.disableTypesWithoutDefinition) {
        this.disableTypesWithNoDefinition(types);
      }

      this.expandSelectableTypes(types);
      this.publishData(types);
      this.config.selectConfig = _.defaults(this.config, {
        multiple: true,
        placeholder: responses[0],
        data: types,
        formatResult: item => {
          return this.formatResult(item);
        }
      });

      if (this.shouldLimitSelectionLength()) {
        this.config.selectConfig.minimumSelectionLength = 1;
      }
    });
  }

  expandSelectableTypes(types) {
    if (this.hasPredefinedTypes()) {
      for (let type of this.config.predefinedData) {
        types.unshift(type);
      }
    }
  }

  shouldLimitSelectionLength() {
    return this.hasClassFilters();
  }

  hasPredefinedTypes() {
    return ObjectTypeSelect.arrayIsNotEmpty(this.config.predefinedData);
  }

  hasClassFilters() {
    return ObjectTypeSelect.arrayIsNotEmpty(this.config.classFilter);
  }

  static arrayIsNotEmpty(array) {
    return array instanceof Array && array.length > 0;
  }

  formatResult(option) {
    return `<span class="type-${option.type} level-${option.level}">${option.text}</span>`;
  }

  loadData() {
    if (this.config.dataLoader) {
      return this.config.dataLoader();
    }
    return this.modelsService.getModels(ModelsService.PURPOSE_SEARCH, undefined, undefined, undefined, this.config.classFilter).then((response) => {
      return response.models;
    });
  }

  disableTypesWithNoDefinition(types) {
    types.forEach(type => {
      if (!type.definitionId) {
        type.disabled = true;
      }

      if (type.subtypes) {
        this.disableTypesWithNoDefinition(type.subtypes);
      }
    });
  }

  /**
   * Converts the class models to a hierarchy tree of classes and their subclasses, than flatten this tree in order to
   * show it in a select component (https://github.com/select2/select2/issues/520#issuecomment-103703792).
   */
  convertData(models) {
    var options = [];

    var classesMap = {};

    // locate the classes and create hierarchy tree
    models.forEach((model) => {
      if (model.type === ModelsService.TYPE_CLASS) {
        var option = classesMap[model.id];
        if (!option) {
          option = {};
          classesMap[model.id] = option;
        }

        _.merge(option, this.toOption(model));
        option.surrogate = undefined;

        // link to parent
        if (!option.parent) {
          options.push(option);
        } else {
          this.attachToParent(option, classesMap);
        }
      }
    });

    this.attachChildrenDefinitions(models, classesMap);

    this.unwrapOptionsWithoutExistingParent(options, classesMap);

    var result = this.flattenHierarchyTree(options, 1);

    return this.stripSingleDefinition(result, classesMap);
  }

  attachChildrenDefinitions(models, classesMap) {
    models.forEach((model)=> {
      if (model.type === ModelsService.TYPE_DEFINITION) {
        var option = this.toOption(model);
        option.definitionId = option.id;

        classesMap[model.parent].subtypes.push(option);
      }
    });
  }

  attachToParent(option, classesMap) {
    var parentOption = classesMap[option.parent];
    // create surrogate parent option in order to attach the child option to it
    if (!parentOption) {
      parentOption = {
        id: option.parent,
        subtypes: [],
        surrogate: true
      };
      classesMap[option.parent] = parentOption;
    }

    parentOption.subtypes.push(option);
  }

  /**
   * Handles cases where there types with parent property but an item for the parent is not provided (i.e. when
   * class filtering is applied). In this case the surrogate option should be unwrapped and its children directly added
   * to the options array.
   */
  unwrapOptionsWithoutExistingParent(options, classesMap) {
    _.forEach(classesMap, function (option) {
      if (option.surrogate) {
        _.forEach(option.subtypes, function (child) {
          options.push(child);
        });
      }
    });
  }

  flattenHierarchyTree(options, level) {
    var result = [];

    options.forEach((option) => {
      option.level = level;
      result.push(option);
      if (option.subtypes.length) {
        result = result.concat(this.flattenHierarchyTree(option.subtypes, level + 1));
      }
    });

    return result;
  }

  toOption(model) {
    return {
      id: model.id,
      text: model.label,
      parent: model.parent,
      type: model.type,
      subtypes: []
    };
  }

  /**
   * Single definition means that it's the definition for the class that is its parent
   */
  stripSingleDefinition(options, classesMap) {
    var result = [];
    options.forEach((option) => {
      var isDefinition = option.type === ModelsService.TYPE_DEFINITION;

      if (isDefinition) {
        var parent = classesMap[option.parent];
        if (this.hasSingleDefinition(parent)) {
          if (this.config.preferDefinitionType) {
            parent.uri = parent.id;
            // Override the parent
            parent.id = option.id;
          }
          parent.definitionId = option.definitionId;
        } else {
          result.push(option);
        }
      } else {
        result.push(option);
      }
    });

    return result;
  }

  hasSingleDefinition(option) {
    var definitionCount = 0;
    option.subtypes.forEach(function (child) {
      if (child.type === ModelsService.TYPE_DEFINITION) {
        definitionCount++;
      }
    });

    return definitionCount === 1;
  }

  publishData(data) {
    if (this.config.publishCallback) {
      this.config.publishCallback(data);
    }
  }

  /**
   * Creates a two-way model binding to pass the select's wrapping.
   */
  bindToModel() {
    if (this.ngModel) {
      this.$scope.$watch(() => {
        return this.config.objectTypes;
      }, (newValue, oldValue) => {
        if (!Select.compareValues(newValue, oldValue)) {
          this.ngModel.$setViewValue(newValue);
        }
      });

      this.$scope.$watch(() => {
        return this.ngModel.$viewValue;
      }, (newValue) => {
        if (!Select.compareValues(newValue, this.config.objectTypes)) {
          this.config.objectTypes = newValue;
        }
      });
    }
  }
}