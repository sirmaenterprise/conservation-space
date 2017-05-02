import {ActionHandler} from 'services/actions/action-handler';

describe('ActionHandler', function() {
  it('should throw error if implementations do not implement an execute function', function() {
    expect(function() {
      new WrongCustomAction();
    }).to.throw(TypeError);
  });

  it('should not throw error if implementations implements an execute function', function() {
    expect(function() {
      new CustomAction();
    }).to.not.throw(TypeError);
  });
});

class CustomAction extends ActionHandler {
  execute() {}
}
class WrongCustomAction extends ActionHandler {
}