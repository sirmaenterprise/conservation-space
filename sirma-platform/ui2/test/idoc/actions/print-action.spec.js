import {PrintAction, NOTIFICATION_LABEL_ID} from 'idoc/actions/print-action';
import {PromiseStub} from 'test/promise-stub';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

describe('PrintAction', () => {

  it('should return notification label id', () => {
    let action = getActionHandlerInstance();
    let labelId = action.getNotificationLabelId();
    expect(labelId).to.equal(NOTIFICATION_LABEL_ID)
  });

  it('should call print service with proper arguments', () => {
    let action = getActionHandlerInstance();
    action.execute({}, {
      currentObject: {
        getId: () => {
          return 'emf:123456'
        }
      }
    });
    expect(printService.print.calledOnce).to.be.true;
    expect(printService.print.getCall(0).args).to.eql(['/#/idoc/emf:123456?mode=print'])
  });

  function getActionHandlerInstance() {
    return new PrintAction({}, printService, actionsService, translateService, notificationService, authenticationService, PromiseAdapterMock.mockImmediateAdapter());
  }
  let authenticationService = {
    getToken: sinon.spy()
  };
  let printService = {
    print: sinon.spy()
  };
  let translateService = {
    translateInstant: sinon.spy()
  };
  let notificationService = {
    info: sinon.spy()
  };
  let exportingResult = {
    data: {
      fileName: 'test'
    }
  };
  let actionsService = {
    exportPDF: () => {
      return PromiseStub.resolve(exportingResult);
    }
  }
});