import {select as d3select} from 'd3';
import {TOOLTIP_PLACEMENT_MOUSE} from 'adapters/tooltip-adapter';
import _ from 'lodash';
import './chart.css!';

/**
 * Class providing some common functionalities for all charts
 */
export class Chart {
  constructor($element, tooltipAdapter, eventbus, resizeDetectorAdapter) {
    if (typeof this.draw !== 'function') {
      throw new Error('Must implement draw method in chart.');
    }
    this.$element = $element;
    this.d3element = d3select(this.$element.find('.chart-body').get(0));
    this.tooltipAdapter = tooltipAdapter;
    this.tooltipsRegistry = new Set();
    this.eventbus = eventbus;
    this.resizeDetectorAdapter = resizeDetectorAdapter;
    this.addPrintListeners();
  }

  ngAfterViewInit() {
    this.init();
    if (!this.config.isPrintMode) {
      this.resizeListener = this.resizeDetectorAdapter.addResizeListener(this.$element.parent().get(0), () => {
        if (_.isFunction(this.onResize)) {
          this.onResize();
        }
      });
    }
    this.draw();
    this.eventbus.publish({
      channel: this.config.channelId,
      topic: 'chart:drawn'
    });
  }

  addPrintListeners() {
    this.mediaQueryListener = (mql) => {
      if (mql.matches) {
        this.redraw();
      }
    };
    this.mediaQueryList = window.matchMedia('print');
    this.mediaQueryList.addListener(this.mediaQueryListener);
  }

  addTooltip(element, title, show) {
    return this.tooltipAdapter.tooltip(element, {
      container: document.body,
      html: 'true',
      trigger: 'hover',
      title,
      placement: TOOLTIP_PLACEMENT_MOUSE
    }, show);
  }

  clearTooltips() {
    this.tooltipAdapter.clearTooltips([...this.tooltipsRegistry]);
    this.tooltipsRegistry.clear();
  }

  getChartSize() {
    let headerHeight = this.$element.find('.chart-header').height();
    let chartSize = this.$element.width();
    if (this.config.isPrintMode) {
      let maxPrintSize = this.getWindowHeight() - headerHeight;
      if (chartSize > maxPrintSize) {
        chartSize = maxPrintSize;
      }
    } else if (this.config.maxSize && chartSize > this.config.maxSize) {
      chartSize = this.config.maxSize;
    }
    if (chartSize < 200) {
      chartSize = 200;
    }
    return chartSize - headerHeight - 40;
  }

  getWindowHeight() {
    return $(window).height();
  }

  redraw() {
    this.clearTooltips();
    this.d3element.selectAll('*').remove();
    this.draw();
  }

  static getTextFromHtml(html, maxLength = 10) {
    let tmpDiv = $('<div></div>').append(html);
    let label = tmpDiv.text();
    tmpDiv.remove();
    if (label.length > maxLength) {
      label = label.substr(0, maxLength - 2) + '..';
    }
    return label;
  }

  static getTotal(data) {
    return data.reduce((sum, cur) => {
      return sum + cur.value;
    }, 0);
  }

  ngOnDestroy() {
    if (this.resizeListener) {
      this.resizeListener();
    }
    this.clearTooltips();
    this.mediaQueryList.removeListener(this.mediaQueryListener);
  }
}
