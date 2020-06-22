import {ModelSelect} from 'administration/model-management/components/controls/select/model-select';

describe('ModelSelect', () => {

  let modelSelect;

  beforeEach(() => {
    modelSelect = new ModelSelect();
    modelSelect.onSelect = sinon.spy();
  });

  it('should call provided select component event', () => {
    modelSelect.onSelectButton();
    expect(modelSelect.onSelect.calledOnce).to.be.true;
  });
});