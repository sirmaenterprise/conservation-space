import {ModelHierarchyBuilder} from 'administration/model-management/services/builders/model-hierarchy-builder';
import {ModelDescriptionLinker} from 'administration/model-management/services/linkers/model-description-linker';

import {stub} from 'test/test-utils';

describe('ModelHierarchyBuilder', () => {

  let modelHierarchyBuilder;
  let modelDescriptionLinkerStub;

  beforeEach(() => {
    modelDescriptionLinkerStub = stub(ModelDescriptionLinker);
    modelHierarchyBuilder = new ModelHierarchyBuilder(modelDescriptionLinkerStub);
  });

  it('should properly built tree representation of hierarchy based on provided data', () => {
    let hierarchy = modelHierarchyBuilder.buildHierarchy(getHierarchy());

    let root = hierarchy[0];
    // the base root of the hierarchy is a class model
    expect(root.getRoot().getId()).to.eq('emf:Entity');

    let children = root.getChildren();
    // child models of the emf:entity class which is root
    expect(children[0].getRoot().getId()).to.eq('entity');
    expect(children[1].getRoot().getId()).to.eq('emf:Object');

    // child models of the entity model definition
    children = root.getChildren()[0].getChildren();
    expect(children[0].getRoot().getId()).to.eq('media');

    // child models of the emf:object class
    children = root.getChildren()[1].getChildren();
    expect(children[0].getRoot().getId()).to.eq('audio');
    expect(children[1].getRoot().getId()).to.eq('video');
  });

  function getHierarchy() {
    return [
      {
        'id': 'emf:Entity',
        'parentId': null,
        'subTypes': [
          {
            'id': 'entity',
            'parentId': null,
            'abstract': true
          }, {
            'id': 'media',
            'parentId': 'entity',
            'abstract': true
          }
        ]
      }, {
        'id': 'emf:Object',
        'parentId': 'emf:Entity',
        'subTypes': [
          {
            'id': 'audio',
            'parentId': 'media',
            'abstract': false
          }, {
            'id': 'video',
            'parentId': 'media',
            'abstract': false
          }
        ]
      }
    ];
  }

});