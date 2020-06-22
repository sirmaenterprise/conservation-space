import {SELECT_OBJECT_AUTOMATICALLY, SELECT_OBJECT_MANUALLY} from 'idoc/widget/object-selector/object-selector';
import {ChartViewWidget} from 'idoc/widget/chart-view-widget/chart-view-widget';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';
import {TranslateService} from 'services/i18n/translate-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';

const CONTROL_ID = 'controlId';

describe('ChartViewWidget', () => {
  let chartViewWidget;
  let eventbus;
  let objectSelectorHelper;

  beforeEach(() => {
    ChartViewWidget.prototype.control = {
      getEditor: () => {
        return {
          $element: $('<div></div>')
        };
      },
      getId: () => CONTROL_ID
    };

    ChartViewWidget.prototype.context = {
      isPrintMode: () => false
    };

    eventbus = stub(Eventbus);
    objectSelectorHelper = stub(ObjectSelectorHelper);
    chartViewWidget = new ChartViewWidget($('<span></span>'), mock$scope(), undefined, eventbus,
      undefined, undefined, undefined, PromiseStub, objectSelectorHelper, undefined, stub(TranslateService));
  });

  it('should generate labels map for object properties', () => {
    chartViewWidget.instanceRestService = {
      loadBatch: (ids) => {
        let data = {
          data: ids.map((id) => {
            return {
              id,
              headers: {
                compact_header: `Compact header for ${id}`
              }
            };
          })
        };
        return PromiseStub.resolve(data);
      }
    };

    chartViewWidget.getLabelsForObjectProperties(['emf:123456', 'emf:999888']).then((result) => {
      expect(Object.keys(result)).to.eql(['emf:123456', 'emf:999888']);
      expect(result['emf:123456']).to.equals('Compact header for emf:123456');
      expect(result['emf:999888']).to.equals('Compact header for emf:999888');
    });
  });

  it('should set error message', () => {
    chartViewWidget.setError('Error message');
    expect(chartViewWidget.errorMessage).to.equals('Error message');
  });

  it('should unsubscribe from eventbus subscriptions on destroy', () => {
    chartViewWidget.chartDrawnSubscription = {
      unsubscribe: sinon.spy()
    };
    chartViewWidget.control = {
      getBaseWidget: () => {
        return {
          ngOnDestroy: sinon.stub()
        };
      }
    };
    chartViewWidget.ngOnDestroy();
    expect(chartViewWidget.chartDrawnSubscription.unsubscribe.calledOnce).to.be.true;
  });

  it('should show empty widget in modeling view', () => {
    let clearChartStub = sinon.stub(chartViewWidget, 'clearChart');
    chartViewWidget.context.isModeling = () => true;
    chartViewWidget.config = {
      selectObjectMode: SELECT_OBJECT_AUTOMATICALLY,
      groupBy: 'Type'
    };
    chartViewWidget.$scope.$digest();
    expect(clearChartStub.calledOnce).to.be.true;
    clearChartStub.restore();
  });

  it('should fire widget ready event if no property is selected for grouping', () => {
    let clearChartStub = sinon.stub(chartViewWidget, 'clearChart');
    let publishWidgetReadyEventStub = sinon.stub(chartViewWidget, 'publishWidgetReadyEvent');

    chartViewWidget.config = {
      selectObjectMode: SELECT_OBJECT_AUTOMATICALLY
    };
    chartViewWidget.$scope.$digest();
    expect(clearChartStub.calledOnce).to.be.true;
    expect(publishWidgetReadyEventStub.calledOnce).to.be.true;
    publishWidgetReadyEventStub.restore();
    clearChartStub.restore();
  });

  describe('loadAggregatedResult', () => {
    it('should load aggregated result when widget is not for version', (done) => {
      chartViewWidget.config = {
        groupBy: 'emf:hasParent'
      };
      let aggregatedResult = 'emf:e6a8b8b9-a610-4720-908c-c339bc33b475\':3';
      objectSelectorHelper.groupSelectedObjects = () => PromiseStub.resolve({
        aggregated: {
          'emf:hasParent': aggregatedResult
        }
      });

      chartViewWidget.loadAggregatedResult();

      chartViewWidget.loadAggregatedResult().then((result) => {
        expect(result).to.be.equal(aggregatedResult);
        done();
      });
    });

    describe('loadAggregatedResult for version', () => {
      it('should return AggregatedResult from version data', () => {
        let groupBy = 'emf:hasParent';
        let aggregatedResult = 'emf:e6a8b8b9-a610-4720-908c-c339bc33b475\':3';
        chartViewWidget.config = {
          groupBy,
          versionData: {
            aggregatedData: {
              'emf:hasParent': aggregatedResult
            }
          }
        };

        let result = chartViewWidget.loadAggregatedResult();
        expect(result).to.be.equal(aggregatedResult);
      });

      it('should reject when version data is without results', () => {
        versionAggregationDataForRejection.forEach((data) => {
          chartViewWidget.config = data;
          chartViewWidget.loadAggregatedResult().catch(error => expect(error).to.be.equal('select.object.results.none'));
        });
      });

      let versionAggregationDataForRejection = [
        {
          groupBy: 'emf:hasParent',
          versionData: {}
        }, {
          groupBy: 'emf:hasParent',
          versionData: {
            aggregatedData: {groupBy: null}
          }
        }, {
          groupBy: 'emf:hasParent',
          versionData: {
            aggregatedData: {groupBy: undefined}
          }
        }, {
          groupBy: 'emf:hasParent',
          versionData: {
            aggregatedData: {groupBy: {}}
          }
        }
      ];
    });
  });

  it('should publish \'WidgetReadyEvent\' with properly data', () => {
    chartViewWidget.publishWidgetReadyEvent();

    expect(eventbus.publish.calledOnce).to.be.true;
    expect(eventbus.publish.args[0][0].getData()[0].widgetId).to.equal(CONTROL_ID);
  });

  it('should error message be set', () => {
    let errorMessage = 'Error message';
    chartViewWidget.setError(errorMessage);

    expect(chartViewWidget.errorMessage).to.equal(errorMessage);
  });

  describe('widgetShouldBeEmpty', () => {
    it('should widget not be empty when widget is for version', () => {
      chartViewWidget.config = {
        versionData: {},
        selectObjectMode: SELECT_OBJECT_AUTOMATICALLY
      };
      chartViewWidget.context.isModeling = () => true;

      expect(chartViewWidget.widgetShouldBeEmpty()).to.be.false;
    });

    it('should widget not be empty when context is modeling and select object mode is not automatically', () => {
      chartViewWidget.config = {
        versionData: {},
        selectObjectMode: SELECT_OBJECT_MANUALLY
      };
      chartViewWidget.context.isModeling = () => true;

      expect(chartViewWidget.widgetShouldBeEmpty()).to.be.false;
    });

    it('should widget not be empty when context is not modeling and select object mode is automatically', () => {
      chartViewWidget.config = {
        versionData: {},
        selectObjectMode: SELECT_OBJECT_AUTOMATICALLY
      };

      expect(chartViewWidget.widgetShouldBeEmpty()).to.be.false;
    });

    it('should widget be empty when context is modeling and select object mode is automatically', () => {
      chartViewWidget.config = {
        selectObjectMode: SELECT_OBJECT_AUTOMATICALLY
      };
      chartViewWidget.context.isModeling = () => true;

      expect(chartViewWidget.widgetShouldBeEmpty()).to.be.true;
    });
  });

  describe('isWidgetForVersion', () => {
    it('should be widget for version when versionData is not present', () => {
      chartViewWidget.config = {
        versionData: {}
      };
      expect(chartViewWidget.isWidgetForVersion() === undefined).to.be.false;
    });

    it('should not be widget for version when versionData is not present', () => {
      chartViewWidget.config = {};
      expect(chartViewWidget.isWidgetForVersion() === undefined).to.be.true;
    });
  });
});
