import {CommandChain, Command} from 'common/command-chain/command-chain';

describe('CommandChain', () => {
  it('should invoke registered commands and execute one that can handle the request', () => {
    let chain = new CommandChain();
    let result = chain.add(new Command1()).add(new Command2()).execute('string');
    expect(result).to.equal('string=string');
    result = chain.execute(true);
    expect(result).to.equal('boolean=true');
  });

  it('should throw Error if no handler command is found', () => {
    expect(() => {
      let chain = new CommandChain();
      chain.add(new Command1()).execute(true);
    }).to.throw(Error);
  });

  it('Command implementations should require canHandle method', () => {
    expect(() => {
      new CommandWithoutCanHandleMethod();
    }).to.throw(TypeError);
  });

  it('Command implementations should require handle method', () => {
    expect(() => {
      new CommandWithoutHandleMethod();
    }).to.throw(TypeError);
  });

  class Command1 extends Command {
    handle(data) {
      return 'string=' + data;
    }

    canHandle(data) {
      return typeof data === 'string';
    }
  }

  class Command2 extends Command {
    handle(data) {
      return 'boolean=' + data;
    }

    canHandle(data) {
      return typeof data === 'boolean';
    }
  }

  class CommandWithoutHandleMethod extends Command {
    canHandle() {

    }
  }

  class CommandWithoutCanHandleMethod extends Command {
    handle() {

    }
  }
});