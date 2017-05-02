import {AddRelationService} from 'idoc/actions/add-relation-service';
import {DialogService} from 'components/dialog/dialog-service';
import {InstanceObject} from 'idoc/idoc-context';
import {StatusCodes} from 'services/rest/status-codes';
import {InstanceRefreshEvent} from 'idoc/actions/events/instance-refresh-event';

import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

const ID = 'emf:123456';
const ADD_RELATION = 'Add Relation';

describe('AddRelationService', () => {
  let scope;
  let dialogService;
  let addRelationService;
  let eventbus;
  let context;

  beforeEach(() => {
    var promiseAdapter = PromiseAdapterMock.mockImmediateAdapter();
    scope = mock$scope();

    context = getActionContext('id');
    eventbus = getEventBusMock();
    dialogService = getDialogServiceMock();

    addRelationService = new AddRelationService(dialogService, getActionServiceMock(StatusCodes.SUCCESS), getNotificationServiceMock(), eventbus, promiseAdapter);
    addRelationService.openDialog(ID, scope, context, getRelationActionModel());
  });

  it('should open a dialog', () => {
    expect(dialogService.create.calledOnce).to.be.true;
  });

  it('should be created search config', () => {
    let searchConfig = getSearchConfig();
    let expected = {
      config: {
        useRootContext: true,
        selection: 'multiple',
        predefinedTypes: [],
        exclusions: [ID],
        triggerSearch: true
      },
      context: context
    };

    expect(expected).to.deep.equal(searchConfig);
  });

  it('should be created dialog config', () => {
    let dialogConfig = getDialogConfig();
    expect(dialogConfig).to.exist;
    expect(dialogConfig.header).to.be.equal(ADD_RELATION);
    expect(dialogConfig.helpTarget).to.exist;
  });

  it('should have ok button', () => {
    let dialogConfig = getDialogConfig();
    expect(dialogConfig.buttons[0].id).to.be.equal(DialogService.OK);
  });

  it('should have click handler for ok button', () => {
    expect(getDialogConfig().buttons[0].onButtonClick).to.exist;
  });

  it('should execute click handler for ok button', () => {
    let config = getDialogConfig();
    config.dismiss = sinon.spy();
    addRelationService.addRelationHandler = sinon.spy();
    let onclick = config.buttons[0].onButtonClick;
    onclick('', {}, config);
    expect(addRelationService.addRelationHandler.calledOnce).to.be.true;
  });

  it('should have click-able ok button if there is selection', () => {
    getSearchConfig().config.selectedItemsIds = [ID];
    scope.$digest();
    expect(getDialogConfig().buttons[0].disabled).to.be.false;
  });

  it('should build model for add relation', () => {
    let expected = {
      removeExisting: false,
      userOperation: 'addRelation',
      relations: {
        'emf:relation': [ID]
      }
    };
    let model = addRelationService.buildRelationRequestData(getRelationActionModel(), [ID]);
    expect(expected).to.deep.equal(model);
  });

  it('should notify when new relation is created', () => {
    let config = getDialogConfig();
    config.dismiss = sinon.spy();
    addRelationService.addRelationHandler(ID, getRelationActionModel(), getDialogConfig());
    expect(addRelationService.notificationService.success.calledOnce).to.be.true;
  });

  it('should notify with state success', () => {
    let config = getDialogConfig();
    config.dismiss = sinon.spy();
    let response = {status: StatusCodes.SUCCESS};
    addRelationService.callback(response, config);
    expect(addRelationService.notificationService.success.calledOnce).to.be.true;
  });

  it('should not notify if state is different than success', () => {
    let config = getDialogConfig();
    config.dismiss = sinon.spy();
    addRelationService.callback('', config);
    expect(addRelationService.notificationService.success.calledOnce).to.be.false;
  });

  it('should have cancel button', () => {
    let dialogConfig = getDialogConfig();
    expect(dialogConfig.buttons[1].id).to.be.equal(DialogService.CANCEL);
  });

  it('should configure the search config with proper idoc context', () => {
    let searchConfig = getSearchConfig();
    expect(searchConfig.context).to.deep.equal(context);
  });

  function getDialogConfig() {
    return dialogService.create.getCall(0).args[2];
  }

  function getSearchConfig() {
    return dialogService.create.getCall(0).args[1];
  }

  function getDialogServiceMock() {
    return {
      create: sinon.spy()
    };
  }

  function getActionServiceMock(state) {
    return {
      addRelation: sinon.spy(() => {
        return PromiseStub.resolve({status: state});
      })
    }
  }

  function getNotificationServiceMock() {
    return {
      success: sinon.spy()
    }
  }

  function getRelationActionModel() {
    return {
      action: 'addRelation',
      label: 'Add Relation',
      configuration: {
        relation: ['emf:relation'],
        selection: 'multiple',
        implicitParams: {
          removeExisting: false
        }
      }
    }
  }

  function getEventBusMock() {
    return {
      subscribe: sinon.spy(),
      publish: sinon.spy()
    };
  }

  function getActionContext(id) {
    var object = new InstanceObject(id);
    return {
      currentObject: object
    };
  }
});
