import { PrintTabAction, NOTIFICATION_LABEL_ID } from 'idoc/actions/print-tab-action';
import {InstanceObject} from 'idoc/idoc-context';
import {IdocMocks} from '../idoc-mocks';
import {PromiseStub} from 'test/promise-stub';

describe('PrintTabAction', () => {

  it('should return notification label id', () => {
    let action = getActionHandlerInstance();
    let labelId = action.getNotificationLabelId();
    expect(labelId).to.equal(NOTIFICATION_LABEL_ID)
  });

  it('should call print service with proper arguments', () => {
    let action = getActionHandlerInstance();
    action.getActiveTabId = () => {return 'tab:123'};
    action.execute({}, {
      currentObject: {
        getId: () => {
          return 'emf:123456'
        }
      }
    });
    expect(printService.print.calledOnce).to.be.true;
    expect(printService.print.getCall(0).args).to.eql(['/#/idoc/emf:123456?mode=print#tab:123'])
  });

  function getActionHandlerInstance() {
    return new PrintTabAction({}, printService, actionsService, translateService, notificationService, authenticationService);
  }

  let printService = {
    print: sinon.spy()
  };

  let translateService = {
    translateInstant: sinon.spy()
  };

  let notificationService = {
    info: sinon.spy()
  };
  let authenticationService = {
    getToken: sinon.spy()
  };
  let exportingResult = {
    data: 'test'
  };

  let actionsService = {
    exportPDF: () => {
      return PromiseStub.resolve(exportingResult);
    }
  }
});