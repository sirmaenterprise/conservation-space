import {ModelDetails} from 'administration/model-management/sections/field/model-details';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelRegion} from 'administration/model-management/model/model-region';
import {ModelProperty} from 'administration/model-management/model/model-property';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelDescription, ModelValue} from 'administration/model-management/model/model-value';
import {ModelMultiAttribute} from 'administration/model-management/model/attributes/model-multi-attribute';

describe('ModelDetails', () => {

  let details;
  let eventStub;

  beforeEach(() => {
    details = new ModelDetails();
    details.onAttributeChange = sinon.spy();
    details.onAttributeLoad = sinon.spy();
    details.onModelNavigate = sinon.spy();
    eventStub = {stopPropagation: sinon.spy()};
  });

  it('should properly resolve model field title when label is present', () => {
    let property = new ModelProperty('emf:status');
    let field = new ModelField('status').setProperty(property);
    let label = new ModelMultiAttribute().setType('label').addValue(new ModelValue('en'));

    field.addAttribute(label);
    label.setValue(label.getValueByLanguage('en'));
    property.setDescription(new ModelDescription('en', 'Semantic Status'));

    // when а label is present then prioritize that instead of semantic
    label.getValue().setValue('Status From Label');
    expect(details.getModelTitle(field)).to.equal('Status From Label');

    // when а label is empty prioritize the semantic one
    label.getValue().setValue('');
    expect(details.getModelTitle(field)).to.equal('Semantic Status');
  });

  it('should properly resolve model field title when description is present', () => {
    let property = new ModelProperty('emf:status');
    let field = new ModelField('status').setProperty(property);
    property.setDescription(new ModelDescription('en', 'Semantic Status'));

    // when not present at all
    field.setDescription(null);
    expect(details.getModelTitle(field)).to.equal('Semantic Status');

    // when the description is empty use semantic one
    field.setDescription(new ModelDescription('en', ''));
    expect(details.getModelTitle(field)).to.equal('Semantic Status');

    // when а description is present prioritize the semantic one
    field.setDescription(new ModelDescription('en', 'Status'));
    expect(details.getModelTitle(field)).to.equal('Semantic Status');
  });

  it('should determine if the model is a field or not', () => {
    details.model = new ModelField();
    expect(details.isField()).to.be.true;

    details.model = new ModelRegion();
    expect(details.isField()).to.be.false;
  });

  it('should determine if the model is a property or not', () => {
    details.model = new ModelProperty();
    expect(details.isProperty()).to.be.true;

    details.model = new ModelRegion();
    expect(details.isProperty()).to.be.false;
  });

  it('should resolve if the provided field is editable from the section', () => {
    details.context = new ModelDefinition();

    details.model = new ModelField();
    details.model.setParent(details.context);
    expect(details.isEditable()).to.be.undefined;

    details.model = new ModelRegion();
    details.model.setParent(details.context);
    expect(details.isEditable()).to.be.undefined;

    details.model = new ModelProperty();
    details.model.setParent(new ModelDefinition());
    expect(details.isEditable()).to.be.false;

    details.model = new ModelProperty();
    details.model.setParent(details.context);
    expect(details.isEditable()).to.be.undefined;
  });

  it('should call component event on field attribute change', () => {
    details.onFieldAttributeChanged();
    expect(details.onAttributeChange.called).to.be.true;
  });

  it('should call component event on property attribute change', () => {
    details.onPropertyAttributeChanged();
    expect(details.onAttributeChange.called).to.be.true;
  });

  it('should call component event on model navigation from field', () => {
    let property = new ModelProperty();
    details.model = new ModelField().setProperty(property);

    details.onModelFieldNavigated(eventStub);
    expect(details.onModelNavigate.called).to.be.true;
    expect(details.onModelNavigate.calledWith({model: details.model})).to.be.true;
    expect(eventStub.stopPropagation.called).to.be.true;
  });

  it('should call component event on model navigation from property', () => {
    let property = new ModelProperty();
    details.model = new ModelField().setProperty(property);

    details.onModelPropertyNavigated(eventStub);
    expect(details.onModelNavigate.called).to.be.true;
    expect(details.onModelNavigate.calledWith({model: property})).to.be.true;
    expect(eventStub.stopPropagation.called).to.be.true;
  });
});