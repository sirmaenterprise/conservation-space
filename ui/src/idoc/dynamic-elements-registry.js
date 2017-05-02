import _ from 'lodash';
import {Injectable, Inject, NgTimeout} from 'app/app';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {ImageReadyEvent} from 'idoc/events/image-ready-event';
import {EditorReadyEvent} from 'idoc/editor/editor-ready-event';
import {IdocReadyEvent} from 'idoc/idoc-ready-event';
import {Eventbus} from 'services/eventbus/eventbus';
import {PluginsService} from 'services/plugin/plugins-service';

/**
 * Listens for "ready" event from dynamic elements (widgets, images, etc) and decreases the provided count.
 * Once the count reaches 0, the IdocReadyEvent is published.
 */
@Injectable()
@Inject(NgTimeout, Eventbus, PluginsService)
export class DynamicElementsRegistry {

  constructor($timeout, eventbus, pluginService) {
    this.$timeout = $timeout;
    this.eventbus = eventbus;
    this.availableWidgets = pluginService.getDefinitions('idoc-widget').map(module => module.name);
  }

  init(dom, tabs) {
    this.readyCache = {};
    this.countElements(dom, tabs);
    this.registerEventHandlers();
  }

  reload(dom, tabs) {
    this.destroy();
    this.init(dom, tabs);
  }

  countElements(dom, tabs) {
    let context;
    if (tabs) {
      let selector = tabs.map((tabId) => {
        return `[data-id="${tabId}"]`;
      }).join(', ');
      context = dom.find(selector);
    } else {
      context = dom.children('section');
    }

    this.count = context.length;

    context.find('.widget').each((i, el) => {
        this.handleWidget($(el));
    });
  }

  handleWidget($widget) {
    let name = $widget.attr('widget');
    if (name && _.includes(this.availableWidgets, name)) {
      this.count++;
    }
  }

  handleImage($img) {
    //Skip attachment of listener if image is already loaded.
    if ($img[0].complete === true) {
      return;
    }
    $img.on('load error', () => this.$timeout(() => this.eventbus.publish(new ImageReadyEvent($img))));
    this.count++;
  }

  registerEventHandlers() {
    let handler = this.handleDynamicElementReadyEvent.bind(this);
    this.subscriptions = [
      this.eventbus.subscribe(ImageReadyEvent, handler),
      this.eventbus.subscribe(WidgetReadyEvent, this.handleWidgetReadyEvent.bind(this)),
      this.eventbus.subscribe(EditorReadyEvent, handler)
    ];
  }

  handleWidgetReadyEvent(event) {
    this.handleDynamicElementReadyEvent({id: event[0].widgetId});
  }

  handleDynamicElementReadyEvent(event) {
    let id = event.id;
    if (id) {
      if (this.readyCache[id]) {
        return;
      } else {
        this.readyCache[id] = true;
      }
    }

    if (!--this.count) {
      this.publishIdocReadyEvent();
    }
  }

  publishIdocReadyEvent() {
    // This event will be fired only from an iframe
    if (parent !== window) {
      parent.postMessage('idocReady', '*');
    }

    this.eventbus.publish(new IdocReadyEvent());
  }

  destroy() {
    _.each(this.subscriptions, subscription => subscription.unsubscribe());
    $('img').off('load error');
  }
}
