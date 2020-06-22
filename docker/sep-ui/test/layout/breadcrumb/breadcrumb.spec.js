import {Breadcrumb} from 'layout/breadcrumb/breadcrumb';
import {EntryItem} from 'layout/breadcrumb/breadcrumb-entry/entry-item';
import {EntryComponent} from 'layout/breadcrumb/breadcrumb-entry/entry-component';

import {Eventbus} from 'services/eventbus/eventbus';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {PluginsService} from 'services/plugin/plugins-service';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {BreadcrumbEntryManager} from 'layout/breadcrumb/breadcrumb-entry-manager';
import {BreadcrumbStateHandler} from 'layout/breadcrumb/breadcrumb-state-handler';

import {RouterStateChangeSuccessEvent} from 'common/router/router-state-change-success-event';
import {EntrySelectedFromBreadcrumbEvent} from 'layout/breadcrumb/breadcrumb-entry/entry-selected-event';

import {HEADER_BREADCRUMB} from 'instance-header/header-constants';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('Tests for the breadcrumb section', function () {

  function getBreadcrumbInstance() {
    let eventBusStub = stub(Eventbus);
    let entryManagerStub = stub(BreadcrumbEntryManager);
    let stateParamStub = stub(StateParamsAdapter);
    let windowAdapterStub = stub(WindowAdapter);
    let pluginsServiceStub = stub(PluginsService);
    let translateServiceStub = stub(TranslateService);
    let instanceServiceStub = stub(InstanceRestService);

    return new Breadcrumb(eventBusStub, entryManagerStub, stateParamStub, windowAdapterStub, {}, pluginsServiceStub, translateServiceStub, instanceServiceStub);
  }

  describe('on init', () => {
    it('should subscribe for RouterStateChangeSuccessEvent and EntrySelectedFromBreadcrumbEvent', () => {
      let breadcrumb = getBreadcrumbInstance();
      breadcrumb.updateBreadcrumb = sinon.spy();
      breadcrumb.ngOnInit();

      expect(breadcrumb.updateBreadcrumb.calledOnce).to.be.true;
      expect(breadcrumb.events.length).to.equal(2);
      expect(breadcrumb.eventbus.subscribe.getCall(0).args[0]).to.eql(RouterStateChangeSuccessEvent);
      expect(breadcrumb.eventbus.subscribe.getCall(1).args[0]).to.eql(EntrySelectedFromBreadcrumbEvent);
    });

    it('should initialize entryIndex when EntrySelectedFromBreadcrumbEvent is fired', () => {
      let breadcrumb = getBreadcrumbInstance();
      // use actual event-bus object to publish
      breadcrumb.eventbus = new Eventbus();
      breadcrumb.updateBreadcrumb = sinon.spy();

      breadcrumb.ngOnInit();
      breadcrumb.eventbus.publish(new EntrySelectedFromBreadcrumbEvent('test'));

      expect(breadcrumb.updateBreadcrumb.calledOnce).to.be.true;
      expect(breadcrumb.entryIndex).to.equal('test');
    });
  });

  describe('updateBreadcrumb', () => {

    it('should correctly handle when state is reset', () => {
      let breadcrumb = getBreadcrumbInstance();
      let stateStub = sinon.stub(BreadcrumbStateHandler, 'isResetState', () => {
        return true;
      });
      breadcrumb.$state = {
        current: {
          name: 'reset'
        },
        href: () => 'url'
      };
      breadcrumb.windowAdapter = {
        document: {}
      };

      breadcrumb.ngOnInit();
      expect(breadcrumb.windowAdapter.document.title).to.equal('reset');
      expect(breadcrumb.breadcrumbEntryManager.clear.calledOnce).to.be.true;
      // restore the stubbed or mocked states
      stateStub.restore();
    });

    it('should correctly handle when state should not be ignored', () => {
      let breadcrumb = getBreadcrumbInstance();
      let stateStub = sinon.stub(BreadcrumbStateHandler, 'isIgnoreState', () => {
        return false;
      });
      breadcrumb.addStateEntry = sinon.spy();
      breadcrumb.translateService.translateInstant.returns('title');
      breadcrumb.$state = {
        current: {
          name: 'search'
        },
        href: () => 'url'
      };
      breadcrumb.pluginsService.loadComponentModules.returns(PromiseStub.resolve({
        search: {
          icon: 'icon',
          label: 'label',
          url: 'url'
        }
      }));
      breadcrumb.ngOnInit();

      expect(breadcrumb.routesPromise).to.not.exist;
      expect(breadcrumb.addStateEntry.calledOnce).to.be.true;
      expect(breadcrumb.addStateEntry.calledWith('search', 'url')).to.be.true;
      expect(breadcrumb.routes).to.deep.eq({
        search: {
          icon: 'icon',
          label: 'label',
          url: 'url'
        }
      });
      // restore the stubbed or mocked states
      stateStub.restore();
    });

    it('should correctly handle instance loading when id is provided', () => {
      let breadcrumb = getBreadcrumbInstance();
      let stateStub = sinon.stub(BreadcrumbStateHandler, 'isIgnoreState', () => {
        return true;
      });
      breadcrumb.stateParamsAdapter.getStateParams.returns({id: 'emf:123'});
      breadcrumb.instanceRestService.load.returns(PromiseStub.resolve({
        data: {
          id: 'emf:123',
          instanceType: 'instance',
          properties: {
            title: 'Case'
          },
          headers: {
            breadcrumb_header: 'header'
          }
        }
      }));
      breadcrumb.windowAdapter = {
        document: {}
      };
      breadcrumb.$state = {
        current: {
          name: 'idoc'
        },
        href: () => 'url'
      };

      breadcrumb.ngOnInit();
      let entry = new EntryItem('emf:123', 'header', 'instance', true, 'url');

      expect(breadcrumb.windowAdapter.document.title).to.equal('Case');
      expect(breadcrumb.instanceRestService.load.calledOnce).to.be.true;

      expect(breadcrumb.breadcrumbEntryManager.add.calledOnce).to.be.true;
      expect(breadcrumb.breadcrumbEntryManager.add.calledWith(entry)).to.be.true;
      // restore the stubbed or mocked states
      stateStub.restore();
    });

    it('should not handle the state if it lacks a name or is abstract', () => {
      let breadcrumb = getBreadcrumbInstance();
      breadcrumb.handleState = sinon.spy();
      breadcrumb.$state = {
        current: {
          name: ''
        }
      };
      breadcrumb.ngOnInit();
      expect(breadcrumb.handleState.called).to.be.false;
    });
  });

  describe('setBrowserTabName', () => {
    it('should apply provided string as document.title', () => {
      let windowAdapter = {
        document: {}
      };
      Breadcrumb.setBrowserTabName(windowAdapter, 'title');
      expect(windowAdapter.document.title).to.equal('title');
    });
  });

  describe('createHeader & addStateEntry', () => {

    it('should properly create header', () => {
      let header = Breadcrumb.createHeader('title', 'url', 'icon');
      let expected = '<span><i class="header-color fa fa-1x fa-fw icon"></i></span><span><a class="instance-link" href="url"><span data-property="title">title</span></a></span>';
      expect(header).to.equal(expected);
    });

    it('should correctly add state to the breadcrumb entries', () => {
      let breadcrumb = getBreadcrumbInstance();
      breadcrumb.routes = {
        search: {
          icon: 'icon',
          label: 'label',
          url: 'url'
        }
      };

      let header = Breadcrumb.createHeader('title', 'url', 'icon');
      let expectedEntry = new EntryItem('search', header, 'search', true, 'url');
      breadcrumb.translateService.translateInstant.returns('title');

      breadcrumb.addStateEntry('search', 'url');
      expect(breadcrumb.breadcrumbEntryManager.add.calledOnce).to.be.true;
      expect(breadcrumb.breadcrumbEntryManager.add.calledWith(expectedEntry)).to.be.true;
      expect(breadcrumb.translateService.translateInstant.calledOnce).to.be.true;
    });
  });

  describe('EntryComponent', () => {

    it('should set the default headerType on init', () => {
      let breadcrumbEntry = getEntryComponentInstance();
      expect(breadcrumbEntry.headerType).to.equal(HEADER_BREADCRUMB);
    });

    it('should publish EntrySelectedFromBreadcrumbEvent when persisted breadcrumb entry is clicked', () => {
      let breadcrumbEntry = getEntryComponentInstance();
      breadcrumbEntry.entry = new EntryItem('1', 'header', 'type', true);
      breadcrumbEntry.element.trigger('click');
      expect(breadcrumbEntry.eventbus.publish.calledOnce).to.be.true;
    });

    it('should stop click events propagation if clicked breadcrumb entry is not persisted', () => {
      let breadcrumbEntry = getEntryComponentInstance();
      breadcrumbEntry.entry =  new EntryItem('1', 'header', 'type', false);
      breadcrumbEntry.element.trigger('click');
      expect(spyPreventDefault.callCount).to.equal(1);
    });

    let spyPreventDefault = sinon.spy();

    let element = {
      handlers: {},
      on: (event, callback) => {
        element.handlers[event] = callback
      },
      trigger: (eventName) => {
        element.handlers[eventName]({
          preventDefault: spyPreventDefault
        });
      },
      find: () => {
        return {
          on: (callback) => {
            callback();
          }
        };
      }
    };

    function getEntryComponentInstance() {
      let entryComponent = new EntryComponent(element, stub(Eventbus));
      entryComponent.ngOnInit();
      return entryComponent;
    }
  });
});