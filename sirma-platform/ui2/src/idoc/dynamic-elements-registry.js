import _ from 'lodash';
import {Injectable, Inject, NgTimeout} from 'app/app';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {ImageReadyEvent} from 'idoc/events/image-ready-event';
import {EditorReadyEvent} from 'idoc/editor/editor-ready-event';
import {IdocReadyEvent} from 'idoc/idoc-ready-event';
import {Eventbus} from 'services/eventbus/eventbus';
import {PluginsService} from 'services/plugin/plugins-service';

/*
  Added timeout for image loading to avoid print/export timeout if some of the images in the document are not accessible.
  If image is not loaded for the given time ImageReadyEvent is fired which allow the system to proceed with print/export.
  Considering that (if everything works properly) all images must be locally stored given timeout should be more than enough.
 */
const IMAGE_LOADING_TIMEOUT = 30000;

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

  init() {
    this.readyCache = {};
    this.count = 0;
    this.registerEventHandlers();
  }

  reload() {
    this.destroy();
    this.init();
  }

  handleWidget(name, id) {
    if (name && _.includes(this.availableWidgets, name)) {
      this.addToCache(id);
    }
  }

  handleEditor(editorName) {
    this.addToCache(editorName);
  }

  handleImage($img) {
    //Skip attachment of listener if image is already loaded.
    if ($img[0].complete === true) {
      return;
    }

    let onLoadFunc = () => this.$timeout(() => {
      this.$timeout.cancel(loadingTimeout);
      this.eventbus.publish(new ImageReadyEvent($img));
    });

    // Either wait for load or error event or for a timeout (whichever occur first)
    let loadingTimeout = this.$timeout(() => {
      $img.off('load error', onLoadFunc);
      this.eventbus.publish(new ImageReadyEvent($img));
    }, IMAGE_LOADING_TIMEOUT);

    $img.on('load error', onLoadFunc);
    this.count++;
  }

  registerEventHandlers() {
    this.subscriptions = [
      this.eventbus.subscribe(ImageReadyEvent, this.handleDynamicElementReadyEvent.bind(this)),
      this.eventbus.subscribe(WidgetReadyEvent, this.handleWidgetReadyEvent.bind(this)),
      this.eventbus.subscribe(EditorReadyEvent, this.handleEditorReadyEvent.bind(this))
    ];
  }

  handleWidgetReadyEvent(event) {
    this.handleDynamicElementReadyEvent({id: event[0].widgetId});
  }

  handleEditorReadyEvent(event){
    this.handleDynamicElementReadyEvent({id: event.editorName })
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

  addToCache(element) {
    this.readyCache[element] = false;
    this.count++;
  }

  publishIdocReadyEvent() {
    // This event will be fired only from an iframe
    if (parent !== window) {
      parent.postMessage('idocReady', '*');
    }

    this.eventbus.publish(new IdocReadyEvent(this.readyCache));
  }

  destroy() {
    _.each(this.subscriptions, subscription => subscription.unsubscribe());
    $('img').off('load error');
  }
}
