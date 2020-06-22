import {AggregatedTable} from 'idoc/widget/aggregated-table/widget';
import {AggregatedTableConfig} from 'idoc/widget/aggregated-table/config'
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';
import {IdocMocks} from 'test/idoc/idoc-mocks';

describe('AggregatedTable', function () {

  it('should calculate and set correct cell width', () => {
    AggregatedTable.prototype.control = {};
    AggregatedTable.prototype.config = {};
    let aggregatedTable = createWidget($element);
    aggregatedTable.tableWidth = 1000;
    aggregatedTable.setColumnsWidth(headers);
    expect(aggregatedTable.config.styles.columns).to.eql({
      'prop1': {width: '500px'},
      'prop2': {width: '500px'}
    });
  });

  it('should decrease cell width in print mode', () => {
    AggregatedTable.prototype.control = {};
    AggregatedTable.prototype.config = {};
    let aggregatedTable = createWidget($element);
    aggregatedTable.config.styles.columns = {};
    aggregatedTable.config.styles.columns['prop1'] = {width: 900};
    aggregatedTable.config.styles.columns['prop2'] = {width: 300};
    aggregatedTable.context = {
      isPrintMode: () => {
        return true;
      }
    };
    aggregatedTable.setColumnsWidth(headers);
    expect(aggregatedTable.config.styles.columns).to.eql({
      'prop1': {width: '300px'},
      'prop2': {width: '100px'}
    });
  });

  it('should update table if groupBy value is changed', () => {
    AggregatedTable.prototype.control = {
      getId: sinon.stub()
    };
    AggregatedTable.prototype.config = {};
    let $scope = mock$scope();
    let aggregatedTable = createWidget($element, {}, $scope);
    aggregatedTable.groupObjects = sinon.spy();
    aggregatedTable.config.groupBy = {name: 'type'};
    $scope.$digest();
    expect(aggregatedTable.groupObjects.callCount).to.equal(1);
    expect(aggregatedTable.groupObjects.args[0][0]).to.equal('type');

    aggregatedTable.clearTable = sinon.spy();
    aggregatedTable.config.groupBy = undefined;
    $scope.$digest();
    expect(aggregatedTable.clearTable.callCount).to.equal(1);
  });

  it('should clear table if error occurs', () => {
    AggregatedTable.prototype.config = {};
    let aggregatedTable = createWidget($element);
    aggregatedTable.selectedObjects = [{
      models: {
        viewModel: {fields: ['1', 'test 1']}
      }
    }];
    aggregatedTable.headers = [
      {name: 'count', labels: ['Number']},
      {name: 'value', labels: ['Value']}
    ];
    aggregatedTable.clearTable();
    expect(aggregatedTable.selectedObjects).to.eql([]);
    expect(aggregatedTable.headers).to.eql([]);
  });

  describe('groupObjects', () => {
    it('should clear table if no objects are found', () => {
      AggregatedTable.prototype.config = {};
      let aggregatedTable = createWidget($element);
      aggregatedTable.objectSelectorHelper = {
        groupSelectedObjects: () => {
          return PromiseStub.reject({reason: 'test error'});
        }
      };
      aggregatedTable.groupObjects();
      expect(aggregatedTable.errorMessage).to.equal('test error');
    });
  });

  describe('swapColumns', () => {
    it('should change columns order if values first option is selected', () => {
      AggregatedTable.prototype.control = {};
      AggregatedTable.prototype.config = {};
      let aggregatedTable = createWidget($element);

      aggregatedTable.headers = [
        {name: 'count', labels: ['Number']},
        {name: 'value', labels: ['Value']}];
      aggregatedTable.selectedObjects = [{
        models: {
          viewModel: {fields: ['1', 'test 1']}
        }
      }, {
        models: {
          viewModel: {fields: ['2', 'test 2']}
        }
      }];

      let expectedHeaders = [
        {name: 'value', labels: ['Value']},
        {name: 'count', labels: ['Number']}];
      let expectedRows = [{
        models: {
          viewModel: {fields: ['test 1', '1']}
        }
      }, {
        models: {
          viewModel: {fields: ['test 2', '2']}
        }
      }];
      aggregatedTable.swapColumns(AggregatedTableConfig.VALUES_FIRST);
      expect(aggregatedTable.headers).to.eql(expectedHeaders);
      expect(aggregatedTable.selectedObjects).to.eql(expectedRows);
    });

    it('should not change columns order if numbers first option is selected', () => {
      AggregatedTable.prototype.control = {};
      AggregatedTable.prototype.config = {};
      let aggregatedTable = createWidget($element);

      aggregatedTable.headers = [
        {name: 'count', labels: ['Number']},
        {name: 'value', labels: ['Value']}];
      aggregatedTable.selectedObjects = [{
        models: {
          viewModel: {fields: ['1', 'test 1']}
        }
      }, {
        models: {
          viewModel: {fields: ['2', 'test 2']}
        }
      }];

      let expectedHeaders = [
        {name: 'count', labels: ['Number']},
        {name: 'value', labels: ['Value']}];
      let expectedRows = [{
        models: {
          viewModel: {fields: ['1', 'test 1']}
        }
      }, {
        models: {
          viewModel: {fields: ['2', 'test 2']}
        }
      }];
      aggregatedTable.swapColumns(AggregatedTableConfig.NUMBERS_FIRST);
      expect(aggregatedTable.headers).to.eql(expectedHeaders);
      expect(aggregatedTable.selectedObjects).to.eql(expectedRows);
    });
  });

  describe('calculateTotal', () => {

    it('should calculate total=0 if not result is returned', () => {
      let total = AggregatedTable.calculateTotal({});
      expect(total).to.equal(0);
    });

    it('should calculate total=3', () => {
      let total = AggregatedTable.calculateTotal({
        'emf:status': {
          'SUBMITTED': 1,
          'STARTED': 1,
          'STOPPED': 1
        }
      });
      expect(total).to.equal(3);
    });

  });

  describe('checkVersionDataResultsExistance', () => {

    it('should group objects when there are results in version data', () => {
      AggregatedTable.prototype.control = {
        getId: function () {
          return 'id';
        }
      };
      AggregatedTable.prototype.config = {
        grid: '',
        versionData: {
          aggregatedData: {
            'emf:value': {
              'emf:admin': 2
            }
          }
        }
      };
      let $scope = mock$scope();
      let widget = createWidget($element, {}, $scope);
      widget.groupObjects = sinon.spy();
      widget.config.groupBy = {name: 'emf:value'};
      $scope.$digest();
      expect(widget.groupObjects.called).to.be.true;
      expect(widget.groupObjects.getCall(0).args[0]).to.be.eq('emf:value');
    });

    it('should not remove error message when there are not results in the version data', () => {
      AggregatedTable.prototype.config = {
        grid: '',
        versionData: {
          aggregatedData: {
            'emf:value': {}
          }
        }
      };
      let $scope = mock$scope();
      let widget = createWidget($element, {}, $scope);
      widget.groupObjects = sinon.spy();
      widget.config.groupBy = {name: 'emf:value'};
      $scope.$digest();
      expect(widget.groupObjects.called).to.be.false;
    });

  });

  describe('handleWidgetReady', () => {
    it('should fire WidgetReadyEvent immediately if no result are found', () => {
      AggregatedTable.prototype.control = {
        getId: () => {
        }
      };
      AggregatedTable.prototype.config = {
        grid: ''
      };
      let widget = createWidget($element);
      widget.selectedObjects = [];
      widget.eventbus.publish = sinon.spy();
      widget.handleWidgetReady(0);
      expect(widget.eventbus.publish.calledOnce).to.be.true;
      expect(widget.eventbus.publish.getCall(0).args[0]).to.be.an.instanceof(WidgetReadyEvent);
    });

    it('should wait until widget renders all rows before WidgetReady event to be fired', () => {
      AggregatedTable.prototype.control = {
        getId: () => {
        }
      };
      AggregatedTable.prototype.config = {
        grid: ''
      };

      let widget = createWidget($element);
      widget.context = {
        isPreviewMode: () => {
          return true
        }
      };
      widget.eventbus.publish = sinon.spy();
      widget.selectedObjects = [{},{},{}];
      widget.handleWidgetReady();
      // these objects are form elements and emmit ``formInitialized`` when ready
      widget.selectedObjects.forEach(object=>object.eventEmitter.publish('formInitialized'));
      expect(widget.eventbus.publish.calledOnce).to.be.true;
      expect(widget.eventbus.publish.getCall(0).args[0]).to.be.an.instanceof(WidgetReadyEvent);
    });
  });

  describe('calculateGroupsCount', () => {
    it('should calculate the table rows count to be 0 if no result is returned', () => {
      expect(AggregatedTable.calculateGroupsCount({})).to.equal(0);
    });

    it('should calculate the table rows count to be 3 when there is one group with three values', () => {
      expect(AggregatedTable.calculateGroupsCount({
        'emf:status': {
          'APPROVED': 1,
          'SUBMITTED': 1,
          'STARTED': 1
        }
      })).to.equal(3);
    });
  });

  describe('Resize', () => {
    let ui = {
      originalElement: {
        next: () => {
          return {outerWidth: () => 50}
        }
      },
      originalSize: {
        width: 100
      },
      size: {
        width: 150
      }
    };

    it('should initialize resizable params correct before resize to start', () => {
      let paramsObject = {
        subTotalWidth: 0,
        nextColumn: {}
      };
      AggregatedTable.onResizeStart(ui, paramsObject);
      expect(paramsObject.subTotalWidth).to.equal(150);
    });

    it('should update widget config when resize stop', () => {
      let paramsObject = {
        subTotalWidth: 0,
        nextColumn: {}
      };
      paramsObject.nextColumn = {
        attr: () => {
          return 'column2'
        },
        width: () => 20
      };
      AggregatedTable.prototype.control = {
        getBaseWidget: () => {
          return {
            saveConfigWithoutReload: () => {
            }
          }
        }
      };
      AggregatedTable.prototype.config = {};
      let widget = createWidget($element);
      widget.config.styles.columns = {
        column1: {width: 70},
        column2: {width: 100}
      };
      let expected = {
        column1: {width: 150},
        column2: {width: 20}
      };
      AggregatedTable.onResizeStop(ui, paramsObject, widget, 'column1');
      expect(widget.config.styles.columns.column1).to.eql(expected.column1);
    });

    it('should update next cell when previous is resized', () => {
      let paramsObject = {
        subTotalWidth: 200,
        nextColumn: {}
      };
      paramsObject.nextColumn = {
        attr: () => {
          return 'column2'
        },
        width: sinon.spy()
      };

      AggregatedTable.prototype.config = {};
      let widget = createWidget($element);
      widget.config.styles.columns = {
        column1: {width: 70},
        column2: {width: 100}
      };
      let expected = {
        column1: {width: 150},
        column2: {width: 20}
      };
      AggregatedTable.onResize(ui, paramsObject, $element, {});
      expect(paramsObject.nextColumn.width.calledOnce).to.be.true;
      expect(paramsObject.nextColumn.width.getCall(0).args[0]).to.equal(50);
    });
  });

  describe('load version data', () => {
    it('should load version data when available', () => {
      let versionData = {
        aggregatedData: {
          version: 'version'
        }
      };
      AggregatedTable.prototype.control = {};
      AggregatedTable.prototype.config = {
        versionData: versionData
      };
      let aggregatedTable = createWidget($element);
      aggregatedTable.convertResponseToModel = sinon.spy();

      aggregatedTable.groupObjects();
      expect(aggregatedTable.convertResponseToModel.called).to.be.true;
      expect(aggregatedTable.convertResponseToModel.args[0][0]['aggregated']).to.equal(versionData.aggregatedData);
    });
  });

  let $element = {
    width: () => {
      return 100;
    },
    find: () => {
      return {
        resizable: () => {
          return {}
        },
        width: () => {
          return {
            next: () => {
              return {
                width: () => {
                }
              }
            }
          }
        },
        length: 3
      };
    }
  };

  function createWidget($element, $interval, $scope) {
    let objectSelectorHelper = {};
    let codelistRestService = {};
    let instanceRestService = {};
    let propertiesSelectorHelper = {
      getDefinitionsArray: () => {
        return PromiseStub.resolve([]);
      }
    };
    return new AggregatedTable($scope || mock$scope(), $element || IdocMocks.mockElement(), $interval || IdocMocks.mockInterval(), objectSelectorHelper, codelistRestService,
      instanceRestService, IdocMocks.mockLogger(), IdocMocks.mockTranslateService(), IdocMocks.mockEventBus(), {}, propertiesSelectorHelper);
  }
});

let headers = [
  {name: 'prop1', labels: ['prop1']},
  {name: 'prop2', labels: ['prop2']}
];