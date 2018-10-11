import {View, Component, Inject, NgElement} from 'app/app';
import {TooltipAdapter} from 'adapters/tooltip-adapter';
import {ToTrustedHtml} from 'filters/to-trusted-html';
import {Eventbus} from 'services/eventbus/eventbus';
import {Chart} from 'components/charts/chart';
import {ResizeDetectorAdapter} from 'adapters/resize-detector-adapter';
import {scaleOrdinal as d3scaleOrdinal, scaleBand as d3scaleBand, scaleLinear as d3scaleLinear, max as d3max,
  axisBottom as d3axisBottom, axisLeft as d3axisLeft, schemeCategory20 as d3schemeCategory20} from 'd3';
import template from './bar-chart.html!text';

@Component({
  selector: 'seip-bar-chart',
  properties: {
    'config': 'config'
  }
})
@View({
  template: template
})
@Inject(NgElement, TooltipAdapter, Eventbus, ResizeDetectorAdapter)
export class BarChart extends Chart {
  constructor($element, tooltipAdapter, eventbus, resizeDetectorAdapter) {
    super($element, tooltipAdapter, eventbus, resizeDetectorAdapter);
    this.tooltipAdapter = tooltipAdapter;
  }

  init() {
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

    let svg = this.d3element.append('svg'),
      margin = {top: 20, right: 20, bottom: 20, left: 40},
      width = this.$element.width() - margin.left - margin.right,
      height = this.chartSize - margin.top - margin.bottom;

    svg.attr('class', 'bar-chart')
      .attr('width', '100%')
      .attr('height', this.chartSize);

    let g = svg.append('g')
      .attr('transform', `translate(${margin.left}, ${margin.top})`);

    let xScale = d3scaleBand().range([0, width]).padding(0.1)
      .domain(this.config.data.map((d) => {
        return d.name;
      }));

    let yScale = d3scaleLinear().range([height, 0])
      .domain([0, d3max(this.config.data, (d) => {
        return d.value;
      })]);

    g.selectAll('.bar')
      .data(this.config.data)
      .enter().append('rect')
      .style('fill', (d, index) => {
        return this.color(index);
      })
      .attr('class', 'bar')
      .attr('x', (d) => {
        return xScale(d.name);
      })
      .attr('width', xScale.bandwidth())
      .attr('y', (d) => {
        return yScale(d.value);
      })
      .attr('height', (d) => {
        return height - yScale(d.value);
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

    g.append('g')
      .attr('transform', `translate(0, ${height})`)
      .attr('class', 'x-axis')
      .call(d3axisBottom(xScale))
      .attr('font-size', null)
      .attr('font-family', null)
      .selectAll('text')
      .text((d, index) => {
        d = this.config.data[index];
        return `${Chart.getTextFromHtml(d.label, 30)} (${d.value})`;
      })
      .attr('y', 0)
      .attr('x', 5)
      .attr('dy', '.35em')
      .attr('transform', 'rotate(-90)')
      .style('text-anchor', 'start')
      .each(function (d, index) {
        super.addTooltip(this, _this.getTooltipText(_this.config.data[index]));
      })
      .on('mouseover', function () {
        _this.tooltipsRegistry.add(_this.tooltipAdapter.show(this));
      })
      .on('mouseout', function () {
        _this.tooltipsRegistry.delete(_this.tooltipAdapter.hide(this));
      });

    g.append('g')
      .attr('class', 'y-axis')
      .call(d3axisLeft(yScale))
      .attr('font-size', null)
      .attr('font-family', null);
  }

  getTooltipText(d) {
    return `${d.label} (${d.value}) ${(d.value / this.total * 100).toFixed(2)}%`;
  }

  onResize() {
    this.redraw();
  }
}
