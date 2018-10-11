import {InstanceUtils} from 'instance/utils';
import {CURRENT_OBJECT_TEMP_ID} from 'models/instance-object';

describe('InstanceUtils', function () {

  describe('isVersion', function () {

    it('should return false if the instance is undefined', function () {
      let result = InstanceUtils.isVersion(null);
      expect(result).to.be.false;
    });

    it('should return false if the instance isVersion is not a function', function () {
      let instance = {
        id: 'instance-id',
        isVersion: []
      };
      let result = InstanceUtils.isVersion(instance);
      expect(result).to.be.false;
    });

    it('should return true if the instance isVersion is a function and it result is true', function () {
      let instance = {
        id: 'instance-id',
        isVersion: function () {
          return true;
        }
      };
      let result = InstanceUtils.isVersion(instance);
      expect(result).to.be.true;
    });

    it('should return false if the instance does not contain property isVersion', function () {
      let instance = {
        id: 'instance-id',
        properties: {property1: 'something'}
      };
      let result = InstanceUtils.isVersion(instance);
      expect(result).to.be.false;
    });

    it('should return true if the instance contain property isVersion and it value is true', function () {
      let instance = {
        id: 'instance-id',
        properties: {isVersion: true}
      };
      let result = InstanceUtils.isVersion(instance);
      expect(result).to.be.true;
    });
  });

  it('should return true for temp id', () => {
    let result = InstanceUtils.isTempId(CURRENT_OBJECT_TEMP_ID);
    expect(result).to.be.true;
  });

  it('should return false for other id', () => {
    let result = InstanceUtils.isTempId("emf:id");
    expect(result).to.be.false;
  });

  it('should return false for undefined ', () => {
    let result = InstanceUtils.isTempId(undefined);
    expect(result).to.be.false;
  });

  it('should return false for null', () => {
    let result = InstanceUtils.isTempId(null);
    expect(result).to.be.false;
  });

});