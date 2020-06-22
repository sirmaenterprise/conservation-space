import {View, Inject, NgScope, NgElement, NgInterval} from 'app/app';
import {Widget} from 'idoc/widget/widget';
import {Logger} from 'services/logging/logger';
import {Eventbus} from 'services/eventbus/eventbus';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {FormWrapper, LAYOUT, LABEL_POSITION_HIDE} from 'form-builder/form-wrapper';
import {SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {CodelistRestService} from 'services/rest/codelist-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {TranslateService} from 'services/i18n/translate-service';
import 'idoc/widget/aggregated-table/table-view/table-view';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {DefinitionModel} from 'models/definition-model';
import {InstanceModel} from 'models/instance-model';
import {AggregatedTableConfig} from 'idoc/widget/aggregated-table/config';
import {EventEmitter} from 'common/event-emitter';
import {PropertiesSelectorHelper} from 'idoc/widget/properties-selector/properties-selector-helper';
import _ from 'lodash';
import template from './widget.html!text';
import './widget.css!';

const ERROR_MESSAGE_KEY = 'select.object.results.none';

@Widget
@View({template})
@Inject(NgScope, NgElement, NgInterval, ObjectSelectorHelper, CodelistRestService, InstanceRestService, Logger, TranslateService, Eventbus, PromiseAdapter, PropertiesSelectorHelper)
export class AggregatedTable {

  constructor($scope, $element, $interval, objectSelectorHelper, codelistRestService, instanceRestService, logger, translateService, eventbus, promiseAdapter, propertiesSelectorHelper) {
    this.$scope = $scope;
    this.$element = $element;
    this.$interval = $interval;
    this.logger = logger;
    this.eventbus = eventbus;
    this.objectSelectorHelper = objectSelectorHelper;
    this.tableWidth = this.$element.width();
    this.codelistRestService = codelistRestService;
    this.instanceRestService = instanceRestService;
    this.promiseAdapter = promiseAdapter;
    this.propertiesSelectorHelper = propertiesSelectorHelper;
    this.setError(ERROR_MESSAGE_KEY);

    //Used to convert the returned format to the current
    if (this.config.versionData) {
      this.versionAggregatedData = {aggregated: this.config.versionData.aggregatedData};
    }

    this.labels = {
      numberColumnLabel: translateService.translateInstant('widget.aggregated.table.number.column')
    };

    this.config.styles = this.config.styles || {
      columns: {},
      grid: this.config.grid
    };

    this.tableConfig = this.tableConfig || {
      layout: LAYOUT.TABLE,
      labelPosition: LABEL_POSITION_HIDE,
      styles: this.config.styles,
      formViewMode: FormWrapper.FORM_VIEW_MODE_PREVIEW,
      showFooter: this.config.showFooter
    };

    $scope.$watch(() => {
      return this.config.grid;
    }, () => {
      this.tableConfig.styles.grid = this.config.grid;
    });

    $scope.$watch(() => {
      return this.config.groupBy;
    }, (groupBy) => {
      if (groupBy && this.existResultsInVersionData(groupBy)) {
        this.getGroupByCodelists(groupBy.name).then((codelists) => {
          this.codelists = codelists;
          this.groupObjects(groupBy.name);
        });
      } else {
        this.clearTable();
        this.setError(ERROR_MESSAGE_KEY);
        this.publishWidgetReadyEvent();
      }
    });

    $scope.$watch(() => {
      return this.config.showFooter;
    }, (value) => {
      this.tableConfig.showFooter = value;
    });
  }

  existResultsInVersionData(value) {
    //If not version the search isn't processed yet
    if (this.versionAggregatedData) {
      let valueData = this.versionAggregatedData.aggregated[value.name];
      if (valueData) {
        for (let key in valueData) {
          //If version data is present with found results returns true
          if (valueData[key]) {
            return true;
          }
        }
        //If version data is present but without results returns false
        return false;
      }
    }
    return true;
  }

  swapColumns(value) {
    if (value === AggregatedTableConfig.VALUES_FIRST) {
      this.headers[0] = this.headers.splice(1, 1, this.headers[0])[0];
      this.selectedObjects.forEach((object) => {
        object.models.viewModel.fields[0] = object.models.viewModel.fields.splice(1, 1, object.models.viewModel.fields[0])[0];
      });
    }
  }

  setError(message) {
    this.errorMessage = message;
  }

  clearTable() {
    this.selectedObjects = [];
    this.headers = [];
  }

  setColumnsWidth(headers) {
    let columnWidth = this.tableWidth / 2 + 'px';
    let columnWidths = {};
    headers.forEach((header) => {
      if (this.config.styles.columns[header.name]) {
        columnWidth = this.config.styles.columns[header.name].width;
        // In print/export preview is smaller and width have to be decreased in needed proportion
        if (this.context.isPrintMode()) {
          columnWidth = this.config.styles.columns[header.name].width / 3 + 'px';
        }
      }
      columnWidths[header.name] = {
        width: columnWidth
      };
    });
    this.config.styles.columns = columnWidths;
    this.tableConfig.styles.columns = columnWidths;
  }

  groupObjects(groupBy) {
    delete this.errorMessage;
    if (this.versionAggregatedData) {
      this.convertResponseToModel(this.versionAggregatedData);
    } else {
      this.objectSelectorHelper.groupSelectedObjects(this.config, this.context, groupBy).then((groupedByObjectsResponse) => {
        if (!(this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY && this.context.isModeling())) {
          this.convertResponseToModel(groupedByObjectsResponse);
        } else if (this.context.isModeling()) {
          this.publishWidgetReadyEvent();
        }
      }).catch((error) => {
        this.clearTable();
        this.setError(error.reason);
        this.publishWidgetReadyEvent();
      });
    }
  }

  publishWidgetReadyEvent() {
    this.eventbus.publish(new WidgetReadyEvent({
      widgetId: this.control.getId()
    }));
  }

  applyResizable(element, headerCells) {
    let widget = this;
    let paramsObject = {
      subTotalWidth: 0,
      nextColumn: {}
    };
    headerCells.resizable({
      handles: 'e',
      start(event, ui) {
        AggregatedTable.onResizeStart(ui, paramsObject);
      },
      stop(event, ui) {
        AggregatedTable.onResizeStop(ui, paramsObject, widget, $(this).attr('data-header-cell-name'));
      },
      resize(event, ui) {
        AggregatedTable.onResize(ui, paramsObject, element, this);
      }
    });
    headerCells.resizable('option', 'disabled', this.context.isPreviewMode());
  }

  static onResizeStart(ui, paramsObject) {
    paramsObject.nextColumn = ui.originalElement.next();
    paramsObject.subTotalWidth = ui.originalSize.width + paramsObject.nextColumn.outerWidth();
  }

  static onResize(ui, paramsObject, element, cell) {
    let nextColumnWidth = paramsObject.subTotalWidth - ui.size.width;
    let resizedColumnSelector = '.table-body form #' + $(cell).attr('data-header-cell-name') + '-wrapper';
    resizedColumnSelector = resizedColumnSelector.replace(/:/i, '\\:');
    element.find(resizedColumnSelector).width(paramsObject.subTotalWidth - nextColumnWidth).next().width(nextColumnWidth);
    paramsObject.nextColumn.width(nextColumnWidth);
  }

  static onResizeStop(ui, paramsObject, widget, cellName) {
    widget.config.styles.columns[cellName].width = ui.size.width;
    widget.config.styles.columns[paramsObject.nextColumn.attr('data-header-cell-name')].width = paramsObject.nextColumn.width();
    widget.control.getBaseWidget().saveConfigWithoutReload(widget.config);
  }

  handleWidgetReady(groupsCount) {
    if (groupsCount === 0) {
      this.publishWidgetReadyEvent();
    } else {
      let selectedObjectsInitialziedCount = 0;
      this.selectedObjects.forEach((selectedObject)=>{
        selectedObject.eventEmitter = new EventEmitter();
        let subscriptionDefinition = selectedObject.eventEmitter.subscribe('formInitialized',()=>{
          selectedObjectsInitialziedCount += 1;
          if(selectedObjectsInitialziedCount === this.selectedObjects.length){
            this.applyResizable(this.$element, this.$element.find('.header-cell'));
            this.publishWidgetReadyEvent();
          }
          subscriptionDefinition.unsubscribe();
        });
      });
    }
  }

  getGroupByCodelists(groupBy) {
    let codelists = new Set();
    return this.propertiesSelectorHelper.getDefinitionsArray(this.config, {}, this.context).then((definitions) => {
      definitions.forEach((definition) => {
        let properties = this.propertiesSelectorHelper.flattenDefinitionProperties(definition);
        let property = _.find(properties, (prop) => {
          return prop.uri === groupBy;
        });
        if (property) {
          if (property.codelist) {
            codelists.add(property.codelist);
          }
        }
      });
      if (codelists.size !== 0) {
        return [...codelists];
      }
    });
  }

  convertResponseToModel(groupedByObjectsResponse) {
    let name = this.config.groupBy.name;
    if (this.codelists) {
      this.createCodelistGroupingModel(groupedByObjectsResponse, name);
    } else {
      name = AggregatedTable.HEADER;
      this.createObjectPropertyGroupingModel(groupedByObjectsResponse, name);
    }

    this.headers = [
      {name: 'count', labels: [this.labels.numberColumnLabel]},
      {name, labels: [this.config.groupBy.label]}
    ];
    this.setColumnsWidth(this.headers);
  }

  createCodelistGroupingModel(groupedByObjectsResponse, name) {
    let promises = [];
    this.codelists.forEach((number) => {
      promises.push(this.codelistRestService.getCodelist({codelistNumber: number}));
    });

    this.promiseAdapter.all(promises).then((results) => {
      let codelisValuesResponse = [];
      results.forEach((result) => {
        codelisValuesResponse = codelisValuesResponse.concat(result.data);
      });
      this.total = AggregatedTable.calculateTotal(groupedByObjectsResponse.aggregated);
      let aggregated = groupedByObjectsResponse.aggregated[this.config.groupBy.name];
      let selectedObjects = [];
      Object.keys(aggregated).forEach((key) => {
        let labels = codelisValuesResponse.reduce((filtered, current) => {
          if (current.value === key && !_.includes(filtered, current.label)) {
            filtered.push(current.label);
          }
          return filtered;
        }, []);

        let label;
        let count;
        if (!labels) {
          this.logger.warn(`Can't retrieve the label for code value ${key}! Probably it is missing in the codelist`);
          label = key;
          count = aggregated[key];
        } else {
          label = labels.join(', ');
          count = aggregated[key];
        }

        let selectedObject = AggregatedTable.createModels(new InstanceModel({
          'count': {
            'value': count
          },
          [name]: {
            'value': label
          }
        }), new DefinitionModel({
          'fields': [
            {
              'identifier': 'count',
              'dataType': 'text',
              'displayType': 'READ_ONLY'
            },
            {
              'identifier': name,
              'dataType': 'text',
              'displayType': 'READ_ONLY'
            }
          ]
        }));

        selectedObjects.push(selectedObject);
      });
      this.selectedObjects = selectedObjects;
      this.handleWidgetReady(AggregatedTable.calculateGroupsCount(groupedByObjectsResponse.aggregated));

      this.swapColumns(this.config.columnsOrder);
    });
  }

  createObjectPropertyGroupingModel(groupedByObjectsResponse, name) {
    let aggregated = groupedByObjectsResponse.aggregated[this.config.groupBy.name];
    let instanceUris = [];
    let selectedObjects = [];
    Object.keys(aggregated).forEach(function (key) {
      instanceUris.push(key);
    });

    if (instanceUris.length > 0) {
      this.instanceRestService.loadBatch(instanceUris, {params: {properties: [AggregatedTable.HEADER]}}).then((response) => {
        // At the moment permissions are not applied to group by query. That's why we need to recalculate total for object properties
        this.total = 0;
        response.data.forEach((instance) => {
          this.total += aggregated[instance.id];
          let selectedObject = AggregatedTable.createModels(new InstanceModel({
            'count': {
              'value': aggregated[instance.id]
            },
            [name]: {
              'value': instance.headers.compact_header
            }
          }), new DefinitionModel({
            'fields': [
              {
                'identifier': 'count',
                'dataType': 'text',
                'displayType': 'READ_ONLY'
              },
              {
                'identifier': name,
                'dataType': 'text',
                'displayType': 'READ_ONLY',
                'control': [{
                  'identifier': 'INSTANCE_HEADER'
                }]
              }
            ]
          }));

          selectedObjects.push(selectedObject);
        });
        this.selectedObjects = selectedObjects;
        this.handleWidgetReady(response.data.length);
        this.swapColumns(this.config.columnsOrder);
      });
    } else {
      this.setError(ERROR_MESSAGE_KEY);
      this.selectedObjects = [];
      this.publishWidgetReadyEvent();
    }
  }

  static createModels(instanceModel, definitionModel) {
    return {
      models: {
        validationModel: instanceModel,
        viewModel: definitionModel
      }
    };
  }

  static calculateGroupsCount(result) {
    let count = 0;
    Object.keys(result).forEach((groupKey) => {
      count += Object.keys(result[groupKey]).length;
    });
    return count;
  }

  static calculateTotal(result) {
    let total = 0;
    Object.keys(result).forEach((groupKey) => {
      let group = result[groupKey];
      Object.keys(group).forEach((key) => {
        total += group[key];
      });
    });
    return total;
  }

  ngOnDestroy() {
    if (this.interval) {
      this.$interval.cancel(this.interval);
    }
  }
}

AggregatedTable.HEADER = 'compact_header';
