import {Storage} from 'services/storage/storage';

describe('Storage', function() {

  describe('getJson(key)', function() {

    var service;
    beforeEach(function() {
      service = new Storage();
      service.get = function(key) {
        switch(key) {
          case 'empty':
            return '';
          case 'null':
            return null;
          case 'undef':
            return undefined;
          case 'obj':
            return '{"a": 1}';
          case 'arr':
            return '[1,2,3]';
          case 'primitive':
            return 'false';
          default:
            return undefined;
        }
      };
    });

    it('should return null if value for key is falsy', function() {
      expect(service.getJson('empty')).to.be.null;
      expect(service.getJson('null')).to.be.null;
      expect(service.getJson('undef')).to.be.null;
    });

    it('should support objects', function() {
      expect(service.getJson('obj')).to.deep.eq({a: 1});
    });

    it('should support arrays', function() {
      expect(service.getJson('arr')).to.deep.eq([1, 2, 3]);
    });

    it('should support primitives', function() {
      expect(service.getJson('primitive')).to.eq(false);
    });

    it('should return default value if value is falsy', function() {
      expect(service.getJson('test', [1, 2, 3])).to.deep.eq([1, 2, 3]);
    });
  });
});