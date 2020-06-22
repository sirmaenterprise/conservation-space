import {IdocMocks} from '../idoc-mocks';
import {IdocContext} from 'idoc/idoc-context';
import {InstanceObject} from 'models/instance-object';
import {LockAction} from 'idoc/actions/lock-action';
import {PromiseStub} from 'test/promise-stub';
import {LOCK} from 'idoc/actions/action-constants';

describe('Lock idoc action', () => {
  const IDOC_ID = 'emf:123456';
  var currentObject;
  beforeEach(() => {
    sinon.stub(IdocContext.prototype, 'getCurrentObject', () => {
      return new Promise((resolve) => {
        resolve(currentObject);
      });
    });
  });
  afterEach(() => {
    IdocContext.prototype.getCurrentObject.restore();
  });

  it('should lock instance when call', () => {
    currentObject = new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage(IDOC_ID);
    idocPage.currentObject = currentObject;
    idocPage.currentObject.models.validationModel.field1.value = 'modified value';
    idocPage.tabsConfig.tabs[0].content = 'modified content';

    let actionsService = {
      lock: () => {
        return PromiseStub.resolve({})
      }
    };
    let spyLockAction = sinon.spy(actionsService, LOCK);
    let handler = new LockAction(actionsService, {});
    handler.execute({
      operation: LOCK,
      action: LOCK
    }, {
      currentObject: currentObject,
      idocPageController: {
        loadActionsAndProcessPermissionChange: ()=> {}
      },
      idocContext: {
        reloadObjectDetails: ()=> PromiseStub.resolve()
      }
    });

    let payload = {
      operation: LOCK,
      userOperation: LOCK,
      contextPath: [],
      targetInstance: {
        definitionId: 'OT210027',
        properties: {
          field1: 'modified value',
          title: 'Title'
        }
      }
    };

    expect(spyLockAction.callCount).to.equal(1);
    expect(spyLockAction.getCall(0).args).to.eql(['emf:123456', payload]);
  });
});