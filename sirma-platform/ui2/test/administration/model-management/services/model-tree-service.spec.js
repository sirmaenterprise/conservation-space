import {ModelTreeService} from 'administration/model-management/services/model-tree-service';

import {ModelBase} from 'administration/model-management/model/model-base';
import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelDescription} from 'administration/model-management/model/model-value';
import {ModelClassHierarchy, ModelDefinitionHierarchy} from 'administration/model-management/model/model-hierarchy';

describe('ModelTreeService', () => {

  let modelTreeService;

  beforeEach(() => {
    modelTreeService = new ModelTreeService();
  });

  it('should build & sort tree data from a hierarchy model', () => {
    let tree = modelTreeService.getTree(getModelsHierarchy());

    let root = tree[0];
    assertTreeItem(root, 'entity', 'Entity', false);
    // during sorting definitions should be grouped before classes
    assertTreeItem(root.children[0], 'media', 'Media', false);

    // during sorting  classes should be after grouped the definitions
    assertTreeItem(root.children[1], 'collection', 'Collection', true);
    assertTreeItem(root.children[2], 'event', 'Event', true);
    assertTreeItem(root.children[3], 'object', 'Object', true);

    let media = root.children[0];
    // get the children of the media definition and assert them
    assertTreeItem(media.children[0], 'audio', 'Audio', true);
    assertTreeItem(media.children[1], 'video', 'Video', true);
  });

  it('should build a tree item from a hierarchy model', () => {
    let model = new ModelClass('model');
    let enDescription = new ModelDescription('en', 'Name');

    model.setIcon('icon');
    model.setDescription(enDescription);

    let modelClassHierarchy = new ModelClassHierarchy(model);

    let base = [];
    modelTreeService.buildTreeItem(modelClassHierarchy, base);
    expect(base[0]).to.deep.eq({
      dbId: 'model',
      text: 'Name',
      icon: 'icon',
      children: [],
      leaf: true
    });
  });

  it('should find path to the root node', () => {
    expect(modelTreeService.findRootNodePath(getModelsTree())).to.deep.eq(['entity']);
    expect(modelTreeService.findRootNodePath(getModelsTree(), 1)).to.deep.eq(['person']);
  });

  it('should find path to a node by id', () => {
    expect(modelTreeService.findNodePathById(getModelsTree(), 'male')).to.deep.eq(['person', 'male']);
    expect(modelTreeService.findNodePathById(getModelsTree(), 'video')).to.deep.eq(['entity', 'media', 'video']);
  });

  function getModelsTree() {
    return [{
      dbId: 'entity',
      children: [{
        dbId: 'media',
        children: [{
          dbId: 'audio',
          children: []
        }, {
          dbId: 'video',
          children: []
        }]
      }, {
        dbId: 'object',
        children: []
      }]
    }, {
      dbId: 'person',
      children: [{
        dbId: 'male',
        children: []
      }]
    }];
  }

  function getModelsHierarchy() {
    let root = new ModelClassHierarchy(createModel('entity', 'Entity', 'Обект'));

    root.insertChild(new ModelClassHierarchy(createModel('event', 'Event', 'Събитие', 'b-icon')));
    root.insertChild(new ModelClassHierarchy(createModel('object', 'Object', 'Обект', 'b-icon')));
    root.insertChild(new ModelClassHierarchy(createModel('collection', 'Collection', 'Колекция', 'b-icon')));
    root.insertChild(new ModelDefinitionHierarchy(createModel('media', 'Media', 'Медия', 'a-icon')));

    let media = root.getChildren()[3];
    media.insertChild(new ModelDefinitionHierarchy(createModel('video', 'Video', 'Видео', 'a-icon')));
    media.insertChild(new ModelDefinitionHierarchy(createModel('audio', 'Audio', 'Аудио', 'a-icon')));

    return root;
  }

  function createModel(id, en, bg, icon) {
    let model = new ModelBase(id);
    let enDescription = new ModelDescription('en', en);
    let bgDescription = new ModelDescription('bg', bg);

    model.setIcon(icon);
    model.setDescription(enDescription);
    model.addDescription(enDescription);
    model.addDescription(bgDescription);

    return model;
  }

  function assertTreeItem(item, id, name, leaf) {
    expect(item.dbId).to.eq(id);
    expect(item.text).to.eq(name);
    expect(item.leaf).to.eq(leaf);
  }
});