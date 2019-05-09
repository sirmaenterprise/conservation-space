import {Component, Inject, NgElement} from 'app/app';
import {TooltipAdapter} from 'adapters/tooltip-adapter';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ContextualEscapeAdapter} from 'adapters/angular/contextual-escape-adapter';
import {InstanceRestService} from 'services/rest/instance-service';
import {UrlUtils} from 'common/url-utils';
import _ from 'lodash';
import template from 'instance-header/instance-header-tooltip/instance-header-tooltip.html!text';
import 'instance-header/instance-header-tooltip/instance-header-tooltip.css!';

export const TOOLTIP_DELAY = 2000;
const MAX_DATA_PROPERTY_VALUE_LENGTH = 150;
const HREF = 'href';

@Component({
  selector: '.instance-link'
})
@Inject(NgElement, TooltipAdapter, PromiseAdapter, ContextualEscapeAdapter, InstanceRestService)
export class InstanceHeaderTooltip {

  constructor($element, tooltipAdapter, promiseAdapter, contextualEscapeAdapter, instanceRestService) {
    this.instanceRestService = instanceRestService;
    this.tooltipAdapter = tooltipAdapter;
    this.promiseAdapter = promiseAdapter;
    this.contextualEscapeAdapter = contextualEscapeAdapter;

    this.tooltipsRegistry = new Set();

    var tooltipLoader = _.debounce((event) => {
      this.loadTooltip($(event.currentTarget));
    }, TOOLTIP_DELAY);
    $element.hover(tooltipLoader, (event) => this.cancelTooltip($(event.currentTarget), tooltipLoader));
    $element.click((event) => this.cancelTooltip($(event.currentTarget), tooltipLoader));
  }

  cancelTooltip(target, tooltipLoader) {
    if (this.tooltipSkipRequest && this.tooltipSkipRequest.resolve) {
      this.tooltipSkipRequest.resolve();
    }
    tooltipLoader.cancel();
    this.tooltipsRegistry.delete(this.tooltipAdapter.destroy(target));
  }

  loadTooltip(target) {
    var id = UrlUtils.getIdFromUrl(target.attr(HREF));
    if (id === null) {
      return;
    }
    this.tooltipSkipRequest = this.promiseAdapter.defer();
    var settings = {
      skipInterceptor: true,
      timeout: this.tooltipSkipRequest.promise
    };
    this.instanceRestService.getTooltip(id, settings).then((response)=> {
      let htmlHeader = $('<div></div>').append(response.data);
      htmlHeader.find('[data-property]').each((i, element) => {
        let property = $(element);
        let propertyText = property.text();
        if (propertyText && propertyText.length > MAX_DATA_PROPERTY_VALUE_LENGTH) {
          property.text(`${propertyText.substr(0, MAX_DATA_PROPERTY_VALUE_LENGTH)}...`);
        }
      });

      var header = this.contextualEscapeAdapter.trustAsHtml(htmlHeader.html());
      this.tooltipsRegistry.add(this.tooltipAdapter.tooltip(target, this.getTooltipSettings(header), true));
    });
  }

  getTooltipSettings(title) {
    return {
      title,
      html: true,
      template,
      placement: 'auto top',
      container: 'body',
      // TODO: research for tooltip positioning based on viewport
      // default viewport does not calculate
      // tooltip position properly
      viewport: '#topHeader'
    };
  }

  ngOnDestroy() {
    this.tooltipAdapter.clearTooltips([...this.tooltipsRegistry]);
    this.tooltipsRegistry.clear();
  }
}
