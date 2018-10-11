export class ResizeDetectorAdapterMock {
  static mockAdapter() {
    return {
      addResizeListener: () => {
        return sinon.spy();
      }
    }
  }
}
