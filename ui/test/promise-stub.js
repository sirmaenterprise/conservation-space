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

  static all(value) {
    let promiseAll = {
      then: (func) => {
        let result = [];
        value.forEach((promise) => {
          promise.then((promiseResult) => {
            result.push(promiseResult);
          });
        });
        func(result);
        return promiseAll;
      },
      catch: () => {}
    };
    return promiseAll;
  }

  static promise(executor) {
    var resolvedValue;
    var rejectedValue;
    var isResolved;
    executor((resolved) => {
      isResolved = true;
      resolvedValue = resolved;
    }, (rejected) => {
      rejectedValue = rejected;
    });

    if (isResolved) {
      return PromiseStub.resolve(resolvedValue);
    }
    return PromiseStub.reject(rejectedValue);
  }
}

export function buildThenable() {
  return {
    then: function (onFulfill, onReject) {
      if (this.resolved && !this.rejected) {
        var returned = onFulfill(this.resolveValue);

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
    }
  };
}