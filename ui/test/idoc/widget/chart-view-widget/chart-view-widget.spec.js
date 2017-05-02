import {SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';
import {ChartViewWidget} from 'idoc/widget/chart-view-widget/chart-view-widget';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';
import {IdocMocks} from 'test/idoc/idoc-mocks';

describe('ChartViewWidget', () => {
  let chartViewWidget;
  before(() => {
    ChartViewWidget.prototype.control = {
      getEditor: () => {
        return {
          $element: $('<div></div>')
        };
      },
      getId: () => 'controlId'
    };

    ChartViewWidget.prototype.context = {
      isPrintMode: () => false
    };

    chartViewWidget = new ChartViewWidget($('<span></span>'), mock$scope(), undefined, IdocMocks.mockEventBus());
  });

  it('should generate labels map for object properties', () => {
    chartViewWidget.instanceRestService = {
      loadBatch: (ids) => {
        let data = {
          data: ids.map((id) => {
            return {
              id: id,
              headers: {
                compact_header: `Compact header for ${id}`
              }
            }
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
    chartViewWidget.ngOnDestroy();
    expect(chartViewWidget.chartDrawnSubscription.unsubscribe.callCount).to.equals(1);
  });

  it('should show empty widget in modeling view', () => {
    chartViewWidget.clearChart = sinon.spy();
    chartViewWidget.context.isModeling =  () => true;
    chartViewWidget.config = {
      selectObjectMode: SELECT_OBJECT_AUTOMATICALLY,
      groupBy: 'Type'
    };
    chartViewWidget.$scope.$digest();
    expect(chartViewWidget.clearChart.calledOnce).to.be.true;
  });
});
