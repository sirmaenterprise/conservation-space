import {HEADER_V2_JSON, RestClient} from 'services/rest-client';
import {PropertiesRestService} from 'services/rest/properties-service';
import {DefinitionService} from 'services/rest/definition-service';
import {RequestsCacheService} from 'services/rest/requests-cache-service';
import {CONTROL_TYPE} from 'models/model-utils';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('PropertiesRestService', () => {

  let service;
  let restClient;
  let definitionService;

  beforeEach(() => {
    restClient = stub(RestClient);
    restClient.get.returns(PromiseStub.resolve());
    restClient.post.returns(PromiseStub.resolve());
    definitionService = stub(DefinitionService);
    definitionService.getFields = sinon.spy(() => {
      return PromiseStub.resolve(getPropertiesResponse());
    });

    service = new PropertiesRestService(restClient, IdocMocks.mockTranslateService(), definitionService, new RequestsCacheService());
  });

  describe('getSearchableProperties()', () => {

    it('should construct correct parameter configuration for single type', () => {
      service.getSearchableProperties(['emf:Type']);
      let expected = ['emf:Type'];

      expect(definitionService.getFields.getCall(0).args[0]).to.deep.equal(expected);
    });

    it('should construct correct parameter configuration for multiple types', () => {
      service.getSearchableProperties(['emf:Type', 'emf:SubType']);
      let expected = ['emf:Type', 'emf:SubType'];

      expect(definitionService.getFields.getCall(0).args[0]).to.deep.equal(expected);
    });

    it('should convert properties before resolving the response', (done) => {
      service.getSearchableProperties().then((properties) => {
        let expected = getConvertedProperties();

        expect(properties.length).to.equal(expected.length);
        expect(properties).to.deep.equal(expected);
        done();
      }).catch(done);
    });
  });

  it('should construct correct parameters for properties suggest', () => {
    service.loadObjectPropertiesSuggest('emf:parentId', 'emf:Document', true);
    expect(restClient.get.getCall(0).args).to.eql(['/properties/suggest?targetId=emf:parentId&type=emf:Document&multivalued=true']);
  });

  describe('checkFieldUniqueness()', () => {
    it('should call the rest service with appropriate data', () => {
      let instanceId = 'instanceId';
      let fieldName = 'fieldName';
      let value = 'value';
      let definitionId = 'definitionId';
      service.checkFieldUniqueness(definitionId, instanceId, fieldName, value);

      expect(restClient.post.args[0][0]).to.be.equal('/properties/unique');
      expect(restClient.post.args[0][1].definitionId).to.equal(definitionId);
      expect(restClient.post.args[0][1].instanceId).to.equal(instanceId);
      expect(restClient.post.args[0][1].propertyName).to.equal(fieldName);
      expect(restClient.post.args[0][1].value).to.equal(value);
    });
  });

  describe('getBindings()', function () {

    it('should perform request for object properties with proper arguments', () => {
      let bindings = {
        'definitionId': 'testDefinition',
        'bindings': [
          {id: 'emf:123456', source: 'dcterms:identifier', target: 'emf:generatedField'},
          {id: 'emf:123456', source: 'emf:createdBy.emf:email', target: 'emf:generatedField'}
        ]
      };
      service.evaluateValues('testDefinition', bindings);
      expect(restClient.post.calledOnce);
      expect(restClient.post.getCall(0).args[0]).to.equal('/properties/value/eval');
      expect(restClient.post.getCall(0).args[1].definitionId).to.equal('testDefinition');
      expect(restClient.post.getCall(0).args[1].bindings).to.eql(bindings);
      expect(restClient.post.getCall(0).args[2].headers).to.eql({
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      });
    });
  });

  function getConvertedProperties() {
    return [
      {
        id: 'freeText',
        text: 'translated message',
        type: 'fts'
      },
      {
        id: 'anyField',
        text: 'translated message',
        type: 'string'
      }, {
        id: 'anyRelation',
        text: 'translated message',
        type: 'object'
      }, {
        id: 'types',
        text: 'translated message',
        type: ''
      },{
        id: 'emf:test1',
        text: 'test1',
        type: 'object'
      }, {
        id: 'emf:test2',
        text: 'test2',
        type: 'dateTime'
      }, {
        id: 'emf:test3',
        text: 'test3',
        type: 'dateTime'
      }, {
        id: 'emf:test4',
        text: 'test4',
        type: 'dateTime'
      }, {
        id: 'emf:test5',
        text: 'test5',
        type: 'numeric'
      }, {
        id: 'emf:test6',
        text: 'test6',
        type: 'string'
      }, {
        id: 'emf:test7',
        text: 'test7',
        type: 'numeric'
      }, {
        id: 'emf:test8',
        text: 'test8',
        codeLists: [1, 2, 3],
        type: 'codeList'
      }, {
        id: 'emf:test9',
        text: 'test9',
        type: 'dateTime'
      }, {
        id: 'emf:test10',
        text: 'test10',
        type: 'numeric'
      }, {
        id: 'emf:test11',
        text: 'test11',
        type: 'numeric'
      }, {
        id: 'emf:test12',
        text: 'test12',
        type: 'boolean'
      }, {
        id: 'emf:test17',
        text: 'test17',
        type: 'dateTime'
      }, {
        id: 'emf:test18',
        text: 'test18',
        type: 'boolean'
      }, {
        id: 'emf:test19',
        text: 'test19',
        type: 'numeric'
      }, {
        id: 'emf:test20',
        text: 'test20',
        type: 'numeric'
      }, {
        id: 'emf:test21',
        text: 'test21',
        type: 'objectType'
      }, {
        id: 'emf:test22',
        text: 'test22',
        type: 'object',
        range: ['Case']
      }, {
        id: 'emf:test23',
        text: 'test23',
        type: 'object',
        range: ['User', 'Group']
      }, {
        id: 'emf:test24',
        text: 'test24',
        type: 'object'
      }, {
        id: 'emf:test25',
        text: 'test25',
        type: 'object',
        range: ['User', 'Group']
      }
    ];
  }

  function getPropertiesResponse() {
    return {
      data: [{
        label: 'Audio',
        fields: [
          {
            uri: 'emf:test1',
            label: 'test1',
            dataType: {name: 'any'}
          }, {
            uri: 'emf:test2',
            label: 'test2',
            dataType: {name: 'date'}
          }, {
            uri: 'emf:test3',
            label: 'test3',
            dataType: {name: 'time'}
          }, {
            uri: 'emf:test4',
            label: 'test4',
            dataType: {name: 'datetime'}
          }, {
            uri: 'emf:test5',
            label: 'test5',
            dataType: {name: 'long'}
          }, {
            uri: 'emf:test6',
            label: 'test6',
            dataType: {name: 'text'}
          }, {
            uri: 'emf:test7',
            label: 'test7',
            dataType: {name: 'float'}
          }, {
            uri: 'emf:test8',
            label: 'test8',
            codelist: 1
          }, {
            uri: 'emf:test8',
            label: 'test8',
            codelist: 2
          }, {
            uri: 'emf:test8',
            label: 'test8',
            codelist: 3
          }
        ]
      }, {
        label: 'Document',
        fields: [
          {
            uri: 'emf:test6',
            label: 'test6',
            dataType: {name: 'text'}
          }, {
            uri: 'emf:test7',
            label: 'test7',
            dataType: {name: 'float'}
          }, {
            uri: 'emf:test9',
            label: 'test9',
            dataType: {name: 'date'}
          }, {
            uri: 'emf:test10',
            label: 'test10',
            dataType: {name: 'int'}
          }, {
            uri: 'emf:test11',
            label: 'test11',
            dataType: {name: 'double'}
          }, {
            uri: 'emf:test12',
            label: 'test12',
            dataType: {name: 'boolean'}
          }, {
            uri: 'emf:test13',
            label: 'test13',
            displayType: 'SYSTEM',
            dataType: {name: 'boolean'}
          }, {
            uri: 'emf:test14',
            label: 'test14',
            displayType: 'SYSTEM',
            dataType: {name: 'boolean'}
          }, {
            uri: 'FORBIDDEN',
            label: 'test15',
            dataType: {name: 'boolean'}
          }, {
            uri: 'emf:test16',
            fields: [{
              uri: 'emf:test17',
              label: 'test17',
              dataType: {name: 'date'}
            }, {
              uri: 'emf:test18',
              label: 'test18',
              dataType: {name: 'boolean'}
            }, {
              uri: 'emf:test19',
              label: 'test19',
              dataType: {name: 'int'}
            }, {
              uri: 'emf:test20',
              label: 'test20',
              dataType: {name: 'double'}
            }, {
              uri: 'emf:test21',
              controlDefinition: {
                identifier: CONTROL_TYPE.OBJECT_TYPE_SELECT
              },
              label: 'test21'
            }, {
              uri: 'emf:test22',
              controlDefinition: {
                controlParams: [
                  {
                    name: 'range',
                    value: 'Case'
                  }
                ]
              },
              label: 'test22',
              dataType: {name: 'any'}
            }, {
              uri: 'emf:test23',
              controlDefinition: {
                controlParams: [
                  {
                    name: 'range',
                    value: 'User,Group'
                  }
                ]
              },
              label: 'test23',
              dataType: {name: 'any'}
            }, {
              uri: 'emf:test24',
              controlDefinition: {
                controlParams: [
                  {
                    name: 'range',
                    value: ''
                  }
                ]
              },
              label: 'test24',
              dataType: {name: 'any'}
            }, {
              uri: 'emf:test25',
              controlDefinition: {
                controlParams: [
                  {
                    name: 'range',
                    value: 'User,  Group '
                  }
                ]
              },
              label: 'test25',
              dataType: {name: 'any'}
            }]
          }
        ]
      }]
    };
  }
});