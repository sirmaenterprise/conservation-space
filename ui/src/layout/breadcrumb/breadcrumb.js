import {View, Component, Inject, Event, NgScope, NgTimeout, NgElement} from 'app/app';
import _ from 'lodash';
import {BreadcrumbEntryManager} from 'layout/breadcrumb/breadcrumb-entry-manager';
import {StaticInstanceHeader} from 'instance-header/static-instance-header/static-instance-header';
import {Eventbus} from 'services/eventbus/eventbus';
import {RouterStateChangeSuccessEvent} from 'common/router/router-state-change-success-event';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {ToTrustedHtml} from 'filters/to-trusted-html';
import {IdocContextFactory} from 'services/idoc/idoc-context-factory';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {HEADER_BREADCRUMB} from 'instance-header/header-constants';
import 'font-awesome/css/font-awesome.css!';
import template from './breadcrumb.html!text';
import './breadcrumb.css!';

const statesConfig = {
  reset: ['search', 'libraries', 'userDashboard']
};

@Component({
  selector: 'seip-breadcrumb'
})
@View({
  template: template
})
@Inject(NgScope, NgTimeout, Eventbus, IdocContextFactory, BreadcrumbEntryManager, StateParamsAdapter, WindowAdapter, '$state')
export class Breadcrumb {
  constructor($scope, $timeout, eventbus, idocContextFactory, breadcrumbEntryManager, stateParamsAdapter, windowAdapter, $state) {
    this.$state = $state;
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.eventbus = eventbus;
    this.idocContextFactory = idocContextFactory;
    this.stateHandler = new StateHandler(statesConfig);
    this.stateParamsAdapter = stateParamsAdapter;
    this.windowAdapter = windowAdapter;
    this.breadcrumbEntryManager = breadcrumbEntryManager;
    this.breadcrumbs = this.breadcrumbEntryManager.getEntries();
    this.events = [
      this.eventbus.subscribe(RouterStateChangeSuccessEvent, this.updateBreadcrumb.bind(this)),
      this.eventbus.subscribe(EntrySelectedFromBreadcrumbEvent, (event)=> {
        this.entryIndex = event[0];
      })
    ];
  }

  updateBreadcrumb(event) {
    if(this.entryIndex) {
      this.breadcrumbEntryManager.clear(this.entryIndex);
      this.entryIndex = null;
    }
    let state = event[0][1].name;
    let currentStateUrl = this.getUrlFromState(event[0][1], this.stateParamsAdapter.getStateParams());
    this.handleState(state, currentStateUrl);
  }

  /**
   * Handle the ng-router state and execute state dependant actions.
   *
   * @param state The current opened ng-router state
   * @param url The url matching provided state
   */
  handleState(state, url) {
    if (this.stateHandler.isResetState(state)) {
      this.breadcrumbEntryManager.clear();
      Breadcrumb.setBrowserTabName(this.windowAdapter, state);
    } else {
      let contextWatch = this.$scope.$watch(()=> {
        return this.idocContextFactory.getCurrentContext();
      }, (context)=> {
        if (context) {
          context.getCurrentObject().then((currentObject)=> {
            let header = currentObject.headers.breadcrumb_header;
            let instanceType = currentObject.instanceType;
            let entry = new Entry(currentObject.getId(), header, instanceType, currentObject.isPersisted(), url);
            this.breadcrumbEntryManager.add(entry);

            let name = _.get(currentObject, 'models.validationModel.title.value', state);
            Breadcrumb.setBrowserTabName(this.windowAdapter, name);
          });
          contextWatch();
        }
      });
    }
  }

  /**
   * Set the browser tab title if provided.
   * @param windowAdapter
   * @param title
   */
  static setBrowserTabName(windowAdapter, title) {
    if(title) {
      windowAdapter.document.title = title;
    }
  }

  getUrlFromState(state, params, options) {
    return this.$state.href(state, params, options);
  }

  ngOnDestroy() {
    for (let event of this.events) {
      event.unsubscribe();
    }
  }
}

export class StateHandler {
  constructor(config) {
    this.states = {
      reset: {}
    };
    for (let state of config.reset) {
      this.states.reset[state] = true;
    }
  }

  isResetState(state) {
    return this.states.reset[state];
  }
}

/**
 * A single entry in the breadcrumb. For now an entry might only be idoc instance object.
 *
 * @param objectId The instance's id
 * @param header The instance breadcrumb header
 * @param instanceType The instance type
 * @param persisted If the instance is persisted or not
 * @param stateUrl The state as router sees it
 */
export class Entry {
  constructor(objectId, header, instanceType, isPersisted, stateUrl) {
    this.objectId = objectId;
    this.header = header;
    this.instanceType = instanceType;
    this.persisted = isPersisted;
    this.stateUrl = stateUrl;
  }

  isPersisted() {
    return this.persisted;
  }

  getId() {
    return this.objectId;
  }

  setIndex(index) {
    this.index = index;
  }

  getIndex() {
    return this.index;
  }

  getStateUrl() {
    return this.stateUrl;
  }
}

@Event()
export class EntrySelectedFromBreadcrumbEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}

@Component({
  selector: 'seip-breadcrumb-entry',
  properties: {
    entry: 'entry'
  }
})
@View({
  template: '<span><seip-static-instance-header header-type="breadcrumbEntry.headerType" header="breadcrumbEntry.entry.header" instance-type="breadcrumbEntry.entry.instanceType" is-disabled="!breadcrumbEntry.entry.isPersisted"></seip-static-instance-header></span>'
})
@Inject(NgScope, NgElement, Eventbus)
export class BreadcrumbEntry {
  constructor($scope, element, eventbus) {
    this.headerType = HEADER_BREADCRUMB;

    element.on('click', (evt) => {
      if (this.entry.isPersisted) {
        let link = element.find('.instance-link');
        link.onclick = eventbus.publish(new EntrySelectedFromBreadcrumbEvent(this.entry.index));
      } else {
        evt.preventDefault();
      }
    });
  }
}