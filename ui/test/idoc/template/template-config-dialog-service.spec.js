import {TemplateConfigDialogService} from 'idoc/template/template-config-dialog-service';
import {DialogService} from 'components/dialog/dialog-service';
import {InstanceObject} from 'idoc/idoc-context';
import {StatusCodes} from 'services/rest/status-codes';
import {TemplateDataPanel} from 'idoc/template/template-data-panel';

import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub'
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

describe('TemplateConfigDialogService', () => {

  var templateConfigDialogServiceService;
  var scopeMock;
  var dialogService;

  beforeEach(() => {
    var promiseAdapter = PromiseAdapterMock.mockImmediateAdapter();
    dialogService = getDialogServiceMock();
    templateConfigDialogServiceService = new TemplateConfigDialogService(dialogService, mockTemplateService(true), promiseAdapter);
    scopeMock = mock$scope();
  });

  it('should open a dialog', () => {
    templateConfigDialogServiceService.openDialog(scopeMock, getInstanceObject());

    expect(dialogService.create.calledOnce).to.be.true;
  });

  it('should build configuration for the dialog component', () => {
    templateConfigDialogServiceService.openDialog(scopeMock, getInstanceObject());

    var expected = {
      config: {
        type: 'definition',
        template: {}
      }
    };
    expect(extractComponentConfig(dialogService)).to.deep.equal(expected);
  });

  it('should build configuration for the dialog component when currentObject is not provided', () => {
    templateConfigDialogServiceService.openDialog(scopeMock);

    var expected = {
      config: {
        template: {}
      }
    };

    expect(extractComponentConfig(dialogService)).to.deep.equal(expected);
  });

  it('should build configuration for the dialog component providing the type filter', () => {
    const TYPE_FILTER = 'testType';
    templateConfigDialogServiceService.openDialog(scopeMock, null, TYPE_FILTER);

    var expected = {
      config: {
        template: {},
        typeFilter: TYPE_FILTER
      }
    };

    expect(extractComponentConfig(dialogService)).to.deep.equal(expected);
  });

  it('should build configuration for the dialog service', () => {
    templateConfigDialogServiceService.openDialog(scopeMock, getInstanceObject());

    var dialogConfig = extractDialogConfig(dialogService);
    expect(dialogConfig).to.exist;
    expect(dialogConfig.showHeader).to.be.true;
  });

  it('should build configuration for OK and CANCEL dialog buttons', () => {
    templateConfigDialogServiceService.openDialog(scopeMock, getInstanceObject());

    var buttons = extractDialogConfig(dialogService).buttons;
    expect(buttons).to.exist;
    expect(buttons[0].id).to.equal(DialogService.OK);
    expect(buttons[1].id).to.equal(DialogService.CANCEL);
  });

  it('should configure a handler for ok button', () => {
    templateConfigDialogServiceService.openDialog(scopeMock, getInstanceObject());

    expect(extractDialogConfig(dialogService).buttons[0].onButtonClick).to.exist;
  });

  it('should invoke a handler if ok button is pressed', () => {
    templateConfigDialogServiceService.openDialog(scopeMock, getInstanceObject());

    templateConfigDialogServiceService.okButtonHandler = sinon.spy();
    templateConfigDialogServiceService.openDialog(mock$scope(), getInstanceObject());
    var okButtonClick = extractDialogConfig(dialogService).buttons[0].onButtonClick;
    var dialogConfig = {
      dismiss: ()=> {
      }
    };
    okButtonClick('', {}, dialogConfig);
    expect(templateConfigDialogServiceService.okButtonHandler.calledOnce).to.be.true;
    expect(templateConfigDialogServiceService.okButtonHandler.getCall(0).args[1]).to.deep.equal(dialogConfig);
  });

  it('should configure to dismiss dialog on cancel button', () => {
    templateConfigDialogServiceService.openDialog(scopeMock, getInstanceObject());

    expect(extractDialogConfig(dialogService).buttons[1].dismiss).to.be.true;
  });

  it('should enable OK button when the title property is valid', () => {
    templateConfigDialogServiceService.openDialog(scopeMock, getInstanceObject());

    var okButton = extractDialogConfig(dialogService).buttons[0];
    var componentConfig = extractComponentConfig(dialogService);
    componentConfig.config.template.title = 'New title';
    scopeMock.$digest();
    expect(okButton.disabled).to.be.false;
  });

  it('should disable OK button when the title property is not valid', () => {
    templateConfigDialogServiceService.openDialog(scopeMock, getInstanceObject());

    var okButton = extractDialogConfig(dialogService).buttons[0];
    var componentConfig = extractComponentConfig(dialogService);
    componentConfig.config.template.title = '';
    scopeMock.$digest();
    expect(okButton.disabled).to.be.true;
  });

  it('should disable OK button when the title property is just whitespace', () => {
    templateConfigDialogServiceService.openDialog(scopeMock, getInstanceObject());

    var okButton = extractDialogConfig(dialogService).buttons[0];
    var componentConfig = extractComponentConfig(dialogService);
    componentConfig.config.template.title = '   ';
    scopeMock.$digest();
    expect(okButton.disabled).to.be.true;
  });

  it('should disable ok button to prevent spamming', () => {
    templateConfigDialogServiceService.openDialog(scopeMock, getInstanceObject());

    var componentConfig = getComponentConfig();
    var dialogConfig = {
      buttons: [{disabled: false}],
      dismiss: sinon.spy()
    };
    templateConfigDialogServiceService.templateService = mockTemplateService(StatusCodes.SUCCESS);
    templateConfigDialogServiceService.notificationService = getNotificationServiceMock();
    templateConfigDialogServiceService.translateService = getTranslateServiceMock();

    templateConfigDialogServiceService.okButtonHandler(getInstanceObject(), dialogConfig, componentConfig, () => {
    });
    expect(dialogConfig.buttons[0].disabled).to.be.true;
  });

  it('should return proper result when the dialog button is pressed', () => {
    var currentInstance = getInstanceObject();
    var componentConfig = getComponentConfig();
    var dialogConfig = {
      buttons: [{disabled: false}],
      dismiss: sinon.spy()
    };

    var result;

    templateConfigDialogServiceService.okButtonHandler(currentInstance, dialogConfig, componentConfig, (payload) => {
      result = payload;
    });

    expect(result).to.eql({
      forType: componentConfig.config.type,
      title: componentConfig.config.template.title,
      purpose: componentConfig.config.template.purpose,
      primary: componentConfig.config.template.primary,
      sourceInstance: currentInstance.id
    });
  });

  it('should return proper result when the dialog button is pressed and current instance is not provided', () => {
    var componentConfig = getComponentConfig();
    var dialogConfig = {
      buttons: [{disabled: false}],
      dismiss: sinon.spy()
    };

    var result;

    templateConfigDialogServiceService.okButtonHandler(null, dialogConfig, componentConfig, (payload) => {
      result = payload;
    });

    expect(result).to.eql({
      forType: componentConfig.config.type,
      title: componentConfig.config.template.title,
      purpose: componentConfig.config.template.purpose,
      primary: componentConfig.config.template.primary,
    });
  });

});

function extractComponentConfig(dialogServiceMock) {
  return dialogServiceMock.create.getCall(0).args[1];
}

function extractDialogConfig(dialogServiceMock) {
  return dialogServiceMock.create.getCall(0).args[2];
}

function getComponentConfig() {
  return {
    config: {
      type: 'type',
      template: {
        title: 'title',
        purpose: TemplateDataPanel.CREATABLE,
        primary: 'primary'
      }
    }
  };
}

function getDialogServiceMock() {
  return {
    create: sinon.spy()
  }
}

function getOptions() {
  return {
    context: getContextMock(),
    scope: mock$scope()
  };
}

function getContextMock() {
  return {
    getCurrentObject: ()=> {
      return PromiseStub.resolve(getInstanceObject());
    }
  };
}

function getInstanceObject() {
  var models = {
    definitionId: 'definition'
  };
  return new InstanceObject('id', models, 'content');
}

function mockTemplateService(state, reject) {
  return {
    create: sinon.spy(() => {
      if (reject) {
        return PromiseStub.reject({status: state});
      }
      return PromiseStub.resolve({status: state});
    })
  }
}

function getNotificationServiceMock() {
  return {
    success: sinon.spy(() => {
    }),
    warning: sinon.spy(),
    error: sinon.spy()
  }
}

function getTranslateServiceMock() {
  return {
    translateInstant: () => {
      return 'translated';
    }
  }
}