import {PromiseStub} from 'test/promise-stub';

export class PromiseAdapterMock {
  static mockAdapter() {
    return {
      promise: (executor) => {
        return new Promise(executor);
      },
      reject: (executor) => {
        return Promise.reject(executor);
      },
      resolve: (executor) => {
        return Promise.resolve(executor);
      },
      all: (executor) => {
        return Promise.all(executor);
      },
      defer: () => {
        return {
          promise: {},
          resolve: () => {}
        }
      }
    };
  }

  static mockImmediateAdapter() {
    return {
      promise: (executor) => {
        return PromiseStub.promise(executor);
      },
      reject: (executor) => {
        return PromiseStub.reject(executor);
      },
      resolve: (executor) => {
        return PromiseStub.resolve(executor);
      },
      all: (executor) => {
        return PromiseStub.all(executor);
      }
    };
  }
}