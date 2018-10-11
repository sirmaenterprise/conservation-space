import {InstanceModel, InstanceModelProperty} from 'models/instance-model';
import _ from 'lodash';

describe('Instance Model', ()=> {
  describe('Model creation', ()=> {
    it('should wrap fed model on creation', ()=> {
      let model = new InstanceModel(_.cloneDeep(testData));
      Object.keys(model._instanceModel).forEach((attribute)=> {
        expect(model._instanceModel[attribute].serialize()).to.eql(testData[attribute]);
      });
    });

    it('should serialize and deserialize data properly and to have the same result', ()=> {
      let model = new InstanceModel(_.cloneDeep(testData));
      Object.keys(model._instanceModel).forEach((attribute)=> {
        expect(model._instanceModel[attribute].serialize()).to.eql(testData[attribute]);
      });

      let serialized = model.serialize();
      let model2 = new InstanceModel(_.cloneDeep(serialized));
      expect(model2.serialize()).to.eql(serialized);
    });

    it('should emit modelValidated on isValid property changed', ()=> {
      let value;
      let receiver = function (data) {
        value = data;
      };
      let model = new InstanceModel(_.cloneDeep(testData));
      model.subscribe('modelValidated', receiver);
      expect(value).to.equal(undefined);
      model.isValid = true;
      expect(value).to.be.true;
    });

    it('should serialize properly', ()=> {
      let model = new InstanceModel(_.cloneDeep(testData));
      model.checkbox.subscribe('propertyChanged', (recieved)=> {
        expect(recieved).to.eql({defaultValue: {test: 'object for value'}})
      });
      model.checkbox.defaultValue = {test: 'object for value'};
      let serialized = model.serialize();
      let expected = _.cloneDeep(testData);
      expected.checkbox.defaultValue = {test: 'object for value'};
      expect(serialized).to.eql(expected);
    });

    it('should add new properties to model', ()=> {
      let model = new InstanceModel(_.cloneDeep(testData));
      let newModelData = {
        testField: {
          defaultValueLabel: "test Field",
          messages: [],
          valid: true,
          value: "testField",
          valueLabel: "test Field"
        }
      };
      let newModel = new InstanceModel(newModelData);
      model.addPropertiesToModel(newModel);
      expect(model._instanceModel['testField'].serialize()).to.eql(newModelData['testField']);
    });
  });

  describe('InstancePropertyModel', ()=> {
    it('should be initialized with proper getters and setters', ()=> {
      let instanceProperty = new InstanceModelProperty(_.cloneDeep(testData.checkbox));
      expect(instanceProperty.value).to.equal('test');
    });

    it('should set new item property with existing getters and setters', ()=> {
      let instanceProperty = new InstanceModelProperty(testData.activityType);
      expect(instanceProperty.valid).to.equal(undefined);
      instanceProperty.valid = true;
      expect(instanceProperty.valid).to.be.true;
    });

    it('should publish event on property change with a proper object as recieved data', ()=> {
      let value;
      let model = new InstanceModel(_.cloneDeep(testData));
      model._instanceModel.activityType.subscribe('propertyChanged', (recieved)=> {
        value = recieved;
      });
      expect(value).to.equal(undefined);
      model._instanceModel.activityType.value = 'test';
      expect(Object.keys(value)[0]).to.equal('value');
      expect(value.value).to.equal('test');
      model._instanceModel.activityType.valid = true;
      model._instanceModel.activityType.subscribe('propertyChanged', (recieved)=> {
        value = recieved;
      });
      model._instanceModel.activityType.valid = false;
      expect(Object.keys(value)[0]).to.equal('valid');
      expect(value.valid).to.be.false;
    });

    describe('Array mutation properties', ()=> {

      describe('Array method debounce', ()=> {
        it('should throw one event after 15 concecutive pushes', (done) => {
          let testData = {messages: []};
          let testInstanceProperty = new InstanceModelProperty(testData);
          testInstanceProperty.subscribe('propertyChanged', (propertyChanged) => {
            expect(propertyChanged.messages).to.eql([0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]);
            done();
          });
          for (var i = 0; i < 15; i++) {
            testInstanceProperty.messages.push(i);
          }
        });
      });

      describe('#CopyWithin()', ()=> {
        let testInstanceProperty;
        beforeEach(()=> {
          let testData = {messages: [1, 2, 3, 4, 5]};
          testInstanceProperty = new InstanceModelProperty(testData);
        });

        it('#copyWithin should function properly and fire propery changed event', (done)=> {
          let testData = {messages: ["alpha", "bravo", "charlie", "delta"]};
          let actual;
          let testInstanceProperty = new InstanceModelProperty(testData);
          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            actual = propertyChanged.messages;
            expectedData.copyWithin['(2, 0)'].forEach((element, index)=> {
              expect(element).to.equal(actual[index]);
            });
            done();
          });

          testInstanceProperty.messages.copyWithin(2, 0);
        });

        it('copyWithin(-2)', (done)=> {
          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.copyWithin['(-2)'].forEach((element, index)=> {
              expect(element).to.equal(propertyChanged.messages[index]);
            });
            done();
          });
          testInstanceProperty.messages.copyWithin(-2);
        });

        it('copyWithin(0, 3)', (done)=> {
          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.copyWithin['(0, 3)'].forEach((element, index)=> {
              expect(element).to.equal(propertyChanged.messages[index]);
            });
            done();
          });
          testInstanceProperty.messages.copyWithin(0, 3);
        });

        it('copyWithin(0, 3, 4)', (done)=> {
          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.copyWithin['(0, 3, 4)'] = [4, 2, 3, 4, 5];
            expectedData.copyWithin['(0, 3, 4)'].forEach((element, index)=> {
              expect(element).to.equal(propertyChanged.messages[index]);
            });
            done();
          });
          testInstanceProperty.messages.copyWithin(0, 3, 4);
        });

        it('copyWithin(-2, -3, -1)', (done)=> {
          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.copyWithin['(-2, -3, -1)'].forEach((element, index)=> {
              expect(element).to.equal(propertyChanged.messages[index]);
            });
            done();
          });
          testInstanceProperty.messages.copyWithin(-2, -3, -1);
        });
      });

      describe('#fill', ()=> {
        let testInstanceProperty;
        beforeEach(()=> {
          let testData = {messages: [1, 2, 3]};
          testInstanceProperty = new InstanceModelProperty(testData);
        });

        it('#fill should function properly and fire propertyChanged event', (done)=> {
          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.fill['(1)'].forEach((element, index)=> {
              expect(element).to.equal(propertyChanged.messages[index]);
            });
            done();
          });
          testInstanceProperty.messages.fill(1);
        });

        it('fill(4)', (done)=> {
          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.fill['(4)'].forEach((element, index)=> {
              expect(element).to.equal(propertyChanged.messages[index]);
            });
            done();
          });
          testInstanceProperty.messages.fill(4);
        });

        it('fill(4, 1)', (done)=> {
          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.fill['(4, 1)'].forEach((element, index)=> {
              expect(element).to.equal(propertyChanged.messages[index]);
            });
            done();
          });
          testInstanceProperty.messages.fill(4, 1);
        });

        it('fill(4, 1, 2)', (done)=> {
          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.fill['(4, 1, 2)'].forEach((element, index)=> {
              expect(element).to.equal(propertyChanged.messages[index]);
            });
            done();
          });
          testInstanceProperty.messages.fill(4, 1, 2);
        });

        it('fill(4, 1, 1)', (done)=> {
          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.fill['(4, 1, 1)'].forEach((element, index)=> {
              expect(element).to.equal(propertyChanged.messages[index]);
            });
            done();
          });
          testInstanceProperty.messages.fill(4, 1, 1);
        });

        it('fill(4, -3, -2)', (done)=> {
          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.fill['(4, -3, -2)'].forEach((element, index)=> {
              expect(element).to.equal(propertyChanged.messages[index]);
            });
            done();
          });
          testInstanceProperty.messages.fill(4, -3, -2);
        });

        it('fill(4, NaN, NaN)', (done)=> {
          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.fill['(4, NaN, NaN)'].forEach((element, index)=> {
              expect(element).to.equal(propertyChanged.messages[index]);
            });
            done();
          });
          testInstanceProperty.messages.fill(4, NaN, NaN);
        });

      });

      describe('#pop', ()=> {
        it('#pop should function properly and fire propertyChanged event and return its proper value.', (done)=> {
          let testData = {messages: [1, 2, 3]};
          let testInstanceProperty = new InstanceModelProperty(testData);

          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expectedData.pop.forEach((item, index)=> {
              expect(propertyChanged.messages[index]).to.equal(propertyChanged.messages[index]);
            });
            done();
          });

          let actualReturnValue = testInstanceProperty.messages.pop(1);
          expect(actualReturnValue).to.equal(3);
        });

      });

      describe('#reverse', ()=> {
        it('#reverse should function properly and fire propertyChanged event', (done)=> {
          let testData = {messages: ['one', 'two', 'three']};
          let testInstanceProperty = new InstanceModelProperty(testData);

          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expectedData.reverse.forEach((item, index)=> {
              expect(item).to.equal(expectedData.reverse[index]);
            });
            done();
          });

          let actualReturnValue = testInstanceProperty.messages.reverse();
          expectedData.reverse.forEach((item, index)=> {
            expect(actualReturnValue[index]).to.equal(item);
          });
        });

      });

      describe('#shift', ()=> {
        it('#shift should function properly and fire propertyChanged event', (done)=> {
          let testData = {messages: [1, 2, 3]};
          let testInstanceProperty = new InstanceModelProperty(testData);

          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expectedData.shift.forEach((item, index)=> {
              expect(item).to.equal(propertyChanged.messages[index]);
            });
            done();
          });

          let actualReturnValue = testInstanceProperty.messages.shift();
          expect(actualReturnValue).to.equal(1);
        });
      });

      describe('#sort', ()=> {
        it('#sort should function properly and fire propertyChanged event', (done) => {
          let testData = {messages: ['cherries', 'apples', 'bananas']};
          let testInstanceProperty = new InstanceModelProperty(testData);

          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expectedData.sort.forEach((item, index)=> {
              expect(item).to.equal(propertyChanged.messages[index]);
            });
            done();
          });
          testInstanceProperty.messages.sort();
        });
      });

      describe('#splice', ()=> {
        it('#splice should function properly and fire propertyChanged event', (done)=> {
          let testData = {messages: ["angel", "clown", "mandarin", "surgeon"]};
          let testInstanceProperty = new InstanceModelProperty(testData);
          let expected = ["angel", "clown", "drum", "mandarin", "surgeon"];

          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expected.forEach((item, index)=> {
              expect(item).to.equal(expected[index]);
            });
            done();
          });

          let actualReturnValue = testInstanceProperty.messages.splice(2, 0, "drum");
          expect(actualReturnValue.length).to.equal([].length);
        });

        it('should Remove 0 elements from index 2, and insert "drum"', (done)=> {
          let testData = {messages: ["angel", "clown", "mandarin", "surgeon"]};
          let testInstanceProperty = new InstanceModelProperty(testData);

          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.splice['(2, 0, "drum")'].forEach((element, index)=> {
              expect(element).to.equal(propertyChanged.messages[index]);
            });
            done();
          });

          let actualReturned = testInstanceProperty.messages.splice(2, 0, "drum");
          expect(actualReturned.length).to.equal(0);
        });

        it('should Remove 1 element from index 3', (done)=> {
          let testData = {messages: ["angel", "clown", "drum", "mandarin", "surgeon"]};

          let testInstanceProperty = new InstanceModelProperty(testData);

          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.splice['(3, 1)'].expected.forEach((element, index)=> {
              expect(element).to.equal(propertyChanged.messages[index]);
            });
            done();
          });

          let actualReturned = testInstanceProperty.messages.splice(3, 1);
          expectedData.splice['(3, 1)'].expectedRemoved.forEach((item, index)=> {
            expect(actualReturned[index]).to.equal(item);
          });
        });

        it('should Remove 1 element from index 2, and insert "trumpet"', (done)=> {
          let testData = {messages: ["angel", "clown", "drum", "surgeon"]};
          let testInstanceProperty = new InstanceModelProperty(testData);

          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.splice['(2, 1, "trumpet")'].expected.forEach((element, index)=> {
              expect(element).to.equal(propertyChanged.messages[index]);
            });
            done();
          });

          let actualReturned = testInstanceProperty.messages.splice(2, 1, 'trumpet');
          expectedData.splice['(2, 1, "trumpet")'].expectedRemoved.forEach((item, index)=> {
            expect(actualReturned[index]).to.equal(item);
          });
        });

        it('should Remove 2 elements from index 0, and insert "parrot", "anemone" and "blue"', (done)=> {
          let testData = {messages: ["angel", "clown", "trumpet", "surgeon"]};
          let testInstanceProperty = new InstanceModelProperty(testData);

          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.splice['(0, 2, "parrot", "anemone", "blue")'].expected.forEach((element, index)=> {
              expect(element).to.equal(propertyChanged.messages[index]);
            });
            done();
          });

          let actualReturned = testInstanceProperty.messages.splice(0, 2, "parrot", "anemone", "blue");
          expectedData.splice['(0, 2, "parrot", "anemone", "blue")'].expectedRemoved.forEach((item, index)=> {
            expect(actualReturned[index]).to.equal(item);
          });
        });

        it('should Remove 2 elements from index 2', (done)=> {
          let testData = {messages: ["parrot", "anemone", "blue", "trumpet", "surgeon"]}
          let testInstanceProperty = new InstanceModelProperty(testData);

          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.splice['(testInstanceProperty.messages.length - 3, 2)'].expected.forEach((element, index)=> {
              expect(element).to.equal(propertyChanged.messages[index]);
            });
            done();
          });

          let actualReturned = testInstanceProperty.messages.splice(testInstanceProperty.messages.length - 3, 2);
          expectedData.splice['(testInstanceProperty.messages.length - 3, 2)'].expectedRemoved.forEach((item, index)=> {
            expect(actualReturned[index]).to.equal(item);
          });
        });

        it('should Remove 1 element from index -2', (done)=> {
          let testData = {messages: ["angel", "clown", "mandarin", "surgeon"]}
          let testInstanceProperty = new InstanceModelProperty(testData);

          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.splice['(-2, 1)'].expected.forEach((element, index)=> {
              expect(element).to.equal(propertyChanged.messages[index]);
            });
            done();
          });

          let actualReturned = testInstanceProperty.messages.splice(-2, 1);
          expectedData.splice['(-2, 1)'].expectedRemoved.forEach((item, index)=> {
            expect(actualReturned[index]).to.equal(item);
          });
        });

      });

      describe('#unshift', ()=> {
        it('#unshift should function properly and fire propertyChanged event', (done)=> {
          let testData = {messages: [1, 2, 3]};
          let testInstanceProperty = new InstanceModelProperty(testData);

          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages).to.be.true;
            expectedData.unshift.expected.forEach((item, index)=> {
              expect(propertyChanged.messages[index]).to.equal(item);
            });
            done();
          });

          let actualReturned = testInstanceProperty.messages.unshift(4, 5);
          expect(actualReturned).to.equal(expectedData.unshift.returned);
        });
      });

      describe('#push', ()=> {
        let testInstanceProperty;
        beforeEach(()=> {
          let testData = {messages: [1, 2, 3]};
          testInstanceProperty = new InstanceModelProperty(testData);
        });

        it('#push should function properly and fire propertyChanged event', (done)=> {
          testInstanceProperty.subscribe('propertyChanged', (propertyChanged)=> {
            expect(!!propertyChanged.messages, 'event fired on correct property').to.be.true;
            expectedData.push.expected.forEach((item, index)=> {
              expect(propertyChanged.messages[index]).to.equal(item);
            });
            done();
          });

          let returnedValue = testInstanceProperty.messages.push(4, 5);
          expect(returnedValue).to.equal(expectedData.push.returned)
        });
      });
    });
  });
  var testData = {
    activityType: {
      defaultValue: undefined,
      messages: [{id: 'mandatory', level: 'error', message: 'This field is mandatory'}],
      value: null,
      valueLabel: '',
    },
    breadcrumb_header: {
      defaultValue: undefined,
      messages: [],
      valid: true,
      value: undefined
    },
    checkbox: {
      defaultValue: 'test',
      messages: [],
      valid: 'true',
      value: 'test',
    },
    'emf:contentId': {
      defaultValue: undefined,
      messages: [],
      valid: true,
    },
    'emf:hasParent': {
      defaultValue: {
        results: ['emf:f1be0ccc-fc91-41ed-b2a9-59d136a513ec'],
        total: 1,
        limit: 0
      },
      value: {
        results: ['emf:f1be0ccc-fc91-41ed-b2a9-59d136a513ec'],
        total: 1,
        limit: 0
      },
      messages: []
    },
    lockedBy: {
      defaultValue: {
        results: ['emf:admin-georgi.com'],
        total: 1,
        limit: 0
      },
      value: {
        results: ['emf:admin-georgi.com'],
        total: 1,
        limit: 0
      },
      messages: [],
      valid: true
    },
    type: {
      definitionId: "UI210001",
      defaultValueLabel: "UI2 Document for testing",
      messages: [],
      valid: true,
      value: "UI210001",
      valueLabel: "UI2 Document for testing"
    },
    version: {
      defaultValue: '1.0',
      messages: [],
      valid: true,
      value: '1.0'
    }
  };

  var expectedData = {
    copyWithin: {
      '(2, 0)': ["alpha", "bravo", "alpha", "bravo"],
      '(-2)': [1, 2, 3, 1, 2],
      '(0, 3)': [4, 5, 3, 4, 5],
      '(0, 3, 4)': [4, 2, 3, 4, 5],
      '(-2, -3, -1)': [1, 2, 3, 3, 4]
    },
    fill: {
      '(1)': [1, 1, 1],
      '(4)': [4, 4, 4],
      '(4, 1)': [1, 4, 4],
      '(4, 1, 2)': [1, 4, 3],
      '(4, 1, 1)': [1, 2, 3],
      '(4, -3, -2)': [4, 2, 3],
      '(4, NaN, NaN)': [1, 2, 3]
    },
    pop: [1, 2],
    reverse: ['three', 'two', 'one'],
    shift: [2, 3],
    sort: ['apples', 'bananas', 'cherries'],
    splice: {
      '(2, 0, "drum")': ["angel", "clown", "drum", "mandarin", "surgeon"],
      '(3, 1)': {
        expected: ["angel", "clown", "drum", "surgeon"],
        expectedRemoved: ["mandarin"]
      },
      '(2, 1, "trumpet")': {
        expected: ["angel", "clown", "trumpet", "surgeon"],
        expectedRemoved: ['drum']
      },
      '(0, 2, "parrot", "anemone", "blue")': {
        expected: ["parrot", "anemone", "blue", "trumpet", "surgeon"],
        expectedRemoved: ["angel", "clown"]
      },
      '(testInstanceProperty.messages.length - 3, 2)': {
        expected: ["parrot", "anemone", "surgeon"],
        expectedRemoved: ["blue", "trumpet"]
      },
      '(-2, 1)': {
        expected: ["angel", "clown", "surgeon"],
        expectedRemoved: ["mandarin"]
      }
    },
    unshift: {expected: [4, 5, 1, 2, 3], returned: 5},
    push: {expected: [1, 2, 3, 4, 5], returned: 5}
  }
});