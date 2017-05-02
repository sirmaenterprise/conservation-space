/**
 * Created by tdossev on 21.10.2016 г..
 */
import {Filter} from 'common/filter/filter';

describe('Filter', function() {
  it('should throw error if implementations do not implement an canHandle function', function() {
    expect(function() {
      new WrongCustomFilter();
    }).to.throw(TypeError);
  });

  it('should throw error if implementations do not implement an filter function', function() {
    expect(function() {
      new AnotherWrongCustomFilter();
    }).to.throw(TypeError);
  });

  it('should throw error if implementations do not implement an filter and canHandle function', function() {
    expect(function() {
      new TotallyWrongCustomFilter();
    }).to.throw(TypeError);
  });

  it('should not throw error if implementations implements an execute function', function() {
    expect(function() {
      new CustomFilter();
    }).to.not.throw(TypeError);
  });
});

class CustomFilter extends Filter {
  filter() {}
  canHandle() {}
}

class WrongCustomFilter extends Filter {
  filter() {}
}

class AnotherWrongCustomFilter extends Filter {
  canHandle() {}
}

class TotallyWrongCustomFilter extends Filter {
}