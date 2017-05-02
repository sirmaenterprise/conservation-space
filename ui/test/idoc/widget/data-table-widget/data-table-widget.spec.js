import {DatatableWidget} from 'idoc/widget/datatable-widget/datatable-widget';
import {DatatableWidgetConfig} from 'idoc/widget/datatable-widget/datatable-widget-config';
import {Eventbus} from 'services/eventbus/eventbus';
import {PromiseAdapterMock} from '../../../adapters/angular/promise-adapter-mock';
import {PropertiesSelectorHelper, COMMON_PROPERTIES} from 'idoc/widget/properties-selector/properties-selector-helper';
import {SELECT_OBJECT_AUTOMATICALLY, SELECT_OBJECT_MANUALLY} from 'idoc/widget/object-selector/object-selector';
import {InstanceObject} from 'idoc/idoc-context';
import {HEADER_DEFAULT, HEADER_COMPACT, NO_HEADER} from 'instance-header/header-constants';
import {DEFAULT_PAGE_SIZE} from 'idoc/widget/datatable-widget/datatable-widget-config';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {DefinitionModel} from 'models/definition-model';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {EditorResizedEvent} from 'idoc/editor/editor-resized-event';
import {BASKET_EXTENSION} from 'services/picker/picker-service';

let isModeling;

describe('DataTableWidget', () => {

  beforeEach(() => {
    isModeling = false;
  });

  describe('on init', () => {

    it('should construct pagination config', () => {
      let datatableWidget = instantiateDataTableWidget();
      expect(datatableWidget.paginationConfig).to.exist;
      expect(datatableWidget.paginationConfig.showFirstLastButtons).to.be.true;
    });

    it('should assign search arguments', () => {
      let datatableWidget = instantiateDataTableWidget();
      expect(datatableWidget.searchArguments).to.exist;
      expect(datatableWidget.searchArguments.pageNumber).to.equal(1);
    });

    it('should assign watchers that triggers models loading', () => {
      let scope = mock$scope();
      let stubLoadModels = sinon.stub(DatatableWidget.prototype, 'loadModels');
      let datatableWidget = instantiateDataTableWidget(scope);
      datatableWidget.config = {};
      datatableWidget.config.selectedProperties = [];
      datatableWidget.config.selectedObjects = [];
      datatableWidget.config.selectObjectMode = SELECT_OBJECT_AUTOMATICALLY;
      datatableWidget.config.criteria = {
        criteria: 'criteria'
      };
      datatableWidget.$scope.$digest();
      expect(stubLoadModels.calledOnce).to.be.true;

      datatableWidget.config.selectObjectMode = SELECT_OBJECT_MANUALLY;
      datatableWidget.$scope.$digest();
      expect(stubLoadModels.calledTwice).to.be.true;
    });

    it('should assign a watcher for the page size configuration', () => {
      let scope = mock$scope();
      let datatableWidget = instantiateDataTableWidget(scope);

      datatableWidget.config.pageSize = 321;
      datatableWidget.$scope.$digest();

      expect(datatableWidget.paginationConfig.pageSize).to.equal(321);
      expect(datatableWidget.searchArguments.pageSize).to.equal(321);

      expect(datatableWidget.paginationConfig.page).to.equal(1);
      expect(datatableWidget.searchArguments.pageNumber).to.equal(1);
    });

    it('should set displayTableHeaderRow to true if not defined', () => {
      let datatableWidget = instantiateDataTableWidget();
      expect(datatableWidget.config.displayTableHeaderRow).to.be.true;
    });

    it('should not set displayTableHeaderRow to true if already set to false', () => {
      let datatableWidget = instantiateDataTableWidget(undefined, {displayTableHeaderRow: false});
      expect(datatableWidget.config.displayTableHeaderRow).to.be.false;
    });
  });

  describe('showDatatable', () => {
    it('should return true in modeling mode', () => {
      isModeling = true;
      let datatableWidget = instantiateDataTableWidget();
      expect(datatableWidget.showDatatable()).to.be.true;
    });

    it('should return false if there aren`t selected objects when modeling mode is not set', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.widgetConfig = {
        selectedObjects: []
      };
      expect(datatableWidget.showDatatable()).to.be.false;
    });

    it('should return true if there are selected objects when modeling mode is not set', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.widgetConfig = {
        selectedObjects: [{}]
      };
      expect(datatableWidget.showDatatable()).to.be.true;
    });
  });

  describe('generateHeaders', () => {
    it('should return promise which resolves with headers map', (done) => {
      let datatableWidget = instantiateDataTableWidget();
      DatatableWidget.prototype.config = {
        instanceHeaderType: HEADER_COMPACT
      };

      let selectedProperties = {
        [COMMON_PROPERTIES]: ['property3'],
        GEP111111: ['property3', 'property5'],
        GEP100002: ['property1', 'property3']
      };

      datatableWidget.generateHeaders(selectedProperties).then((headers) => {
        expect(headers).to.eql([
          {name: HEADER_COMPACT, labels: ['dtw.column.header']},
          {name: 'property3', labels: ['GEP111111 Property 3', 'GEP100002 Property 3']},
          {name: 'property5', labels: ['Property 5']},
          {name: 'property1', labels: ['Property 1']}
        ]);
        expect(headers[1].labels).to.eql(['GEP111111 Property 3', 'GEP100002 Property 3']);
        done();
      }).catch(done);
    });

    it('should resolve with single entry for instance\'s compact header if selected properties is not defined', (done) => {
      let datatableWidget = instantiateDataTableWidget();
      DatatableWidget.prototype.config = {
        instanceHeaderType: HEADER_COMPACT
      };
      datatableWidget.generateHeaders().then((headers) => {
        expect(headers).to.have.length(1);
        expect(headers[0]).to.have.property('name', HEADER_COMPACT);
        done();
      }).catch(done);
    });

    it('should resolve with single entry for instance\'s compact header if selected properties is empty', (done) => {
      let datatableWidget = instantiateDataTableWidget();
      DatatableWidget.prototype.config = {
        instanceHeaderType: HEADER_COMPACT
      };
      datatableWidget.generateHeaders({}).then((headers) => {
        expect(headers).to.have.length(1);
        expect(headers[0]).to.have.property('name', HEADER_COMPACT);
        done();
      }).catch(done);
    });

    it('should resolve with table headers array without instance headers column', (done) => {
      let datatableWidget = instantiateDataTableWidget();
      DatatableWidget.prototype.config = {
        instanceHeaderType: NO_HEADER
      };
      let selectedProperties = {
        [COMMON_PROPERTIES]: ['property3'],
        GEP111111: ['property3', 'property5'],
        GEP100002: ['property1', 'property3']
      };
      datatableWidget.generateHeaders(selectedProperties).then((headers) => {
        expect(headers).to.eql([
          {name: 'property3', labels: ['GEP111111 Property 3', 'GEP100002 Property 3']},
          {name: 'property5', labels: ['Property 5']},
          {name: 'property1', labels: ['Property 1']}
        ]);
        done();
      }).catch(done);
    });
  });
  describe('Datatable grid', ()=> {
    it('watcher should update the formControl grid value on config.grid value change', ()=> {
      let scope = mock$scope();
      let datatableWidget = instantiateDataTableWidget(scope);
      expect(datatableWidget.formConfig.styles.grid).to.equal(undefined);
      datatableWidget.config.grid = DatatableWidgetConfig.GRID_ON;
      datatableWidget.$scope.$digest();
      expect(datatableWidget.formConfig.styles.grid).to.equal(DatatableWidgetConfig.GRID_ON);
    });
  });
  describe('setColumnsWidth', () => {
    it('should calculate and set columns width style in the widget config', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.$element.find = () => {return {width: () => {}, add: () => {return {width: () => {}}}}};
      datatableWidget.$element.width = () => {
        return 1000
      };
      datatableWidget.setColumnsWidth(headers);
      expect(datatableWidget.config.styles.columns).to.eql({
        'prop1': {width: 200},
        'prop2': {width: 200},
        'prop3': {width: 200},
        'prop4': {width: 200},
        'prop5': {width: 200}
      });
    });

    it('should set columns with to default minimum calculated width is less that the allowed minimum', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.$element.find = () => {return {width: () => {}, add: () => {return {width: () => {}}}}};
      datatableWidget.$element.width = () => {
        return 500
      };
      datatableWidget.setColumnsWidth(headers);
      expect(datatableWidget.config.styles.columns).to.eql({
        'prop1': {width: 150},
        'prop2': {width: 150},
        'prop3': {width: 150},
        'prop4': {width: 150},
        'prop5': {width: 150}
      });
    });

    it('should call setColumnsWidth function when EditorResizedEvent is fired and editor width is changed', () => {
      let datatableWidget = instantiateDataTableWidget();
      let handleEditorResizeStub = sinon.stub(datatableWidget, 'handleEditorResize');
      datatableWidget.widgetConfig.headers = {};
      datatableWidget.eventbus.publish(new EditorResizedEvent({
        editorId: 'id',
        widthChanged: true
      }));
      expect(handleEditorResizeStub.callCount).to.equal(1);
    });

    it('should not call setColumnsWidth function when EditorResizedEvent is fired if width is not changed', () => {
      let datatableWidget = instantiateDataTableWidget();
      let handleEditorResizeStub = sinon.stub(datatableWidget, 'handleEditorResize');
      datatableWidget.widgetConfig.headers = {};
      datatableWidget.eventbus.publish(new EditorResizedEvent({
        editorId: 'id',
        widthChanged: false
      }));
      expect(handleEditorResizeStub.callCount).to.equal(0);
    });

    it('should set columns width using ratio between new and old widget size', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.$element.find = () => {return {width: () => {}, add: () => {return {width: () => {}}}}};
      datatableWidget.$element.width = () => {
        return 1000
      };
      datatableWidget.setColumnsWidth(headers);
      expect(datatableWidget.config.styles.columns).to.eql({
        'prop1': {width: 200},
        'prop2': {width: 200},
        'prop3': {width: 200},
        'prop4': {width: 200},
        'prop5': {width: 200}
      });

      // when ratio is 0.5 (widgetWidth / $element.width = 0.5 ) cells size should be 400 (initial size 200 / 0.5 = 400)
      datatableWidget.config.widgetWidth = 500;
      datatableWidget.setColumnsWidth(headers);
      expect(datatableWidget.config.styles.columns).to.eql({
        'prop1': {width: 400},
        'prop2': {width: 400},
        'prop3': {width: 400},
        'prop4': {width: 400},
        'prop5': {width: 400}
      });
    });
  });

  describe('paginationCallback', () => {
    it('should update the search arguments', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.getObjectsOnPage = () => {
      };
      datatableWidget.paginationCallback({
        pageNumber: 123
      });
      expect(datatableWidget.searchArguments.pageNumber).to.equal(123);
    });
  });

  it('getColumnHeaderLabel should concatenate all labels into comma separated string', () => {
    let datatableWidget = instantiateDataTableWidget();
    let tableHeader = {name: 'property3', labels: ['GEP111111 Property 3', 'GEP100002 Property 3']};
    expect(datatableWidget.getColumnHeaderLabel(tableHeader)).to.equal('GEP111111 Property 3, GEP100002 Property 3');
  });

  it('should return correct set of objects for given page', () => {
    let datatableWidget = instantiateDataTableWidget();
    datatableWidget.widgetConfig.selectedObjects = [{}, {}, {}];
    datatableWidget.config.pageSize = 2;
    expect(datatableWidget.getObjectsOnPage(2).length).to.equal(1);
  });

  it('should return true if pagination element must be hidden and false otherwise', () => {
    let datatableWidget = instantiateDataTableWidget();
    datatableWidget.config.showFirstPageOnly = true;
    datatableWidget.paginationConfig = {pageSize: 5};
    expect(datatableWidget.isPaginationHidden()).to.be.true;

    datatableWidget.config.showFirstPageOnly = false;
    datatableWidget.paginationConfig = {pageSize: 0};
    expect(datatableWidget.isPaginationHidden()).to.be.true;

    datatableWidget.config.showFirstPageOnly = false;
    datatableWidget.paginationConfig = {pageSize: 2};
    expect(datatableWidget.isPaginationHidden()).to.be.false;
  });

  it('convertViewModel should return an instance with converted view model', () => {
    let datatableWidget = instantiateDataTableWidget();
    let models = {
      definitionId: 'GEP111111',
      validationModel: {},
      viewModel: new DefinitionModel({
        fields: [{
          identifier: 'field1'
        }, {
          identifier: 'field2'
        }, {
          identifier: HEADER_COMPACT
        }]
      })
    };
    let instance = new InstanceObject('emf:123456', models);
    let headers = [{
      name: HEADER_COMPACT
    }, {
      name: 'field1'
    }, {
      name: 'field3'
    }];

    let selectedProperties = {
      GEP111111: ['field1', 'field2']
    };
    let convertedInstance = datatableWidget.convertViewModel(instance, headers, selectedProperties);
    expect(convertedInstance.models.viewModel.fields).to.have.length(3);
    expect(convertedInstance.models.viewModel.fields[0].identifier).to.equal(HEADER_COMPACT);
    expect(convertedInstance.models.viewModel.fields[1].identifier).to.equal('field1');
    expect(convertedInstance.models.viewModel.fields[1].control).to.be.undefined;
    expect(convertedInstance.models.viewModel.fields[2].identifier).to.equal('field3');
    expect(convertedInstance.models.viewModel.fields[2].control.identifier).to.equal('EMPTY_CELL');
  });

  it('loadModels should remove not found objects from widget\'s config', (done) => {
    let datatableWidget = instantiateDataTableWidget();
    datatableWidget.$element.find = () => {return {width: () => {}, add: () => {return {width: () => {}}}}};
    datatableWidget.config.selectObjectMode = SELECT_OBJECT_MANUALLY;
    datatableWidget.objectSelectorHelper.getSelectedObjects = () => {
      return Promise.resolve([]);
    };
    sinon.stub(datatableWidget, 'generateHeaders', () => {
      return Promise.resolve([]);
    });
    sinon.stub(datatableWidget.context, 'getSharedObjects', () => {
      return Promise.resolve({
        data: [],
        notFound: ['emf:123456']
      });
    });
    datatableWidget.context.isEditMode = () => {
      return true;
    };
    datatableWidget.objectSelectorHelper.removeSelectedObjects = sinon.spy();
    datatableWidget.control.saveConfig = sinon.spy();

    datatableWidget.loadModels().then(() => {
      expect(datatableWidget.objectSelectorHelper.removeSelectedObjects.callCount).to.equal(1);
      expect(datatableWidget.objectSelectorHelper.removeSelectedObjects.args[0][1]).to.eql(['emf:123456']);
      expect(datatableWidget.control.saveConfig.callCount).to.equal(1);
      done();
    }).catch(done);
  });

  it('should reset the page number before search in the load models watcher', () => {
    let datatableWidget = instantiateDataTableWidget();
    datatableWidget.searchArguments.pageNumber = 100;
    datatableWidget.paginationConfig.page = 100;

    datatableWidget.loadModels = sinon.spy();

    // Triggering digest cycle to invoke the watcher handler
    datatableWidget.$scope.$digest();

    expect(datatableWidget.searchArguments.pageNumber).to.equal(1);
    expect(datatableWidget.paginationConfig.page).to.equal(1);
    expect(datatableWidget.loadModels.called).to.be.true;
  });

  it('should fire widgetReadyEvent if no object is selected', (done) => {
    let datatableWidget = instantiateDataTableWidget();
    datatableWidget.$element.find = () => {return {width: () => {}, add: () => {return {width: () => {}}}}};
    datatableWidget.objectSelectorHelper.getSelectedObjects = () => {
      return Promise.resolve([]);
    };

    let spyEventHandler = sinon.spy();
    datatableWidget.eventbus.subscribe(WidgetReadyEvent, spyEventHandler);

    datatableWidget.loadModels().then(() => {
      expect(spyEventHandler.calledOnce).to.be.true;
      expect(spyEventHandler.getCall(0).args[1].topic).to.equal('widgetReadyEvent');
      done();
    }).catch(done);
  });

  it('should properly configure for fullscreen mode for automatically select mode', ()=> {
    let expectedConfig = {
      renderOptions : false,
      renderCriteria : false,
      hideWidgerToolbar : false,
      instanceHeaderType : HEADER_DEFAULT
    };
    let datatableWidget = instantiateDataTableWidget(undefined, {});
    datatableWidget.config.selectObjectMode = SELECT_OBJECT_AUTOMATICALLY;
    expect(datatableWidget.createConfig()).to.contain(expectedConfig);
  });

  it('should properly configure for fullscreen mode for manually select mode', ()=> {
    let expectedConfig = {
      renderOptions : false,
      renderCriteria : false,
      hideWidgerToolbar : false,
      instanceHeaderType : HEADER_DEFAULT
    };
    let datatableWidget = instantiateDataTableWidget(undefined, {});
    datatableWidget.config.selectObjectMode = SELECT_OBJECT_MANUALLY;
    var fullscreenConfig = datatableWidget.createConfig();
    expect(fullscreenConfig).to.contain(expectedConfig);
    expect(fullscreenConfig.tabsConfig).to.exist;
    expect(fullscreenConfig.tabsConfig.inclusions).to.deep.equal([BASKET_EXTENSION]);
  });

  it('should prepare correct properties config', ()=> {
    let datatableWidget = instantiateDataTableWidget();
    datatableWidget.context.isEditMode = () => {
      return false;
    };
    datatableWidget.config.instanceHeaderType = 'default_header';
    datatableWidget.config.selectedProperties = {
      [COMMON_PROPERTIES]: ['property3'],
      GEP111111: ['property3', 'property5'],
      GEP100002: ['property1', 'property3']
    };
    let expected = {
      params: {
        properties: ['default_header', 'property3', 'property5', 'property1']
      }
    };
    expect(datatableWidget.getConfig()).to.eql(expected);
    datatableWidget.context.isEditMode = () => {
      return true;
    };
    expect(datatableWidget.getConfig()).to.eql({});
  });

  describe('Ordering', () => {
    it('should order objects properties correct', () => {
      let datatableWidget = instantiateDataTableWidget();
      let object = [{models: {viewModel: {fields: [{identifier: 'compact_header', label:'Entity'}, {identifier: 'status', label:'State'}, {identifier: 'functional', label:'Functional'}]}}}];
      datatableWidget.config.columnsOrder.columns = {compact_header: {index: 2}, status: {index: 0}, functional: {index: 1}};
      let expected = [{models: {viewModel: {fields: [{identifier: 'status', label:'State'}, {identifier: 'functional', label:'Functional'}, {identifier: 'compact_header', label:'Entity'}]}}}];
      expect(datatableWidget.orderObjectsOnPage(object)).to.eql(expected);
      datatableWidget.config.columnsOrder.columns = {};
      expect(datatableWidget.orderObjectsOnPage(object)).to.eql(expected);
    });

    it('should order headers correct', () => {
      let datatableWidget = instantiateDataTableWidget();
      let object = [{name: 'compact_header', labels: 'Entity'}, {name: 'status', labels: 'State'}, {name: 'functional', labels: 'Func'}];
      datatableWidget.config.columnsOrder.columns = {compact_header: {index: 2}, status: {index: 0}, functional: {index: 1}};
      let expected = [{name: 'status', labels: 'State'}, {name: 'functional', labels: 'Func'}, {name: 'compact_header', labels: 'Entity'}];
      expect(datatableWidget.orderHeaders(object)).to.eql(expected);
      datatableWidget.config.columnsOrder.columns = {};
      expect(datatableWidget.orderHeaders(object)).to.eql(object);
    });
  });

  describe('Resize', () => {
    let ui = {
      originalElement: {
        next: () => {return {outerWidth: () => 50}}
      },
      originalSize: {
        width: 100
      },
      size: {
        width: 150
      }
    };

    it('should initialize resizable params correct', () => {
      let paramsObject = {
        subTotalWidth : 0,
        parentWidth : 0,
        nextColumn: {}
      };
      let tableHeader = {
        width: () => 700
      };
      DatatableWidget.onResizeStart(ui, paramsObject, tableHeader);
      expect(paramsObject.subTotalWidth).to.equal(150);
      expect(paramsObject.parentWidth).to.equal(700);
    });

    it('should update widget config after resize', () => {
      let paramsObject = {
        subTotalWidth : 0,
        parentWidth : 0,
        nextColumn: {}
      };
      let widget = instantiateDataTableWidget();
      widget.config.styles.columns = {
        column1: {width: 20},
        column2: {width: 40},
        column3: {width: 60}
      };
      let expected = {
        column1: {width: 20},
        column2: {width: 150},
        column3: {width: 60}
      };
      let cells = $(`<div data-header-cell-name='column1' style='width: 20px'></div><div data-header-cell-name='column2' style='width: 150px'></div><div data-header-cell-name='column3' style='width: 60px'></div>`);
      DatatableWidget.onResizeStop(widget, cells);
      expect(widget.config.styles.columns).to.eql(expected);

      // default (min table) width can not be changed
      widget.$element.width = () => {
        return 200
      };
      DatatableWidget.onResizeStop(widget, cells);
      expect(widget.config.styles.columns).to.eql(expected);

      widget.$element.width = () => {
        return 20
      };
      paramsObject.nextColumn = {
        attr: () => {return 'column3'},
        width: () => 20
      };
      expected = {
        column1: {width: 20},
        column2: {width: 150},
        column3: {width: 20}
      };
      cells = $(`<div data-header-cell-name='column1' style='width: 20px'></div><div data-header-cell-name='column2' style='width: 150px'></div><div data-header-cell-name='column3' style='width: 20px'></div>`);
      DatatableWidget.onResizeStop(widget, cells);
      expect(widget.config.styles.columns).to.eql(expected);
    });

    it('should calculate new table width correct', () => {
      expect(DatatableWidget.newTableWidth(350, ui)).to.equal(400);
    });

    it('should recalculate cells width if editor size is changed', () => {
      let widget = instantiateDataTableWidget();
      widget.$element.find = () => {return {width: () => {}, add: () => {return {width: () => {}}}}};
      widget.panelsWidth = 500;
      widget.actualPanelsWidth = 500;
      widget.recalculatePanelsWidth(1000);
      expect(widget.panelsWidth).to.equal(1000);

      widget.elementIsResized = true;
      widget.recalculatePanelsWidth(400);
      expect(widget.panelsWidth).to.equal(500);
    });

    it('should reset panels width in print mode', () => {
      let widget = instantiateDataTableWidget();
      widget.$element.find = () => {return {width: () => {}, add: () => {return {width: () => {}}}}};
      widget.panelsWidth = 500;
      widget.context.isPrintMode = () => {
        return true
      };
      widget.recalculatePanelsWidth(1000);
      expect(widget.panelsWidth).to.eql({});
    });
  });

  describe('Sort', () => {
    it('should initialize sortable params correct', () => {
      let paramsObject = {
        initialColumnWidth: 0,
        itemIndex: 0
      };
      let cell = {
        width: () => 100,
        index: () => 5
      };
      let expected = {
        initialColumnWidth: 100,
        itemIndex: 5
      };
      DatatableWidget.onDragStart(cell, paramsObject);
      expect(paramsObject).to.eql(expected);
    });

    it('should update widget config after reorder', () => {
      let paramsObject = {
        initialColumnWidth: 0,
        itemIndex: 0
      };
      let cell = {
        width: () => 100,
        index: () => 2
      };
      let widget = instantiateDataTableWidget();
      widget.widgetConfig.objectsOnPage = [{
        models: {
          viewModel : {}
        }
      }];
      widget.widgetConfig.objectsOnPage[0].models.viewModel.fields = [{identifier: 'field1'}, {identifier: 'field2'}, {identifier: 'field3'}];
      let expected = {field2: {index: 0}, field3: {index: 1}, field1: {index: 2}};

      DatatableWidget.onDrop(cell, paramsObject, widget);
      expect(widget.config.columnsOrder.columns).to.eql(expected);
    });
  });

  it('should reset columns config if properties set is changed', () => {
    let datatableWidget = instantiateDataTableWidget();
    datatableWidget.config.styles.columns = {header2: 200};
    datatableWidget.config.columnsOrder.columns = {header2: 3};
    let headers = [{name: 'header1', labels: ['header1']}, {name: 'header2', labels: ['header2']}, {name: 'header3', labels: ['header3']}];
    let response = [{name: 'header3', labels: ['header3']}, {name: 'header4', labels: ['header4']}, {name: 'header1', labels: ['header1']}];
    datatableWidget.resetOrderAndWidthConfig(headers, response);
    expect(datatableWidget.config.styles.columns).to.eql({});
    expect(datatableWidget.config.columnsOrder.columns).to.eql({});
  });

  it('should reset columns config if property is added or removed', () => {
    let datatableWidget = instantiateDataTableWidget();
    datatableWidget.config.styles.columns = {header2: 200};
    datatableWidget.config.columnsOrder.columns = {header2: 3};
    let headers = [{name: 'header1'}, {name: 'header2'}, {name: 'header3'}];
    let response = [{name: 'header1'}, {name: 'header2'}, {name: 'header3'}, {name: 'header4'}];
    datatableWidget.resetOrderAndWidthConfig(headers, response);
    expect(datatableWidget.config.styles.columns).to.eql({});
    expect(datatableWidget.config.columnsOrder.columns).to.eql({});
  });

  it('should not reset columns config if headers are not saved', () => {
    let datatableWidget = instantiateDataTableWidget();
    datatableWidget.config.styles.columns = {header2: 200};
    datatableWidget.config.columnsOrder.columns = {header2: 3};
    let response = [{name: 'header3'}, {name: 'header2'}, {name: 'header1'}];
    datatableWidget.resetOrderAndWidthConfig(undefined, response);
    expect(datatableWidget.config.styles.columns).to.eql({header2: 200});
    expect(datatableWidget.config.columnsOrder.columns).to.eql({header2: 3});
  });

  it('should not reset columns config if properties set is not changed', () => {
    let datatableWidget = instantiateDataTableWidget();
    datatableWidget.config.styles.columns = {header2: 200};
    datatableWidget.config.columnsOrder.columns = {header2: 3};
    let headers = [{name: 'header1', labels: ['header1']}, {name: 'header2', labels: ['header2']}, {name: 'header3', labels: ['header3']}];
    let response = [{name: 'header3', labels: ['header3']}, {name: 'header2', labels: ['header2']}, {name: 'header1', labels: ['header1']}];
    datatableWidget.resetOrderAndWidthConfig(headers, response);
    expect(datatableWidget.config.styles.columns).to.eql({header2: 200});
    expect(datatableWidget.config.columnsOrder.columns).to.eql({header2: 3});
  });

  it('should not reset columns config if header type in Entity column is changed', () => {
    let datatableWidget = instantiateDataTableWidget();
    datatableWidget.translateService.translateInstant = () => { return 'Entity'; };

    datatableWidget.config.styles.columns = {header2: 200};
    datatableWidget.config.columnsOrder.columns = {header2: 3};
    let headers = [{name: 'header1', labels: ['header1']}, {name: 'header2', labels: ['Entity']}, {name: 'header3', labels: ['header3']}];
    let response = [{name: 'header3', labels: ['header3']}, {name: 'header5', labels: ['Entity']}, {name: 'header1', labels: ['header1']}];
    datatableWidget.resetOrderAndWidthConfig(headers, response);
    expect(datatableWidget.config.styles.columns).to.eql({header5: 200});
    expect(datatableWidget.config.columnsOrder.columns).to.eql({header5: 3});
  });

  it('should unsubscribe from the events when destroyed', ()=> {
    let spyUnsubscribe = {
      unsubscribe: sinon.spy()
    };
    let eventbus = {
      subscribe: () => {
        return spyUnsubscribe
      }
    };
    let datatableWidget = instantiateDataTableWidget(undefined, undefined, eventbus);
    datatableWidget.ngOnDestroy();
    expect(spyUnsubscribe.unsubscribe.called).to.be.true;
  });

  it('should properly check if headers are rendered', () => {
    let datatableWidget = instantiateDataTableWidget();
    let headersCells = $('<div><div class="header-cell" data-header-cell-name="header1"></div><div class="header-cell" data-header-cell-name="header2"></div></div>').find('.header-cell');
    let headers = [{
      name: 'header1'
    }, {
      name: 'header2'
    }];
    expect(datatableWidget.areHeadersRendered(headersCells, headers)).to.be.true;
    headers = [{
      name: 'header1'
    }];
    expect(datatableWidget.areHeadersRendered(headersCells, headers)).to.be.false;
  });
});

let selectedProperties = {
  'objectId1': ['prop1', 'prop2'],
  'objectId2': ['prop1', 'prop2', 'prop3', 'prop4', 'prop5'],
  'objectId3': ['prop1', 'prop2', 'prop3']
};

let headers = [
  {name: 'prop1', labels: ['prop1']},
  {name: 'prop2', labels: ['prop2']},
  {name: 'prop3', labels: ['prop3']},
  {name: 'prop4', labels: ['prop4']},
  {name: 'prop5', labels: ['prop5']}
];

function instantiateDataTableWidget(scope, config, eventbus) {
  DatatableWidget.prototype.config = config || {};
  DatatableWidget.prototype.context = mockContext();
  DatatableWidget.prototype.control = mockControl();
  var tooltipsAdapter = {};
  var objectSelectorHelper = {};
  return new DatatableWidget(scope || mock$scope(), mockDefinitionService(), instantiatePropertiesSelectorHelper(),
    PromiseAdapterMock.mockAdapter(), objectSelectorHelper, translateService(), tooltipsAdapter,
    eventbus || new Eventbus(), IdocMocks.mockLocationAdapter('/#/idoc/id?mode=edit'), {}, IdocMocks.mockElement(),
    IdocMocks.mockInterval(), IdocMocks.mockLogger());
}

function mockControl() {
  return {
    getId: () => {
      return 'widget123456';
    },
    saveConfig: () => {},
    getBaseWidget: () => {
      return { saveConfigWithoutReload: () => {} }
    }
  };
}

function mockContext() {
  return {
    getSharedObjects: (objectIds) => {
      let instanceObjects = objectIds.map((objectId) => {
        return new InstanceObject(objectId);
      });
      return Promise.resolve(instanceObjects);
    },
    isPreviewMode: () => {
      return true;
    },
    isEditMode: () => {
      return false;
    },
    getMode: function() {

    },
    isPrintMode: () => {
      return false;
    },
    isModeling: () => {
      return isModeling;
    }
  };
}

function instantiatePropertiesSelectorHelper() {
  return new PropertiesSelectorHelper(PromiseAdapterMock.mockAdapter(), mockDefinitionService());
}

function mockDefinitionService() {
  return {
    getFields: () => {
      return Promise.resolve({data: mockMultipleDefinitions()});
    }
  };
}

function translateService() {
  return {
    translateInstant: (key) => {
      return key;
    }
  }
}


function mockMultipleDefinitions() {
  return [
    {
      identifier: 'GEP111111',
      label: 'Project for testing',
      fields: [
        {
          name: 'property1',
          label: 'Property 1',
          fields: [
            {
              name: 'property5',
              label: 'Property 5'
            },
            {
              name: 'property6',
              label: 'Property 6'
            }
          ]
        },
        {
          name: 'property2',
          label: 'Property 2'
        },
        {
          name: 'property3',
          label: 'GEP111111 Property 3'
        }
      ]
    },
    {
      identifier: 'GEP100002',
      label: 'Test Project',
      fields: [
        {
          name: 'property1',
          label: 'Property 1'
        },
        {
          name: 'property3',
          label: 'GEP100002 Property 3'
        },
        {
          name: 'property5',
          label: 'Property 5'
        }
      ]
    }
  ];
}