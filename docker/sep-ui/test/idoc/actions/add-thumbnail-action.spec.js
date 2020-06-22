import {AddThumbnailAction} from 'idoc/actions/add-thumbnail-action';
import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';
import {ActionsService} from 'services/rest/actions-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {Logger} from 'services/logging/logger';
import {DialogService} from 'components/dialog/dialog-service';
import {PickerService} from 'services/picker/picker-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {IDOC_PAGE_ACTIONS_PLACEHOLDER} from 'idoc/idoc-constants';
import {ValidationModelBuilder} from 'test/form-builder/validation-model-builder';
import {ViewModelBuilder} from 'test/form-builder/view-model-builder';
import {IdocContext} from 'idoc/idoc-context';
import {InstanceObject} from 'models/instance-object';
import {Eventbus} from 'services/eventbus/eventbus';
import {RelatedObject} from 'models/related-object';

describe('AddThumbnailAction', () => {

  const CURRENT_OBJECT_ID = 'emf:0001';

  // constants for hasAttachment property of current object
  const HAS_THUMBNAIL_PROPERTY_NAME = 'hasThumbnail';
  const HAS_THUMBNAIL_PROPERTY_VALUE = [createValue(1)];
  const HAS_THUMBNAIL_PROPERTY_LABEL = 'Has thumbnail';
  const HAS_THUMBNAIL_PROPERTY_URI = 'emf:hasThumbnail';

  let action;
  let dialogService;
  let pickerService;
  let actionsService;
  let notificationService;
  let translateService;
  let logger;
  let instanceRestService;
  let eventbus;

  beforeEach(() => {
    dialogService = stub(DialogService);
    pickerService = stub(PickerService);
    actionsService = stub(ActionsService);
    notificationService = stub(NotificationService);
    translateService = stub(TranslateService);
    logger = stub(Logger);
    eventbus = stub(Eventbus);

    instanceRestService = stub(InstanceRestService);
    instanceRestService.loadInstanceObject.returns(PromiseStub.resolve(new InstanceObject('id1', { viewModel: {}, validationModel: {}})));

    actionsService.addThumbnail.returns(PromiseStub.resolve());
    action = new AddThumbnailAction(dialogService, pickerService, actionsService, notificationService, translateService, logger, PromiseStub, instanceRestService, eventbus);
  });

  describe('execute', () => {
    it('should execute action if thumbnail is changed', () => {
      let currentObject = createCurrentObject(false);
      let idocContext = stub(IdocContext);
      idocContext.getCurrentObject.returns(PromiseStub.resolve(currentObject));
      let actionContext = {
        idocContext,
        currentObject,
        placeholder: IDOC_PAGE_ACTIONS_PLACEHOLDER
      };

      let newThumbnail = createValue(1);
      pickerService.configureAndOpen.returns(PromiseStub.resolve([newThumbnail]));

      action.execute({}, actionContext).then(() => {
        expect(actionsService.addThumbnail.calledOnce).to.be.true;
        expect(notificationService.success.calledOnce).to.be.true;
        expect(eventbus.publish.calledOnce).to.be.true;
      });
    });

    it('should reject action if thumbnail not changed', () => {
      let idocContext = stub(IdocContext);
      let currentObject = createCurrentObject(false);
      idocContext.getCurrentObject.returns(PromiseStub.resolve(currentObject));
      let actionContext = {
        idocContext,
        currentObject,
        placeholder: IDOC_PAGE_ACTIONS_PLACEHOLDER
      };
      pickerService.configureAndOpen.returns(PromiseStub.resolve([]));

      action.execute({}, actionContext).then(() => {
        expect.fail(null, null, 'Add thumbnail operation should be unsuccessful!');
      });
    });
  });

  describe('loadCurrentObjectProperties', () => {
    it('should load currentObject if context placeholder is not idoc page', () => {
      let viewModel = new ViewModelBuilder()
        .addField('propertyName', 'EDITABLE', 'text', undefined, false, false, [], undefined, undefined, false, undefined)
        .getModel();
      let validationModel = new ValidationModelBuilder()
        .addProperty('propertyName', 'propertyValue')
        .getModel();

      let models = {
        viewModel,
        validationModel
      };

      instanceRestService.loadInstanceObject.returns(PromiseStub.resolve(new InstanceObject(CURRENT_OBJECT_ID, models)));

      let currentObject = createCurrentObject(true);
      let actionContext = {
        currentObject
      };

      action.loadInstanceObjectProperties(actionContext).then((loadedCurrentObject) => {
        expect(loadedCurrentObject.id).to.be.equal(CURRENT_OBJECT_ID);
        expect(models).to.be.equal(loadedCurrentObject.models);
      });
    });

    it('should not load currentObject if context placeholder is idoc page', () => {
      let idocContext = stub(IdocContext);
      let currentObject = createCurrentObject(true);
      idocContext.getCurrentObject.returns(PromiseStub.resolve(currentObject));
      let actionContext = {
        idocContext,
        currentObject,
        placeholder: IDOC_PAGE_ACTIONS_PLACEHOLDER
      };

      action.loadInstanceObjectProperties(actionContext).then(() => {
        expect(actionContext.currentObject).to.be.equal(currentObject);
      });
    });
  });

  describe('confirmActionExecution', () => {
    it('should not open confirm dialog if current object has not thumbnail', () => {
      let currentObject = createCurrentObject(false);
      let openConfirmDialogStub = sinon.stub(action, 'openConfirmDialog');

      action.confirmActionExecution(currentObject);
      expect(openConfirmDialogStub.calledOnce).to.be.false;

      openConfirmDialogStub.restore();
    });

    it('should open confirm dialog if current object has thumbnail', () => {
      let currentObject = createCurrentObject(true);
      let openConfirmDialogStub = sinon.stub(action, 'openConfirmDialog');

      action.confirmActionExecution(currentObject);
      expect(openConfirmDialogStub.calledOnce).to.be.true;

      openConfirmDialogStub.restore();
    });
  });

  it('should open confirm dialog with proper parameters', () => {
    let expectedMessage = 'confirm message';
    let expectedConfigurationOfButton = [{
      id: DialogService.YES,
      label: 'dialog.button.yes',
      cls: 'btn-primary'
    }, {
      id: DialogService.CANCEL,
      label: 'dialog.button.cancel'
    }];

    translateService.translateInstant.withArgs('action.add.thumbnail.existing').returns(expectedMessage);

    action.openConfirmDialog();

    let message = dialogService.confirmation.args[0][0];
    let header = dialogService.confirmation.args[0][1];
    let buttons = dialogService.confirmation.args[0][2].buttons;

    expect(message).to.equal(expectedMessage);
    expect(header === undefined).to.be.true;
    expect(buttons).to.deep.equal(expectedConfigurationOfButton);

  });

  it('should return old and new thumbnail', () => {
    let currentObject = createCurrentObject(true);
    let newThumbnail = createValue(4);
    pickerService.configureAndOpen.returns(PromiseStub.resolve([newThumbnail]));

    let result = action.selectThumbnail(undefined, currentObject);

    result.then((data) => {
      expect(data.oldThumbnail).to.deep.equal(HAS_THUMBNAIL_PROPERTY_VALUE[0]);
      expect(data.newThumbnail).to.deep.equal(newThumbnail);
    });
  });

  it('should call resolve callback when \'Ok\' button of confirm dialog is clicked', () => {
    let resolve = sinon.spy();
    let reject = sinon.spy();
    let dialogConfig = {
      dismiss: sinon.spy()
    };
    let confirmationDialogConfig = action.getConfirmationDialogConfig(resolve, reject);

    confirmationDialogConfig.onButtonClick(DialogService.YES, undefined, dialogConfig);

    expect(resolve.calledOnce).to.be.true;
    expect(reject.calledOnce).to.be.false;
    expect(dialogConfig.dismiss.calledOnce).to.be.true;
  });

  it('should call reject callback when \'Cancel\' button of confirm dialog is clicked', () => {
    let resolve = sinon.spy();
    let reject = sinon.spy();
    let dialogConfig = {
      dismiss: sinon.spy()
    };
    let confirmationDialogConfig = action.getConfirmationDialogConfig(resolve, reject);

    confirmationDialogConfig.onButtonClick(DialogService.CANCEL, undefined, dialogConfig);

    expect(resolve.calledOnce).to.be.false;
    expect(reject.calledOnce).to.be.true;
    expect(dialogConfig.dismiss.calledOnce).to.be.true;
  });

  it('should create picker configuration', () => {
    let expectedPickerConfiguration = {
      header: 'action.add.thumbnail.header',
      extensions: {
        'seip-object-picker-search': {
          predefinedTypes: ['emf:Image'],
          results: {
            config: {
              selectedItems: []
            }
          }
        }
      },
      tabs: {
        'seip-object-picker-basket': {
          label: HAS_THUMBNAIL_PROPERTY_LABEL
        }
      }
    };

    let pickerConfig = AddThumbnailAction.getPickerConfiguration(HAS_THUMBNAIL_PROPERTY_LABEL);

    expect(pickerConfig).to.deep.equal(expectedPickerConfiguration);
  });

  it('should call actions Service with properly request data', () => {
    let expectedRequestData = {
      instanceId: CURRENT_OBJECT_ID,
      thumbnailObjectId: HAS_THUMBNAIL_PROPERTY_VALUE[0].id
    };
    actionsService.addThumbnail.returns(Promise.resolve());
    action.addThumbnail(createCurrentObject(true), HAS_THUMBNAIL_PROPERTY_VALUE[0]);

    expect(actionsService.addThumbnail.calledOnce).to.be.true;
    expect(actionsService.addThumbnail.args[0][0]).to.deep.equal(expectedRequestData);
  });


  it('should return basket label when current object has thumbnail', () => {
    let currentObject = createCurrentObject(true);

    expect(AddThumbnailAction.getBasketLabel(currentObject)).to.deep.equal(HAS_THUMBNAIL_PROPERTY_LABEL);
  });

  describe('getThumbnail', () => {
    it('should return thumbnail when current object has thumbnail', () => {
      let currentObject = createCurrentObject(true);

      expect(AddThumbnailAction.getThumbnail(currentObject)).to.deep.equal(HAS_THUMBNAIL_PROPERTY_VALUE);
    });

    it('should not return thumbnail when current object has thumbnail', () => {
      let currentObject = createCurrentObject(false);

      expect(AddThumbnailAction.getThumbnail(currentObject) === undefined).to.be.true;
    });
  });

  function createCurrentObject(withThumbnail) {
    let viewModel = new ViewModelBuilder()
      .addField(HAS_THUMBNAIL_PROPERTY_NAME, 'EDITABLE', 'text', HAS_THUMBNAIL_PROPERTY_LABEL, false, false, [], undefined, undefined, false, HAS_THUMBNAIL_PROPERTY_URI)
      .getModel();
    let models = {
      viewModel
    };
    if (withThumbnail) {
      models.validationModel = new ValidationModelBuilder()
        .addProperty(HAS_THUMBNAIL_PROPERTY_NAME, new RelatedObject({results: [1]}))
        .getModel();
    }
    return new InstanceObject(CURRENT_OBJECT_ID, models, null, null);
  }

  function createValue(id) {
    return {id};
  }
});