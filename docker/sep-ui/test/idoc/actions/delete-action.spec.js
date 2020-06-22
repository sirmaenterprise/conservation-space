import {DeleteAction} from 'idoc/actions/delete-action';
import {Eventbus} from 'services/eventbus/eventbus';
import {AfterIdocDeleteEvent} from 'idoc/actions/events/after-idoc-delete-event';
import {USER_DASHBOARD, IDOC_PAGE_ACTIONS_PLACEHOLDER} from 'idoc/idoc-constants';
import {Logger} from 'services/logging/logger';
import {Router} from 'adapters/router/router';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {BreadcrumbEntryManager} from 'layout/breadcrumb/breadcrumb-entry-manager';
import {EntryItem} from 'layout/breadcrumb/breadcrumb-entry/entry-item';
import {ActionsService} from 'services/rest/actions-service';
import {StatusCodes} from 'services/rest/status-codes';

import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';

describe('DeleteAction', () => {

  const ID = 'emf:123456';
  const PREVIOUS_ID = 'emf:qwerty';
  const ACTION_NAME = 'deleteAction';

  let handler;
  let breadcrumbEntryManager;
  let actionsService;
  let eventbus = new Eventbus();
  let actionDefinition;
  let context;

  beforeEach(() => {
    actionDefinition = {
      action: ACTION_NAME,
      forceRefresh: true
    };
    context = {
      currentObject: {
        getId: () => {
          return ID;
        }
      },
      placeholder: IDOC_PAGE_ACTIONS_PLACEHOLDER
    };

    actionsService = stub(ActionsService);
    actionsService.delete.returns(PromiseStub.resolve({}));

    breadcrumbEntryManager = stub(BreadcrumbEntryManager);
    breadcrumbEntryManager.getPreviousEntry.returns(getInstanceStateEntry());

    handler = new DeleteAction(actionsService, stub(Logger), stub(NotificationService), stub(TranslateService),
      breadcrumbEntryManager, eventbus, stub(Router), PromiseStub, stub(WindowAdapter));
  });

  function getInstanceStateEntry() {
    return new EntryItem(PREVIOUS_ID, 'header', 'instanceType', true, 'stateUrl', true);
  }

  it('should call service for delete with the object id', () => {
    let actionServiceDeleteSpy = actionsService.delete;
    handler.execute(actionDefinition, context);
    expect(actionServiceDeleteSpy.calledOnce).to.be.true;
    expect(actionServiceDeleteSpy.getCall(0).args).to.eql([ID, ACTION_NAME]);
  });

  it('should fire AfterIdocDeletedEvent if successful', () => {
    let spyEventHandler = sinon.spy();
    eventbus.subscribe(AfterIdocDeleteEvent, spyEventHandler);
    handler.execute(actionDefinition, context);
    expect(spyEventHandler.calledOnce).to.be.true;
  });

  it('should rise notification for success', () => {
    handler.execute(actionDefinition, context);
    expect(handler.notificationService.success.calledOnce).to.be.true;
  });

  it('should set forceRefresh to false if executed from object\'s landing page', () => {
    expect(actionDefinition.forceRefresh).to.be.true;
    handler.execute(actionDefinition, context);
    expect(actionDefinition.forceRefresh).to.be.false;
  });

  it('should set forceRefresh to true if executed not from object\'s landing page', () => {
    context.placeholder = 'search.actions.menu';
    expect(actionDefinition.forceRefresh).to.be.true;
    handler.execute(actionDefinition, context);
    expect(actionDefinition.forceRefresh).to.be.true;
  });

  it('should perform navigation to previous state if the state can be navigated to', () => {
    handler.execute(actionDefinition, context);
    expect(handler.windowAdapter.navigate.calledOnce).to.be.true;
    expect(handler.windowAdapter.navigate.calledWith('stateUrl')).to.be.true;
  });

  it('should remove all deleted objects from the breadcrumb manager', () => {
    actionsService.delete.returns(PromiseStub.resolve({data: ['id-1', 'id-2']}));
    breadcrumbEntryManager.getLastEntry.returns(getInstanceStateEntry());
    handler.execute(actionDefinition, context);
    expect(breadcrumbEntryManager.removeEntry.calledTwice).to.be.true;
    expect(handler.windowAdapter.navigate.calledOnce).to.be.true;
    expect(handler.windowAdapter.navigate.calledWith('stateUrl')).to.be.true;
  });

  it('should navigate to user dashboard if the last state cannot be navigated to', () => {
    breadcrumbEntryManager.getPreviousEntry.returns(new EntryItem('last-state', 'header', 'type', true, undefined));
    handler.execute(actionDefinition, context);
    expect(handler.router.navigate.calledOnce).to.be.true;
    expect(handler.router.navigate.getCall(0).args[0]).to.equal(USER_DASHBOARD);
  });

  it('should navigate to user dashboard if executed from the object\'s landing page and the action request is rejected with error', () => {
    actionsService.delete.returns(PromiseStub.reject({
      status: 405,
      data: {
        message: 'error'
      }
    }));

    handler.execute(actionDefinition, context);

    expect(handler.router.navigate.calledOnce).to.be.true;
    expect(handler.router.navigate.getCall(0).args[0]).to.equal(USER_DASHBOARD);
  });

  it('should stay on the same page and print warning message, when the request is rejected and the status code is 409', () => {
    actionsService.delete.returns(PromiseStub.reject({
      status: StatusCodes.CONFLICT, data: {
        message: 'error'
      }
    }));

    handler.execute(actionDefinition, context);

    expect(handler.notificationService.warning.calledOnce).to.be.true;
    expect(handler.router.navigate.calledOnce).to.be.false;
  });

  it('should navigate to user dashboard if executed from the object\'s landing page and the action request is rejected without error', () => {
    actionsService.delete.returns(PromiseStub.reject());

    handler.execute(actionDefinition, context);

    expect(handler.router.navigate.calledOnce).to.be.true;
    expect(handler.router.navigate.getCall(0).args[0]).to.equal(USER_DASHBOARD);
  });

  it('should navigate to user dashboard if executed from object\'s landing page but there is no previous state', () => {
    breadcrumbEntryManager.getPreviousEntry.returns(undefined);

    handler.execute(actionDefinition, context);

    expect(handler.router.navigate.calledOnce).to.be.true;
    expect(handler.router.navigate.getCall(0).args[0]).to.equal(USER_DASHBOARD);
  });

  it('should not navigate to previous state if executed not from object\'s landing page', () => {
    context.placeholder = 'search.actions.menu';
    handler.execute(actionDefinition, context);
    expect(handler.router.navigate.calledOnce).to.be.false;
  });
});