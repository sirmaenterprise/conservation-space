import {IdocContextFactory} from 'services/idoc/idoc-context-factory';
import {SessionStorageService} from 'services/storage/session-storage-service';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {IdocContext} from 'idoc/idoc-context';
import {IdocMocks} from '../../idoc/idoc-mocks';
import {PromiseAdapterMock} from '../../adapters/angular/promise-adapter-mock';
import {Eventbus} from 'services/eventbus/eventbus';
import {RouterStateChangeStartEvent} from 'common/router/router-state-change-start-event';

describe('IdocContextFactory', () => {
  const IDOC_ID_1 = 'emf:123456';
  const IDOC_ID_2 = 'emf:999888';

  let idocContextFactory;
  let eventbus;

  beforeEach(() => {
    let sessionStorageService = new SessionStorageService(new WindowAdapter(window));
    sessionStorageService.set('models', JSON.stringify(IdocMocks.generateModels()));
    eventbus = new Eventbus();
    idocContextFactory = new IdocContextFactory(IdocMocks.mockInstanceRestService(IDOC_ID_1), sessionStorageService,
      PromiseAdapterMock.mockAdapter(), eventbus);
  });

  it('should create and return new context', () => {
    expect(idocContextFactory.context).to.be.undefined;
    let context = idocContextFactory.createNewContext(IDOC_ID_1, 'edit');
    expect(context).instanceof(IdocContext);
    expect(context.id).to.equal(IDOC_ID_1);
  });

  it('getCurrentContext() should return last created context', () => {
    expect(idocContextFactory.getCurrentContext()).to.be.undefined;
    idocContextFactory.createNewContext(IDOC_ID_1, 'edit');
    let context1 = idocContextFactory.getCurrentContext();
    expect(context1.id).to.equal(IDOC_ID_1);

    idocContextFactory.createNewContext(IDOC_ID_2, 'edit');
    let context2 = idocContextFactory.getCurrentContext();
    expect(context2.id).to.equal(IDOC_ID_2);
    expect(context1).to.not.equal(context2);
  });

  it('clearCurrentContext() should clear created context', () => {
    idocContextFactory.createNewContext(IDOC_ID_1, 'edit');

    idocContextFactory.clearCurrentContext();
    expect(idocContextFactory.getCurrentContext()).to.be.undefined;
  });

  it('should clear the current context when the view is changed', () => {
    idocContextFactory.createNewContext(IDOC_ID_1, 'edit');

    eventbus.publish(new RouterStateChangeStartEvent());

    expect(idocContextFactory.getCurrentContext()).to.be.undefined;
  });
});
