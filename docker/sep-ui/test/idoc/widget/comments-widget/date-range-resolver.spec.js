import {DateRangeResolver} from 'idoc/widget/comments-widget/date-range-resolver';

describe('Date range resolver', () => {
  let dateRangeResolver;

  let dynamicDateRangeResolver = {
    resolveRule: sinon.spy()
  };

  beforeEach(() => {
    dateRangeResolver = new DateRangeResolver(dynamicDateRangeResolver);
  });

  it('should resolve dynamic rule', ()=> {

    let rule = {
      operator: 'within',
      value: 'asdf'
    };

    dateRangeResolver.resolveRule(rule);
    expect(dynamicDateRangeResolver.resolveRule.called).to.be.true;
  });

  it('should resolve is before rule', ()=> {
    let rule = {
      operator: 'before',
      value: 10
    };

    let resolved = dateRangeResolver.resolveRule(rule);
    expect(resolved[1]).to.equal(rule.value);
  });

  it('should resolve is before rule', ()=> {
    let rule = {
      operator: 'after',
      value: 10
    };

    let resolved = dateRangeResolver.resolveRule(rule);
    expect(resolved[0]).to.equal(rule.value);
  });

  it('should resolve rule with no operator', ()=> {
    let rule = {
      value: 10
    };

    let resolved = dateRangeResolver.resolveRule(rule);
    expect(resolved[0]).to.equal('');
  });

  it('should resolve between rule', ()=> {
    let rule = {
      operator: 'between',
      value: 10
    };

    let resolved = dateRangeResolver.resolveRule(rule);
    expect(resolved).to.equal(rule.value);
  });
});
