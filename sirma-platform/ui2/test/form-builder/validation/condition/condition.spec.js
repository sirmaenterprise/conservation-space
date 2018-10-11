import {Condition} from 'form-builder/validation/condition/condition';
import {ConditionEvaluator} from 'form-builder/validation/condition-evaluator';

describe('Condition validator', function () {

  describe('validate', () => {
    it('should call conditionEvaluator with proper arguments', () => {
      let conditionEvaluator = new ConditionEvaluator({}, {
        get: ()=> {
        }
      });
      let stubEvaluate = sinon.stub(conditionEvaluator, 'evaluate');
      let validator = new Condition(conditionEvaluator);
      let argArray = ['field1', {}, {}, {}];
      validator.validate.apply(validator, argArray);
      expect(stubEvaluate.calledOnce).to.be.true;
      expect(stubEvaluate.getCall(0).args).to.eql(argArray);
    });
  });

});