export class SelectMocks {

  static mockElement() {
    let listeners = {};
    return {
      find: () => {
        var element = {
          select2: sinon.spy(() => {}),
          width: () => {},
          listeners: listeners,
          on: sinon.spy((event, listener) => {
            listeners[event] = listener;
            return element;
          }),
          change: sinon.spy(),
          trigger: sinon.spy(),
          empty: sinon.spy(),
          data: () => {
            return {};
          },
          focus: sinon.spy(),
          val: () => {return {trigger: ()=> {}}},
          append: sinon.spy()
        };
        return element;
      },
      controller: () => {
        return {};
      }
    };
  }

  static mockTimeout() {
    return (fn) => {
      fn();
    };
  }
}
