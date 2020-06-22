import {PrintTabAction, NOTIFICATION_LABEL_ID} from 'idoc/actions/print-tab-action';
import {IdocMocks} from '../idoc-mocks';
import {PromiseStub} from 'test/promise-stub';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

describe('PrintTabAction', () => {

  it('should return notification label id', () => {
    let action = getActionHandlerInstance();
    let labelId = action.getNotificationLabelId();
    expect(labelId).to.equal(NOTIFICATION_LABEL_ID)
  });

  it('should call print service with proper arguments', () => {
    let action = getActionHandlerInstance();
    action.getActiveTabId = () => {
      return 'tab:123'
    };
    action.execute({}, {
      currentObject: {
        getId: () => {
          return 'emf:123456'
        }
      },
      idocPageController: IdocMocks.instantiateIdocPage()
    });
    expect(printService.print.calledOnce).to.be.true;
    expect(printService.print.getCall(0).args).to.eql(['/#/idoc/emf:123456?mode=print#tab:123'])
  });

  it('should detect system tab', () => {
    let idocPage = IdocMocks.instantiateIdocPage();
    let tab = {
      system: true
    };
    idocPage.tabsConfig = {
      getActiveTab: ()=> {
        return tab;
      }
    };
    let context = {
      idocPageController: idocPage
    };
    let action = getActionHandlerInstance();
    expect(action.hasSystemContent(context)).to.be.true;
    tab.system = false;
    expect(action.hasSystemContent(context)).to.be.false;
  });

  function getActionHandlerInstance() {
    return new PrintTabAction({}, printService, actionsService, translateService, notificationService, authenticationService, PromiseAdapterMock.mockImmediateAdapter());
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