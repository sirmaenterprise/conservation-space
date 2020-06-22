import {CodeDescriptions} from 'administration/code-lists/manage/code-descriptions';
import {PREVIEW, EDIT} from 'administration/code-lists/manage/code-manage-modes';

describe('CodeDescriptions', () => {

  let descriptions;

  beforeEach(() => {
    descriptions = new CodeDescriptions();
    descriptions.descriptions = {};
    descriptions.onChange = sinon.spy();
  });

  it('should call on change and modify value when model change', () => {
    let value = {isModified: false};
    descriptions.onModelChange(value);

    expect(value.isModified).to.be.true;
    expect(descriptions.onChange.calledOnce).to.be.true;
  });

  it('should properly resolve if in preview mode', () => {
    descriptions.mode = PREVIEW;
    expect(descriptions.isPreviewMode()).to.be.true;

    descriptions.mode = EDIT;
    expect(descriptions.isPreviewMode()).to.be.false;
  });

  it('should determine if value\'s field is invalid using the validation model of the value', () => {
    let value = {id: 'APPROVED', name: 'Approved'};
    expect(descriptions.isValueFieldInvalid(value, 'name')).to.be.false;

    value.validationModel = {};
    value.validationModel['name'] = {valid: true};
    expect(descriptions.isValueFieldInvalid(value, 'name')).to.be.false;

    value.validationModel['name'] = {valid: false};
    expect(descriptions.isValueFieldInvalid(value, 'name')).to.be.true;
  });

  it('should provide a sorted sequence of descriptions based on their respective language', () => {
    descriptions.descriptions = {
      'EN': {name: 'English'}, 'BG': {name: 'Bulgarian'}, 'AR': {name: 'Arabic'}
    };
    expect(descriptions.getDescriptions()).to.deep.eq([{name: 'Arabic'}, {name: 'Bulgarian'}, {name: 'English'}]);
  });
});