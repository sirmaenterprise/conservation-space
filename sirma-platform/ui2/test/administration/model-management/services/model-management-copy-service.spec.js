import {ModelManagementCopyService} from 'administration/model-management/services/model-management-copy-service';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelMultiAttribute} from 'administration/model-management/model/attributes/model-multi-attribute';
import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelRegion} from 'administration/model-management/model/model-region';
import {ModelValue} from 'administration/model-management/model/model-value';
import {ModelPath} from 'administration/model-management/model/model-path';

const LABEL = ModelAttributeTypes.MULTI_VALUE.MODEL_LABEL_TYPE;
const INTEGER = ModelAttributeTypes.SINGLE_VALUE.MODEL_INTEGER_TYPE;

describe('ModelManagementCopyService', () => {

  let modelManagementCopyService;

  beforeEach(() => {
    modelManagementCopyService = new ModelManagementCopyService();
  });

  it('should restore all inherited model nodes from a path to a given model', () => {
    let path = getPath();
    let model = getModel();

    // collect the testing models before the restore is done
    let inheritedRegion = model.getRegion('region1');
    let inheritedField = model.getField('field2');
    let inheritedAttribute = inheritedField.getAttribute('value');

    // Mock overridden region, field and attribute models
    // since all of them have only a single overridden model
    // they can be restored completely from the parent
    let newRegion = new ModelRegion('region1').setParent(model);
    let newField = new ModelField('field2').setParent(model).setRegionId(newRegion.getId());
    let newAttribute = new ModelSingleAttribute('value', INTEGER).setParent(newField);

    model.addField(newField);
    model.addRegion(newRegion);
    newField.addAttribute(newAttribute);

    modelManagementCopyService.restoreFromPath(path, model);

    // collect the same models after the copying is done
    let restoredRegion = model.getRegion('region1');
    let restoredField = model.getField('field2');
    let restoredAttribute = restoredField.getAttribute('value');

    // restored models should be restored from the inherited
    expect(restoredRegion).to.equal(inheritedRegion);
    expect(restoredField).to.equal(inheritedField);
    expect(restoredAttribute).to.equal(inheritedAttribute);
  });

  it('should copy all inherited model nodes from a path to a given model', () => {
    let path = getPath();
    let model = getModel();

    // collect the testing models before the copying is done
    let inheritedRegion = model.getRegion('region1');
    let inheritedField = model.getField('field2');
    let inheritedAttribute = inheritedField.getAttribute('value');

    modelManagementCopyService.copyFromPath(path, model);

    // collect the same models after the copying is done
    let overriddenRegion = model.getRegion('region1');
    let overriddenField = model.getField('field2');
    let overriddenAttribute = overriddenField.getAttribute('value');

    // overridden models should be copied from the inherited
    expect(overriddenRegion).to.not.equal(inheritedRegion);
    expect(overriddenField).to.not.equal(inheritedField);
    expect(overriddenAttribute).to.not.equal(inheritedAttribute);

    // overridden models should have newly assigned owners
    expect(overriddenRegion.getParent()).to.equal(model);
    expect(overriddenField.getParent()).to.equal(model);
    expect(overriddenAttribute.getParent()).to.equal(overriddenField);

    // not overridden label attribute should remain the same
    expect(overriddenRegion.getAttributeByType(LABEL)).to.equal(inheritedRegion.getAttributeByType(LABEL));
    expect(overriddenField.getAttributeByType(LABEL)).to.equal(inheritedField.getAttributeByType(LABEL));

    // links to the description of the model should remain the same
    expect(overriddenRegion.getDescription()).to.equal(overriddenRegion.getAttributeByType(LABEL).getValue());
    expect(overriddenField.getDescription()).to.equal(overriddenField.getAttributeByType(LABEL).getValue());
  });

  function getPath() {
    let definitionNode = new ModelPath('definition', 'PR001');
    let fieldNode = new ModelPath('field', 'field2');
    let attributeNode = new ModelPath('attribute', 'value');

    attributeNode.setNext(null);
    fieldNode.setNext(attributeNode);
    definitionNode.setNext(fieldNode);
    return definitionNode;
  }

  function getModel() {
    let coreParent = new ModelDefinition('parent-model');
    let model = new ModelDefinition('PR001').setParent(coreParent);

    // Mock both field and region as if they are inherited

    let regionInDefinition = new ModelRegion('region1').setParent(coreParent);
    let fieldInRegion = new ModelField('field2').setParent(coreParent);

    // Append an inherited region & field to the definitions

    model.addField(fieldInRegion);
    coreParent.addField(fieldInRegion);

    model.addRegion(regionInDefinition);
    coreParent.addRegion(regionInDefinition);

    let multiAttribute = new ModelMultiAttribute('name', LABEL).setParent(regionInDefinition);
    let singleAttribute = new ModelSingleAttribute('value', INTEGER).setParent(regionInDefinition);

    multiAttribute.addValue(new ModelValue('en', 'general'));
    multiAttribute.addValue(new ModelValue('bg', 'общи'));

    singleAttribute.setValue(new ModelValue('en', 'region-value'));
    multiAttribute.setValue(multiAttribute.getValueByLanguage('en'));

    regionInDefinition.addAttribute(singleAttribute);
    regionInDefinition.addAttribute(multiAttribute);

    // Append an inherited field to the region in the definition

    fieldInRegion.setRegionId(regionInDefinition.getId());

    multiAttribute = new ModelMultiAttribute('name', LABEL).setParent(fieldInRegion);
    singleAttribute = new ModelSingleAttribute('value', INTEGER).setParent(fieldInRegion);

    multiAttribute.addValue(new ModelValue('en', 'type'));
    multiAttribute.addValue(new ModelValue('bg', 'тип'));

    singleAttribute.setValue(new ModelValue('en', 'field-value'));
    multiAttribute.setValue(multiAttribute.getValueByLanguage('en'));

    fieldInRegion.addAttribute(singleAttribute);
    fieldInRegion.addAttribute(multiAttribute);

    return model;
  }
});