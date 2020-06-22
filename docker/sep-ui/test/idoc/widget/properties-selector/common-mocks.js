import {InstanceObject} from 'models/instance-object';

export class CommonMocks {

  static mockPropertiesSelectorHelper() {
    return {
      initConfiguration: () => {
      },
      getDefinitionsArray: () => {
      }
    }
  };

  static mockContext() {
    let models = CommonMocks.mockModels();
    return {
      currentObjectId: 'currentObjectId',
      getCurrentObjectId: function() {
        return this.currentObjectId;
      },
      getCurrentObject: () => {
        return this.getSharedObject(this.currentObjectId);
      },
      getSharedObject: (objectId) => {
        return new Promise((resolve) => {
          resolve(new InstanceObject(objectId, models));
        });
      },
      getSharedObjects: (objectIds) => {
        let sharedObjects = objectIds.map((objectId) => {
          return new InstanceObject(objectId, models);
        });
        return Promise.resolve({
          data: sharedObjects
        });
      }
    }
  }

  static mockModels() {
    return {
      definitionId: 'definitionId',
      definitionLabel: 'definitionLabel',
      viewModel: {
        fields: [
          {
            identifier: 'property1',
            label: 'Property 1',
            fields: [
              {
                identifier: 'property5',
                label: 'Property 5'
              },
              {
                identifier: 'property6',
                label: 'Property 6',
                displayType: 'SYSTEM'
              }
            ]
          },
          {
            identifier: 'property2',
            label: 'Property 2',
            displayType: 'SYSTEM'
          },
          {
            identifier: 'property3',
            label: 'Property 3'
          }
        ]
      }
    };
  }

  static mockCriteria() {
    return {
      condition: 'AND',
      rules: [{
        field: 'types',
        operation: 'in',
        value: ['emf:Case', 'GEP11111']
      }]
    }
  }
}
