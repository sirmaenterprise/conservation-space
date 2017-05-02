import {StateHandler, BreadcrumbEntry, Breadcrumb, EntrySelectedFromBreadcrumbEvent} from 'layout/breadcrumb/breadcrumb';
import {IdocMocks} from '../../idoc/idoc-mocks';
import {Eventbus} from 'services/eventbus/eventbus';
import {RouterStateChangeSuccessEvent} from 'common/router/router-state-change-success-event';
import {HEADER_BREADCRUMB} from 'instance-header/header-constants';

describe('Tests for the breadcrumb section', function () {

  describe('on init', () => {
    it('should subscribe for RouterStateChangeSuccessEvent and EntrySelectedFromBreadcrumbEvent', () => {
      let $scope = {};
      let $timeout = {};
      let idocContextFactory = {};
      let breadcrumbEntryManager = {
        getEntries: sinon.stub()
      };
      let eventbus = new Eventbus();
      let spySubscribe = sinon.spy(eventbus, 'subscribe');
      let breadcrumb = new Breadcrumb($scope, $timeout, eventbus, idocContextFactory, breadcrumbEntryManager);
      expect(breadcrumb.events.length).to.equal(2);
      expect(spySubscribe.getCall(0).args[0]).to.eql(RouterStateChangeSuccessEvent);
      expect(spySubscribe.getCall(1).args[0]).to.eql(EntrySelectedFromBreadcrumbEvent);
    });

    it('should initialize entryIndex when EntrySelectedFromBreadcrumbEvent is fired', () => {
      let $scope = {};
      let $timeout = {};
      let idocContextFactory = {};
      let breadcrumbEntryManager = {
        getEntries: sinon.stub()
      };
      let eventbus = new Eventbus();
      let breadcrumb = new Breadcrumb($scope, $timeout, eventbus, idocContextFactory, breadcrumbEntryManager);

      eventbus.publish(new EntrySelectedFromBreadcrumbEvent('test'));
      expect(breadcrumb.entryIndex).to.equal('test');
    });
  });

  describe('updateBreadcrumb', () => {
    it('should update correctly breadcrumb depending on entryIndex', () => {
      let $scope = {
        $watch: ()=> {}
      };
      let $timeout = {};
      let idocContextFactory = {};
      let breadcrumbEntryManager = {
        getEntries: sinon.stub(),
        clear: sinon.stub()
      };
      let eventbus = new Eventbus();
      let breadcrumb = new Breadcrumb($scope, $timeout, eventbus, idocContextFactory, breadcrumbEntryManager, IdocMocks.mockStateParamsAdapter(), {}, {href: () => {}});
      let handleStateSpy = sinon.spy(breadcrumb, 'handleState');

      breadcrumb.updateBreadcrumb({0: {1: {name: ""}}});
      expect(handleStateSpy.calledOnce).to.be.true;
      expect(breadcrumbEntryManager.clear.calledOnce).to.be.false;
      handleStateSpy.reset();

      breadcrumb.entryIndex = 1;
      breadcrumb.updateBreadcrumb({0: {1: {name: ""}}});
      expect(handleStateSpy.calledOnce).to.be.true;
      expect(breadcrumbEntryManager.clear.calledOnce).to.be.true;
      expect(breadcrumb.entryIndex).to.equal(null);
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

  describe('StateHandler', function () {

    it('should test if isResetState() returns true if the state is marked as reset one', function () {
      let statesConfig = {
        reset: ['search', 'library']
      };
      let stateHandler = new StateHandler(statesConfig);

      expect(stateHandler.isResetState('search')).to.be.true;
      expect(stateHandler.isResetState('library')).to.be.true;
    });
  });

  describe('BreadcrumbEntry', () => {
    it('should set the default headerType on init', () => {
      let $scope = {};
      let element = {
        on: sinon.stub()
      };
      let breadcrumbEntry = new BreadcrumbEntry($scope, element, new Eventbus());
      expect(breadcrumbEntry.headerType).to.equal(HEADER_BREADCRUMB);
    });

    it('should publish EntrySelectedFromBreadcrumbEvent when persisted breadcrumb entry is clicked', () => {
      BreadcrumbEntry.prototype.entry = {
        isPersisted: true
      };
      getBreadcrumbEntryInstance();
      element.trigger('click');
      expect(eventbus.publish.calledOnce).to.be.true;
    });

    it('should stop click events propagation if clicked breadcrumb entry is not persisted', () => {
      BreadcrumbEntry.prototype.entry = {
        isPersisted: false
      };
      getBreadcrumbEntryInstance();
      element.trigger('click');
      expect(spyPreventDefault.callCount).to.equal(1);
    });
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
      })
    },
    find: () => {
      return {
        on: (callback) => {
          callback();
        }
      }
    }
  };

  let eventbus = {
    publish: sinon.spy()
  };

  function getBreadcrumbEntryInstance(elementCustom, eventbusCustom) {
    let $scope = {};
    return new BreadcrumbEntry($scope, elementCustom || element, eventbusCustom || eventbus);
  }

});