import {WindowMessageDispatcher} from 'common/window-message-dispatcher';

describe('WindowMessageDispatcher', () => {
  let $rootScope = {
    $evalAsync: (callback) => callback()
  };
  let windowMessageDispatcher = new WindowMessageDispatcher($rootScope);
  let publishSpy = sinon.spy(windowMessageDispatcher, 'publish');

  beforeEach(() => {
    publishSpy.reset();
  });

  describe('#onMessageListener', () => {
    it('should exclude messages coming with the same origin as the window location', () => {
      let message = {
        origin: window.location.href
      };
      windowMessageDispatcher.onMessageListener(message);
      expect(publishSpy.called).to.be.false;
    });
    it('should publish extracted topic and data', () => {
      let message = {
        origin: 'test-origin.com',
        data: {topic: 'TEST_TOPIC', data: `TEST_DATA`}
      };
      windowMessageDispatcher.subscribe('TEST_TOPIC', (recieved) => {
        expect(recieved.data).to.equal('TEST_DATA');
      });
      windowMessageDispatcher.onMessageListener(message);
    })
  })
});
