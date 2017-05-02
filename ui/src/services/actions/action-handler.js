/**
 * Abstract class for every action handler. Forces implementations to implement the execute function.
 *
 * All implementations should ensure that execute() return a promise which is resolved when the action is completed.
 * If some action cannot return a promise it should not return anything (undefined).
 *
 * @author svelikov
 */
export class ActionHandler {

  constructor() {
    if (typeof this.execute !== 'function') {
      throw new TypeError('Action handlers must override the \'execute\' function!');
    }
  }

}
