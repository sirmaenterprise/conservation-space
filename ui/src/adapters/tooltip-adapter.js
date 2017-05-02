import {Injectable} from 'app/app';

export const SHOW = 'show';
export const HIDE = 'hide';
export const DESTROY = 'destroy';

const DATA_ORIGINAL_TITLE = 'data-original-title';

// Bootstrap tooltip adds tooltip id as aria-describedby attribute to its target element
const TOOLTIP_ID_ATTRIBUTE = 'aria-describedby';

/**
 * Adapter for tooltip.
 */
@Injectable()
export class TooltipAdapter {

  tooltip(element, config, show) {
    let $element = $(element);
    $element.tooltip(config).attr(DATA_ORIGINAL_TITLE, config.title);
    if (show) {
      return this.show(element);
    }
  }

  show(element) {
    $(element).tooltip(SHOW);
    return $(element).attr(TOOLTIP_ID_ATTRIBUTE);
  }

  hide(element) {
    $(element).tooltip(HIDE);
    return $(element).attr(TOOLTIP_ID_ATTRIBUTE);
  }

  destroy(element) {
    $(element).tooltip(DESTROY);
    return $(element).attr(TOOLTIP_ID_ATTRIBUTE);
  }

  clearTooltips(tooltipIds) {
    tooltipIds.forEach((tooltipId) => {
      $(`#${tooltipId}`).remove();
    });
  }
}
