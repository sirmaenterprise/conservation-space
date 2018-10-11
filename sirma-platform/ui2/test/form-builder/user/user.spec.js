import {User} from 'form-builder/instance-type-resource/user/user';
import {EventEmitter} from 'common/event-emitter';
import {stub} from 'test-utils';
import {mockFormWrapper} from 'test/form-builder/form-wrapper-mock';

describe('User field', () => {

  describe('Set user createdBy value', () => {

    it('should return a title value', () => {
      User.prototype.formWrapper = mockFormWrapper()
        .setValidationModel({
          createdBy: {
            value: {
              title: 'John Doe'
            }
          }
        }).setFieldsMap({
          createdBy: {
            identifier: 'createdBy'
          }
        }).get();

      User.prototype.identifier = 'createdBy';

      expect(new User().validationModel.createdBy.value.title).to.equal('John Doe');
    });

    it('should not return an empty array if value is not set', () => {
      User.prototype.formWrapper = mockFormWrapper()
        .setValidationModel({
          createdBy: {}
        }).setFieldsMap({
          createdBy: {
            identifier: 'createdBy'
          }
        }).get();

      User.prototype.identifier = 'createdBy';

      //validation model property is wrapped in a instance model property
      expect(new User().validationModel.createdBy).to.eql({});
    });
  });

  describe('#ngAfterViewInit', () => {

    it('should not emit event by default to formWrapper',() => {
      let user = new User();
      user.formEventEmitter = stub(EventEmitter);
      user.ngAfterViewInit();
      expect(user.formEventEmitter.publish.callCount).to.equal(0);
    });
  });

});
