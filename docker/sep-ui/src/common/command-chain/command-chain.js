import _ from 'lodash';

/**
 * The command chain allows commands that can handle particular problems to be registered and invoked in sequence of
 * registration order until one that is capable of handling the problem is found. Then that command is executed and the
 * result is returned.
 */
export class CommandChain {

  constructor(commands = []) {
    this.commands = commands;
  }

  add(command) {
    this.commands.push(command);
    return this;
  }

  execute(data) {
    let handler = _.find(this.commands, (command) => {
      return command.canHandle(data);
    });
    if (!handler) {
      throw Error(`Handler for request ${data} is not found!`);
    }
    return handler.handle(data);
  }
}

export class Command {
  constructor() {
    if (typeof this.handle !== 'function') {
      throw new TypeError('Command must override the \'handle\' function!');
    }
    if (typeof this.canHandle !== 'function') {
      throw new TypeError('Command must override the \'canHandle\' function!');
    }
  }
}