import {Settings} from 'layout/top-header/main-menu/user-menu/settings/settings';
import {ThumbnailUpdatedEvent} from 'idoc/actions/events/thumbnail-updated-event';

import {IdocMocks} from 'test/idoc/idoc-mocks';

describe('Settings', () => {
  let mockCompile;
  let mockElement;
  let mockScope;
  let mockUserService;
  let settings;

  beforeEach(() => {
    mockElement = IdocMocks.mockElement();
    mockScope = IdocMocks.mockScope();
    mockCompile = mockCompileService();
    mockUserService = IdocMocks.mockUserService();

    settings = new Settings(mockElement, mockScope, mockCompile, mockUserService);
  });

  describe('#ngOnInit', () => {
    it('should initially compile the header', () => {
      let prependSpy = sinon.spy(mockElement, 'prepend');
      settings.ngOnInit();
      expect(prependSpy.calledOnce).to.be.true;
    });
  });
});

export function mockCompileService() {
  return () => () => {
  }
}