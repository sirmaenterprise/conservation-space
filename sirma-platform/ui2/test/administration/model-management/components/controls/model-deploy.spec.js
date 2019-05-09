import {ModelDeploy} from 'administration/model-management/components/controls/deploy/model-deploy';
import {ModelList} from 'administration/model-management/model/model-list';
import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelDescription} from 'administration/model-management/model/model-value';
import {ModelDeployRequest} from 'administration/model-management/model/request/model-deploy-request';
import {ModelValidationReport} from 'administration/model-management/model/validation/model-validation-report';
import {ModelListValidationView} from 'administration/model-management/components/list/model-list-validation-view';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {DialogService} from 'components/dialog/dialog-service';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ModelDeploy', () => {

  let modelDeploy;
  let dialogServiceStub;
  let translateServiceStub;
  let notificationServiceStub;

  let deploymentRequest;

  beforeEach(() => {
    dialogServiceStub = stub(DialogService);
    translateServiceStub = stub(TranslateService);
    notificationServiceStub = stub(NotificationService);

    dialogServiceStub.createButton.returns({});
    translateServiceStub.translateInstant.returns('success');

    modelDeploy = new ModelDeploy(dialogServiceStub, notificationServiceStub, translateServiceStub);
    modelDeploy.ngOnInit();
    modelDeploy.enabled = true;

    deploymentRequest = new ModelDeployRequest();
    deploymentRequest.setModels(new ModelList().insert(getModelClass('1')).insert(getModelClass('2')));
    deploymentRequest.setSelectedModels(new ModelList().insert(deploymentRequest.getModels().getModel('1')));
    deploymentRequest.setValidationReport(new ModelValidationReport());

    modelDeploy.onDeployRequested = sinon.spy(() => PromiseStub.resolve(deploymentRequest));
    modelDeploy.onDeployConfirmed = sinon.spy(() => PromiseStub.resolve());
  });

  function getModelClass(id) {
    let modelClass = new ModelClass(id);
    modelClass.setDescription(new ModelDescription('en', id));
    return modelClass;
  }

  it('should provide a default configuration', () => {
    expect(modelDeploy.config).to.deep.eq({
      primary: true,
      label: 'administration.models.management.deploy.changes'
    });
  });

  it('should open and create dialog on model deploy request', () => {
    modelDeploy.onModelDeploy();
    expect(modelDeploy.deployConfig).to.exist;
    expect(dialogServiceStub.create.calledWith(ModelListValidationView)).to.be.true;
  });

  it('should not be able to trigger another deploy when one is already in progress', () => {
    modelDeploy.startDeploying();
    modelDeploy.onModelDeploy();
    expect(modelDeploy.deployConfig).to.not.exist;
    expect(dialogServiceStub.create.calledWith(ModelListValidationView)).to.be.false;
  });

  it('should set button state based on selected items', () => {
    let selected = new ModelList().insert(new ModelClass());
    modelDeploy.deployConfig = {config: {selected}};
    modelDeploy.dialogConfig = {buttons: [{}]};

    modelDeploy.onListAction();
    expect(modelDeploy.dialogConfig.buttons[0].disabled).to.be.false;
  });

  it('should build correct model deploy configuration', () => {
    let deployConfig = modelDeploy.getModelDeployConfiguration(deploymentRequest);

    expect(deployConfig.config).to.deep.eq({
      selectableItems: true,
      singleSelection: false,
      selected: deploymentRequest.getSelectedModels()
    });
    expect(deployConfig.onAction).to.exist;
    expect(deployConfig.report instanceof ModelValidationReport).to.be.true;
    expect(deployConfig.models).to.deep.eq(deploymentRequest.getModels());
  });

  it('should build correct modal dialog configuration', () => {
    let config = modelDeploy.getDialogConfiguration(deploymentRequest);

    expect(config.onButtonClick).to.exist;
    expect(config.buttons.length).to.eq(2);
    expect(dialogServiceStub.createButton.calledTwice).to.be.true;
    expect(dialogServiceStub.createButton.calledWith(DialogService.CLOSE)).to.be.true;
    expect(dialogServiceStub.createButton.calledWith(DialogService.CONFIRM)).to.be.true;
  });

  it('should confirm deployment when dialog confirm button is pressed', () => {
    modelDeploy.deploying = true;
    modelDeploy.deployConfig = {config: {selected: deploymentRequest.getSelectedModels()}};

    let config = modelDeploy.getDialogConfiguration(deploymentRequest);
    config.dismiss = sinon.spy();
    config.onButtonClick(DialogService.CONFIRM, {}, config);

    expect(config.dismiss.calledOnce).to.be.true;
    expect(modelDeploy.onDeployConfirmed.calledOnce).to.be.true;
    expect(modelDeploy.onDeployConfirmed.calledWith({deploymentRequest})).to.be.true;

    expect(modelDeploy.deploying).to.be.false;
    expect(notificationServiceStub.success.calledOnce).to.be.true;
    expect(notificationServiceStub.success.calledWith('success')).to.be.true;
  });
});