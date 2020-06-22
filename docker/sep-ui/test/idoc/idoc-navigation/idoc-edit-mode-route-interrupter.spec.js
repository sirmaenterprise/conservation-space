import {IdocEditModeRouteInterrupter} from 'idoc/idoc-navigation/idoc-edit-mode-route-interrupter';
import {MODE_EDIT, IDOC_STATE, MODE_PREVIEW} from 'idoc/idoc-constants';

describe('IdocEditModeRouteInterrupter', ()=> {

  describe('shouldInterrupt', () => {
    it('should interrupt idoc in edit mode and session did not timed out', () => {
      let routeInterrupter = new IdocEditModeRouteInterrupter(mockLocalStorageService('false'));
      expect(routeInterrupter.shouldInterrupt(mockRouter(IDOC_STATE, MODE_EDIT))).to.be.true;
    });

    it('should not interrupt idoc in preview mode', () => {
      let routeInterrupter = new IdocEditModeRouteInterrupter(mockLocalStorageService('false'));
      expect(routeInterrupter.shouldInterrupt(mockRouter(IDOC_STATE, MODE_PREVIEW))).to.be.false;
    });

    it('should not interrupt idoc in edit mode when session has timed out', () => {
      let routeInterrupter = new IdocEditModeRouteInterrupter(mockLocalStorageService('true'));
      expect(routeInterrupter.shouldInterrupt(mockRouter(IDOC_STATE, MODE_PREVIEW))).to.be.false;
    });
  });
});

function mockRouter(name, mode) {
  return {
    $state: {
      current: {
        name: name
      },
      params: {
        mode: mode
      }
    },
    getCurrentState: function() {
      return name;
    }
  }
}

function mockLocalStorageService(value) {
  return {
    get: function() {
      return value;
    }
  }
}