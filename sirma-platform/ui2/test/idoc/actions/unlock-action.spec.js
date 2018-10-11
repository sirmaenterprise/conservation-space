import {IdocMocks} from '../idoc-mocks';
import {IdocContext} from 'idoc/idoc-context';
import {InstanceObject} from 'models/instance-object';
import {UnlockAction} from 'idoc/actions/unlock-action';
import {PromiseStub} from 'test/promise-stub';
import {UNLOCK} from 'idoc/actions/action-constants';

describe('Unlock idoc action', () => {
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

  it('should unlock instance when call', () => {
    currentObject = new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage(IDOC_ID);
    idocPage.currentObject = currentObject;
    idocPage.currentObject.models.validationModel.field1.value = 'modified value';
    idocPage.tabsConfig.tabs[0].content = 'modified content';

    let actionsService = {
      unlock: () => {
        return PromiseStub.resolve({})
      }
    };
    let spyUnlockAction = sinon.spy(actionsService, UNLOCK);
    let handler = new UnlockAction(actionsService, {});
    handler.execute({
      operation: UNLOCK,
      action: UNLOCK
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
      operation: UNLOCK,
      userOperation: UNLOCK,
      contextPath: [],
      targetInstance: {
        definitionId: 'OT210027',
        properties: {
          field1: 'modified value',
          title: 'Title'
        }
      }
    };

    expect(spyUnlockAction.callCount).to.equal(1);
    expect(spyUnlockAction.getCall(0).args).to.eql(['emf:123456', payload]);
  });

});