import {View, Inject, NgElement, NgScope, NgCompile} from 'app/app';
import {Widget} from 'idoc/widget/widget';
import {Eventbus} from 'services/eventbus/eventbus';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {PluginsService} from 'services/plugin/plugins-service';
import {CodelistRestService} from 'services/rest/codelist-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {PropertiesSelectorHelper} from 'idoc/widget/properties-selector/properties-selector-helper';
import {SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';
import {TranslateService} from 'services/i18n/translate-service';
import {HEADER_COMPACT} from 'instance-header/header-constants';
import _ from 'lodash';

import template from './chart-view-widget.html!text';
import './chart-view-widget.css!';

// Max chart size depends on the screen resolution. Optimal value is to fit the chart on one screen.
// Size shouldn't exceed editor's height.
// In some cases editor's height is too low because of opened browser console or resized window so we assign some min size in order for charts to be readable.
const MIN_MAX_SIZE = 400;

@Widget
@View({
  template: template
})
@Inject(NgElement, NgScope, NgCompile, Eventbus, PluginsService, CodelistRestService, InstanceRestService, PromiseAdapter, ObjectSelectorHelper, PropertiesSelectorHelper, TranslateService)
export class ChartViewWidget {
  constructor($element, $scope, $compile, eventbus, pluginsService, codelistRestService, instanceRestService, promiseAdapter, objectSelectorHelper, propertiesSelectorHelper, translateService) {
    this.$element = $element;
    this.$scope = $scope;
    this.$compile = $compile;

    this.eventbus = eventbus;
    this.pluginsService = pluginsService;
    this.codelistRestService = codelistRestService;
    this.instanceRestService = instanceRestService;
    this.promiseAdapter = promiseAdapter;
    this.objectSelectorHelper = objectSelectorHelper;
    this.propertiesSelectorHelper = propertiesSelectorHelper;
    this.translateService = translateService;

    this.chartBodyElement = this.$element.find('.chart-view').eq(0);

    $scope.$watch(() => {
      return this.config;
    }, () => {
      delete this.errorMessage;
      if (!this.config.groupBy) {
        this.errorMessage = this.translateService.translateInstant('search.select.group.by.placeholder');
        this.clearChart();
        this.publishWidgetReadyEvent();
      } else {
        if (this.widgetShouldBeEmpty()) {
          this.clearChart();
          this.publishWidgetReadyEvent();
          return;
        }
        this.loadChartData().then(() => {
          this.drawChart();
        }).catch((error) => {
          this.clearChart();
          this.setError(error.reason);
          this.publishWidgetReadyEvent();
        });
      }
    }, true);

    // editor might be missing in some sandboxes
    let editor = this.control.getEditor();
    let maxSize;
    if (editor) {
      /* window height - editor top offset - widget header */
      maxSize = $(window).height() - editor.$element.offset().top - 34;
      if (maxSize < MIN_MAX_SIZE) {
        maxSize = MIN_MAX_SIZE;
      }
    }

    this.chartConfig = {
      maxSize: maxSize,
      channelId: this.control.getId(),
      isPrintMode: this.context.isPrintMode()
    };

    this.chartDrawnSubscription = this.eventbus.subscribe({
      channel: this.control.getId(),
      topic: 'chart:drawn',
      callback: () => {
        this.publishWidgetReadyEvent();
      }
    });
  }

  drawChart() {
    this.pluginsService.loadComponentModules('chart-view-charts', 'name').then((charts) => {
      this.clearChart();
      let chartDefinition = charts[this.config.chartType];
      let chartHtml = `<${chartDefinition.component} config="chartViewWidget.chartConfig"></${chartDefinition.component}>`;
      this.innerScope = this.$scope.$new();
      this.chartBodyElement.append(this.$compile(chartHtml)(this.innerScope));
    });
  }

  loadAggregatedResult() {
    if (this.isWidgetForVersion()) {
      let versionAggregateData = this.config.versionData.aggregatedData;
      if (!_.isEmpty(versionAggregateData)) {
        let groupByAggregateData = versionAggregateData[this.config.groupBy];
        if (!_.isEmpty(groupByAggregateData)) {
          return groupByAggregateData;
        }
      }
      //if we have not aggregated data or aggregated data is empty
      return this.promiseAdapter.reject('select.object.results.none');
    }
    return this.objectSelectorHelper.groupSelectedObjects(this.config, this.context, this.config.groupBy).then((result) =>{
      return result.aggregated[this.config.groupBy];
    });
  }

  loadChartData() {
    return this.promiseAdapter.all([this.loadAggregatedResult(), this.getGroupByCodelists()]).then(([aggregatedResult, groupByCodelists]) => {
      let labelsLoader;
      if (groupByCodelists) {
        labelsLoader = this.getLabelsForCodelist(groupByCodelists);
      } else {
        labelsLoader = this.getLabelsForObjectProperties(Object.keys(aggregatedResult));
      }
      return labelsLoader.then((labelsMap) => {
        this.chartConfig.data = Object.keys(aggregatedResult).map((name) => {
          return {
            name: name,
            label: labelsMap[name] || name,
            value: aggregatedResult[name]
          };
        });
      });
    });
  }

  getGroupByCodelists() {
    let groupByLabels = new Set();
    let codelists = new Set();
    return this.propertiesSelectorHelper.getDefinitionsArray(this.config, {}, this.context).then((definitions) => {
      definitions.forEach((definition) => {
        let properties = this.propertiesSelectorHelper.flattenDefinitionProperties(definition);
        let property = _.find(properties, (prop) => {
          return prop.uri === this.config.groupBy;
        });
        if (property) {
          groupByLabels.add(property.label);
          if (property.codelist) {
            codelists.add(property.codelist);
          }
        }
      });
      this.chartConfig.title = `${this.translateService.translateInstant('search.select.group.by')}: ${[...groupByLabels].join(', ')}`;
      if (codelists.size !== 0) {
        return [...codelists];
      }
    });
  }

  getLabelsForCodelist(codelists) {
    let labelsMap = {};
    let codelistLoaders = codelists.map((codelistNumber) => {
      return this.codelistRestService.getCodelist({codelistNumber: codelistNumber});
    });
    return this.promiseAdapter.all(codelistLoaders).then((results) => {
      results.forEach((result) => {
        result.data.forEach((codelistValue) => {
          let codelistValueLabels = labelsMap[codelistValue.value] || new Set();
          codelistValueLabels.add(codelistValue.label);
          labelsMap[codelistValue.value] = codelistValueLabels;
        });
      });
      Object.keys(labelsMap).forEach((codelistValue) => {
        labelsMap[codelistValue] = [...labelsMap[codelistValue]].join(', ');
      });
      return labelsMap;
    });
  }

  getLabelsForObjectProperties(groupByValues) {
    let labelsMap = {};
    return this.instanceRestService.loadBatch(groupByValues, {params: {properties: [HEADER_COMPACT]}}).then((response) => {
      response.data.forEach((instance) => {
        labelsMap[instance.id] = instance.headers[HEADER_COMPACT];
      });
      return labelsMap;
    });
  }

  clearChart() {
    if (this.innerScope) {
      this.innerScope.$destroy();
    }
    this.chartBodyElement.empty();
  }

  publishWidgetReadyEvent() {
    this.eventbus.publish(new WidgetReadyEvent({
      widgetId: this.control.getId()
    }));
  }

  setError(message) {
    this.errorMessage = message;
  }

  widgetShouldBeEmpty() {
    return !this.isWidgetForVersion() && this.context.isModeling() && this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY;
  }

  ngOnDestroy() {
    this.chartDrawnSubscription.unsubscribe();
  }

  isWidgetForVersion() {
    return this.config.versionData;
  }
}