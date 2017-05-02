import {View, Inject, NgScope, NgElement, NgInterval} from 'app/app';
import {Widget} from 'idoc/widget/widget';
import {Eventbus} from 'services/eventbus/eventbus';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {EditorResizedEvent} from 'idoc/editor/editor-resized-event';
import {LAYOUT, LABEL_POSITION_HIDE} from 'form-builder/form-wrapper';
import {DefinitionService} from 'services/rest/definition-service';
import {DialogService} from 'components/dialog/dialog-service';
import {LocationAdapter} from 'adapters/angular/location-adapter';
import {PropertiesSelectorHelper, COMMON_PROPERTIES} from 'idoc/widget/properties-selector/properties-selector-helper';
import {SELECT_OBJECT_AUTOMATICALLY, SELECT_OBJECT_MANUALLY} from 'idoc/widget/object-selector/object-selector';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {DEFAULT_PAGE_SIZE} from 'idoc/widget/datatable-widget/datatable-widget-config';
import {NO_SELECTION} from 'search/search-selection-modes';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TooltipAdapter} from 'adapters/tooltip-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {INSTANCE_HEADERS, HEADER_DEFAULT, NO_HEADER} from 'instance-header/header-constants';
import {MODE_PRINT, MODE_PREVIEW} from 'idoc/idoc-constants';
import {DefinitionModel} from 'models/definition-model';
import {ModelUtils} from 'models/model-utils';
import {UrlUtils} from 'common/url-utils';
import {DragAndDrop} from 'components/draganddrop/drag-and-drop';
import _ from 'lodash';
import 'johnny/jquery-sortable';
import {Pagination} from 'search/components/common/pagination';
import {BASKET_EXTENSION} from 'services/picker/picker-service';
import {Logger} from 'services/logging/logger';
import template from './datatable-widget.html!text';
import './widget.css!';

const DTW_COLUMN_MIN_WIDTH = 150;

@Widget
@View({
  template: template
})
@Inject(NgScope, DefinitionService, PropertiesSelectorHelper, PromiseAdapter, ObjectSelectorHelper, TranslateService, TooltipAdapter, Eventbus, LocationAdapter, DialogService, NgElement, NgInterval, Logger)
export class DatatableWidget {

  constructor($scope, definitionService, propertiesSelectorHelper, promiseAdapter, objectSelectorHelper, translateService, tooltipAdapter, eventbus, locationAdapter, dialogService, $element, $interval, logger) {
    this.$element = $element;
    this.eventbus = eventbus;
    this.definitionService = definitionService;
    this.dialogService = dialogService;
    this.locationAdapter = locationAdapter;
    this.propertiesSelectorHelper = propertiesSelectorHelper;
    this.promiseAdapter = promiseAdapter;
    this.objectSelectorHelper = objectSelectorHelper;
    this.translateService = translateService;
    this.$scope = $scope;
    this.$interval = $interval;
    this.logger = logger;

    this.widgetConfig = this.widgetConfig || {};
    if (this.config.displayTableHeaderRow === undefined) {
      this.config.displayTableHeaderRow = true;
    }
    this.setStyles();
    this.config.columnsOrder = this.config.columnsOrder || {
        columns: {}
      };

    this.formConfig = this.formConfig || {
        layout: LAYOUT.TABLE,
        labelPosition: LABEL_POSITION_HIDE,
        styles: this.config.styles,
        hintClass: 'datatable-tooltip',
        enableHint: false,
        results: {
          config: {
            // display actions menu inside result fields
            renderMenu: true,
            // menu reload and positioning
            reloadMenu: true,
            placeholder: 'widget'
          }
        }
      };

    this.assignLoadModelsTriggerWatchers();

    // Don't watch for form view mode in modeling mode because the widget don't render data after all.
    if (!this.context.isModeling()) {
      $scope.$watch(() => {
        return this.context.getMode();
      }, () => {
        this.formConfig.formViewMode = this.context.getMode().toUpperCase();
        this.toggleResizeAndReorder();
      });
    } else {
      // In templating mode, widget is rendered in preview
      this.formConfig.formViewMode = MODE_PREVIEW.toUpperCase();
    }

    this.handleEditorResize = _.debounce(this.setColumnsWidth.bind(this), 100);

    let editorId = $element.closest('.idoc-editor').attr('id');
    // Table size should be recalculated if editor size is changed
    this.editorResizedHandler = this.eventbus.subscribe(EditorResizedEvent, (eventData) => {
      if (eventData.widthChanged && eventData.editorId === editorId && this.widgetConfig.headers) {
        this.handleEditorResize(this.widgetConfig.headers);
      }
    });

    $scope.$watch(() => {
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
      if (this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
        this.loadModels(false);
      } else {
        this.widgetConfig.objectsOnPage = this.getObjectsOnPage(params.pageNumber);
      }
    };

    $scope.$watch(() => {
      return this.config.pageSize;
    }, (pageSize) => {
      this.paginationConfig.pageSize = parseInt(pageSize || DEFAULT_PAGE_SIZE);
      this.paginationConfig.page = 1;
      this.searchArguments.pageSize = this.paginationConfig.pageSize;
      this.searchArguments.pageNumber = 1
    });

    $(this.control.element).on('mouseover', function (event) {
      let tooltipElement = $(event.target).closest('.form-group');
      let tooltipMessage = $.trim(tooltipElement.find('.messages').text());

      // Recreate tooltip only if there's actual changes in it's value. Otherwise FF causes endless loop
      if (tooltipElement.attr('data-original-title') !== tooltipMessage) {
        tooltipAdapter.tooltip(tooltipElement, {
          placement: 'auto right',
          html: 'true',
          trigger: 'hover',
          title: tooltipMessage
        }, true);
      }
      if (tooltipMessage === '') {
        tooltipAdapter.hide(tooltipElement);
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
    this.$element.find('.table-body, .table-header').width(this.panelsWidth);
  }

  /**
   * Wrap header cells in resizable component
   * @param element datatable widget
   * @param headerCells cells (columns) in datatable widget header
   * @param tableHeader datatable widget header panel
   * @param tableBody datatable widget body panel
   */
  applyResizable(element, headerCells, tableHeader, tableBody) {
    let widget = this;
    let paramsObject = {
      subTotalWidth: 0,
      parentWidth: 0,
      nextColumn: {}
    };
    headerCells.resizable({
      handles: 'e',
      minWidth: DTW_COLUMN_MIN_WIDTH,
      start: function (event, ui) {
        DatatableWidget.onResizeStart(ui, paramsObject, tableHeader);
      },
      stop: function () {
        DatatableWidget.onResizeStop(widget, element.find('.header-cell'));
      },
      resize: function (event, ui) {
        DatatableWidget.onResize(ui, paramsObject, element, tableHeader, tableBody, widget, this);
      }
    });
    headerCells.resizable('option', 'disabled', this.isResizableDisabled());
  }

  static onResizeStart(ui, paramsObject, tableHeader) {
    paramsObject.nextColumn = ui.originalElement.next();
    paramsObject.subTotalWidth = ui.originalSize.width + paramsObject.nextColumn.outerWidth();
    paramsObject.parentWidth = tableHeader.width();
  }

  static onResize(ui, paramsObject, element, tableHeader, tableBody, widget, cell) {
    let $cell = $(cell);
    let resizeCells = '#' + $cell.attr('data-header-cell-name') + '-wrapper';
    resizeCells = resizeCells.replace(/:/i, "\\:");
    let tableBodyColumn = element.find('.table-body form ' + resizeCells);
    if (element.width() > DatatableWidget.newTableWidth(paramsObject.parentWidth, ui)) {
      // restore last cell width if user try to make it smaller than table
      tableHeader.add(tableBody).width(element.width());
      widget.actualPanelsWidth = element.width();
      if (_.isEmpty(paramsObject.nextColumn) && ui.originalSize.width > ui.size.width) {
        setTimeout(function () {
          tableBodyColumn.width($cell.width());
          $cell.resizable('widget').width($cell.width());
        }, 0);
        $cell.resizable('widget').trigger('mouseup');
        return;
      }
      paramsObject.nextColumn.width(paramsObject.subTotalWidth - ui.size.width);
      tableBodyColumn.width(ui.size.width).next().width(paramsObject.subTotalWidth - ui.size.width);
    } else {
      tableHeader.add(tableBody).width(DatatableWidget.newTableWidth(paramsObject.parentWidth, ui));
      widget.actualPanelsWidth = DatatableWidget.newTableWidth(paramsObject.parentWidth, ui);
      if (_.isEmpty(paramsObject.nextColumn)) {
        element.scrollLeft(tableHeader.width());
      }
      tableBodyColumn.width(ui.size.width);
    }
  }

  static onResizeStop(widget, cells) {
    cells.each(function () {
      widget.config.styles.columns[$(this).attr('data-header-cell-name')].width = $(this).width();
    });
    widget.control.getBaseWidget().saveConfigWithoutReload(widget.config);
  }

  /**
   * Wrap header cells in sortable component
   * @param tableHeader datatable widget header panel
   */
  applySortable(tableHeader) {
    let widget = this;
    let paramsObject = {
      initialColumnWidth: 0,
      itemIndex: 0
    };
    DragAndDrop.makeDraggable(tableHeader, {
      // ol or ul with li by default
      itemSelector: '.header-cell',
      containerSelector: '.table-header',
      delay: 200,
      handle: '.title-cell',
      onDragStart: function ($item, container, _super) {
        _super($item, container);
        container.el.css({'padding-left': '5px'});
        DatatableWidget.onDragStart($item, paramsObject);
      },
      onDrop: function ($item, container, _super) {
        _super($item, container);
        container.el.css({'padding': '0px'});
        DatatableWidget.onDrop($item, paramsObject, widget);
      }
    });
    if (this.isSortableDisabled()) {
      DragAndDrop.disable(tableHeader);
    } else {
      DragAndDrop.enable(tableHeader);
    }
  }

  static onDragStart($item, paramsObject) {
    paramsObject.initialColumnWidth = $item.width();
    paramsObject.itemIndex = $item.index();
  }

  static onDrop($item, paramsObject, widget) {
    $item.width(paramsObject.initialColumnWidth);
    let cellsArray = widget.widgetConfig.objectsOnPage[0].models.viewModel.fields;
    cellsArray.splice($item.index(), 0, cellsArray.splice(paramsObject.itemIndex, 1)[0]);
    let orderMap = {};
    cellsArray.forEach(function (cell, index) {
      orderMap[cell.identifier] = {
        index: index
      };
    });
    widget.config.columnsOrder.columns = orderMap;
    widget.control.saveConfig(widget.config);
  }

  static newTableWidth(parentWidth, ui) {
    return parentWidth + (ui.size.width - ui.originalSize.width);
  }

  isResizableDisabled() {
    return this.context.isPreviewMode();
  }

  isSortableDisabled() {
    return this.isResizableDisabled() || (this.widgetConfig.headers && this.widgetConfig.headers.length === 1);
  }

  toggleResizeAndReorder() {
    $(this.control.element).find('.header-cell').resizable('option', 'disabled', this.isResizableDisabled());
    if (this.isSortableDisabled()) {
      DragAndDrop.disable($(this.control.element).find('.table-header'));
    } else {
      DragAndDrop.enable($(this.control.element).find('.table-header'));
    }
  }

  getObjectsOnPage(pageNumber) {
    return this.widgetConfig.selectedObjects.slice(this.config.pageSize * (pageNumber - 1), this.config.pageSize * pageNumber);
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
      this.searchArguments.orderBy = this.config.orderBy;
      this.searchArguments.orderDirection = this.config.orderDirection;
      this.loadModels(true);
    });
  }

  /**
   * Loads the models of the objects in the widget.
   *
   * @param resetObjects shows if shared objects in the context should be reset or not. [true/false]
   * @returns {*}
   */
  loadModels(resetObjects) {
    delete this.errorMessage;
    this.paginationConfig.disabled = true;
    let headersLoader = this.generateHeaders(this.config.selectedProperties);
    let objectsLoader = this.objectSelectorHelper.getSelectedObjects(this.config, this.context, this.searchArguments).then((selectedObjectsResponse) => {
      return this.context.getSharedObjects(selectedObjectsResponse.results, this.control.getId(), resetObjects, this.getConfig()).then((sharedObjects) => {
        this.paginationConfig.disabled = false;

        if (sharedObjects.notFound.length > 0 && this.config.selectObjectMode === SELECT_OBJECT_MANUALLY) {
          this.objectSelectorHelper.removeSelectedObjects(this.config, sharedObjects.notFound);
          this.control.saveConfig(this.config);
        }
        let result = {
          total: selectedObjectsResponse.total,
          objects: sharedObjects.data
        };
        if (this.context.isModeling() && this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
          result.objects = [];
          result.total = 0;
        }
        return result;
      });
    }).catch((message) => {
      // If no object is selected to be displayed mark widget as ready for print
      this.eventbus.publish(new WidgetReadyEvent({
        widgetId: this.control.getId()
      }));
      if (!this.context.isModeling()) {
        this.errorMessage = message.reason;
      }
      return [];
    });

    return this.promiseAdapter.all([headersLoader, objectsLoader]).then((loadersResults) => {
      this.resetOrderAndWidthConfig(this.widgetConfig.headers, loadersResults[0]);
      this.widgetConfig.headers = this.orderHeaders(loadersResults[0]);
      this.setColumnsWidth(this.widgetConfig.headers);
      let instances = loadersResults[1].objects || [];
      this.widgetConfig.selectedObjects = instances.map(instance => this.convertViewModel(instance, this.widgetConfig.headers, this.config.selectedProperties));

      if (this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
        this.widgetConfig.objectsOnPage = this.orderObjectsOnPage(this.widgetConfig.selectedObjects);
        this.paginationConfig.resultSize = loadersResults[1].total;
      } else {
        this.widgetConfig.objectsOnPage = this.orderObjectsOnPage(this.paginationConfig.pageSize === 0 ? this.widgetConfig.selectedObjects :
          this.widgetConfig.selectedObjects.slice(0, this.paginationConfig.pageSize));
        this.paginationConfig.resultSize = this.widgetConfig.selectedObjects.length;
      }

      // Wait for the table to be rendered before applying resizable and sortable and firing WidgetReadyEvent
      this.interval = this.$interval(() => {
        // Wait for all objects to be loaded in the DTW
        if (this.widgetConfig.objectsOnPage && this.$element.find('.form-content').length === this.widgetConfig.objectsOnPage.length) {
          // Wait for all the headers to be rendered
          this.headerCells = this.$element.find('.header-cell');
          if (this.areHeadersRendered(this.headerCells, this.widgetConfig.headers)) {
            this.$interval.cancel(this.interval);
            let tableHeader = this.$element.find('.table-header');
            let tableBody = this.$element.find('.table-body');
            this.applyResizable(this.$element, this.headerCells, tableHeader, tableBody);
            this.applySortable(tableHeader);
            this.eventbus.publish(new WidgetReadyEvent({
              widgetId: this.control.getId()
            }));
          }
        }
      }, 100);

    }).catch((error) => {
      this.logger.error(error);
      this.eventbus.publish(new WidgetReadyEvent({
        widgetId: this.control.getId()
      }));
    });
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
      return !!properties[definition].length;
    });
  }

  getConfig() {
    let config = {};
    if (!this.context.isEditMode()) {
      let propertiesToLoad = [this.config.instanceHeaderType];
      Object.keys(this.config.selectedProperties).forEach((definition) => {
        this.config.selectedProperties[definition].forEach((property) => {
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
   * @returns true if there's no change in selection false otherwise
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
   * @returns array of ordered properties
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
    let columnWidth = this.$element.width() / columnsCount;
    if (columnWidth < DTW_COLUMN_MIN_WIDTH) {
      columnWidth = DTW_COLUMN_MIN_WIDTH;
    }
    this.actualPanelsWidth = 0;
    let columnWidths = {};

    if (this.config.widgetWidth && this.config.widgetWidth !== this.$element.width()) {
      ratio = this.config.widgetWidth / this.$element.width();
    }

    headers.forEach((header) => {
      if (this.config.styles.columns[header.name]) {
        columnWidth = this.config.styles.columns[header.name].width / ratio;
      }
      columnWidths[header.name] = {
        width: columnWidth
      };
      this.actualPanelsWidth = this.actualPanelsWidth + parseInt(columnWidth);
    });
    this.setStyles();
    this.config.styles.columns = columnWidths;
    this.formConfig.styles.columns = columnWidths;

    this.config.widgetWidth = this.$element.width();
    this.recalculatePanelsWidth(this.$element.width());
  }

  generateHeaders(selectedProperties) {
    let headersArray = [];
    this.formConfig.instanceLinkType = this.config.instanceHeaderType;
    let headerType = this.config.instanceHeaderType;
    if (headerType !== NO_HEADER) {
      headersArray.push({
        name: headerType,
        labels: [this.translateService.translateInstant('dtw.column.header')]
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
      let headers = new Map();
      definitions.data.forEach((definition) => {
        let propertiesForDefinition = selectedProperties[definition.identifier] || [];
        let flatDefinitionProperties = this.propertiesSelectorHelper.flattenDefinitionProperties(definition);

        propertiesForDefinition.forEach((propertyIdentifier) => {
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
            headers.set(propertyIdentifier, {name: propertyIdentifier, labels: [fieldLabel]});
          }

        });
      });
      headersArray.push(...Array.from(headers.values()));
      return headersArray;
    });
  }

  getColumnHeaderLabel(columnHeader) {
    return columnHeader.labels.join(', ');
  }

  convertViewModel(instance, headers, selectedProperties) {
    let instanceSelectedProperties = PropertiesSelectorHelper.getSelectedPropertiesArray(instance.models, selectedProperties);

    let instanceFlatViewModel = ModelUtils.flatViewModel(instance.models.viewModel);
    let convertedViewModel = {
      fields: []
    };
    headers.forEach((header) => {
      // get field's current view model if such field exists for the given instance
      let instanceField = instanceFlatViewModel.get(header.name);

      // show field if it is amongst instance's selected properties
      let shouldShowField = INSTANCE_HEADERS[header.name] || (instanceSelectedProperties && instanceSelectedProperties.indexOf(header.name) !== -1);

      if (shouldShowField && instanceField) {
        convertedViewModel.fields.push(_.clone(instanceField.serialize()));
      } else {
        convertedViewModel.fields.push(ModelUtils.buildEmptyCell(header.name));
      }
    });

    // clone instance and link it to the actual validation model
    let clonedInstance = _.cloneDeep(instance);
    clonedInstance.models.validationModel = instance.models.validationModel;
    //view model must be wrapped in the DefinitionModel class.
    clonedInstance.models.viewModel = new DefinitionModel(convertedViewModel);
    clonedInstance.models.id = clonedInstance.id;
    return clonedInstance;
  }

  showDatatable() {
    if (!this.context.isModeling()) {
      return this.widgetConfig.selectedObjects && this.widgetConfig.selectedObjects.length > 0;
    }
    return true;
  }

  showResults() {
    let control = this.control;
    let widgetConfig = {
      modalCls: control.getConfigSelector(),
      config: this.createConfig(),
      definition: control.getDefinition(),
      context: this.context
    };
    let dialogConfig = {
      header: this.config.title,
      buttons: [
        {
          id: DialogService.CANCEL,
          label: 'idoc.widget.config.button.cancel'
        }],
      onButtonClick: function (buttonId, componentScope, dialogConfig) {
        dialogConfig.dismiss();
      }
    };

    //This CSS class is located in the search-results.scss file
    dialogConfig.customStyles = "fullscreen";
    this.dialogService.create(control.getConfigSelector(), widgetConfig, dialogConfig);
  }

  createConfig() {
    let fullscreenConfig = _.cloneDeep(this.config);
    //Passes properties to disable selected components in the dialog.
    fullscreenConfig.renderOptions = false;
    fullscreenConfig.renderCriteria = false;
    fullscreenConfig.hideWidgerToolbar = false;
    fullscreenConfig.instanceHeaderType = HEADER_DEFAULT;
    if (fullscreenConfig.selectObjectMode === SELECT_OBJECT_MANUALLY) {
      // Ensures that only the basket tab will be rendered and thus hidden in the object picker
      fullscreenConfig.tabsConfig = {
        inclusions: [BASKET_EXTENSION]
      };
      fullscreenConfig.selection = NO_SELECTION;
    }
    return fullscreenConfig;
  }

  ngOnDestroy() {
    this.editorResizedHandler.unsubscribe();
    if (this.interval) {
      this.$interval.cancel(this.interval);
    }
    if (this.headerCells && this.headerCells.data('ui-resizable')) {
      this.headerCells.resizable("destroy");
    }
    $(this.control.element).off('mouseover');
  }
}
