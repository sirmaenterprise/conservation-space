import {ModelPathBuilder} from 'administration/model-management/services/builders/model-path-builder';

import {ModelPath} from 'administration/model-management/model/model-path';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';

describe('ModelPathBuilder', () => {

  let modelPathBuilder;

  beforeEach(() => {
    modelPathBuilder = new ModelPathBuilder();
  });

  it('should build a model path from a string', () => {
    let path = modelPathBuilder.buildPathFromString(getStringPath());
    expect(path).to.deep.eq(getModelPath());
  });

  it('should build a path from a model', () => {
    let path = modelPathBuilder.buildPathFromModel(getModelAttribute());
    expect(path).to.deep.eq(getModelPath());
  });

  it('should build a string from a path', () => {
    let path = modelPathBuilder.buildStringFromPath(getModelPath());
    expect(path).to.deep.eq(getStringPath());
  });

  function getModelAttribute() {
    let definition = new ModelDefinition('PR0001');
    let attribute = new ModelSingleAttribute('abstract');
    definition.addAttribute(attribute) && attribute.setParent(definition);
    return attribute;
  }

  function getModelPath() {
    let root = new ModelPath('definition', 'PR0001');
    let child = new ModelPath('attribute', 'abstract');
    root.setNext(child) && child.setPrevious(root);
    return root;
  }

  function getStringPath() {
    return 'definition=PR0001/attribute=abstract';
  }
});