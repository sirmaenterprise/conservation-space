import {PropertiesRestService, SERVICE_URL} from 'services/rest/properties-service';
import {IdocMocks} from 'test/idoc/idoc-mocks';

describe('PropertiesRestService', () => {

  var restClient;
  var service;
  beforeEach(() => {
    restClient = {
      get: sinon.spy(() => {
        return {
          then: () => {
          }
        };
      })
    };
    service = new PropertiesRestService(restClient, IdocMocks.mockTranslateService());
  });

  describe('getSearchableProperties()', () => {
    it('should call the correct REST endpoint', () => {
      service.getSearchableProperties();
      expect(restClient.get.calledOnce).to.be.true;

      var expectedUrl = SERVICE_URL + '/searchable/semantic';
      expect(restClient.get.getCall(0).args[0]).to.equal(expectedUrl);
    });

    it('should construct correct parameter configuration for single type', () => {
      service.getSearchableProperties('emf:Type');
      var expected = {
        params: {
          'forType': 'emf:Type'
        }
      };
      expect(restClient.get.getCall(0).args[1]).to.deep.equal(expected);
    });

    it('should construct correct parameter configuration for single type in array', () => {
      service.getSearchableProperties(['emf:Type']);
      var expected = {
        params: {
          'forType': 'emf:Type'
        }
      };
      expect(restClient.get.getCall(0).args[1]).to.deep.equal(expected);
    });

    it('should construct correct parameter configuration for multiple types', () => {
      service.getSearchableProperties(['emf:Type', 'emf:SubType']);
      var expected = {
        params: {
          'forType': 'emf:Type,emf:SubType'
        }
      };
      expect(restClient.get.getCall(0).args[1]).to.deep.equal(expected);
    });

    it('should convert properties before resolving the response', (done) => {
      service.restClient.get = () => {
        return Promise.resolve(getPropertiesResponse());
      };
      service.getSearchableProperties().then((properties) => {
        var expected = [{
          id: 'anyField',
          text: 'translated message',
          type: 'string'
        }, {
          id: 'anyRelation',
          text: 'translated message',
          type: 'object'
        }, {
          id: 'property',
          text: 'Property',
          type: 'string'
        }, {
          id: 'status',
          text: 'Status',
          type: 'codeList',
          codeLists: [1]
        }, {
          id: 'priority',
          text: 'Priority',
          type: 'string'
        }, {
          id: 'hasChild',
          text: 'Has child',
          type: 'object'
        }, {
          id: 'numericProperty',
          text: '123',
          type: 'numeric'
        }];
        expect(properties.length).to.equal(7);
        expect(properties).to.deep.equal(expected);
        done();
      }).catch(done);
    });

    it('should construct correct parameters for properties suggest', () => {
      service.loadObjectPropertiesSuggest('emf:parentId', 'emf:Document', true);
      var expected = '/properties/suggest?targetId=emf:parentId&type=emf:Document&multivalued=true';
      expect(restClient.get.getCall(0).args[0]).to.equal(expected);
    });

    it('should convert different numeric properties', (done) => {
      var numericResponse = {
        data: [{
          uri: 'intProperty',
          text: 'Integer property',
          rangeClass: 'int'
        }, {
          uri: 'longProperty',
          text: 'Long property',
          rangeClass: 'long'
        }, {
          uri: 'floatProperty',
          text: 'Float property',
          rangeClass: 'float'
        }, {
          uri: 'doubleProperty',
          text: 'Double property',
          rangeClass: 'double'
        }]
      };
      service.restClient.get = () => {
        return Promise.resolve(numericResponse);
      };
      service.getSearchableProperties().then((properties) => {
        expect(properties.length).to.equal(6);
        // First two are any field  & any relation
        expect(properties[2].type).to.equal('numeric');
        expect(properties[3].type).to.equal('numeric');
        expect(properties[4].type).to.equal('numeric');
        expect(properties[5].type).to.equal('numeric');
        done();
      }).catch(done);
    });
  });

  function getPropertiesResponse() {
    return {
      data: [{
        uri: 'property',
        text: 'Property',
        rangeClass: 'string'
      }, {
        // Duplicate by uri
        uri: 'property',
        text: 'New property',
        rangeClass: 'string'
      }, {
        uri: 'status',
        text: 'Status',
        rangeClass: 'string',
        codeLists: [1]
      }, {
        // No codelists
        uri: 'priority',
        text: 'Priority',
        rangeClass: 'string',
        codeLists: []
      }, {
        // Object property
        uri: 'hasChild',
        text: 'Has child',
        rangeClass: 'emf:Activity',
        propertyType: 'object'
      }, {
        // Numeric property
        uri: 'numericProperty',
        text: '123',
        rangeClass: 'long'
      }]
    };
  }
});