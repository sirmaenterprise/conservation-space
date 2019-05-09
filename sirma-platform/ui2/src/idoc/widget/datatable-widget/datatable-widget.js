import {View, Inject, NgScope, NgElement, NgInterval} from 'app/app';
import {Widget} from 'idoc/widget/widget';
import {EventEmitter} from 'common/event-emitter';
import {Eventbus} from 'services/eventbus/eventbus';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {LAYOUT, LABEL_POSITION_HIDE} from 'form-builder/form-wrapper';
import {DefinitionService} from 'services/rest/definition-service';
import {DialogService} from 'components/dialog/dialog-service';
import {LocationAdapter} from 'adapters/angular/location-adapter';
import {PropertiesSelectorHelper, COMMON_PROPERTIES} from 'idoc/widget/properties-selector/properties-selector-helper';
import {SELECT_OBJECT_AUTOMATICALLY, SELECT_OBJECT_MANUALLY} from 'idoc/widget/object-selector/object-selector';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {DEFAULT_PAGE_SIZE} from 'idoc/widget/datatable-widget/datatable-widget-config';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TooltipAdapter} from 'adapters/tooltip-adapter';
import {NavigatorAdapter} from 'adapters/navigator-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {INSTANCE_HEADERS, NO_HEADER} from 'instance-header/header-constants';
import {MODE_PRINT, MODE_PREVIEW} from 'idoc/idoc-constants';
import {DefinitionModel, DefinitionModelProperty} from 'models/definition-model';
import {InstanceModelProperty} from 'models/instance-model';
import {ModelUtils} from 'models/model-utils';
import {UrlUtils} from 'common/url-utils';
import _ from 'lodash';
import 'johnny/jquery-sortable';
import 'search/components/common/pagination';
import {Logger} from 'services/logging/logger';
import 'idoc/widget/datatable-widget/datatable-filter/datatable-filter';
import {PropertiesRestService} from 'services/rest/properties-service';
import {ORDER_ASC, ORDER_DESC} from 'search/order-constants';
import {ResizeDetectorAdapter} from 'adapters/resize-detector-adapter';
import {DatatableResizableIntegration} from 'idoc/widget/datatable-widget/datatable-resizable-integration';
import {DatatableSortableIntegration} from 'idoc/widget/datatable-widget/datatable-sortable-integration';

import template from './datatable-widget.html!text';
import './datatable-widget.css!';

const DTW_COLUMN_MIN_WIDTH = 150;

@Widget
@View({
  template
})
@Inject(NgScope, DefinitionService, PropertiesSelectorHelper, PromiseAdapter, ObjectSelectorHelper, TranslateService, TooltipAdapter,
  Eventbus, LocationAdapter, DialogService, NgElement, NgInterval, Logger, PropertiesRestService, ResizeDetectorAdapter, DatatableResizableIntegration, DatatableSortableIntegration)
export class DatatableWidget {

  constructor($scope, definitionService, propertiesSelectorHelper, promiseAdapter, objectSelectorHelper, translateService, tooltipAdapter, eventbus, locationAdapter, dialogService, $element, $interval, logger, propertiesRestService, resizeDetectorAdapter, datatableResizableIntegration, datatableSortableIntegration) { // NOSONAR
    this.$element = $element;
    this.eventbus = eventbus;
    this.definitionService = definitionService;
    this.dialogService = dialogService;
    this.locationAdapter = locationAdapter;
    this.propertiesSelectorHelper = propertiesSelectorHelper;
    this.promiseAdapter = promiseAdapter;
    this.objectSelectorHelper = objectSelectorHelper;
    this.translateService = translateService;
    this.tooltipAdapter = tooltipAdapter;
    this.$scope = $scope;
    this.$interval = $interval;
    this.logger = logger;
    this.propertiesRestService = propertiesRestService;
    this.resizeDetectorAdapter = resizeDetectorAdapter;
    this.datatableResizableIntegration = datatableResizableIntegration;
    this.datatableSortableIntegration = datatableSortableIntegration;
  }

  ngOnInit() {
    this.config.showRelatedProperties = this.config.showRelatedProperties || false;

    this.multivalueEntryText = this.translateService.translateInstant('dtw.entry.multivalue.text');

    // Handle old instances where widget config has different format
    Object.keys(this.config.selectedProperties).forEach((definition) => {
      this.config.selectedProperties[definition] = PropertiesSelectorHelper.transformSelectedProperies(this.config.selectedProperties[definition]);
    });
    this.config.selectedSubPropertiesData = this.config.selectedSubPropertiesData || {};

    this.initializedFormsCount = 0;
    this.widgetConfig = this.widgetConfig || {};

    if (this.config.displayTableHeaderRow === undefined) {
      this.config.displayTableHeaderRow = true;
    }
    this.setStyles();
    this.config.columnsOrder = this.config.columnsOrder || {columns: {}};

    this.formConfig = this.createFormConfig();

    this.assignLoadModelsTriggerWatchers();

    // Don't watch for form view mode in modeling mode because the widget don't render data after all.
    if (!this.context.isModeling()) {
      this.$scope.$watch(() => {
        return this.context.getMode();
      }, () => {
        this.formConfig.formViewMode = this.context.getMode().toUpperCase();
        this.toggleResizeAndReorder();
      });
    } else {
      // In templating mode, widget is rendered in preview
      this.formConfig.formViewMode = MODE_PREVIEW.toUpperCase();
    }

    this.addWidgetResizeHandler();

    this.$scope.$watch(() => {
      return this.config.grid;
    }, () => {
      this.formConfig.styles.grid = this.config.grid;
    });

    // Always show all results in print mode
    if (UrlUtils.getParameter(this.locationAdapter.url(), 'mode') === MODE_PRINT) {
      this.config.pageSize = '0';
    }

    this.paginationConfig = {
      showFirstLastButtons: true,
      page: 1,
      pageSize: parseInt(this.config.pageSize || DEFAULT_PAGE_SIZE)
    };

    this.searchArguments = {
      pageNumber: 1
    };

    this.paginationCallback = (params) => {
      this.searchArguments.pageNumber = params.pageNumber;
      this.loadObjects(false);
    };

    this.$scope.$watch(() => {
      return this.config.pageSize;
    }, (pageSize) => {
      this.paginationConfig.pageSize = parseInt(pageSize || DEFAULT_PAGE_SIZE);
      this.paginationConfig.page = 1;
      this.searchArguments.pageSize = this.paginationConfig.pageSize;
      this.searchArguments.pageNumber = 1;
    });

    this.initializeTooltips();

    this.initializeFilterRow();
  }

  createFormConfig() {
    return this.formConfig || {
      layout: LAYOUT.TABLE,
      labelPosition: LABEL_POSITION_HIDE,
      styles: this.config.styles,
      hintClass: 'datatable-tooltip',
      enableHint: false,
      results: {
        config: {
          renderMenu: this.context.isPreviewMode(),
          reloadMenu: true,
          placeholder: 'widget'
        }
      }
    };
  }

  addWidgetResizeHandler() {
    this.handleWidgetResize = _.debounce(() => {
      // Debounce uses setTimeout and is executed outside of Angular's lifecycle
      this.$scope.$evalAsync(() => {
        this.setColumnsWidth(this.widgetConfig.headers);
      });
    }, 100);

    // Table size should be recalculated if widget size is changed
    this.resizeListener = this.resizeDetectorAdapter.addResizeListener(this.control.element[0], () => {
      if (this.widgetConfig.headers) {
        this.handleWidgetResize();
      }
    });
  }

  initializeTooltips() {
    $(this.control.element).on('mouseover', (event) => {
      let tooltipElement = $(event.target).closest('.form-group');
      let tooltipMessage = $.trim(tooltipElement.find('.messages').text());

      // Recreate tooltip only if there's actual changes in it's value. Otherwise FF causes endless loop
      if (tooltipElement.attr('data-original-title') !== tooltipMessage) {
        this.tooltipAdapter.tooltip(tooltipElement, {
          placement: 'auto right',
          html: 'true',
          trigger: 'hover',
          title: tooltipMessage
        }, true);
      }
      if (tooltipMessage === '') {
        this.tooltipAdapter.hide(tooltipElement);
      }
    });
  }

  initializeFilterRow() {
    this.filterConfig = {};

    this.isVersion().then((isVersion) => {
      if (isVersion) {
        return;
      }

      let initialCriteria = this.control.getDataFromAttribute('data-filter-criteria');
      if (!_.isEmpty(initialCriteria)) {
        this.filterConfig.filterCriteria = initialCriteria;
      }

      if (this.context.isEditMode()) {
        this.$scope.$watch(() => {
          return this.config.insertFilterRow;
        }, () => {
          // reset criteria each time when insert filter row configuration is changed
          this.setFilterCriteria(undefined);
          this.showFilterRow = this.config.insertFilterRow;
        });
      } else if (this.context.isPreviewMode() && this.config.insertFilterRow) {
        this.control.subscribe('toggleFilterRow', () => {
          this.showFilterRow = !this.showFilterRow;
          // reset filter and filter criteria if filter row is hidden i.e. display unfiltered results
          if (!this.showFilterRow) {
            this.onFilter(undefined);
          }
        });
      }
    });
  }

  /**
   * Recalculate table body and table header panels width. This recalculation is needed when for some reason widget width
   * is change or columns width is reset.
   * @param elementWidth width of datatable widget
   */
  recalculatePanelsWidth(elementWidth) {
    if (this.context.isPrintMode()) {
      this.panelsWidth = {};
    } else {
      if (elementWidth >= this.actualPanelsWidth) {
        this.panelsWidth = elementWidth;
      } else {
        this.panelsWidth = this.actualPanelsWidth;
      }
    }
    DatatableResizableIntegration.setTableWrapperWidth(this.$element, this.panelsWidth);
  }

  isResizableDisabled() {
    return this.context.isPreviewMode();
  }

  isSortableDisabled() {
    return this.isResizableDisabled() || (this.widgetConfig.headers && this.widgetConfig.headers.length === 1);
  }

  toggleResizeAndReorder() {
    DatatableResizableIntegration.disableResizable($(this.control.element).find('.header-cell'), this.isResizableDisabled.bind(this));
    DatatableSortableIntegration.disableSortable($(this.control.element).find('.table-header'), this.isSortableDisabled.bind(this));
  }

  isPaginationHidden() {
    return this.config.showFirstPageOnly || this.paginationConfig.pageSize === 0;
  }

  showResultSize() {
    return this.paginationConfig.resultSize > 0;
  }

  assignLoadModelsTriggerWatchers() {
    this.$scope.$watchCollection(() => {
      let watchConditions = [this.config.selectedProperties, this.config.selectObjectMode, this.config.selectedObjects];
      if (this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
        watchConditions.push(this.config.criteria);
      }
      return watchConditions;
    }, () => {
      // Resetting the pagination, because the new criteria might return fewer results and cause misbehaving with the pages.
      this.searchArguments.pageNumber = 1;
      this.paginationConfig.page = 1;
      // reset sorting when configuration changes
      delete this.orderBy;
      delete this.orderDirection;
      delete this.orderByCodelistNumbers;
      this.config.showRelatedProperties = this.config.selectedSubPropertiesData.showRelatedProperties;
      this.publishOrderChangedEvent();
      this.loadModels(true);
    });
  }

  /**
   * Loads the models and objects in the widget. Models should be loaded only once after widget configuration is changed.
   * For navigating between pages or filtering use loadObjects method which doesn't load models again.
   *
   * @param resetObjects shows if shared objects in the context should be reset or not. [true/false]
   * @returns {*}
   */
  loadModels(resetObjects) {
    this.setFilterCriteria(undefined);
    // reset tooltips when models are changed
    this.sortTooltipsInitialized = false;

    delete this.errorMessage;
    this.paginationConfig.disabled = true;

    let loaderConfig;
    let headers = _.cloneDeep(this.widgetConfig.headers);

    let headersLoader = this.generateHeaders(this.config.selectedProperties);
    let objectsLoaderConfig = this.getObjectsLoaderConfig();

    return this.promiseAdapter.all([headersLoader, objectsLoaderConfig]).then(([headersModel, config]) => {
      this.widgetConfig.headers = headersModel;
      loaderConfig = config;

      let objectsLoader = this.getObjectsLoader(loaderConfig, resetObjects);
      let searchablePropertiesLoader = this.getSearchableProperties();

      return this.promiseAdapter.all([objectsLoader, searchablePropertiesLoader]);
    }).then((loadersResults) => {
      let objects = loadersResults[0];
      let searchableProperties = loadersResults[1];

      this.resetOrderAndWidthConfig(headers, this.widgetConfig.headers);
      this.widgetConfig.headers = this.orderHeaders(this.widgetConfig.headers);

      this.filterConfig.headers = this.widgetConfig.headers;
      this.addSearchInformationToHeaders(this.widgetConfig.headers, searchableProperties);

      this.setColumnsWidth(this.widgetConfig.headers);

      this.afterObjectsLoaded(loaderConfig, objects);
    }).catch((error) => {
      this.logger.error(error);
      this.fireWidgetReadyEvent();
    });
  }

  shouldLoadRelatedObjects() {
    return this.config.showRelatedProperties;
  }

  /**
   * Selected objects are loaded first. Related objects are loaded afterwards as their ids are now known in advance.
   * After related objects are loaded they are merged in the result maintaining the relations.
   *
   * @param config The loaders config containing which object properties should be loaded.
   * @param resetObjects Flag which if true, says if the widget is fully reloaded with the new loaded objects or just
   * updated with the new objects registered to it.
   * @returns A promise which resolves with object containing the loaded objects.
   */
  getObjectsLoader(config, resetObjects) {
    this.filterConfig.disabled = true;

    // either manually set order or config (search) order
    this.searchArguments.orderBy = this.orderBy || this.config.orderBy;
    this.searchArguments.orderDirection = this.orderDirection || this.config.orderDirection;
    this.searchArguments.orderByCodelistNumbers = this.orderByCodelistNumbers || this.config.orderByCodelistNumbers;

    let sharedObjects;

    return this.objectSelectorHelper.getSelectedObjects(config, this.context, this.searchArguments).then((selectedObjectsResponse) => {

      return this.context.getSharedObjects(selectedObjectsResponse.results, this.control.getId(), resetObjects, this.getConfig()).then((loadedSharedObjects) => {

        sharedObjects = loadedSharedObjects;

        return this.loadRelatedObjects(sharedObjects, config.selectedProperties);

      }).then((relatedObjectsMap) => {

        this.paginationConfig.disabled = false;

        if (sharedObjects.notFound.length > 0 && this.config.selectObjectMode === SELECT_OBJECT_MANUALLY) {
          this.objectSelectorHelper.removeSelectedObjects(this.config, sharedObjects.notFound);
          this.control.saveConfig(this.config);
        }

        let result = {
          total: selectedObjectsResponse.total,
          objects: sharedObjects.data || [],
          relatedObjectsMap
        };

        if (this.context.isModeling() && this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
          result.objects = [];
          result.total = 0;
          // fire widget ready event since nothing will be rendered.
          this.fireWidgetReadyEvent();
        }

        this.filterConfig.disabled = false;

        return result;
      });

    }).catch((message) => {
      // If no object is selected to be displayed mark widget as ready for print
      this.fireWidgetReadyEvent();
      if (!this.context.isModeling()) {
        this.errorMessage = message.reason;
      }
      this.filterConfig.disabled = false;
      return [];
    });
  }

  /**
   * Loads related objects referred configured in the widget configuration. The process is follows the steps below:
   * - Exclude COMMON_PROPERTIES from selectedPropertiesModel as what's there should be found in every other definition in the model.
   * - From selectedPropertiesModel find definitions where there are selected properties from relation - there is a selectedPropertiesModel attribute.
   * -- Accumulate relatedProperties in a map with key the definitionId:
   *    { DT20001: [ 'emf:createdBy' ], PR10002: [ 'emf:createdBy', 'emf:references' ] }
   * - Iterate sharedObjects:
   * -- Check if the definitionId from the InstanceObject is found in the relatedProperties map and if found:
   * --- If there is more than one instance id in the respective object property, DON'T LOAD THOSE
   * --- Otherwise accumulate the ids from the respective object property in relatedObjects map. Store them in a map by instance id.
   *    { 'instanceId': [InstanceObject], 'instanceId': [InstanceObject] }
   * --- Add configuration which properties should be loaded for every related object.
   * - Load relatedObjects through idoc context.
   * - Use loaded related objects to build DTW models.
   *
   * @param sharedObjects The loaded objects which should be rendered in the widget. Those objects are obtained through
   * IdocContext and are registered in the context (shared objects).
   * @param selectedPropertiesModel A model which maps selected properties to definition or semantic class.
   * @returns Promise which resolves with a RelatedObjectsMap
   */
  loadRelatedObjects(sharedObjects, selectedPropertiesModel) {
    let relatedProperties = {};
    Object.keys(selectedPropertiesModel).forEach((definitionId) => {
      if (definitionId !== COMMON_PROPERTIES) {
        let selectionByDefinition = selectedPropertiesModel[definitionId];
        Object.keys(selectionByDefinition).forEach((propertyName) => {
          if (selectionByDefinition[propertyName].selectedProperties) {
            if (relatedProperties[definitionId] === undefined) {
              relatedProperties[definitionId] = []; // eventually use Set
            }
            relatedProperties[definitionId].push(propertyName);
          }
        });
      }
    });

    let relatedObjectsMap = new RelatedObjectsMap();
    let relatedObjectLoaders = [];

    sharedObjects.data.forEach((sharedObject) => {

      // Check by definitionId first. If selected properties are mapped to semantic class, then get them by class.
      let relatedPropertiesByType = DatatableWidget.getRelatedPropertiesByType(sharedObject, relatedProperties);

      relatedPropertiesByType.forEach((relatedProperty) => {

        let relatedObjectIds;
        if (sharedObject.getPropertyValue(relatedProperty)) {
          relatedObjectIds = sharedObject.getPropertyValue(relatedProperty).getValue();
        }

        if (relatedObjectIds) {
          relatedObjectsMap.addRelatedIds(sharedObject.getId(), relatedProperty, relatedObjectIds);

          let config;
          // partially loaded objects are forbidden in edit mode because of the conditions
          if (!this.context.isEditMode()) {
            config = DatatableWidget.buildRelatedObjectsLoaderConfig(relatedProperty, this.widgetConfig.headers);
          }

          // Load related objects in parallel by relation type. This means for every relation for which there are
          // selected properties will be executed single request for related objects to be loaded. And for every
          // selected object in the DTW could have multiple such request according to the widget's configuration.
          relatedObjectLoaders.push(this.context.getSharedObjects(relatedObjectIds, this.control.getId(), false, config));
        }
      });

    });

    return this.promiseAdapter.all(relatedObjectLoaders).then((relatedObjects) => {
      // - relatedObjects is an array holding results from each loader in format:
      // [ { data: [ {"id":"id:1_1","shouldReload":false}], notFound: [] }, { data: [{"id":"id:2_1","shouldReload":false}], notFound: [] } ]

      // Now match objects from the relatedObjects to relatedObjectsMap
      relatedObjectsMap.populateObjects(relatedObjects);

      return relatedObjectsMap;
    });
  }

  static getRelatedPropertiesByType(sharedObject, relatedProperties) {
    let type = sharedObject.getModels().definitionId;
    if (!relatedProperties[type]) {
      // The type of the object should be always last in the semantic hierarchy, so we need to start traversing the
      // hierarchy backwards. We do the traversing and not just picking the class of the current object, because during
      // the widget configuration might have been selected a type which is not leaf and we need to find the first one
      // that match in the related properties mapping. For example: widget configured with auto selection and type
      // http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#CulturalObject, but result of type
      // http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Drawing. Obviously using the later would not
      // match in the mapping, so we find if the object hierarchy contains type that match and use it instead.
      let semanticHierarchy = sharedObject.getSemanticHierarchy().reverse();
      semanticHierarchy.some(clazz => {
        if (relatedProperties[clazz]) {
          type = clazz;
          return true;
        }
      });
    }
    return relatedProperties[type] || [];
  }

  static buildRelatedObjectsLoaderConfig(relatedProperty, headersModel) {
    let propertiesToLoad = headersModel.filter((header) => {
      return header.relationName === relatedProperty;
    }).map((header) => {
      return header.propertyName;
    });
    return {params: {properties: propertiesToLoad || []}};
  }

  loadObjects(resetObjects) {
    delete this.errorMessage;
    let loaderConfig;
    return this.getObjectsLoaderConfig().then((config) => {
      loaderConfig = config;
      return this.getObjectsLoader(config, resetObjects);
    }).then((response) => {
      this.afterObjectsLoaded(loaderConfig, response);
    });
  }

  /**
   * Returns configuration for object loader.
   * Configuration is returned as is if select mode is manually and there are no selected objects or if current object is version
   * Otherwise
   * @returns promise which resolves with the proper configuration
   */
  getObjectsLoaderConfig() {
    return this.isVersion().then((isVersion) => {
      if ((this.config.selectObjectMode === SELECT_OBJECT_MANUALLY && this.config.selectedObjects.length === 0) || isVersion) {
        return this.config;
      }
      // In all cases (except when manually selection with no selected objects) a search should be performed in order to sort the results
      return ObjectSelectorHelper.getFilteringConfiguration(this.config, this.filterConfig.filterCriteria);
    });
  }

  afterObjectsLoaded(actualConfig, loadersResults) {
    this.buildTableModel(loadersResults);
    let results = {};

    if (actualConfig.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
      this.widgetConfig.objectsOnPage = this.orderObjectsOnPage(this.widgetConfig.selectedObjects);
      this.paginationConfig.resultSize = loadersResults.total;
      results.data = loadersResults.objects;
      // !!! this is the total returned by the backend and not the actual length including the selected related objects columns!!!
      results.size = loadersResults.total;
    } else {
      let objectsOnFirstPage = this.paginationConfig.pageSize === 0 ? this.widgetConfig.selectedObjects :
        this.widgetConfig.selectedObjects.slice(0, this.paginationConfig.pageSize);
      this.widgetConfig.objectsOnPage = this.orderObjectsOnPage(objectsOnFirstPage);
      this.paginationConfig.resultSize = this.widgetConfig.selectedObjects.length;
      results.data = this.widgetConfig.selectedObjects;
      results.size = this.widgetConfig.selectedObjects.length;
    }

    this.control.publish('dataLoaded', results);
  }

  onFormInitialized(results) {
    // TODO: Separate logic for loading models (headers, etc.) and loading objects (instances displayed in DTW - initially, after sorting, after filtering, pagination). This should be done only when models are (re)loaded
    // Wait for the table to be rendered before applying resizable and sortable and firing WidgetReadyEvent
    if (results.length > 0 || this.context.isModeling()) {

      // Wait for all objects to be loaded in the DTW
      if (this.initializedFormsCount === results.length - 1 || this.context.isModeling()) {
        this.waitForComponents();
      } else {
        this.initializedFormsCount++;
      }

    }
  }

  waitForComponents() {
    this.controlsLoadingPoller = this.$interval(() => {
      // Wait for all the headers to be rendered
      this.handleTableHeaders();
    }, 100);
  }

  handleTableHeaders() {
    this.headerCells = this.$element.find('.header-cell');
    if (this.areHeadersRendered(this.headerCells, this.widgetConfig.headers)) {
      this.$interval.cancel(this.controlsLoadingPoller);
      this.initializedFormsCount = 0;
      let tableHeader = this.$element.find('.table-header');
      let tableBody = this.$element.find('.table-body');
      this.datatableResizableIntegration.applyResizable(this.$element, this, this.headerCells, tableHeader, tableBody, DTW_COLUMN_MIN_WIDTH, this.isResizableDisabled.bind(this));
      this.datatableSortableIntegration.applySortable(tableHeader, this, this.isSortableDisabled.bind(this));
      this.config.columnHeaders = this.widgetConfig.headers;
      this.control.getBaseWidget().saveConfigWithoutReload(this.config);
      this.addSortingTooltips();
      if (NavigatorAdapter.isSafari()) {
        this.$element[0].scrollLeft = 0;
      }
      this.fireWidgetReadyEvent();
    }
  }

  /**
   * Checking if headers are rendered by comparing DOM elements and headers array.
   * This methods presumes that headers in the DOM follow the same order as headers array.
   * @param headerCells DOM selection of header cells
   * @param headers array containing header objects to be rendered
   * @returns {boolean}
   */
  areHeadersRendered(headerCells, headers) {
    let headerCellNames = headerCells.map((index, elem) => {
      return $(elem).data('header-cell-name');
    }).get();

    let headerNames = headers.map((header) => {
      return header.name;
    });
    return _.isEqual(headerCellNames, headerNames);
  }

  hasSelectedProperties(properties, definitions) {
    return definitions.some((definition) => {
      return !!Object.keys(properties[definition]).length;
    });
  }

  getConfig() {
    let config = {};
    if (!this.context.isEditMode()) {
      let propertiesToLoad = [this.config.instanceHeaderType];
      Object.keys(this.config.selectedProperties).forEach((definition) => {
        Object.keys(this.config.selectedProperties[definition]).forEach((property) => {
          if (!_.includes(propertiesToLoad, property)) {
            propertiesToLoad.push(property);
          }
        });
      });
      config = {params: {properties: propertiesToLoad}};
    }
    return config;
  }

  setStyles() {
    if (!this.config.styles) {
      this.config.styles = {
        columns: {},
        grid: this.config.grid
      };
    }
  }

  /**
   * Clear saved columns order and width if property is removed or new is added
   * @param oldProperties selected properties saved in config
   * @param newProperties new selected properties
   */
  resetOrderAndWidthConfig(oldProperties, newProperties) {
    if (oldProperties && !this.propertiesAreSame(oldProperties, newProperties)) {
      this.config.styles.columns = {};
      this.config.columnsOrder.columns = {};
      this.control.getBaseWidget().saveConfigWithoutReload(this.config);
    }
  }

  /**
   * Detect if old selected properties and new once are same
   * @param oldProperties selected properties saved in config
   * @param newProperties new selected properties
   * @returns boolean true if there's no change in selection or false otherwise
   */
  propertiesAreSame(oldProperties, newProperties) {
    if (oldProperties.length !== newProperties.length) {
      return false;
    }
    let uniqueOldProperties = this.getUniqueProperties(oldProperties, newProperties);
    let uniqueNewProperties = this.getUniqueProperties(newProperties, oldProperties);

    if (!uniqueOldProperties.length && !uniqueNewProperties.length) {
      return true;
    }
    if (uniqueOldProperties.length === 1 && uniqueNewProperties.length === 1) {
      let entityColumn = this.translateService.translateInstant('dtw.column.header');
      if (uniqueOldProperties[0].labels[0] === entityColumn && uniqueNewProperties[0].labels[0] === entityColumn) {
        this.config.styles.columns[uniqueNewProperties[0].name] = this.config.styles.columns[uniqueOldProperties[0].name];
        this.config.columnsOrder.columns[uniqueNewProperties[0].name] = this.config.columnsOrder.columns[uniqueOldProperties[0].name];
        delete this.config.styles.columns[uniqueOldProperties[0].name];
        delete this.config.columnsOrder.columns[uniqueOldProperties[0].name];
        this.control.getBaseWidget().saveConfigWithoutReload(this.config);
        return true;
      }
    }
    return false;
  }

  /**
   * Filter array of properties objects and return unique once
   * @param properties selected properties saved in config
   * @param newProperties new selected properties
   * @returns array of unique properties
   */
  getUniqueProperties(properties, newProperties) {
    return properties.filter(function (property) {
      return !newProperties.some(function (newProperty) {
        return property.name === newProperty.name;
      });
    });
  }

  /**
   * Order datatable widget headers corresponding to saved configuration
   * @param headers array of properties
   * @returns Array of ordered properties
   */
  orderHeaders(headers) {
    let ordersMap = this.config.columnsOrder.columns;
    let reorderedHeaders = [];
    headers.forEach(function (header) {
      if (ordersMap[header.name]) {
        reorderedHeaders[ordersMap[header.name].index] = header;
      }
    });
    if (reorderedHeaders.length > 0) {
      return reorderedHeaders;
    }
    return headers;
  }

  orderObjectsOnPage(objects) {
    let ordersMap = this.config.columnsOrder.columns;
    objects.forEach(function (object) {
      let fields = object.models.viewModel.fields;
      fields.forEach(function (field, index) {
        if (ordersMap[field.identifier]) {
          fields.splice(ordersMap[field.identifier].index, 0, fields.splice(index, 1)[0]);
        }
      });
    });
    return objects;
  }

  /**
   * Set datatable widget headers width corresponding to saved configuration
   * @param headers array of properties
   */
  setColumnsWidth(headers) {
    if (this.context.isPrintMode() || !this.config.reseted) {
      this.config.styles.columns = {};
      this.config.reseted = true;
    }
    let columnsCount = headers.length;
    let ratio = 1;
    let defaultColumnWidth = this.$element.width() / columnsCount;
    this.actualPanelsWidth = 0;
    let columnWidths = {};

    if (this.config.widgetWidth && this.config.widgetWidth !== this.$element.width()) {
      ratio = this.config.widgetWidth / this.$element.width();
    }

    headers.forEach((header) => {
      let columnWidth = defaultColumnWidth;
      if (this.config.styles.columns[header.name]) {
        columnWidth = (this.config.styles.columns[header.name].calculatedWidth || this.config.styles.columns[header.name].width) / ratio;
      }
      let columnStyle = {
        calculatedWidth: columnWidth
      };
      if (columnWidth < DTW_COLUMN_MIN_WIDTH) {
        columnWidth = DTW_COLUMN_MIN_WIDTH;
      }
      columnStyle.width = columnWidth;
      columnWidths[header.name] = columnStyle;

      // Names of some headers contain colon (ex. emf:isThumbnailOf)
      let safeHeaderName = header.name.replace(/:/g, '\\:');
      let tableBodyColumn = this.$element.find(`.table-body form #${safeHeaderName}-wrapper, .filter-cell[data-filter-cell-name=${safeHeaderName}], .header-cell[data-header-cell-name=${safeHeaderName}]`);
      tableBodyColumn.width(columnWidth);

      this.actualPanelsWidth += parseInt(columnWidth);
    });
    this.setStyles();
    this.config.styles.columns = columnWidths;
    this.formConfig.styles.columns = columnWidths;

    this.config.widgetWidth = this.$element.width();
    this.recalculatePanelsWidth(this.$element.width());
  }

  generateHeaders(selectedProperties) {
    let headersArray = [];

    return this.isVersion().then((isVersion) => {
      this.formConfig.instanceLinkType = this.config.instanceHeaderType;
      let headerType = this.config.instanceHeaderType;
      if (headerType !== NO_HEADER) {
        headersArray.push({
          name: headerType,
          labels: [this.translateService.translateInstant('dtw.column.header')],
          // sort by breadcrumb header aka altTitle
          uri: 'emf:altTitle',
          type: 'string',
          showSortIcon: !isVersion
        });
      }

      if (!selectedProperties) {
        return this.promiseAdapter.resolve(headersArray);
      }

      let definitionsIdentifiers = Object.keys(selectedProperties);
      // avoid unnecessary load of all definitions.
      if (!this.hasSelectedProperties(selectedProperties, definitionsIdentifiers)) {
        return this.promiseAdapter.resolve(headersArray);
      }
      let commonPropertiesIndex = definitionsIdentifiers.indexOf(COMMON_PROPERTIES);
      if (commonPropertiesIndex !== -1) {
        definitionsIdentifiers.splice(commonPropertiesIndex, 1);
      }

      return this.definitionService.getFields(definitionsIdentifiers).then((definitions) => {
        let filteredSelection = this.propertiesSelectorHelper.removeMissingSelectedProperties(definitions.data, selectedProperties);

        let headers = new Map();

        this.processDefinition(definitions, filteredSelection, headers, isVersion);

        headersArray.push(...Array.from(headers.values()));

        return headersArray;
      });
    });
  }

  processDefinition(definitions, filteredSelection, headers, isVersion) {
    definitions.data.forEach((definition) => {
      let selectedPropertiesByDefinition = filteredSelection[definition.identifier] || {};
      let flatDefinitionProperties = this.propertiesSelectorHelper.flattenDefinitionProperties(definition);

      Object.keys(selectedPropertiesByDefinition).forEach((propertyIdentifier) => {
        let definitionField = _.find(flatDefinitionProperties, (field) => {
          return field.name === propertyIdentifier;
        });
        let fieldLabel = definitionField ? definitionField.label : propertyIdentifier;
        let fieldHeader = headers.get(propertyIdentifier);
        if (fieldHeader) {
          if (fieldHeader.labels.indexOf(fieldLabel) === -1) {
            fieldHeader.labels.push(fieldLabel);
          }
        } else {
          headers.set(propertyIdentifier, {
            name: propertyIdentifier,
            labels: [fieldLabel],
            uri: definitionField.uri,
            multivalue: definitionField.multivalue,
            showSortIcon: !isVersion && !definitionField.multivalue
          });
        }

        // if current selected property is for relation, then process its selected properties
        let selectedRelatedObjectProperties = DatatableWidget.getSelectedRelatedObjectProperties(selectedPropertiesByDefinition, propertyIdentifier);
        if (selectedRelatedObjectProperties.length) {
          selectedRelatedObjectProperties.forEach((selectedProperty) => {
            // Make a composite key to prevent key collisions between target and related object property names.
            // A composite key is used as property name later on when table model is built to prevent collisions with
            // the base object properties. The original property name is passed under different attribute "propertyName".
            let headerKey = `${propertyIdentifier}:${selectedProperty.name}`;
            headers.set(headerKey, {
              name: headerKey,
              propertyName: selectedProperty.name,
              relationName: propertyIdentifier,
              relationProperty: true,
              labels: [`${fieldLabel}: ${selectedProperty.label}`],
              uri: undefined,
              multivalue: undefined,
              showSortIcon: false
            });
          });
        }
      });
    });
  }

  static getSelectedRelatedObjectProperties(selectedPropertiesByDefinition, propertyIdentifier) {
    return selectedPropertiesByDefinition[propertyIdentifier].selectedProperties || [];
  }

  getColumnHeaderLabel(columnHeader) {
    return columnHeader.labels.join(', ');
  }

  buildTableModel(loadersResults) {
    let loadedObjects = loadersResults.objects || [];
    if (loadedObjects.length === 0) {
      this.widgetConfig.selectedObjects = [];
      if(this.context.isModeling()) {
        this.onFormInitialized(loadedObjects);
      }
    }
    this.widgetConfig.selectedObjects = loadedObjects.map(loadedObject => this.convertViewModel(loadedObject, this.widgetConfig.headers, this.config.selectedProperties, loadersResults));
  }

  /**
   * Build the table model using the before assembled header's model - a.k.a. the table columns. This method is provided
   * with a single result InstanceObject with it's loaded models which are cut off according to which properties and
   * related properties are selected to be visible in the table.
   *
   * @param instanceObject Current table row entry. Every table row is represented by an InstanceObject. In case of
   * multy relation selection, every row entry is build from multiple InstanceModelProperty's.
   * @param headersModel The table header cells model. This is an array of objects containing names labels and other
   * data for the table headers. The table model is created using the header's model.
   * @param selectedProperties Selected properties map gives information which properties of the base object and relation
   * properties are selected to be visible by the user.
   * @param loadersResults Loaded objects which should be rendered in the table.
   * @returns An InstanceObject containing data for the table row entry which should be rendered.
   */
  convertViewModel(instanceObject, headersModel, selectedProperties, loadersResults) {
    const relatedObjectsMap = loadersResults.relatedObjectsMap;
    const instanceId = instanceObject.getId();
    let instanceObjectModels = instanceObject.getModels();
    let instanceSelectedProperties = Object.keys(PropertiesSelectorHelper.getSelectedProperties(instanceObjectModels, selectedProperties));
    let instanceFlatViewModel = instanceObject.getModels().viewModel.flatModelMap;
    let convertedViewModel = {
      fields: []
    };

    // Clone instanceObject and link it to the actual validation model.
    // !!! The result of the clone operation is plain object instead of InstanceObject!!!
    let clonedInstanceObject = instanceObject.clone();
    let clonedInstanceObjectModels = clonedInstanceObject.models;
    clonedInstanceObjectModels.validationModel = instanceObjectModels.validationModel;

    headersModel.forEach((header) => {
      // Could be a field from current object or from a related object
      let definitionField;

      const isRelationProperty = header.relationProperty;

      if (isRelationProperty) {

        const valuesCount = relatedObjectsMap.getRelatedObjects(instanceId, header.relationName).length;

        // if no values -> render empty cell
        // if single value -> render it
        // if more than one value -> render 'Multiple objects' text
        if (valuesCount === 1) {
          const firstRelatedObject = relatedObjectsMap.getFirstRelatedObject(instanceId, header.relationName);
          definitionField = firstRelatedObject.models.viewModel.flatModelMap[header.propertyName];
          if (definitionField) {
            definitionField = definitionField.serialize();
            definitionField = new DefinitionModelProperty(definitionField);

            definitionField.identifier = header.name;
            definitionField.modelProperty.owningRelatedObjectId = firstRelatedObject.id;
            definitionField.modelProperty.subPropertyName = header.propertyName;

            let instanceModelProperty = firstRelatedObject.models.validationModel[header.propertyName];
            instanceModelProperty.subscriptions = [];
            clonedInstanceObject.models.validationModel[header.name] = instanceModelProperty;
          }
        } else if (valuesCount > 1) {
          definitionField = new DefinitionModelProperty(ModelUtils.buildPreviewTextField(header.name));
          clonedInstanceObject.models.validationModel[header.name] = new InstanceModelProperty(ModelUtils.createProperty(this.multivalueEntryText, true));
        }

      } else {
        // get field's current view model if such field exists for the given instanceObject
        definitionField = instanceFlatViewModel[header.name];
      }

      // show field if it is amongst instanceObject's selected properties
      let shouldShowField = INSTANCE_HEADERS[header.name] || (instanceSelectedProperties && instanceSelectedProperties.indexOf(header.name) !== -1) || isRelationProperty;
      if (shouldShowField && definitionField) {
        let fieldModel = _.clone(definitionField.serialize());
        delete fieldModel._displayType;
        delete fieldModel._isMandatory;
        delete fieldModel._preview;
        convertedViewModel.fields.push(fieldModel);
      } else {
        convertedViewModel.fields.push(ModelUtils.buildEmptyCell(header.name));
      }

      // view model must be wrapped in the DefinitionModel class.
      clonedInstanceObjectModels.viewModel = new DefinitionModel(convertedViewModel);
      // The full viewModel is needed for the validation purposes. There are some rules which involve more than
      // one field and when not all involved fields are selected, the rules might not work as expected.
      clonedInstanceObjectModels.fullViewModel = instanceObjectModels.viewModel;
      clonedInstanceObjectModels.id = clonedInstanceObject.id;
    });

    this.initFormInitializedHandlers(clonedInstanceObject, loadersResults.objects);

    // When the widget is configured to render related properties, then it's always in preview.
    if (this.shouldLoadRelatedObjects()) {
      clonedInstanceObject.writeAllowed = false;
    }

    return clonedInstanceObject;
  }

  initFormInitializedHandlers(clonedInstanceObject, objects) {
    clonedInstanceObject.eventEmitter = new EventEmitter();
    let formInitializedSubscription = clonedInstanceObject.eventEmitter.subscribe('formInitialized', () => {
      this.onFormInitialized(objects);
      formInitializedSubscription.unsubscribe();
    });
  }

  showDatatable() {
    if (!this.context.isModeling()) {
      return this.filterConfig.filterCriteria !== undefined ||
        this.widgetConfig.selectedObjects && this.widgetConfig.selectedObjects.length > 0;
    }
    return true;
  }

  getSearchableProperties() {
    return this.propertiesSelectorHelper.getDefinitionsArray(this.config, {}, this.context).then((definitions) => {
      let types = definitions.map((definition) => {
        return definition.identifier;
      });
      return this.propertiesRestService.getSearchableProperties(types);
    });
  }

  /**
   * Appends searchable information (type of the header, codelists, etc.) to headers array.
   * @param headers
   * @param searchableProperties
   */
  addSearchInformationToHeaders(headers, searchableProperties) {
    let searchablePropertiesMap = {};
    searchableProperties.forEach((searchableProperty) => {
      searchablePropertiesMap[searchableProperty.id] = searchableProperty;
    });
    headers.forEach((header) => {
      let searchableProperty = searchablePropertiesMap[header.uri];
      if (searchableProperty) {
        header.type = searchableProperty.type;
        header.codeLists = searchableProperty.codeLists;
      }
    });
  }

  /**
   * Event method passed to datatable filter component and executed each time when datatable filter initiates a filter.
   * @param filterCriteria passed from datatable filter
   */
  onFilter(filterCriteria) {
    if (filterCriteria !== this.filterConfig.filterCriteria) {
      this.setFilterCriteria(filterCriteria);

      // go back to first page
      this.paginationConfig.page = 1;
      this.searchArguments.pageNumber = 1;

      this.loadObjects(false);
    }
  }

  /**
   * Store filter criteria as widget's data attribute,
   * so if widget's component is reloaded for some reason filter criteria can be restored if needed.
   * @param filterCriteria
   */
  setFilterCriteria(filterCriteria) {
    this.filterConfig.filterCriteria = filterCriteria;
    this.control.storeDataInAttribute(this.filterConfig.filterCriteria, 'data-filter-criteria');
  }

  insertFilterRow() {
    return this.showFilterRow && this.filterConfig.headers;
  }

  sortObjects(header) {
    if (!this.filterConfig.disabled) {
      // reset sort icons for all columns
      this.$element.find('[data-header-cell-name] span.sort-icon i').removeClass('fa-sort-desc fa-sort-asc').addClass('fa-sort');
      this.resetSortingTooltips();

      if (this.orderBy === header.uri) {
        // if current order was descending then reset (remove) sorting to default (sorting applied with the search)
        if (this.orderDirection === ORDER_DESC) {
          delete this.orderBy;
          delete this.orderDirection;
          delete this.orderByCodelistNumbers;

          this.tooltipAdapter.show(this.getSortIconElement(header));
        } else {
          this.orderDirection = ORDER_DESC;
          this.updateSortIcons(header, this.orderDirection);
        }
      } else {
        // when order by field is changed always start with ascending direction
        this.orderBy = header.uri;
        this.orderDirection = ORDER_ASC;

        if (header.codeLists instanceof Array) {
          this.orderByCodelistNumbers = header.codeLists.join(',');
        }

        this.updateSortIcons(header, this.orderDirection);
      }

      this.publishOrderChangedEvent();
      this.loadObjects();
    }
  }

  updateSortIcons(header, orderDirection) {
    let sortIconElement = this.getSortIconElement(header);
    sortIconElement.find('i').removeClass('fa-sort').addClass(`fa-sort-${orderDirection}`);

    let tooltipKey = orderDirection === ORDER_ASC ? 'sort.descending' : 'sort.reset';
    this.tooltipAdapter.changeTitle(sortIconElement, this.translateService.translateInstant(tooltipKey));
    this.tooltipAdapter.show(sortIconElement);
  }

  getSortIconElement(header) {
    let safeHeaderName = header.name.replace(/:/i, '\\:');
    return this.$element.find(`[data-header-cell-name=${safeHeaderName}] span.sort-icon`);
  }

  addSortingTooltips() {
    if (!this.sortTooltipsInitialized) {
      let sortIconElements = this.$element.find('[data-header-cell-name] span.sort-icon');
      this.tooltipAdapter.tooltip(sortIconElements, {
        placement: 'auto top',
        html: 'true',
        trigger: 'hover',
        title: this.translateService.translateInstant('sort.ascending')
      });
      this.sortTooltipsInitialized = true;
    }
  }

  resetSortingTooltips() {
    let sortIconElements = this.$element.find('[data-header-cell-name] span.sort-icon');
    this.tooltipAdapter.changeTitle(sortIconElements, this.translateService.translateInstant('sort.ascending'));
  }

  /**
   * @returns {*} Promise which resolves with true or false depending if current object is version or not
   */
  isVersion() {
    return this.context.getCurrentObject().then((currentObject) => {
      return currentObject.isVersion();
    });
  }

  publishOrderChangedEvent() {
    this.control.publish('orderChanged', {
      orderBy: this.orderBy,
      orderDirection: this.orderDirection,
      orderByCodelistNumbers: this.orderByCodelistNumbers
    });
  }

  fireWidgetReadyEvent() {
    this.eventbus.publish(new WidgetReadyEvent({
      widgetId: this.control.getId()
    }));
  }

  ngOnDestroy() {
    this.setFilterCriteria(undefined);

    if (this.resizeListener) {
      this.resizeListener();
    }

    if (this.controlsLoadingPoller) {
      this.$interval.cancel(this.controlsLoadingPoller);
    }
    if (this.headerCells && this.headerCells.data('ui-resizable')) {
      this.headerCells.resizable('destroy');
    }
    $(this.control.element).off('mouseover');
  }
}

/**
 * RelatedObjectsMap is used to link related objects with their targets by instance id and relation type and allows for
 * easier finding of related objects by relation and instance id.
 * {
 *   "instanceId": {
 *     "relationType": [ "instanceId|InstanceObject" ]
 *   }
 * }
 */
export class RelatedObjectsMap {

  constructor() {
    this.map = {};
  }

  getInstanceMapping(instanceId) {
    if (!this.map[instanceId]) {
      this.map[instanceId] = {};
    }
    return this.map[instanceId];
  }

  getRelatedObjects(instanceId, relationType) {
    let mapping = this.getInstanceMapping(instanceId);
    mapping[relationType] = mapping[relationType] || [];
    return mapping[relationType];
  }

  getFirstRelatedObject(instanceId, relationType) {
    let relatedObjects = this.getRelatedObjects(instanceId, relationType);
    return relatedObjects && relatedObjects[0];
  }

  addRelatedIds(instanceId, relationType, relatedIds) {
    this.getRelatedObjects(instanceId, relationType).push(...relatedIds);
  }

  /**
   * Walks the map and replaces the related object ids with InstanceObjects. The objectsList contains two-dimensional
   * array with loaded instance objects in order of their requesting.
   *
   * @param objectsList An array containing response like objects with data attribute which is an array of InstanceObjects
   */
  populateObjects(objectsList) {
    objectsList.forEach((objectsGroup) => {
      objectsGroup.data.forEach((object) => {
        this.replaceIdWithObject(object);
      });
    });
  }

  /**
   * Find places for the provided InstanceObject in the mapping. The object could be populated in more than one place.
   *
   * @param object an InstanceObject
   */
  replaceIdWithObject(object) {
    let identifier = object.getId();
    Object.keys(this.map).forEach((instanceId) => {
      let instanceMapping = this.map[instanceId];
      Object.keys(instanceMapping).forEach((relationType) => {
        let relatedObjects = instanceMapping[relationType];
        // during the process relatedObjects could contain string id or InstanceObject
        relatedObjects.forEach((item, index) => {
          if (typeof item === 'string' && item === identifier) {
            relatedObjects.splice(index, 1, object);
          }
        });
      });
    });
  }

}