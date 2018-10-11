import {AdminToolRegistry} from 'administration/admin-tool-registry';

describe('AdminToolRegistry', () => {

  let registry;
  beforeEach(() => registry = new AdminToolRegistry());

  it('should store the state of specific administration tool', () => {
    expect(registry.stateRegistry).to.deep.equal({});
    registry.setState('tool', true);
    expect(registry.stateRegistry['tool']).to.equal(true);
  });

  it('should clear all present states', () => {
    registry.setState('tool', true);
    registry.clearStates();
    expect(registry.stateRegistry).to.deep.equal({});
  });

  it('should determine if a tool has unsaved changes', () => {
    expect(registry.hasUnsavedState()).to.be.false;

    registry.setState('tool', false);
    expect(registry.hasUnsavedState()).to.be.false;

    registry.setState('tool2', true);
    expect(registry.hasUnsavedState()).to.be.true;
  });

  it('should extract the state of a given tool', () => {
    registry.setState('tool', true);
    expect(registry.getState('tool')).to.be.true;

    registry.clearStates();
    expect(registry.getState('tool')).to.be.false;
  });
});
