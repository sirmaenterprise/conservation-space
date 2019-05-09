import {View, Component, Inject, NgElement} from 'app/app';
import {TooltipAdapter} from 'adapters/tooltip-adapter';
import 'filters/to-trusted-html';
import {Eventbus} from 'services/eventbus/eventbus';
import {Chart} from 'components/charts/chart';
import {ResizeDetectorAdapter} from 'adapters/resize-detector-adapter';
import {line as d3line, scalePoint as d3scalePoint, scaleLinear as d3scaleLinear,  max as d3max, mouse as d3mouse,
  axisBottom as d3axisBottom, axisLeft as d3axisLeft} from 'd3';
import template from './line-chart.html!text';
import './line-chart.css!';

@Component({
  selector: 'seip-line-chart',
  properties: {
    'config': 'config'
  }
})
@View({template})
@Inject(NgElement, TooltipAdapter, Eventbus, ResizeDetectorAdapter)
export class LineChart extends Chart {
  constructor($element, tooltipAdapter, eventbus, resizeDetectorAdapter) {
    super($element, tooltipAdapter, eventbus, resizeDetectorAdapter);
    this.tooltipAdapter = tooltipAdapter;
  }

  init() {
    this.total = Chart.getTotal(this.config.data);
    this.initialized = true;
  }

  draw() {
    if (!this.initialized) {
      this.init();
    }
    this.chartSize = this.getChartSize();

    this.svg = this.d3element.append('svg');
    this.margin = {top: 20, right: 20, bottom: 128, left: 128};
    this.width = this.$element.width() - this.margin.left - this.margin.right;
    this.height = this.chartSize - this.margin.top - this.margin.bottom;

    this.svg.attr('class', 'line-chart')
      .attr('width', '100%')
      .attr('height', this.chartSize);

    this.xScale = d3scalePoint()
      .range([0, this.width])
      .domain(this.config.data.map((d) => {
        return d.name;
      }));

    this.yScale = d3scaleLinear().range([this.height, 0])
      .domain([0, d3max(this.config.data, (d) => {
        return d.value;
      })]);

    let line = d3line()
      .x((d) => {
        return this.xScale(d.name);
      })
      .y((d) => {
        return this.yScale(d.value);
      });

    this.chartGroupElement = this.svg.append('g')
      .attr('transform', `translate(${this.margin.left}, ${this.margin.top})`);

    this.chartGroupElement.append('path')
      .datum(this.config.data)
      .attr('class', 'line')
      .attr('d', line);

    this.chartGroupElement.append('g')
      .attr('class', 'x-axis')
      .attr('transform', `translate(0, ${this.height})`)
      .call(d3axisBottom(this.xScale))
      .attr('font-size', null)
      .attr('font-family', null)
      .selectAll('text')
      .text((d, index) => {
        let data = this.config.data[index];
        return `${Chart.getTextFromHtml(data.label, 20)} (${data.value})`;
      })
      .attr('dx', '-.8em')
      .attr('dy', '.15em')
      .attr('transform', 'rotate(-45)')
      .style('text-anchor', 'end');

    this.chartGroupElement.append('g')
      .attr('class', 'y-axis')
      .call(d3axisLeft(this.yScale))
      .attr('font-size', null)
      .attr('font-family', null);

    this.addOverlay();
    this.addFocusElement();
  }

  /**
   * Adds overlay rectangle to capture mouse move events
   */
  addOverlay() {
    this.overlayElement = this.svg.append('rect')
      .attr('transform', `translate(${this.margin.left}, ${this.margin.top})`)
      .attr('class', 'overlay')
      .attr('width', this.width)
      .attr('height', this.height)
      .on('mouseover', () => {
        this.focus.style('display', null);
      })
      .on('mouseout', () => {
        this.focus.style('display', 'none');
        this.tooltipsRegistry.delete(this.tooltipAdapter.hide(this.focus.node()));
        delete this.currentlyHoveredSegmentIndex;
      })
      .on('mousemove', () => {
        this.onMouseMove();
      });
  }

  /**
   * Adds focus element to indicate which is the value hovered by the mouse
   */
  addFocusElement() {
    this.focus = this.chartGroupElement.append('g')
      .attr('class', 'focus')
      .style('display', 'none');
    this.focus.append('circle').attr('r', 4);
  }

  getTooltipText(d) {
    return `${d.label} (${d.value}) ${(d.value / this.total * 100).toFixed(2)}%`;
  }

  /**
   * Handles mouse move over overlay element and translates focus element and tooltip depending on the currently hovered segment from the chart
   */
  onMouseMove() {
    let xPos = d3mouse(this.overlayElement.node())[0];
    let stepWidth = this.xScale.step();
    let hoveredSegmentIndex = parseInt((xPos + stepWidth / 2) / stepWidth);

    if (this.currentlyHoveredSegmentIndex !== hoveredSegmentIndex) {
      let d = this.config.data[hoveredSegmentIndex];
      this.focus.attr('transform', `translate(${this.xScale(d.name)}, ${this.yScale(d.value)})`);
      this.tooltipsRegistry.add(super.addTooltip(this.overlayElement.node(), this.getTooltipText(d), true));
      $('.tooltip').css('pointer-events', 'none');
      this.currentlyHoveredSegmentIndex = hoveredSegmentIndex;
    }
  }

  onResize() {
    this.redraw();
  }
}
