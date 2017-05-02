import {SaveIdocDialog} from 'idoc/save-idoc-dialog/save-idoc-dialog';
import {FormWrapper} from 'form-builder/form-wrapper';

describe('SaveIdocDialog', () => {

  it('should subscribe to AfterFormValidationEvent when component is created', () => {
    let eventbus = {
      subscribe: sinon.spy()
    };
    SaveIdocDialog.prototype.config = {};
    let dialog = new SaveIdocDialog(eventbus);
    expect(eventbus.subscribe.calledOnce).to.be.true;
    // should set view mode for the form to EDIT
    expect(dialog.config.formViewMode).to.equal(FormWrapper.FORM_VIEW_MODE_EDIT)
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
    SaveIdocDialog.prototype.config = {};
    let dialog = new SaveIdocDialog(eventbus);
    dialog.ngOnDestroy();
    expect(spyUnsubscribe.unsubscribe.calledOnce).to.be.true;
  });
});