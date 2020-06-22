import {DatatableWidget} from 'idoc/widget/datatable-widget/datatable-widget';
import {DatatableWidgetConfig} from 'idoc/widget/datatable-widget/datatable-widget-config';
import {RelatedObjectsMap} from 'idoc/widget/datatable-widget/datatable-widget';
import {PromiseStub} from 'test/promise-stub';
import {COMMON_PROPERTIES} from 'idoc/widget/properties-selector/properties-selector-helper';
import {SELECT_OBJECT_AUTOMATICALLY, SELECT_OBJECT_MANUALLY} from 'idoc/widget/object-selector/object-selector';
import {InstanceObject} from 'models/instance-object';
import {HEADER_COMPACT, NO_HEADER} from 'instance-header/header-constants';
import {DefinitionModel} from 'models/definition-model';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {ORDER_ASC, ORDER_DESC} from 'search/order-constants';
import {instantiateDataTableWidget, stubElementFind} from 'test/idoc/widget/data-table-widget/datatable-test-helpers';
import {getObjectModels} from './data-table-widget.stub';

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
      let datatableWidget = instantiateDataTableWidget();
      sinon.stub(datatableWidget, 'loadModels');
      let publishOrderChangedEventSpy = sinon.spy(datatableWidget, 'publishOrderChangedEvent');
      datatableWidget.config = {};
      datatableWidget.config.selectedSubPropertiesData = {};
      datatableWidget.config.selectedProperties = [];
      datatableWidget.config.selectedObjects = [];
      datatableWidget.config.selectObjectMode = SELECT_OBJECT_AUTOMATICALLY;
      datatableWidget.config.criteria = {
        criteria: 'criteria'
      };
      datatableWidget.$scope.$digest();
      expect(datatableWidget.loadModels.calledOnce).to.be.true;
      expect(publishOrderChangedEventSpy.calledOnce).to.be.true;

      datatableWidget.config.selectObjectMode = SELECT_OBJECT_MANUALLY;
      datatableWidget.$scope.$digest();
      expect(datatableWidget.loadModels.calledTwice).to.be.true;
      expect(publishOrderChangedEventSpy.calledTwice).to.be.true;
    });

    it('should reset sorting parameters if configuration changes', () => {
      let datatableWidget = instantiateDataTableWidget();
      sinon.stub(datatableWidget, 'loadModels');
      datatableWidget.config = {
        selectObjectMode: SELECT_OBJECT_AUTOMATICALLY
      };
      datatableWidget.config.selectedSubPropertiesData = {};
      datatableWidget.orderBy = 'dcterms:title';
      datatableWidget.orderDirection = 'asc';
      datatableWidget.$scope.$digest();

      expect(datatableWidget.orderBy).to.be.undefined;
      expect(datatableWidget.orderDirection).to.be.undefined;
    });

    it('should assign a watcher for the page size configuration', () => {
      const PAGE_SIZE = 321;
      let datatableWidget = instantiateDataTableWidget();
      sinon.stub(datatableWidget, 'loadModels');

      datatableWidget.config.pageSize = PAGE_SIZE;
      datatableWidget.$scope.$digest();

      expect(datatableWidget.paginationConfig.pageSize).to.equal(PAGE_SIZE);
      expect(datatableWidget.searchArguments.pageSize).to.equal(PAGE_SIZE);

      expect(datatableWidget.paginationConfig.page).to.equal(1);
      expect(datatableWidget.searchArguments.pageNumber).to.equal(1);
    });

    it('should set displayTableHeaderRow to true if not defined', () => {
      let datatableWidget = instantiateDataTableWidget();
      expect(datatableWidget.config.displayTableHeaderRow).to.be.true;
    });

    it('should not set displayTableHeaderRow to true if already set to false', () => {
      let datatableWidget = instantiateDataTableWidget({displayTableHeaderRow: false});
      expect(datatableWidget.config.displayTableHeaderRow).to.be.false;
    });
  });

  describe('onFormInitialized', () => {
    it('should fire WidgetReadyEvent when all forms and headers are loaded or increase the initialized forms counter', () => {
      let datatableWidget = instantiateDataTableWidget();

      let stubAreHeadersRendered = sinon.stub(datatableWidget, 'areHeadersRendered');
      stubAreHeadersRendered.returns(false);
      datatableWidget.onFormInitialized([{}]);
      expect(datatableWidget.initializedFormsCount).to.equal(0);
      expect(datatableWidget.eventbus.publish.callCount).to.equal(0);

      datatableWidget.onFormInitialized([]);
      expect(datatableWidget.initializedFormsCount).to.equal(0);
      expect(datatableWidget.eventbus.publish.callCount).to.equal(0);

      datatableWidget.onFormInitialized([{}, {}]);
      expect(datatableWidget.initializedFormsCount).to.equal(1);
      expect(datatableWidget.eventbus.publish.callCount).to.equal(0);

      stubAreHeadersRendered.returns(true);
      datatableWidget.initializedFormsCount = 1;
      datatableWidget.onFormInitialized([{}, {}]);
      expect(datatableWidget.eventbus.publish.callCount).to.equal(1);
      expect(datatableWidget.eventbus.publish.getCall(0).args[0]).to.be.instanceof(WidgetReadyEvent);
      expect(datatableWidget.initializedFormsCount).to.equal(0);
    });
  });

  describe('showDatatable', () => {
    it('should return true in modeling mode', () => {
      isModeling = true;
      let datatableWidget = instantiateDataTableWidget({}, isModeling);
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

    it('should return promise which resolves with all headers map including selected related object properties', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.config = {
        instanceHeaderType: HEADER_COMPACT,
        selectedProperties: {}
      };
      datatableWidget.ngOnInit();

      let selectedProperties = {
        [COMMON_PROPERTIES]: {
          'property3': {'name': 'property3'},
          'emf:createdBy': {
            'name': 'emf:createdBy',
            'selectedProperties': [
              {'name': 'title', 'label': 'Title'},
              {'name': 'emf:department', 'label': 'Department'}
            ]
          }
        },
        GEP111111: {
          'property3': {'name': 'property3'},
          'property5': {'name': 'property5'},
          'emf:createdBy': {
            'name': 'emf:createdBy',
            'selectedProperties': [
              {'name': 'title', 'label': 'Title'},
              {'name': 'emf:department', 'label': 'Department'}
            ]
          }
        },
        GEP100002: {
          'property1': {'name': 'property1'},
          'property3': {'name': 'property3'},
          'emf:createdBy': {
            'name': 'emf:createdBy',
            'selectedProperties': [
              {'name': 'title', 'label': 'Title'},
              {'name': 'emf:department', 'label': 'Department'}
            ]
          }
        }
      };

      datatableWidget.generateHeaders(selectedProperties).then((headers) => {
        const expected = [
          {'name': HEADER_COMPACT, 'labels': ['dtw.column.header'], 'uri': 'emf:altTitle', 'type': 'string', 'showSortIcon': true},
          {'name': 'property3', 'labels': ['GEP111111 Property 3', 'GEP100002 Property 3'], 'uri': undefined, 'multivalue': undefined, 'showSortIcon': true},
          {'name': 'property5', 'labels': ['Property 5'], 'uri': undefined, 'multivalue': undefined, 'showSortIcon': true},
          {'name': 'emf:createdBy', 'labels': ['Created by'], 'uri': undefined, 'multivalue': undefined, 'showSortIcon': true},
          {'name': 'emf:createdBy:title', 'propertyName': 'title', 'relationName': 'emf:createdBy', 'labels': ['Created by: Title'], 'uri': undefined, 'multivalue': undefined, 'showSortIcon': false, relationProperty: true},
          {'name': 'emf:createdBy:emf:department', 'propertyName': 'emf:department', 'relationName': 'emf:createdBy', 'labels': ['Created by: Department'], 'uri': undefined, 'multivalue': undefined, 'showSortIcon': false, relationProperty: true},
          {'name': 'property1', 'labels': ['Property 1'], 'uri': undefined, 'multivalue': undefined, 'showSortIcon': true}
        ];
        expect(headers).to.eql(expected);
        expect(headers[1].labels).to.eql(['GEP111111 Property 3', 'GEP100002 Property 3']);
      });
    });

    it('should return promise which resolves with headers map', (done) => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.config = {
        instanceHeaderType: HEADER_COMPACT,
        selectedProperties: {}
      };
      datatableWidget.ngOnInit();

      let selectedProperties = {
        [COMMON_PROPERTIES]: {'property3': {'name': 'property3'}},
        GEP111111: {'property3': {'name': 'property3'}, 'property5': {'name': 'property5'}},
        GEP100002: {'property1': {'name': 'property1'}, 'property3': {'name': 'property3'}}
      };
      datatableWidget.generateHeaders(selectedProperties).then((headers) => {
        expect(headers).to.eql([
          {name: HEADER_COMPACT, labels: ['dtw.column.header'], uri: 'emf:altTitle', type: 'string', showSortIcon: true},
          {name: 'property3', labels: ['GEP111111 Property 3', 'GEP100002 Property 3'], uri: undefined, multivalue: undefined, showSortIcon: true},
          {name: 'property5', labels: ['Property 5'], uri: undefined, multivalue: undefined, showSortIcon: true},
          {name: 'property1', labels: ['Property 1'], uri: undefined, multivalue: undefined, showSortIcon: true}
        ]);
        expect(headers[1].labels).to.eql(['GEP111111 Property 3', 'GEP100002 Property 3']);
        done();
      }).catch(done);
    });

    it('should resolve with single entry for instance\'s compact header if selected properties is not defined', (done) => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.config = {
        instanceHeaderType: HEADER_COMPACT,
        selectedProperties: {}
      };
      datatableWidget.ngOnInit();
      datatableWidget.generateHeaders().then((headers) => {
        expect(headers).to.have.length(1);
        expect(headers[0]).to.have.property('name', HEADER_COMPACT);
        done();
      }).catch(done);
    });

    it('should resolve with single entry for instance\'s compact header if selected properties is empty', (done) => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.config = {
        instanceHeaderType: HEADER_COMPACT,
        selectedProperties: {}
      };
      datatableWidget.ngOnInit();
      datatableWidget.generateHeaders({}).then((headers) => {
        expect(headers).to.have.length(1);
        expect(headers[0]).to.have.property('name', HEADER_COMPACT);
        done();
      }).catch(done);
    });

    it('should resolve with table headers array without instance headers column', (done) => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.config = {
        instanceHeaderType: NO_HEADER,
        selectedProperties: {}
      };
      datatableWidget.ngOnInit();
      let selectedProperties = {
        [COMMON_PROPERTIES]: {'property3': {'name': 'property3'}},
        GEP111111: {'property3': {'name': 'property3'}, 'property5': {'name': 'property5'}},
        GEP100002: {'property1': {'name': 'property1'}, 'property3': {'name': 'property3'}}
      };
      datatableWidget.generateHeaders(selectedProperties).then((headers) => {
        expect(headers).to.eql([
          {name: 'property3', labels: ['GEP111111 Property 3', 'GEP100002 Property 3'], uri: undefined, multivalue: undefined, showSortIcon: true},
          {name: 'property5', labels: ['Property 5'], uri: undefined, multivalue: undefined, showSortIcon: true},
          {name: 'property1', labels: ['Property 1'], uri: undefined, multivalue: undefined, showSortIcon: true}
        ]);
        done();
      }).catch(done);
    });

    it('should resolve with showSortIcon false if current object is version', (done) => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.config = {
        instanceHeaderType: NO_HEADER,
        selectedProperties: {}
      };
      datatableWidget.ngOnInit();
      let selectedProperties = {
        [COMMON_PROPERTIES]: {'property3': {'name': 'property3'}},
        GEP111111: {'property3': {'name': 'property3'}, 'property5': {'name': 'property5'}},
        GEP100002: {'property1': {'name': 'property1'}, 'property3': {'name': 'property3'}}
      };
      datatableWidget.isVersion = () => {
        return PromiseStub.resolve(true);
      };
      datatableWidget.generateHeaders(selectedProperties).then((headers) => {
        expect(headers).to.eql([
          {name: 'property3', labels: ['GEP111111 Property 3', 'GEP100002 Property 3'], uri: undefined, multivalue: undefined, showSortIcon: false},
          {name: 'property5', labels: ['Property 5'], uri: undefined, multivalue: undefined, showSortIcon: false},
          {name: 'property1', labels: ['Property 1'], uri: undefined, multivalue: undefined, showSortIcon: false}
        ]);
        done();
      }).catch(done);
    });
  });

  describe('Datatable grid', ()=> {
    it('watcher should update the formControl grid value on config.grid value change', ()=> {
      let datatableWidget = instantiateDataTableWidget();
      sinon.stub(datatableWidget, 'loadModels');
      expect(datatableWidget.formConfig.styles.grid).to.equal(undefined);
      datatableWidget.config.grid = DatatableWidgetConfig.GRID_ON;
      datatableWidget.$scope.$digest();
      expect(datatableWidget.formConfig.styles.grid).to.equal(DatatableWidgetConfig.GRID_ON);
    });
  });

  describe('setColumnsWidth', () => {
    const WIDTH_100 = 100;
    const WIDTH_200 = 200;
    const WIDTH_150 = 150;
    const WIDTH_400 = 400;


    it('should calculate and set columns width style in the widget config', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.$element.find = stubElementFind();
      datatableWidget.$element.width = () => {
        return 1000;
      };
      datatableWidget.setColumnsWidth(headers);
      expect(datatableWidget.config.styles.columns).to.eql({
        'prop1': {
          calculatedWidth: WIDTH_200,
          width: WIDTH_200
        },
        'prop2': {
          calculatedWidth: WIDTH_200,
          width: WIDTH_200
        },
        'prop3': {
          calculatedWidth: WIDTH_200,
          width: WIDTH_200
        },
        'prop4': {
          calculatedWidth: WIDTH_200,
          width: WIDTH_200
        },
        'prop5': {
          calculatedWidth: WIDTH_200,
          width: WIDTH_200
        }
      });
    });

    it('should set columns with to default minimum calculated width is less that the allowed minimum', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.$element.find = stubElementFind();
      datatableWidget.$element.width = () => {
        return 500;
      };
      datatableWidget.setColumnsWidth(headers);
      expect(datatableWidget.config.styles.columns).to.eql({
        'prop1': {
          calculatedWidth: WIDTH_100,
          width: WIDTH_150
        },
        'prop2': {
          calculatedWidth: WIDTH_100,
          width: WIDTH_150
        },
        'prop3': {
          calculatedWidth: WIDTH_100,
          width: WIDTH_150
        },
        'prop4': {
          calculatedWidth: WIDTH_100,
          width: WIDTH_150
        },
        'prop5': {
          calculatedWidth: WIDTH_100,
          width: WIDTH_150
        }
      });
    });

    it('should set columns elements width', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.$element.width = () => {
        return 800;
      };
      let findStub = sinon.stub(datatableWidget.$element, 'find', () => {
        return {
          width: sinon.spy()
        };
      });
      datatableWidget.recalculatePanelsWidth = sinon.spy();
      let testHeaders = [{name: 'emf:isTemplateOf', labels: ['Is template of']}].concat(headers);
      datatableWidget.setColumnsWidth(testHeaders);
      expect(findStub.callCount).to.equals(testHeaders.length);
      expect(findStub.getCall(0).args[0]).to.equals('.table-body form #emf\\:isTemplateOf-wrapper, .filter-cell[data-filter-cell-name=emf\\:isTemplateOf], .header-cell[data-header-cell-name=emf\\:isTemplateOf]');
      expect(findStub.getCall(0).returnValue.width.callCount).to.equals(1);
      expect(findStub.getCall(0).returnValue.width.getCall(0).args[0]).to.equals(WIDTH_150);
    });

    it('should add widget resize listener on creation', () => {
      let datatableWidget = instantiateDataTableWidget();
      expect(datatableWidget.handleWidgetResize).to.be.not.undefined;
    });

    it('should set columns width using ratio between new and old widget size', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.$element.find = stubElementFind();
      datatableWidget.$element.width = () => {
        return 1000;
      };
      datatableWidget.setColumnsWidth(headers);
      expect(datatableWidget.config.styles.columns).to.eql({
        'prop1': {
          calculatedWidth: WIDTH_200,
          width: WIDTH_200
        },
        'prop2': {
          calculatedWidth: WIDTH_200,
          width: WIDTH_200
        },
        'prop3': {
          calculatedWidth: WIDTH_200,
          width: WIDTH_200
        },
        'prop4': {
          calculatedWidth: WIDTH_200,
          width: WIDTH_200
        },
        'prop5': {
          calculatedWidth: WIDTH_200,
          width: WIDTH_200
        }
      });

      // when ratio is 0.5 (widgetWidth / $element.width = 0.5 ) cells size should be 400 (initial size 200 / 0.5 = 400)
      datatableWidget.config.widgetWidth = 500;
      datatableWidget.setColumnsWidth(headers);
      expect(datatableWidget.config.styles.columns).to.eql({
        'prop1': {
          calculatedWidth: WIDTH_400,
          width: WIDTH_400
        },
        'prop2': {
          calculatedWidth: WIDTH_400,
          width: WIDTH_400
        },
        'prop3': {
          calculatedWidth: WIDTH_400,
          width: WIDTH_400
        },
        'prop4': {
          calculatedWidth: WIDTH_400,
          width: WIDTH_400
        },
        'prop5': {
          calculatedWidth: WIDTH_400,
          width: WIDTH_400
        }
      });
    });
  });

  describe('paginationCallback', () => {
    it('should update the search arguments', () => {
      const PAGE_NUMBER = 123;
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.loadObjects = sinon.spy();
      datatableWidget.paginationCallback({
        pageNumber: PAGE_NUMBER
      });
      expect(datatableWidget.searchArguments.pageNumber).to.equal(PAGE_NUMBER);
      expect(datatableWidget.loadObjects.calledOnce).to.be.true;
    });
  });

  it('getColumnHeaderLabel should concatenate all labels into comma separated string', () => {
    let datatableWidget = instantiateDataTableWidget();
    let tableHeader = {name: 'property3', labels: ['GEP111111 Property 3', 'GEP100002 Property 3']};
    expect(datatableWidget.getColumnHeaderLabel(tableHeader)).to.equal('GEP111111 Property 3, GEP100002 Property 3');
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

  describe('convertViewModel', () => {
    it('should return an instance with converted view model', () => {
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
        GEP111111: {'field1': {'name': 'field1'}, 'field2': {'name': 'field2'}}
      };
      const loadersResults = {
        objects: [],
        relatedObjectsMap: {}
      };
      let convertedInstance = datatableWidget.convertViewModel(instance, headers, selectedProperties, loadersResults);
      expect(convertedInstance.models.viewModel.fields).to.have.length(3);
      expect(convertedInstance.models.viewModel.fields[0].identifier).to.equal(HEADER_COMPACT);
      expect(convertedInstance.models.viewModel.fields[1].identifier).to.equal('field1');
      expect(convertedInstance.models.viewModel.fields[1].control).to.be.undefined;
      expect(convertedInstance.models.viewModel.fields[2].identifier).to.equal('field3');
      expect(convertedInstance.models.viewModel.fields[2].control.identifier).to.equal('EMPTY_CELL');
    });
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

  it('should prepare correct properties config', ()=> {
    let datatableWidget = instantiateDataTableWidget();
    datatableWidget.context.isEditMode = () => {
      return false;
    };
    datatableWidget.config.instanceHeaderType = 'default_header';
    datatableWidget.config.selectedProperties = {
      [COMMON_PROPERTIES]: {'property3': {'name': 'property3'}},
      GEP111111: {'property3': {'name': 'property3'}, 'property5': {'name': 'property5'}},
      GEP100002: {'property1': {'name': 'property1'}, 'property3': {'name': 'property3'}}
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

  describe('resetOrderAndWidthConfig', () => {
    const HEADER_WIDTH = 200;

    it('should reset columns config if properties set is changed', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.config.styles.columns = {header2: HEADER_WIDTH};
      datatableWidget.config.columnsOrder.columns = {header2: 3};
      let headers = [{name: 'header1', labels: ['header1']}, {name: 'header2', labels: ['header2']}, {name: 'header3', labels: ['header3']}];
      let response = [{name: 'header3', labels: ['header3']}, {name: 'header4', labels: ['header4']}, {name: 'header1', labels: ['header1']}];
      datatableWidget.resetOrderAndWidthConfig(headers, response);
      expect(datatableWidget.config.styles.columns).to.eql({});
      expect(datatableWidget.config.columnsOrder.columns).to.eql({});
    });

    it('should reset columns config if property is added or removed', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.config.styles.columns = {header2: HEADER_WIDTH};
      datatableWidget.config.columnsOrder.columns = {header2: 3};
      let headers = [{name: 'header1'}, {name: 'header2'}, {name: 'header3'}];
      let response = [{name: 'header1'}, {name: 'header2'}, {name: 'header3'}, {name: 'header4'}];
      datatableWidget.resetOrderAndWidthConfig(headers, response);
      expect(datatableWidget.config.styles.columns).to.eql({});
      expect(datatableWidget.config.columnsOrder.columns).to.eql({});
    });

    it('should not reset columns config if headers are not saved', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.config.styles.columns = {header2: HEADER_WIDTH};
      datatableWidget.config.columnsOrder.columns = {header2: 3};
      let response = [{name: 'header3'}, {name: 'header2'}, {name: 'header1'}];
      datatableWidget.resetOrderAndWidthConfig(undefined, response);
      expect(datatableWidget.config.styles.columns).to.eql({header2: HEADER_WIDTH});
      expect(datatableWidget.config.columnsOrder.columns).to.eql({header2: 3});
    });

    it('should not reset columns config if properties set is not changed', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.config.styles.columns = {header2: HEADER_WIDTH};
      datatableWidget.config.columnsOrder.columns = {header2: 3};
      let headers = [{name: 'header1', labels: ['header1']}, {name: 'header2', labels: ['header2']}, {name: 'header3', labels: ['header3']}];
      let response = [{name: 'header3', labels: ['header3']}, {name: 'header2', labels: ['header2']}, {name: 'header1', labels: ['header1']}];
      datatableWidget.resetOrderAndWidthConfig(headers, response);
      expect(datatableWidget.config.styles.columns).to.eql({header2: HEADER_WIDTH});
      expect(datatableWidget.config.columnsOrder.columns).to.eql({header2: 3});
    });

    it('should not reset columns config if header type in Entity column is changed', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.translateService.translateInstant = () => {
        return 'Entity';
      };

      datatableWidget.config.styles.columns = {header2: HEADER_WIDTH};
      datatableWidget.config.columnsOrder.columns = {header2: 3};
      let headers = [{name: 'header1', labels: ['header1']}, {name: 'header2', labels: ['Entity']}, {name: 'header3', labels: ['header3']}];
      let response = [{name: 'header3', labels: ['header3']}, {name: 'header5', labels: ['Entity']}, {name: 'header1', labels: ['header1']}];
      datatableWidget.resetOrderAndWidthConfig(headers, response);
      expect(datatableWidget.config.styles.columns).to.eql({header5: HEADER_WIDTH});
      expect(datatableWidget.config.columnsOrder.columns).to.eql({header5: 3});
    });
  });

  it('should remove the resizeListener when destroyed', ()=> {
    let datatableWidget = instantiateDataTableWidget();
    datatableWidget.control.baseWidget = {
      $onDestroy: sinon.stub()
    };

    datatableWidget.ngOnDestroy();
    expect(datatableWidget.resizeListener.callCount).to.equals(1);
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

  describe('loadModels', () => {
    const WIDGET_WIDTH = 200;

    it('should remove not found objects from widget\'s config', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.$element.find = stubElementFind();
      datatableWidget.config.selectObjectMode = SELECT_OBJECT_MANUALLY;
      datatableWidget.config.selectedObjects = [];
      datatableWidget.objectSelectorHelper.getSelectedObjects.returns(PromiseStub.resolve([]));
      sinon.stub(datatableWidget, 'generateHeaders', () => {
        return PromiseStub.resolve([]);
      });

      sinon.stub(datatableWidget, 'getSearchableProperties', () => {
        return PromiseStub.resolve([]);
      });

      sinon.stub(datatableWidget, 'getObjectsLoaderConfig', () => {
        return PromiseStub.resolve(datatableWidget.config);
      });

      sinon.stub(datatableWidget.context, 'getSharedObjects', () => {
        return PromiseStub.resolve({
          data: [],
          notFound: ['emf:123456']
        });
      });
      datatableWidget.context.isEditMode = () => {
        return true;
      };
      datatableWidget.control.saveConfig = sinon.spy();

      datatableWidget.loadModels().then(() => {
        expect(datatableWidget.objectSelectorHelper.removeSelectedObjects.callCount).to.equal(1);
        expect(datatableWidget.objectSelectorHelper.removeSelectedObjects.args[0][1]).to.eql(['emf:123456']);
        expect(datatableWidget.control.saveConfig.callCount).to.equal(1);
      });
    });

    it('should fire widgetReadyEvent if no object is selected', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.$element.width = () => {
        return WIDGET_WIDTH;
      };

      datatableWidget.$element.find = stubElementFind();

      datatableWidget.objectSelectorHelper.getSelectedObjects.returns(PromiseStub.reject({}));

      datatableWidget.generateHeaders = () => {
        return PromiseStub.resolve([]);
      };

      datatableWidget.getSearchableProperties = () => {
        return PromiseStub.resolve([]);
      };

      datatableWidget.getObjectsLoaderConfig = () => {
        return PromiseStub.resolve(datatableWidget.config);
      };

      datatableWidget.loadModels().then(() => {
        expect(datatableWidget.eventbus.publish.calledOnce).to.be.true;
        expect(datatableWidget.eventbus.publish.getCall(0).args[0]).to.be.instanceof(WidgetReadyEvent);
        expect(datatableWidget.config.widgetWidth).to.equal(WIDGET_WIDTH);
      });
    });

    it('should clear filter criteria', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.$element.width = () => {
        return WIDGET_WIDTH;
      };

      datatableWidget.$element.find = stubElementFind();

      datatableWidget.objectSelectorHelper.getSelectedObjects.returns(PromiseStub.reject({}));

      datatableWidget.generateHeaders = () => {
        return PromiseStub.resolve([]);
      };

      datatableWidget.getSearchableProperties = () => {
        return PromiseStub.resolve([]);
      };

      datatableWidget.getObjectsLoaderConfig = () => {
        return PromiseStub.resolve(datatableWidget.config);
      };

      datatableWidget.setFilterCriteria = sinon.spy();

      datatableWidget.loadModels().then(() => {
        expect(datatableWidget.setFilterCriteria.callCount).to.equals(1);
        expect(datatableWidget.setFilterCriteria.getCall(0).args[0]).to.be.undefined;
      });
    });

    it('should reset sort tooltips initialization', () => {
      let datatableWidget = instantiateDataTableWidget();

      datatableWidget.objectSelectorHelper.getSelectedObjects.returns(PromiseStub.reject({}));
      datatableWidget.generateHeaders = () => {
        return PromiseStub.resolve([]);
      };
      datatableWidget.getSearchableProperties = () => {
        return PromiseStub.resolve([]);
      };
      datatableWidget.getObjectsLoaderConfig = () => {
        return PromiseStub.resolve(datatableWidget.config);
      };

      datatableWidget.sortTooltipsInitialized = true;
      datatableWidget.loadModels();
      expect(datatableWidget.sortTooltipsInitialized).to.be.false;
    });

    it('should reset columns order and width when properties are added or removed', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.$element.find = stubElementFind();
      datatableWidget.config.selectObjectMode = SELECT_OBJECT_MANUALLY;
      datatableWidget.config.selectedObjects = [];
      datatableWidget.config.styles.columns = {
        compact_header: { width: 500 },
        title: { width: 200 }
      };
      datatableWidget.config.columnsOrder.columns = {
        title: { index: 0 },
        compact_header: { index: 0 }
      };

      datatableWidget.widgetConfig.headers = [
        {
          name: 'title',
          type: 'string',
          labels: ['Title'],
          multivalue: false
        },
        {
          name: 'version',
          type: 'string',
          labels: ['Version'],
          multivalue: false
        }
      ];

      datatableWidget.objectSelectorHelper.getSelectedObjects.returns(PromiseStub.resolve([]));
      sinon.stub(datatableWidget, 'setColumnsWidth');

      sinon.stub(datatableWidget, 'getSearchableProperties', () => {
        return PromiseStub.resolve([]);
      });

      sinon.stub(datatableWidget, 'getObjectsLoaderConfig', () => {
        return PromiseStub.resolve(datatableWidget.config);
      });

      sinon.stub(datatableWidget.context, 'getSharedObjects', () => {
        return PromiseStub.resolve({
          data: [],
          notFound: ['emf:123456']
        });
      });

      sinon.stub(datatableWidget, 'generateHeaders', () => {
        return PromiseStub.resolve([
          {
            name: 'title',
            type: 'string',
            labels: ['Title'],
            multivalue: false
          },
          {
            name: 'version',
            type: 'string',
            labels: ['Version'],
            multivalue: false
          },
          {
            name: 'modifiedOn',
            type: 'datetime',
            labels: ['Modified on'],
            multivalue: false
          }
        ]);
      });

      datatableWidget.loadModels();

      expect(datatableWidget.config.styles.columns).to.eql({});
      expect(datatableWidget.config.columnsOrder.columns).to.eql({});
      expect(datatableWidget.control.getBaseWidget().saveConfigWithoutReload.called).to.be.true;
    });
  });

  describe('loadRelatedObjects', () => {
    it('should load related object properties if there are selected', () => {
      let sharedObjects = {
        data: [], // InstanceObject's array
        notFound: []
      };

      let models = getObjectModels();
      Object.keys(models).forEach((id) => {
        let instanceObject = new InstanceObject(id, models[id].models);
        sharedObjects.data.push(instanceObject);
      });

      let selectedProperties = {
        [COMMON_PROPERTIES]: {
          'title': {'name': 'title'},
          'emf:createdBy': {
            'name': 'emf:createdBy',
            'selectedProperties': [
              {'name': 'title', 'label': 'Title'},
              {'name': 'emf:department', 'label': 'Department'}
            ]
          }
        },
        GEP111111: {
          'title': {'name': 'title'},
          'emf:createdBy': {
            'name': 'emf:createdBy',
            'selectedProperties': [
              {'name': 'title', 'label': 'Title'},
              {'name': 'emf:department', 'label': 'Department'}
            ]
          }
        },
        GEP100002: {
          'title': {'name': 'title'},
          'emf:createdBy': {
            'name': 'emf:createdBy',
            'selectedProperties': [
              {'name': 'title', 'label': 'Title'},
              {'name': 'emf:department', 'label': 'Department'}
            ]
          }
        }
      };
      let headersModel = [];

      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.widgetConfig.headers = headersModel;

      datatableWidget.loadRelatedObjects(sharedObjects, selectedProperties).then(relatedObjectsMap => {
        let expected = new RelatedObjectsMap();
        expected.map = {
          'id:1': {
            'emf:createdBy': [
              new InstanceObject('id:1_1')
            ]
          },
          'id:2': {
            'emf:createdBy': [
              new InstanceObject('id:2_1')
            ]
          }
        };
        expect(relatedObjectsMap).to.eql(expected);
      });
    });
  });

  describe('buildRelatedObjectsLoaderConfig', () => {
    it('should build configuration for partial instance properties loading', () => {
      let headersModel = [{
        'name': 'compact_header',
        'labels': ['dtw.column.header'],
        'uri': 'emf:altTitle',
        'type': 'string',
        'showSortIcon': true
      }, {
        'name': 'emf:createdBy',
        'labels': ['Created by'],
        'showSortIcon': true
      }, {
        'name': 'emf:createdBy:title',
        'propertyName': 'title',
        'relationName': 'emf:createdBy',
        'relationProperty': true,
        'labels': ['Created by: Title'],
        'showSortIcon': false
      }, {
        'name': 'emf:createdBy:emf:department',
        'propertyName': 'emf:department',
        'relationName': 'emf:createdBy',
        'relationProperty': true,
        'labels': ['Created by: Department'],
        'showSortIcon': false
      }, {
        'name': 'Status',
        'labels': ['Status'],
        'showSortIcon': true
      }];
      let config = DatatableWidget.buildRelatedObjectsLoaderConfig('emf:createdBy', headersModel);
      expect(config).to.eql({
        params: {
          properties: ['title', 'emf:department']
        }
      });
    });
  });

  describe('getObjectsLoader', () => {
    it('should set search arguments for sorting with user defined arguments', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.objectSelectorHelper.getSelectedObjects.returns(PromiseStub.resolve({}));
      datatableWidget.context.getSharedObjects = () => PromiseStub.resolve({data: [], notFound: []});
      datatableWidget.getConfig = sinon.spy();
      datatableWidget.config.orderBy = 'emf:createdOn';
      datatableWidget.config.orderDirection = 'desc';
      datatableWidget.orderBy = 'dcterms:title';
      datatableWidget.orderDirection = 'asc';
      datatableWidget.getObjectsLoader(datatableWidget.config);
      expect(datatableWidget.searchArguments.orderBy).to.equals('dcterms:title');
      expect(datatableWidget.searchArguments.orderDirection).to.equals('asc');
    });

    it('should set search arguments for sorting with default arguments', () => {
      let datatableWidget = instantiateDataTableWidget();
      datatableWidget.objectSelectorHelper.getSelectedObjects.returns(PromiseStub.resolve({}));
      datatableWidget.context.getSharedObjects = () => PromiseStub.resolve({data: [], notFound: []});
      datatableWidget.getConfig = sinon.spy();
      datatableWidget.config.orderBy = 'emf:createdOn';
      datatableWidget.config.orderDirection = 'desc';
      datatableWidget.getObjectsLoader(datatableWidget.config);
      expect(datatableWidget.searchArguments.orderBy).to.equals('emf:createdOn');
      expect(datatableWidget.searchArguments.orderDirection).to.equals('desc');
    });
  });

  describe('sortObjects', () => {
    const CODELISTS = [7, 210];

    let datatableWidget;

    beforeEach(() => {
      datatableWidget = instantiateDataTableWidget();
      datatableWidget.$element = $('<div><div data-header-cell-name="title"><span class="sort-icon"><i class="fa fa-sort"></i></span></div><div data-header-cell-name="type"><span class="sort-icon"><i class="fa fa-sort"></i></span></div></div>');
      sinon.stub(datatableWidget, 'loadObjects');
    });

    it('should set order by to passed header URL and order direction to ascending', () => {
      let header = buildHeader('title', 'dcterms:title');
      datatableWidget.sortObjects(header);
      expect(datatableWidget.orderBy).to.equals('dcterms:title');
      expect(datatableWidget.orderDirection).to.equals(ORDER_ASC);
      expect(datatableWidget.$element.find(`[data-header-cell-name=${header.name}] i`).attr('class')).to.equals('fa fa-sort-asc');
    });

    it('should set order direction to descending if called second time for the same header', () => {
      let header = buildHeader('title', 'dcterms:title');
      datatableWidget.orderBy = 'dcterms:title';
      datatableWidget.orderDirection = ORDER_ASC;
      datatableWidget.sortObjects(header);
      expect(datatableWidget.orderBy).to.equals('dcterms:title');
      expect(datatableWidget.orderDirection).to.equals(ORDER_DESC);
      expect(datatableWidget.$element.find(`[data-header-cell-name=${header.name}] i`).attr('class')).to.equals('fa fa-sort-desc');
    });

    it('should set order direction to ascending and change order by when called for another header', () => {
      let header = buildHeader('type', 'emf:type');
      datatableWidget.orderBy = 'dcterms:title';
      datatableWidget.orderDirection = ORDER_ASC;
      datatableWidget.sortObjects(header);
      expect(datatableWidget.orderBy).to.equals('emf:type');
      expect(datatableWidget.orderDirection).to.equals(ORDER_ASC);
      expect(datatableWidget.$element.find(`[data-header-cell-name=${header.name}] i`).attr('class')).to.equals('fa fa-sort-asc');
    });

    // Sorting works in the following order when sorting by the same column: ascending -> descending -> reset (no user defined order)
    it('should reset (remove) soring parameters if called for the same header if previous order direction was descending', () => {
      let header = buildHeader('title', 'dcterms:title');
      datatableWidget.orderBy = 'dcterms:title';
      datatableWidget.orderDirection = ORDER_DESC;
      datatableWidget.sortObjects(header);
      expect(datatableWidget.orderBy).to.be.undefined;
      expect(datatableWidget.orderDirection).to.be.undefined;
      expect(datatableWidget.$element.find(`[data-header-cell-name=${header.name}] i`).attr('class')).to.equals('fa fa-sort');
    });

    it('should add codelist number to order arguments', () => {
      let header = buildHeader('type', 'emf:type', CODELISTS);
      datatableWidget.sortObjects(header);
      expect(datatableWidget.orderBy).to.equals('emf:type');
      expect(datatableWidget.orderDirection).to.equals(ORDER_ASC);
      expect(datatableWidget.orderByCodelistNumbers).to.equals('7,210');
      expect(datatableWidget.$element.find(`[data-header-cell-name=${header.name}] i`).attr('class')).to.equals('fa fa-sort-asc');
    });

    it('should publish order changed event', () => {
      let header = buildHeader('type', 'emf:type', CODELISTS);
      datatableWidget.sortObjects(header);
      expect(datatableWidget.control.publish.callCount).to.equals(1);
      expect(datatableWidget.control.publish.args[0][0]).to.equals('orderChanged');
      expect(datatableWidget.control.publish.args[0][1]).to.eql({
        orderBy: 'emf:type',
        orderDirection: ORDER_ASC,
        orderByCodelistNumbers: '7,210'
      });
    });

    it('should reset sorting tooltips', () => {
      let header = buildHeader('type', 'emf:type', CODELISTS);
      datatableWidget.resetSortingTooltips = sinon.spy();
      datatableWidget.sortObjects(header);
      expect(datatableWidget.resetSortingTooltips.callCount).to.equals(1);
    });
  });

  it('publishOrderChangedEvent should publish orderChanged event with proper payload', () => {
    let datatableWidget = instantiateDataTableWidget();
    datatableWidget.orderBy = 'emf:type';
    datatableWidget.orderDirection = ORDER_ASC;
    datatableWidget.orderByCodelistNumbers = '7,210';
    datatableWidget.publishOrderChangedEvent();
    expect(datatableWidget.control.publish.callCount).to.equals(1);
    expect(datatableWidget.control.publish.args[0][0]).to.equals('orderChanged');
    expect(datatableWidget.control.publish.args[0][1]).to.eql({
      orderBy: 'emf:type',
      orderDirection: ORDER_ASC,
      orderByCodelistNumbers: '7,210'
    });
  });
});

let headers = [
  {name: 'prop1', labels: ['prop1']},
  {name: 'prop2', labels: ['prop2']},
  {name: 'prop3', labels: ['prop3']},
  {name: 'prop4', labels: ['prop4']},
  {name: 'prop5', labels: ['prop5']}
];

function buildHeader(name, uri, cls) {
  let header = {
    name,
    uri
  };
  if (cls) {
    header.codeLists = cls;
  }
  return header;
}