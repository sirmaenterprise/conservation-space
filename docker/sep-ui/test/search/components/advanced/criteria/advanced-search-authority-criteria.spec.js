import {AdvancedSearchAuthorityCriteria} from 'search/components/advanced/criteria/advanced-search-authority-criteria';

describe('AdvancedSearchAuthorityCriteria', () => {

  let advancedSearchAuthorityCriteria;

  beforeEach(() => {
    advancedSearchAuthorityCriteria = new AdvancedSearchAuthorityCriteria();
  });

  it('should not be disabled by default', () => {
    expect(advancedSearchAuthorityCriteria.config.disabled).to.be.false;
  });

  it('should proper convert resources to authority objects', () => {
    let resource = {
      id: 'emf:tester1',
      label: 'Tester1',
      type: 'user',
      value: 'tester1@test'
    };
    let expectedAuthority = {
      id: 'tester1@test',
      text: 'Tester1',
      type: 'user',
      value: 'tester1@test'
    };
    let converted = advancedSearchAuthorityCriteria.convertResource(resource);
    expect(converted).to.deep.equals(expectedAuthority);
  });

  it('should build proper select config', () => {
    let selectConfig = {
      includeUsers: true,
      includeGroups: false
    };
    advancedSearchAuthorityCriteria.ngOnInit();
    expect(advancedSearchAuthorityCriteria.selectConfig.includeUsers).to.equal(selectConfig.includeUsers);
    expect(advancedSearchAuthorityCriteria.selectConfig.includeGroups).to.equal(selectConfig.includeGroups);
  });
});

