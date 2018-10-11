import {Injectable} from 'app/app';
import _ from 'lodash';

export const SHOW = 'show';
export const HIDE = 'hide';
export const DESTROY = 'destroy';

export const TOOLTIP_PLACEMENT_MOUSE = 'mouse';

const DATA_ORIGINAL_TITLE = 'data-original-title';
const DATA_TOOLTIP_PLACEMENT = 'data-tooltip-placement';

// Bootstrap tooltip adds tooltip id as aria-describedby attribute to its target element
const TOOLTIP_ID_ATTRIBUTE = 'aria-describedby';

// Approximate pointer size
const POINTER_SIZE = 10;
// Tooltip offset (in any direction) from the pointer
const TOOLTIP_POINTER_OFFSET = 2;

/**
 * Adapter for tooltip.
 * Configuration for the tooltip can be seen in bootstrap's documentation.
 * Additional placement configuration is added - mouse, which allows tooltip to be attached and moved with the mouse pointer.
 */
@Injectable()
export class TooltipAdapter {

  tooltip(element, config, show) {
    let $element = $(element);
    $element.tooltip(config).attr({
      [DATA_ORIGINAL_TITLE]: config.title,
      [DATA_TOOLTIP_PLACEMENT]: config.placement
    });
    if (show) {
      return this.show(element);
    }
  }

  show(element) {
    let $element = $(element);
    $element.tooltip(SHOW);

    if ($element.attr(DATA_TOOLTIP_PLACEMENT) === TOOLTIP_PLACEMENT_MOUSE) {
      this.attachTooltipToMouse($element);
    }

    return $element.attr(TOOLTIP_ID_ATTRIBUTE);
  }

  hide(element) {
    let $element = $(element);
    $element.tooltip(HIDE);
    $element.off('mousemove', TooltipAdapter.positionTooltipOnMouseMove);
    return $element.attr(TOOLTIP_ID_ATTRIBUTE);
  }

  /**
   * Change tooltip title for given element
   * @param element
   * @param newTitle
   */
  changeTitle(element, newTitle) {
    let $element = $(element);
    $element.attr('title', newTitle).tooltip('fixTitle');
  }

  destroy(element) {
    let $element = $(element);
    $element.tooltip(DESTROY);
    $element.off('mousemove', TooltipAdapter.positionTooltipOnMouseMove);
    return $element.attr(TOOLTIP_ID_ATTRIBUTE);
  }

  clearTooltips(tooltipIds) {
    tooltipIds.forEach((tooltipId) => {
      $(`#${tooltipId}`).remove();
    });
  }

  /**
   * Attach tooltip element to mouse and move it relatively to the mouse pointer.
   * Tooltip element is positioned on the top right side of the pointer.
   * If tooltip is about to leave the page on the top and/or right its position is switched to the opposite direction.
   * @param $element to which we add the tooltip
   */
  attachTooltipToMouse($element) {
    let tooltipId = $element.attr(TOOLTIP_ID_ATTRIBUTE);
    let tooltipElement = $(`#${tooltipId}`);

    // Remove pointer events because if mouse is moved quickly it can hover the tooltip and to remove it because mouseout event will be fired on the tooltip target
    tooltipElement.css('pointer-events', 'none');
    tooltipElement.find('.tooltip-arrow').css('display', 'none');

    $element.on('mousemove', undefined, {tooltipElement}, TooltipAdapter.positionTooltipOnMouseMove);
  }

  /**
   * Position tooltip element relatively to mouse on mouse move event.
   * @param evt mousemove event. One additional field must be send with event data:
   *  - tooltipElement which should be repositioned
   */
  static positionTooltipOnMouseMove(evt) {
    let tooltipElement = evt.data.tooltipElement;
    let windowWidth = TooltipAdapter.getWindowWidth();

    let tooltipWidth = tooltipElement.width();
    let tooltipHeight = tooltipElement.height();

    let positionX = evt.pageX + POINTER_SIZE + TOOLTIP_POINTER_OFFSET;
    if (positionX + tooltipWidth > windowWidth) {
      positionX = evt.pageX - tooltipWidth - TOOLTIP_POINTER_OFFSET;
    }

    let positionY = evt.pageY - tooltipHeight - TOOLTIP_POINTER_OFFSET;
    if (positionY < 0) {
      positionY = evt.pageY + TOOLTIP_POINTER_OFFSET;
    }

    window.requestAnimationFrame(() => {
      tooltipElement.css({
        'left': positionX,
        'top': positionY
      });
    });
  }

  static getWindowWidth() {
    return window.innerWidth;
  }
}
