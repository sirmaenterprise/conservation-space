import {View, Component, Inject, NgElement} from 'app/app';
import {TooltipAdapter} from 'adapters/tooltip-adapter';
import 'filters/to-trusted-html';
import {Eventbus} from 'services/eventbus/eventbus';
import {Chart} from 'components/charts/chart';
import {ResizeDetectorAdapter} from 'adapters/resize-detector-adapter';
import {pie as d3pie, scaleOrdinal as d3scaleOrdinal, schemeCategory20 as d3schemeCategory20, arc as d3arc} from 'd3';
import template from './pie-chart.html!text';
import './pie-chart.css!';

@Component({
  selector: 'seip-pie-chart',
  properties: {
    'config': 'config'
  }
})
@View({template})
@Inject(NgElement, TooltipAdapter, Eventbus, ResizeDetectorAdapter)
export class PieChart extends Chart {
  constructor($element, tooltipAdapter, eventbus, resizeDetectorAdapter) {
    super($element, tooltipAdapter, eventbus, resizeDetectorAdapter);
    this.tooltipAdapter = tooltipAdapter;
  }

  init() {
    this.pie = d3pie()
      .sort(null)
      .value((d) => {
        return d.value;
      });
    this.color = d3scaleOrdinal(d3schemeCategory20);
    this.total = Chart.getTotal(this.config.data);
    this.initialized = true;
  }

  draw() {
    if (!this.initialized) {
      this.init();
    }
    let _this = this;
    this.chartSize = this.getChartSize();

    let radius = this.chartSize / 2;

    let arc = d3arc()
      .outerRadius(radius - 90)
      .innerRadius(0);

    let labelArc = d3arc()
      .innerRadius(radius - 88)
      .outerRadius(radius);

    let halfChartSize = this.chartSize / 2;

    let svg = this.d3element.append('svg')
      .attr('class', 'pie-chart')
      .attr('width', this.chartSize)
      .attr('height', this.chartSize)
      .append('g')
      .attr('transform', `translate(${halfChartSize}, ${halfChartSize})`);

    let g = svg.selectAll('.arc')
      .data(this.pie(this.config.data))
      .enter().append('g')
      .attr('class', 'arc');

    g.append('path')
      .attr('d', arc)
      .style('fill', (d) => {
        return this.color(d.index);
      })
      .each(function (d) {
        super.addTooltip(this, _this.getTooltipText(d));
      })
      .on('mouseover', function () {
        _this.tooltipsRegistry.add(_this.tooltipAdapter.show(this));
      })
      .on('mouseout', function () {
        _this.tooltipsRegistry.delete(_this.tooltipAdapter.hide(this));
      });

    g.append('text')
      .attr('transform', (d) => {
        return `translate(${labelArc.centroid(d)}) rotate( ${PieChart.getAngle(d)})`;
      })
      .attr('dy', '.35em')
      .text((d) => {
        return `${Chart.getTextFromHtml(d.data.label)} (${d.data.value})`;
      })
      .each(function (d) {
        super.addTooltip(this, _this.getTooltipText(d));
      })
      .on('mouseover', function () {
        _this.tooltipsRegistry.add(_this.tooltipAdapter.show(this));
      })
      .on('mouseout', function () {
        _this.tooltipsRegistry.delete(_this.tooltipAdapter.hide(this));
      });
  }

  static getAngle(d) {
    let angle = (180 / Math.PI * (d.startAngle + d.endAngle) / 2 - 90);
    if(angle > 90) {
      angle -= 180;
    }
    return angle;
  }

  getTooltipText(d) {
    return `${d.data.label} (${d.data.value}) ${(d.data.value / this.total * 100).toFixed(2)}%`;
  }

  onResize() {
    if (this.getChartSize() !== this.chartSize) {
      this.redraw();
    }
  }
}
