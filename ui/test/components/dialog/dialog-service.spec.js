import {DialogService} from 'components/dialog/dialog-service';

function mockEventBus() {
  return {
    subscribe: function() {}
  }
}

function mockTranslateService() {
  return {
    translateInstant: function(id) {
      return `label-for-${id}`;
    }
  }
}

describe('DialogService', function() {
  var service;

  beforeEach(() => {
    service = new DialogService(null, null, null, mockEventBus(), mockTranslateService())
  });

  describe('createButton()', function() {

    it('should create a button with id and label', function() {
      var btn = service.createButton('a', 'b');

      expect(btn).to.deep.eq({
        id: 'a',
        label: 'label-for-b'
      });
    });

    it('should add primary cls if third params is true', function() {
      var btn = service.createButton('a', 'b', true);

      expect(btn).to.deep.eq({
        id: 'a',
        label: 'label-for-b',
        cls: 'btn-primary'
      });
    });
  });

})