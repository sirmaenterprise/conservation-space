import {ChangePasswordDialog} from 'user/change-password/change-password-dialog';

describe('ChangePasswordDialog', () => {

  it('should subscribe to AfterFormValidationEvent when component is created', () => {
    let eventbus = {
      subscribe: sinon.spy()
    };
    let translateService = {
      translateInstant: () => {}
    };
    ChangePasswordDialog.prototype.config = {};
    new ChangePasswordDialog(translateService, eventbus);
    expect(eventbus.subscribe.calledOnce).to.be.true;
  });

  it('should unsubscribe from events when component is destroyed', () => {
    let spyUnsubscribe = {
      unsubscribe: sinon.spy()
    };
    let eventbus = {
      subscribe: () => {
        return spyUnsubscribe
      }
    };
    let translateService = {
      translateInstant: () => {}
    };
    ChangePasswordDialog.prototype.config = {};
    let dialog = new ChangePasswordDialog(translateService, eventbus);
    dialog.ngOnDestroy();
    expect(spyUnsubscribe.unsubscribe.calledOnce).to.be.true;
  });
});