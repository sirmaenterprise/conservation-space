export class StateParamsAdapterMock {
  static mockAdapter() {
    let stateParams = {};
    return {
      setStateParam: (key, value) => {
        stateParams[key] = value;
      },
      getStateParams: () => {
        return stateParams;
      },
      getStateParam: (key) => {
        return stateParams[key];
      }
    };
  }
}