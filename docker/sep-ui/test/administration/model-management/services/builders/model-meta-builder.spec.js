import {ModelMetaDataBuilder} from 'administration/model-management/services/builders/model-meta-builder';
import {ModelMetaDataLinker} from 'administration/model-management/services/linkers/model-meta-linker';

import {ModelFieldMetaData} from 'administration/model-management/meta/model-field-meta';
import {ModelRegionMetaData} from 'administration/model-management/meta/model-region-meta';
import {ModelControlMetaData} from 'administration/model-management/meta/model-control-meta';
import {ModelControlParamMetaData} from 'administration/model-management/meta/model-control-param-meta';
import {ModelPropertyMetaData} from 'administration/model-management/meta/model-property-meta';
import {ModelAttributeMetaData} from 'administration/model-management/meta/model-attribute-meta';
import {ModelHeaderMetaData} from 'administration/model-management/meta/model-header-meta';
import {ModelActionMetaData} from 'administration/model-management/meta/model-action-meta';
import {ModelActionGroupMetaData} from 'administration/model-management/meta/model-action-group-meta';

import {stub} from 'test/test-utils';

describe('ModelMetaDataBuilder', () => {

  let modelMetaBuilder;
  let modelMetaDataLinker;

  beforeEach(() => {
    modelMetaDataLinker = stub(ModelMetaDataLinker);
    modelMetaBuilder = new ModelMetaDataBuilder(modelMetaDataLinker);
  });

  it('should properly build models meta data from a provided response', () => {
    let meta = modelMetaBuilder.buildMetaData(getMetaData());

    let semantics = meta.getSemantics();
    expect(semantics.getModel('http://purl.org/dc/terms/title') instanceof ModelAttributeMetaData).to.be.true;
    expect(semantics.getModel('http://purl.org/dc/terms/description') instanceof ModelAttributeMetaData).to.be.true;

    let definitions = meta.getDefinitions();
    expect(definitions.getModel('identifier') instanceof ModelAttributeMetaData).to.be.true;
    expect(definitions.getModel('abstract') instanceof ModelAttributeMetaData).to.be.true;

    let fields = meta.getFields();
    expect(fields.getModel('name') instanceof ModelFieldMetaData).to.be.true;
    expect(fields.getModel('type') instanceof ModelFieldMetaData).to.be.true;

    let regions = meta.getRegions();
    expect(regions.getModel('identifier') instanceof ModelRegionMetaData).to.be.true;
    expect(regions.getModel('label') instanceof ModelRegionMetaData).to.be.true;

    let controls = meta.getControls();
    expect(controls.getModel('id') instanceof ModelControlMetaData).to.be.true;

    let controlParams = meta.getControlParams();
    expect(controlParams.getModel('id') instanceof ModelControlParamMetaData).to.be.true;
    expect(controlParams.getModel('type') instanceof ModelControlParamMetaData).to.be.true;
    expect(controlParams.getModel('name') instanceof ModelControlParamMetaData).to.be.true;
    expect(controlParams.getModel('value') instanceof ModelControlParamMetaData).to.be.true;

    let properties = meta.getProperties();
    expect(properties.getModel('http://www.w3.org/2000/01/rdf-schema#label') instanceof ModelPropertyMetaData).to.be.true;
    expect(properties.getModel('http://www.w3.org/2004/02/skos/core#definition') instanceof ModelPropertyMetaData).to.be.true;

    let headers = meta.getHeaders();
    expect(headers.getModel('default_header') instanceof ModelHeaderMetaData).to.be.true;
    expect(headers.getModel('compact_header') instanceof ModelHeaderMetaData).to.be.true;

    let actions = meta.getActions();
    expect(actions.getModel('id') instanceof ModelActionMetaData).to.be.true;

    let groups = meta.getActionGroups();
    expect(groups.getModel('id') instanceof ModelActionGroupMetaData).to.be.true;
  });

  function getMetaData() {
    return {
      'semantics': [
        {
          id: 'title',
          uri: 'http://purl.org/dc/terms/title'
        }, {
          id: 'description',
          uri: 'http://purl.org/dc/terms/description'
        }
      ],
      'definitions': [
        {
          id: 'identifier'
        }, {
          id: 'abstract'
        }
      ],
      'properties': [
        {
          id: 'label',
          uri: 'http://www.w3.org/2000/01/rdf-schema#label'
        }, {
          id: 'description',
          uri: 'http://www.w3.org/2004/02/skos/core#definition'
        }
      ],
      'regions': [
        {
          id: 'identifier'
        }, {
          id: 'label'
        }
      ],
      'fields': [
        {
          id: 'name'
        }, {
          id: 'type'
        }
      ],
      'controls': [
        {
          id: 'id'
        }
      ],
      'controlParams': [
        {
          id: 'id'
        },
        {
          id: 'type'
        },
        {
          id: 'name'
        },
        {
          id: 'value'
        }
      ],
      'headers': [
        {
          'id': 'default_header',
          'uri': null,
          'type': 'header',
          'defaultValue': {},
          'validationModel': {
            'mandatory': false,
            'updateable': true
          },
          'labels': {},
          'order': 0
        },
        {
          'id': 'compact_header',
          'uri': null,
          'type': 'header',
          'defaultValue': {},
          'validationModel': {
            'mandatory': false,
            'updateable': true
          },
          'labels': {},
          'order': 1
        }
      ],
      'actionGroups': [
        {
          'id': 'id',
          'type': 'identifier',
          'defaultValue': '',
          'validationModel': {
            'mandatory': true,
            'updateable': true
          }
        }
      ],
      'actions': [
        {
          'id': 'id',
          'type': 'identifier',
          'defaultValue': '',
          'validationModel': {
            'mandatory': true,
            'updateable': true
          }
        }
      ]
    };
  }
});