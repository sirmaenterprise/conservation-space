import {AggregatedTableConfig, PROPERTY_TYPE_PICKER} from 'idoc/widget/aggregated-table/config';
import {PropertiesSelectorHelper} from 'idoc/widget/properties-selector/properties-selector-helper';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {IdocMocks} from 'test/idoc/idoc-mocks';

describe('AggregatedTableConfig', function () {

  describe('convertToSelectorModel', () => {
    it('should convert properties view models to model for select component', () => {
      let properties = [
        buildPropertyViewModel('type', 'emf:type', 'Type', true),
        buildPropertyViewModel('status', 'emf:status', 'Status', true),
        buildPropertyViewModel('createdBy', 'emf:createdBy', 'Created by', false, true),
        buildPropertyViewModel('customProp', 'FORBIDDEN', 'Custom object property', false, true)
      ];
      var actual = AggregatedTableConfig.convertToSelectorModel(properties);
      expect(actual).to.eql([
        {
          'id': 'emf:type',
          'text': 'Type'
        },
        {
          'id': 'emf:status',
          'text': 'Status'
        },
        {
          'id': 'emf:createdBy',
          'text': 'Created by'
        }
      ]);
    });
  });

  describe('isGroupByProperty', () => {
    it('should return true if given property is a codelist one and can be used for grouping', () => {
      let definition = {
        fields: [
          buildPropertyViewModel('description', 'emf:description', 'Description'),
          buildPropertyViewModel('status', 'emf:status', 'Status', true)
        ],
        identifier: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project',
        label: 'Project'
      };
      let config = getConfigInstance();
      let isGroupByProperty = config.isGroupByProperty(definition, 'status', []);
      expect(isGroupByProperty).to.be.true;
    });

    it('should return true if given property is an object property and can be used for grouping', () => {
      let definition = {
        fields: [
          buildPropertyViewModel('createdBy', 'emf:createdBy', 'Created by', false, true),
          buildPropertyViewModel('description', 'emf:description', 'Description')
        ],
        identifier: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project',
        label: 'Project'
      };
      let config = getConfigInstance();
      let isGroupByProperty = config.isGroupByProperty(definition, 'createdBy', []);
      expect(isGroupByProperty).to.be.true;
    });

    it('should return false if given property is not a codelist nor object property and can`t be used for grouping', () => {
      let definition = {
        fields: [
          buildPropertyViewModel('createdOn', 'emf:createdOn', 'Created on'),
          buildPropertyViewModel('description', 'emf:description', 'Description')
        ],
        identifier: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project',
        label: 'Project'
      };
      let config = getConfigInstance();
      let isGroupByProperty = config.isGroupByProperty(definition, 'description');
      expect(isGroupByProperty).to.be.false;
    });
  });

  describe('setSelectedObjects', () => {
    it('should convert selected objects array to array of object id`s', () => {
      let config = getConfigInstance();
      config.setSelectedObjects([
        {
          id: 'emf:123456',
          definitionId: 'GEP11111'
        },
        {
          id: 'emf:234567',
          definitionId: 'GEP11111'
        }
      ]);
      expect(config.config.selectedObjects).to.eql(['emf:123456', 'emf:234567']);
    });

    it('should not populated selectedObjects if no selected objects are found or argument is not provided', () => {
      let config = getConfigInstance();
      config.setSelectedObjects();
      expect(config.config.selectedObjects).to.be.undefined;
      config.setSelectedObjects([]);
      expect(config.config.selectedObjects).to.eql([]);
    });
  });

  describe('onGroupByChanged', () => {
    it('should change groupBy property', () => {
      let aggregatedTableConfig = getConfigInstance();
      aggregatedTableConfig.onGroupByChanged('test');
      expect(aggregatedTableConfig.config.groupBy).to.equal(undefined);
    });
  });

  function getConfigInstance() {
    let propertiesSelectorHelper = new PropertiesSelectorHelper(PromiseAdapterMock.mockImmediateAdapter(), {}, {});
    let objectSelectorHelper = {
      getSelectedItems: () => {
      }
    };
    return new AggregatedTableConfig(objectSelectorHelper, propertiesSelectorHelper, IdocMocks.mockTranslateService(), IdocMocks.mockTimeout());
  }

  function buildPropertyViewModel(name, uri, label, codelist, objectProperty) {
    let model = {
      'name': name,
      'uri': uri,
      'label': label,
      'previewEmpty': true,
      'displayType': 'READ_ONLY',
      'defaultValue': 'GEP11111',
      'dataType': {
        'name': 'text'
      },
      'isMandatory': false
    };
    if (codelist && !objectProperty) {
      model.codelist = 2;
      model.codelists = [2]
    }
    if (objectProperty && !codelist) {
      model.controlDefinition = {
        identifier: PROPERTY_TYPE_PICKER
      }
    }
    return model;
  }
});
