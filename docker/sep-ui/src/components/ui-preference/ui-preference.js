import {Component, Inject, NgElement} from 'app/app';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {MODE_PRINT} from 'idoc/idoc-constants';
import {ResizeDetectorAdapter} from 'adapters/resize-detector-adapter';
import _ from 'lodash';

const DEBOUNCE_INTERVAL = 100;

/**
 * UI preference component is used to position and resize elements based on other elements positions and sizes.
 * Source elements for left and/or top direction can be specified and current element will be positioned relatively to them.
 *
 * UI preference config object can contain the following keys:
 * sourceElements:
 * {
 *  'left': '',
 *  'top': '',
 * } - A list of selectors of elements next to which we want to position our target element.
 * fillAvailableHeight - boolean. If true current element fill all the available height up to 100%
 * fillAvailableWidth - boolean. If true current element fill all the available width up to 100%
 * copyElementWidth - selector of element which width should be inherited (copied)
 * copyParentWidth - same as copyElementWidth but selects the element only amongst current element's parents. Can be used if there is no way to provide specific selector.
 */
@Component({
  selector: '[seip-ui-preference]',
  properties: {
    uiPreferenceConfig: 'ui-preference-config'
  }
})
@Inject(NgElement, StateParamsAdapter, ResizeDetectorAdapter)
export class UiPreference {
  constructor($element, stateParamsAdapter, resizeDetectorAdapter) {
    this.$element = $element;
    this.resizeDetectorAdapter = resizeDetectorAdapter;
    if (stateParamsAdapter.getStateParam('mode') !== MODE_PRINT && this.validateConfig()) {
      this.addResizeListeners();
      this.onSourceElementReposition();
    }
  }

  validateConfig() {
    if (!_.isEmpty(this.uiPreferenceConfig.sourceElements)) {
      Object.keys(this.uiPreferenceConfig.sourceElements).forEach((direction) => {
        let sourceElement = $(this.uiPreferenceConfig.sourceElements[direction]);
        if (sourceElement.length !== 1) {
          throw new Error(`Element with selector ${this.uiPreferenceConfig.sourceElements[direction]} either doesn't exist or there are more than one element with the same selector. Please use more specific selector.`);
        }
      });
    }

    if (this.uiPreferenceConfig.copyElementWidth && this.uiPreferenceConfig.copyParentWidth) {
      throw new Error('copyElementWidth and copyParentWidth configurations are mutually exclusive.');
    }

    if ((this.uiPreferenceConfig.copyElementWidth || this.uiPreferenceConfig.copyParentWidth) && this.uiPreferenceConfig.fillAvailableWidth) {
      throw new Error('copyElementWidth or copyParentWidth and fillAvailableWidth configurations are mutually exclusive.');
    }

    return true;
  }

  /**
   * Convert selectors to elements and add resize listeners
   */
  addResizeListeners() {
    this.repositionWithDebounce = this.uiPreferenceConfig.noDebounce ? this.onSourceElementReposition : _.debounce(this.onSourceElementReposition, DEBOUNCE_INTERVAL);
    this.copyElementWidthWithDebounce = this.uiPreferenceConfig.noDebounce ? this.copyElementWidth : _.debounce(this.copyElementWidth, DEBOUNCE_INTERVAL);
    this.resizeListeners = [];
    this.sourceElements = {};
    if (this.uiPreferenceConfig.sourceElements) {
      this.resizeListeners.push(...Object.keys(this.uiPreferenceConfig.sourceElements).map((direction) => {
        let sourceElement = $(this.uiPreferenceConfig.sourceElements[direction]);
        this.sourceElements[direction] = sourceElement;
        return this.resizeDetectorAdapter.addResizeListener(sourceElement[0], this.repositionWithDebounce.bind(this));
      }));
    }

    if (this.uiPreferenceConfig.copyElementWidth) {
      let widthSourceElement = $(this.uiPreferenceConfig.copyElementWidth);
      this.resizeListeners.push(this.resizeDetectorAdapter.addResizeListener(widthSourceElement[0], this.copyElementWidthWithDebounce.bind(this, widthSourceElement)));
    }

    if (this.uiPreferenceConfig.copyParentWidth) {
      let widthSourceElement = this.$element.closest(this.uiPreferenceConfig.copyParentWidth);
      this.resizeListeners.push(this.resizeDetectorAdapter.addResizeListener(widthSourceElement[0], this.copyElementWidthWithDebounce.bind(this, widthSourceElement)));
    }
  }

  copyElementWidth(sourceElement) {
    this.$element.css({
      width: sourceElement.width()
    });
  }

  onSourceElementReposition() {
    this.updateOffsets();

    if (this.uiPreferenceConfig.fillAvailableWidth) {
      this.fillAvailableWidth();
    }

    if (this.uiPreferenceConfig.fillAvailableHeight) {
      this.fillAvailableHeight();
    }
  }

  fillAvailableWidth() {
    let elementOffset = this.$element.offset();
    let scrollOffset = this.$element.css('position') === 'fixed' ? $(window).scrollLeft() : 0;
    let takenWidth = elementOffset.left - scrollOffset;
    this.$element.css({
      width: 'calc(100% - ' + takenWidth + 'px)'
    });
  }

  fillAvailableHeight() {
    let elementOffset = this.$element.offset();
    let scrollOffset = this.$element.css('position') === 'fixed' ? $(window).scrollTop() : 0;
    let takenHeight = elementOffset.top - scrollOffset;
    this.$element.css({
      height: 'calc(100% - ' + takenHeight + 'px)'
    });
  }

  /**
   * Calculate and update current element's offsets based on source elements
   */
  updateOffsets() {
    let newOffset = {};
    Object.keys(this.sourceElements).forEach((direction) => {
      let sourceElement = this.sourceElements[direction];
      let sourceOffset = sourceElement.offset();

      // either width or height of the source element, depending on the offset direction
      let sourceElementDimension;
      let scrollOffset;
      if (direction === 'left') {
        sourceElementDimension = sourceElement.outerWidth();
        scrollOffset = window.pageXOffset;
      } else if (direction === 'top') {
        sourceElementDimension = sourceElement.outerHeight();
        scrollOffset = window.pageYOffset;
      }

      // fixed elements position doesn't depend on the page scroll
      if (this.$element.css('position') === 'fixed') {
        scrollOffset = 0;
      }

      newOffset[direction] = sourceOffset[direction] + sourceElementDimension - scrollOffset;
    });

    this.$element.offset(newOffset);
  }

  ngOnDestroy() {
    if (this.resizeListeners) {
      this.resizeListeners.forEach((deregisterFunc) => {
        deregisterFunc();
      });
    }
  }
}
