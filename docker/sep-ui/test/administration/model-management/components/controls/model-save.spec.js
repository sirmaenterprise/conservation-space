import {ModelSave} from 'administration/model-management/components/controls/save/model-save';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ModelSave', () => {

  let modelSave;
  let translateServiceStub;
  let notificationServiceStub;

  beforeEach(() => {
    translateServiceStub = stub(TranslateService);
    notificationServiceStub = stub(NotificationService);
    translateServiceStub.translateInstant.returns('success');

    modelSave = new ModelSave(notificationServiceStub, translateServiceStub);
    modelSave.onSave = sinon.spy(() => PromiseStub.resolve({}));
    modelSave.ngOnInit();
  });

  it('should provide a default configuration', () => {
    expect(modelSave.config).to.deep.eq({
      primary: true,
      label: 'administration.models.management.save.changes'
    });
  });

  it('should translate and set save message on init', () => {
    expect(modelSave.message).to.eq('success');
  });

  it('should call provided save component event', () => {
    modelSave.onSaveButton();

    expect(modelSave.onSave.calledOnce).to.be.true;
    expect(notificationServiceStub.success.calledOnce).to.be.true;
    expect(notificationServiceStub.success.calledWith('success')).to.be.true;
  });
});