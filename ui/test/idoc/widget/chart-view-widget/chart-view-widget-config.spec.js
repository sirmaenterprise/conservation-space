import {ChartViewWidgetConfig} from 'idoc/widget/chart-view-widget/chart-view-widget-config';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {PromiseStub} from 'test/promise-stub';

describe('ChartViewWidgetConfig', () => {
  let chartViewWidgetConfig;
  before(() => {
    PluginRegistry.add('chart-view-charts', {});
    let objectSelectorHelper = {
      getSelectedItems: () => {
      }
    };
    let propertiesSelectorHelper = {
      getDefinitionsArray: () => {
        return PromiseStub.resolve();
      }
    };
    chartViewWidgetConfig = new ChartViewWidgetConfig(objectSelectorHelper, propertiesSelectorHelper, IdocMocks.mockTranslateService());
  });

  it('createGroupBySelectorModel should return common properties eligible for grouping', () => {
    let definitions = [{
      fields: [{
        codelist: 210,
        uri: 'emf:title',
        label: 'Title'
      }, {
        codelist: 200,
        uri: 'emf:status',
        label: 'Status'
      }, {
        codelist: 190,
        uri: 'emf:type',
        label: 'Type'
      }, {
        uri: 'emf:description',
        label: 'Description'
      }]
    }, {
      fields: [{
        codelist: 210,
        uri: 'emf:title',
        label: 'Title'
      }, {
        codelist: 200,
        uri: 'emf:status',
        label: 'State'
      }]
    }];
    chartViewWidgetConfig.propertiesSelectorHelper.getDefinitionsArray = () => {
      return PromiseStub.resolve(definitions);
    };
    chartViewWidgetConfig.propertiesSelectorHelper.flattenDefinitionProperties = (definition) => {
      return definition.fields;
    };
    chartViewWidgetConfig.createGroupBySelectorModel();
    expect(chartViewWidgetConfig.groupBySelectorConfig.data).to.eql([{'id':'emf:title','text':'Title'},{'id':'emf:status','text':'Status, State'}]);
  });
});
