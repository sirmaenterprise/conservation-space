import {Command} from 'common/command-chain/command-chain';

export class BooleanConverter extends Command {
  canHandle(data) {
    return typeof data === 'boolean' || data === 'true' || data === 'false';
  }

  handle(data) {
    if (typeof data === 'boolean') {
      return data;
    }
    if (typeof data === 'string') {
      return data === 'true';
    }
  }
}

export class NumberConverter extends Command {
  canHandle(data) {
    // Notes on implementation below:
    // Number(true) => 1
    // Number(false) => 0
    // Number([]) => 0
    // Number([1, 2]) => 1
    // Number(null) => 0
    return typeof data === 'number' ||
      (!Number.isNaN(Number(data)) && data !== true && data !== false && data !== null && data.constructor !== Array);
  }

  handle(data) {
    return Number(data);
  }
}

export class StringConverter extends Command {
  canHandle(data) {
    return typeof data === 'string';
  }

  handle(data) {
    return data;
  }
}