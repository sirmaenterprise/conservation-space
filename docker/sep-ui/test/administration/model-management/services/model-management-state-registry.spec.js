import {ModelManagementStateRegistry} from 'administration/model-management/services/model-management-state-registry';

describe('ModelManagementStateRegistry', () => {

  let registry;
  beforeEach(() => registry = new ModelManagementStateRegistry());

  it('should set the dirty state of given section', () => {
    registry.setSectionState('general', true);
    registry.setSectionState('fields', false);
    expect(registry.sectionStates['general']).to.be.true;
    expect(registry.sectionStates['fields']).to.be.false;
  });

  it('should clear section states', () => {
    // Should be empty by default
    expect(registry.sectionStates).to.deep.equal({});
    registry.setSectionState('general', true);
    registry.clearSectionStates();
    expect(registry.sectionStates).to.deep.equal({});
  });

  it('should determine if there is a dirty state among the sections', () => {
    expect(registry.hasDirtyState()).to.be.false;
    registry.setSectionState('fields', false);
    expect(registry.hasDirtyState()).to.be.false;
    registry.setSectionState('general', true);
    expect(registry.hasDirtyState()).to.be.true;
  });
});