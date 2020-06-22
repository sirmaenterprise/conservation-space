export class PromiseStub {
  static resolve(value) {
    let thenable = buildThenable();
    thenable.resolved = true;
    thenable.rejected = false;
    thenable.resolveValue = value;
    return thenable;
  }

  static reject(value) {
    let thenable = buildThenable();
    thenable.resolved = false;
    thenable.rejected = true;
    thenable.rejectValue = value;
    return thenable;
  }

  static all(promises) {
    let result = [];
    let rejected;

    promises.forEach((promise) => {
      if (promise.rejected) {
        rejected = promise.rejectValue;
      }
      promise.then((promiseResult) => {
        result.push(promiseResult);
      });
    });

    if (rejected !== undefined) {
      return PromiseStub.reject(rejected);
    }
    return PromiseStub.resolve(result);
  }

  static promise(executor) {
    let thenable = buildThenable();
    executor((resolved) => {
      thenable.resolved = true;
      thenable.resolveValue = resolved
    }, (rejected) => {
      thenable.rejected = true;
      thenable.rejectValue = rejected;
    });
    return thenable;
  }

  static defer() {
    return {
      promise: buildThenable()
    }
  }
}

export function buildThenable() {
  return {
    then: function (onFulfill, onReject) {
      if (this.resolved && !this.rejected) {
        let returned = onFulfill(this.resolveValue);

        // promise returned, return that for next handler in chain
        if (returned && returned.then) {
          return returned;
        }

        // update resolve value for next promise in chain
        if (returned !== undefined) {
          this.resolveValue = returned;
        }
        return this;
      }

      if (this.rejected && onReject) {
        return this.catch(onReject);
      }
      return this;
    },

    catch: function (onReject) {
      if (this.rejected) {
        try {
          const value = onReject(this.rejectValue);
          this.resolved = true;
          this.rejected = false;
          this.resolveValue = value;
          this.rejectValue = undefined;
        } catch (e) {
          this.rejectValue = e;
        }
        return this;
      }
      return this;
    },

    finally: function (callback) {
      if (this.resolved || this.rejected) {
        callback();
      }
      return this;
    }
  };
}