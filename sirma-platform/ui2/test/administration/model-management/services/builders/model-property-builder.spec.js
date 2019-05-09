import {ModelPropertyBuilder} from 'administration/model-management/services/builders/model-property-builder';
import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';
import {ModelDescriptionLinker} from 'administration/model-management/services/linkers/model-description-linker';

import {ModelValue} from 'administration/model-management/model/model-value';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';

import {stub} from 'test/test-utils';

describe('ModelPropertyBuilder', () => {

  let modelPropertyBuilder;
  let modelAttributeLinkerStub;
  let modelDescriptionLinkerStub;

  beforeEach(() => {
    modelAttributeLinkerStub = stub(ModelAttributeLinker);
    modelDescriptionLinkerStub = stub(ModelDescriptionLinker);
    modelPropertyBuilder = new ModelPropertyBuilder(modelAttributeLinkerStub, modelDescriptionLinkerStub);
  });

  it('should properly build properties from a provided response', () => {
    let properties = modelPropertyBuilder.buildProperties(getProperties(), {});

    expect(properties.getModels().length).to.eq(3);
    expect(modelAttributeLinkerStub.linkAttributes.callCount).to.equal(3);
    expect(modelDescriptionLinkerStub.insertDescriptions.callCount).to.equal(3);

    expect(properties.getModel('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title')).to.exist;
    expect(properties.getModel('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#emailAddress')).to.exist;
    expect(properties.getModel('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#description')).to.exist;
  });

  it('should properly attach the related domain to the property', () => {
    modelAttributeLinkerStub.linkAttributes = linkAttributes;
    let properties = modelPropertyBuilder.buildProperties(getProperties(), {});

    expect(properties.getModels().length).to.eq(3);
    expect(properties.getModel('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title').getParent()).to.eq('ptop:Entity');
    expect(properties.getModel('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#emailAddress').getParent()).to.eq('emf:Activity');
    expect(properties.getModel('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#description').getParent()).to.eq('skos:Definition');
  });

  function getProperties() {
    return [{
      id: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title',
      attributes: [{
        name: ModelAttribute.DOMAIN_ATTRIBUTE,
        type: 'uri',
        value: 'ptop:Entity'
      }]
    }, {
      id: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#emailAddress',
      attributes: [{
        name: ModelAttribute.DOMAIN_ATTRIBUTE,
        type: 'uri',
        value: 'emf:Activity'
      }]
    }, {
      id: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#description',
      attributes: [{
        name: ModelAttribute.DOMAIN_ATTRIBUTE,
        type: 'uri',
        value: 'skos:Definition'
      }]
    }];
  }

  function linkAttributes(model, attributes) {
    model.addAttribute(new ModelSingleAttribute(ModelAttribute.DOMAIN_ATTRIBUTE, 'string', new ModelValue('en', attributes[0].value)));
  }
});