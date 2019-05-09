import {ModelCancel} from 'administration/model-management/components/controls/cancel/model-cancel';

describe('ModelCancel', () => {

  let modelCancel;

  beforeEach(() => {
    modelCancel = new ModelCancel();
    modelCancel.onCancel = sinon.spy();
  });

  it('should provide a default configuration', () => {
    expect(modelCancel.config).to.deep.eq({
      label: 'administration.models.management.cancel.changes'
    });
  });

  it('should call provided cancel component event', () => {
    modelCancel.onCancelButton();
    expect(modelCancel.onCancel.calledOnce).to.be.true;
  });
});