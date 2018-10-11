import {Configuration} from 'common/application-config';
import {TranslateService} from 'services/i18n/translate-service';
import {ModelManagementService} from 'administration/model-management/services/model-management-service';
import {ModelManagementRestService} from 'administration/model-management/services/model-management-rest-service';

import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ModelManagementService', () => {

  let configurationStub;
  let translateServiceStub;
  let modelManagementService;
  let modelManagementRestServiceStub;

  beforeEach(() => {
    configurationStub = stub(Configuration);
    translateServiceStub = stub(TranslateService);
    modelManagementRestServiceStub = stub(ModelManagementRestService);

    configurationStub.get.returns('BG');
    translateServiceStub.getCurrentLanguage.returns('EN');
    modelManagementRestServiceStub.getModelData.returns(PromiseStub.resolve(getModelData()));
    modelManagementRestServiceStub.getModelsHierarchy.returns(PromiseStub.resolve(getModelsHierarchy()));
    modelManagementRestServiceStub.getModelsMetaData.returns(PromiseStub.resolve(getMetaData()));
    modelManagementRestServiceStub.getModelProperties.returns(PromiseStub.resolve(getProperties()));

    modelManagementService = new ModelManagementService(modelManagementRestServiceStub, PromiseStub, configurationStub, translateServiceStub);
  });

  it('should correctly fetch a model and link all related data', () => {
    let provider = null;
    modelManagementService.getHierarchy().then(hierarchy => provider = (id) => hierarchy.flat[id].getRoot());

    let properties = null;
    modelManagementService.getProperties().then(props => properties = props);

    modelManagementService.getModel('media', provider).then(model => {
      modelManagementService.linkModel(model, properties);

      expect(model.isLoaded()).to.be.true;
      expect(model.getParent()).to.equal(provider('entity'));
      expect(model.getType()).to.equal(provider('emf:Entity'));

      // model descriptions should directly reference the values of it's label attribute
      expect(model.getDescriptions()).to.equal(model.getAttribute('label').getValues());
      expect(model.getParent().getDescriptions()).to.equal(provider('entity').getAttribute('label').getValues());
      expect(model.getType().getDescriptions()).to.equal(provider('emf:Entity').getAttribute('http://purl.org/dc/terms/title').getValues());

      // assert attributes directly associated with the model
      assertMultiLangAttributeItem(model.getAttribute('label'), 'label', 'Media', 'Медиа', 'Label', 'Име');
      assertSingleAttributeItem(model.getAttribute('abstract'), 'boolean', false, 'Is abstract', 'Абстрактна');

      assertMultiLangAttributeItem(model.getParent().getAttribute('label'), 'label', 'entity', 'обект', 'Label', 'Име');
      assertSingleAttributeItem(model.getParent().getAttribute('abstract'), 'boolean', true, 'Is abstract', 'Абстрактна');

      assertMultiLangAttributeItem(model.getType().getAttribute('http://purl.org/dc/terms/title'), 'label', 'Entity', 'Обект', 'Title', 'Заглавие');
      assertMultiLangAttributeItem(model.getType().getAttribute('http://purl.org/dc/terms/description'), 'multiLangString', 'Some description', 'Някакво описание', 'Description', 'Описание');

      // assert fields and regions directly associated with the model
      assertModel(model.getField('description'), model, 'Description', 'Описание');

      let region = model.getRegion('generalDetails');
      assertModel(region, model, 'Base details', 'Главни детайли');
      assertModel(region.getField('title'), model, 'Title', 'Наименование');

      region = model.getRegion('specificDetails');
      assertModel(region, model, 'Specific details', 'Специфични детайли');
      assertModel(region.getField('emailAddress'), model, 'E-mail address', 'Електронна поща');
    });
  });

  it('should reject if the requested model is missing', (done) => {
    let provider = () => undefined;
    modelManagementService.getModel('missing_model', provider).catch(() => done());
  });

  it('should correctly construct class hierarchy tree', () => {
    modelManagementService.getHierarchy().then(hierarchy => {
      let tree = hierarchy.tree[0];
      assertHierarchyItem(tree, 'emf:Entity', 'emf:Entity', 'emf:Елемент');

      let children = tree.getChildren();
      assertHierarchyItem(children[0], 'entity', 'Entity', 'Обект');
      assertHierarchyItem(children[0].getChildren()[0], 'media', 'Media', 'Медия');

      assertHierarchyItem(children[1], 'emf:Object', 'emf:Object', 'emf:Обект');
      assertHierarchyItem(children[1].getChildren()[0], 'audio', 'Audio', 'Аудио');
      assertHierarchyItem(children[1].getChildren()[1], 'video', 'Video', 'Видео');
    });
  });

  it('should correctly construct class flat hierarchy', () => {
    modelManagementService.getHierarchy().then(hierarchy => {
      let flat = hierarchy.flat;

      assertHierarchyItem(flat['emf:Entity'], 'emf:Entity', 'emf:Entity', 'emf:Елемент');
      assertHierarchyItem(flat['emf:Object'], 'emf:Object', 'emf:Object', 'emf:Обект');

      assertHierarchyItem(flat['entity'], 'entity', 'Entity', 'Обект');
      assertHierarchyItem(flat['media'], 'media', 'Media', 'Медия');

      assertHierarchyItem(flat['audio'], 'audio', 'Audio', 'Аудио');
      assertHierarchyItem(flat['video'], 'video', 'Video', 'Видео');
    });
  });

  it('should correctly resolve the current language & descriptions', () => {
    configurationStub.get.returns('DE');
    translateServiceStub.getCurrentLanguage.returns('FI');

    modelManagementService.getHierarchy().then(hierarchy => {
      let flat = hierarchy.flat;

      expect(flat['emf:Entity'].getRoot().getDescription().getValue()).to.eq('emf:Entity');
      expect(flat['emf:Object'].getRoot().getDescription().getValue()).to.eq('emf:Object');

      expect(flat['entity'].getRoot().getDescription().getValue()).to.eq('Entity');
      expect(flat['media'].getRoot().getDescription().getValue()).to.eq('Media');

      expect(flat['audio'].getRoot().getDescription().getValue()).to.eq('Audio');
      expect(flat['video'].getRoot().getDescription().getValue()).to.eq('Video');
    });
  });

  it('should correctly default the current language & descriptions', () => {
    configurationStub.get.returns('DE');
    translateServiceStub.getCurrentLanguage.returns('BG');

    modelManagementService.getHierarchy().then(hierarchy => {
      let flat = hierarchy.flat;

      expect(flat['emf:Entity'].getRoot().getDescription().getValue()).to.eq('emf:Елемент');
      expect(flat['emf:Object'].getRoot().getDescription().getValue()).to.eq('emf:Обект');

      expect(flat['entity'].getRoot().getDescription().getValue()).to.eq('Обект');
      expect(flat['media'].getRoot().getDescription().getValue()).to.eq('Медия');

      expect(flat['audio'].getRoot().getDescription().getValue()).to.eq('Аудио');
      expect(flat['video'].getRoot().getDescription().getValue()).to.eq('Видео');
    });
  });

  it('should resolve model identifier by default when labels are not present', () => {
    let models = getModelsHierarchy();
    delete models[0].labels;
    modelManagementRestServiceStub.getModelsHierarchy.returns(PromiseStub.resolve(models));

    modelManagementService.getHierarchy().then(hierarchy => {
      expect(hierarchy.flat['emf:Entity'].getRoot().getDescription().getValue()).to.eq('emf:Entity');
    });
  });

  it('should resolve attribute identifier by default when labels are not present', () => {
    let provider = null;
    modelManagementService.getHierarchy().then(hierarchy => provider = (id) => hierarchy.flat[id].getRoot());

    let data = getMetaData();
    delete data.semantics[0].labels;
    modelManagementRestServiceStub.getModelsMetaData.returns(PromiseStub.resolve(data));

    modelManagementService.getModel('emf:Entity', provider).then(model => {
      expect(model.getAttribute('http://purl.org/dc/terms/title').getMetaData().getDescription().getValue()).to.eq('http://purl.org/dc/terms/title');
    });
  });

  it('should correctly link parent and type relations inside the hierarchy tree by direct references', () => {
    modelManagementService.getHierarchy().then(hierarchy => {
      let flat = hierarchy.flat;

      expect(flat['audio'].getRoot().getParent()).to.equal(flat['media'].getRoot());

      expect(flat['video'].getRoot().getParent()).to.equal(flat['media'].getRoot());
      expect(flat['media'].getRoot().getParent()).to.equal(flat['entity'].getRoot());
      expect(flat['emf:Object'].getRoot().getParent()).to.equal(flat['emf:Entity'].getRoot());

      expect(flat['media'].getRoot().getType()).to.equal(flat['emf:Entity'].getRoot());
      expect(flat['entity'].getRoot().getType()).to.equal(flat['emf:Entity'].getRoot());

      expect(flat['audio'].getRoot().getType()).to.equal(flat['emf:Object'].getRoot());
      expect(flat['video'].getRoot().getType()).to.equal(flat['emf:Object'].getRoot());
    });
  });

  it('should correctly construct and cache meta data', () => {
    modelManagementService.getMetaData();

    let semanticMetaDatas = modelManagementService.modelsMetaData.getSemantics();
    assertMetaData(semanticMetaDatas['http://purl.org/dc/terms/title'], 'label', 'Title', 'Заглавие');
    assertMetaData(semanticMetaDatas['http://purl.org/dc/terms/description'], 'multiLangString', 'Description', 'Описание');

    let definitionMetaDatas = modelManagementService.modelsMetaData.getDefinitions();
    assertMetaData(definitionMetaDatas['abstract'], 'boolean', 'Is abstract', 'Абстрактна');
    assertMetaData(definitionMetaDatas['label'], 'label', 'Label', 'Име');
  });

  it('should cache model meta data on successive calls', () => {
    expect(modelManagementService.modelsMetaData).to.not.exist;

    modelManagementService.getMetaData();
    let hierarchy = modelManagementService.modelsMetaData;
    expect(modelManagementService.modelsMetaData).to.exist;
    expect(modelManagementRestServiceStub.getModelsMetaData.calledOnce).to.be.true;

    modelManagementRestServiceStub.getModelsMetaData.reset();

    modelManagementService.getMetaData();
    expect(modelManagementService.modelsMetaData).to.eq(hierarchy);
    expect(modelManagementRestServiceStub.getModelsMetaData.calledOnce).to.be.false;
  });

  function assertModel(model, parent, en, bg) {
    parent && expect(model.getParent()).to.eq(parent);
    en && expect(model.getDescription().getValue()).to.equal(en);

    en && expect(model.getDescriptionByLanguage('EN').getValue()).to.equal(en);
    bg && expect(model.getDescriptionByLanguage('BG').getValue()).to.equal(bg);
  }

  function assertSingleAttributeItem(attribute, type, value, en, bg) {
    expect(attribute.getType()).to.equal(type);
    value && expect(attribute.getValue().value).to.equal(value);

    en && expect(attribute.getMetaData().getDescriptionByLanguage('EN').getValue()).to.equal(en);
    bg && expect(attribute.getMetaData().getDescriptionByLanguage('BG').getValue()).to.equal(bg);
  }

  function assertMultiLangAttributeItem(attribute, type, enValue, bgValue, enLabel, bgLabel) {
    expect(attribute.getType()).to.equal(type);
    enValue && expect(attribute.getValue().value).to.equal(enValue);

    enValue && expect(attribute.getValueByLanguage('EN').getValue()).to.equal(enValue);
    bgValue && expect(attribute.getValueByLanguage('BG').getValue()).to.equal(bgValue);

    enLabel && expect(attribute.getMetaData().getDescriptionByLanguage('EN').getValue()).to.equal(enLabel);
    bgLabel && expect(attribute.getMetaData().getDescriptionByLanguage('BG').getValue()).to.equal(bgLabel);
  }

  function assertMetaData(metaData, type, enLabel, bgLabel) {
    type && expect(metaData.getType()).to.equal(type);
    enLabel && expect(metaData.getDescriptionByLanguage('EN').getValue()).to.equal(enLabel);
    bgLabel && expect(metaData.getDescriptionByLanguage('BG').getValue()).to.equal(bgLabel);
  }

  function assertHierarchyItem(hierarchy, id, en, bg) {
    expect(hierarchy.getRoot().getId()).to.equal(id);
    expect(hierarchy.getRoot().getDescriptionByLanguage('EN').getValue()).to.equal(en);
    expect(hierarchy.getRoot().getDescriptionByLanguage('BG').getValue()).to.equal(bg);
  }

  function getModelData() {
    return {
      'classes': [
        {
          'id': 'emf:Entity',
          'parent': null,
          'labels': {
            'en': 'Entity'
          },
          'attributes': [
            {
              'name': 'http://purl.org/dc/terms/title',
              'type': 'label',
              'value': {
                'en': 'Entity',
                'bg': 'Обект'
              }
            },
            {
              'name': 'http://purl.org/dc/terms/description',
              'type': 'multiLangString',
              'value': {
                'en': 'Some description',
                'bg': 'Някакво описание'
              }
            }
          ]
        }
      ],
      'definitions': [
        {
          'id': 'media',
          'parent': 'entity',
          'labels': {'en': 'Media'},
          'attributes': [{
            'name': 'abstract',
            'type': 'boolean',
            'value': false
          }, {
            'name': 'label',
            'type': 'label',
            'value': {
              'en': 'Media',
              'bg': 'Медиа'
            }
          }],
          fields: [
            {
              id: 'title',
              parent: null,
              regionId: 'generalDetails',
              attributes: [
                {
                  name: 'uri',
                  type: 'uri',
                  value: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title'
                }, {
                  name: 'label',
                  type: 'label',
                  value: {en: 'Title', bg: 'Наименование'}
                }, {
                  name: 'type',
                  type: 'type',
                  value: 'an..180'
                }, {
                  name: 'displayType',
                  type: 'displayType',
                  value: 'EDITABLE'
                }, {
                  name: 'mandatory',
                  type: 'boolean',
                  value: true
                }]
            }, {
              id: 'description',
              parent: null,
              regionId: null,
              attributes: [
                {
                  name: 'uri',
                  type: 'uri',
                  value: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#description'
                }, {
                  name: 'label',
                  type: 'label',
                  value: {en: 'Description', bg: 'Описание'}
                }, {
                  name: 'type',
                  type: 'type',
                  value: 'an..180'
                }, {
                  name: 'displayType',
                  type: 'displayType',
                  value: 'EDITABLE'
                }, {
                  name: 'mandatory',
                  type: 'boolean',
                  value: true
                }]
            }, {
              id: 'emailAddress',
              parent: null,
              regionId: 'specificDetails',
              attributes: [
                {
                  name: 'uri',
                  type: 'uri',
                  value: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#emailAddress'
                }, {
                  name: 'label',
                  type: 'label',
                  value: {en: 'E-mail address', bg: 'Електронна поща'}
                }, {
                  name: 'type',
                  type: 'type',
                  value: 'an..180'
                }, {
                  name: 'displayType',
                  type: 'displayType',
                  value: 'EDITABLE'
                }, {
                  name: 'mandatory',
                  type: 'boolean',
                  value: true
                }]
            }],
          regions: [
            {
              id: 'generalDetails',
              attributes: [
                {
                  name: 'label',
                  type: 'label',
                  value: {bg: 'Главни детайли', en: 'Base details'}
                }, {
                  name: 'displayType',
                  type: 'displayType',
                  value: 'EDITABLE'
                }, {
                  name: 'order',
                  type: 'integer',
                  value: 10
                }]
            }, {
              id: 'specificDetails',
              attributes: [
                {
                  name: 'label',
                  type: 'label',
                  value: {bg: 'Специфични детайли', en: 'Specific details'}
                }, {
                  name: 'displayType',
                  type: 'displayType',
                  value: 'EDITABLE'
                }, {
                  name: 'order',
                  type: 'integer',
                  value: 20
                }]
            }
          ]
        },
        {
          'id': 'entity',
          'parent': null,
          'labels': {
            'en': 'entity'
          },
          'attributes': [{
            'name': 'identifier',
            'type': 'string',
            'value': 'entity'
          }, {
            'name': 'abstract',
            'type': 'boolean',
            'value': true
          }, {
            'name': 'label',
            'type': 'label',
            'value': {
              'en': 'entity',
              'bg': 'обект'
            }
          }],
          fields: [
            {
              id: 'title',
              parent: null,
              regionId: 'generalDetails',
              attributes: [
                {
                  name: 'uri',
                  type: 'uri',
                  value: 'http://purl.org/dc/terms/title'
                }, {
                  name: 'label',
                  type: 'label',
                  value: {en: 'Title', bg: 'Наименование'}
                }, {
                  name: 'type',
                  type: 'type',
                  value: 'an..180'
                }, {
                  name: 'displayType',
                  type: 'displayType',
                  value: 'EDITABLE'
                }, {
                  name: 'mandatory',
                  type: 'boolean',
                  value: true
                }]
            }, {
              id: 'description',
              parent: null,
              regionId: null,
              attributes: [
                {
                  name: 'uri',
                  type: 'uri',
                  value: 'http://purl.org/dc/terms/title'
                }, {
                  name: 'label',
                  type: 'label',
                  value: {en: 'Description', bg: 'Описание'}
                }, {
                  name: 'type',
                  type: 'type',
                  value: 'an..180'
                }, {
                  name: 'displayType',
                  type: 'displayType',
                  value: 'EDITABLE'
                }, {
                  name: 'mandatory',
                  type: 'boolean',
                  value: true
                }]
            }],
          regions: [
            {
              id: 'generalDetails',
              attributes: [
                {
                  name: 'label',
                  type: 'label',
                  value: {bg: 'Главни детайли', en: 'Base details'}
                }, {
                  name: 'displayType',
                  type: 'displayType',
                  value: 'EDITABLE'
                }, {
                  name: 'order',
                  type: 'integer',
                  value: 10
                }]
            }
          ]
        }
      ]
    };
  }

  function getMetaData() {
    return {
      'semantics': [
        {
          'id': 'title',
          'uri': 'http://purl.org/dc/terms/title',
          'type': 'label',
          'defaultValue': '',
          'validationModel': {
            'mandatory': true
          },
          'labels': {
            'en': 'Title',
            'bg': 'Заглавие'
          }
        },
        {
          'id': 'description',
          'uri': 'http://purl.org/dc/terms/description',
          'type': 'multiLangString',
          'defaultValue': '',
          'validationModel': {
            'mandatory': false
          },
          'labels': {
            'en': 'Description',
            'bg': 'Описание'
          }
        }
      ],
      'definitions': [
        {
          'id': 'identifier',
          'uri': null,
          'type': 'string',
          'defaultValue': '',
          'validationModel': {
            'mandatory': true
          },
          'labels': {
            'bg': 'Идентификатор',
            'en': 'Identifier'
          }
        },
        {
          'id': 'abstract',
          'uri': null,
          'type': 'boolean',
          'defaultValue': true,
          'validationModel': {
            'mandatory': false
          },
          'labels': {
            'bg': 'Абстрактна',
            'en': 'Is abstract'
          }
        },
        {
          'id': 'label',
          'uri': null,
          'type': 'label',
          'defaultValue': '',
          'validationModel': {
            'mandatory': true
          },
          'labels': {
            'bg': 'Име',
            'en': 'Label'
          }
        }
      ],
      'properties': [
        {
          defaultValue: '',
          id: 'label',
          labels: {},
          type: 'label',
          uri: 'http://www.w3.org/2000/01/rdf-schema#label',
          validationModel: {mandatory: true}
        }, {
          defaultValue: '',
          id: 'description',
          labels: {},
          type: 'multiLangString',
          uri: 'http://www.w3.org/2004/02/skos/core#definition',
          validationModel: {mandatory: false}
        }, {
          defaultValue: '',
          id: 'range',
          labels: {},
          type: 'semanticType',
          uri: 'http://www.w3.org/2000/01/rdf-schema#range',
          validationModel: {mandatory: true}
        }, {
          defaultValue: true,
          id: 'searchable',
          labels: {},
          type: 'boolean',
          uri: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#isSearchable',
          validationModel: {mandatory: false}
        }
      ],
      'regions': [
        {
          defaultValue: '',
          id: 'identifier',
          labels: {},
          type: 'identifier',
          uri: null,
          validationModel: {mandatory: true}
        }, {
          defaultValue: '',
          id: 'label',
          labels: {},
          type: 'label',
          uri: null,
          validationModel: {mandatory: true}
        }, {
          defaultValue: 0,
          id: 'order',
          labels: {},
          type: 'integer',
          uri: null,
          validationModel: {mandatory: false}
        }],
      'fields': [
        {
          defaultValue: '',
          id: 'name',
          labels: {},
          type: 'identifier',
          uri: null,
          validationModel: {mandatory: true}
        }, {
          defaultValue: '',
          id: 'type',
          labels: {},
          type: 'type',
          uri: null,
          validationModel: {mandatory: true}
        }, {
          defaultValue: '',
          id: 'label',
          labels: {},
          type: 'label',
          uri: null,
          validationModel: {mandatory: false}
        }, {
          defaultValue: 'HIDDEN',
          id: 'displayType',
          labels: {},
          type: 'displayType',
          uri: null,
          validationModel: {mandatory: false}
        }, {
          defaultValue: '',
          id: 'codeList',
          labels: {},
          type: 'codeList',
          uri: null,
          validationModel: {mandatory: false}
        }
      ]
    };
  }

  function getModelsHierarchy() {
    return [
      {
        'id': 'emf:Entity',
        'parentId': null,
        'labels': {
          'BG': 'emf:Елемент',
          'EN': 'emf:Entity'
        },
        'subTypes': [
          {
            'id': 'entity',
            'parentId': null,
            'labels': {
              'BG': 'Обект',
              'EN': 'Entity'
            },
            'abstract': true
          }, {
            'id': 'media',
            'parentId': 'entity',
            'labels': {
              'BG': 'Медия',
              'EN': 'Media'
            },
            'abstract': true
          }
        ]
      }, {
        'id': 'emf:Object',
        'parentId': 'emf:Entity',
        'labels': {
          'BG': 'emf:Обект',
          'EN': 'emf:Object'
        },
        'subTypes': [
          {
            'id': 'audio',
            'parentId': 'media',
            'labels': {
              'BG': 'Аудио',
              'EN': 'Audio'
            },
            'abstract': false
          }, {
            'id': 'video',
            'parentId': 'media',
            'labels': {
              'BG': 'Видео',
              'EN': 'Video'
            },
            'abstract': false
          }
        ]
      }
    ];
  }

  function getProperties() {
    return [
      {
        id: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title',
        attributes: [{
          value: {en: 'Title', bg: 'Наименование'},
          name: 'http://www.w3.org/2000/01/rdf-schema#label',
          type: 'label'
        }]
      }, {
        id: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#emailAddress',
        attributes: [{
          value: {en: 'E-mail address', bg: 'Електронна поща'},
          name: 'http://www.w3.org/2000/01/rdf-schema#label',
          type: 'label'
        }]
      }, {
        id: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#description',
        attributes: [{
          value: {en: 'Description', bg: 'Описание'},
          name: 'http://www.w3.org/2000/01/rdf-schema#label',
          type: 'label'
        }]
      }
    ];
  }
});