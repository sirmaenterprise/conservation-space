import {User} from 'form-builder/user/user'
import {InstanceModelProperty} from 'models/instance-model';

describe('User ', () => {
  describe('Set user createdBy value', ()=> {
    it('Should return a title value', ()=> {
      User.prototype.fieldViewModel = {
        identifier: 'createdBy'
      };
      User.prototype.validationModel = {
        createdBy: {
          value: {
            title: 'John Doe'
          }
        }
      };
      expect(new User().validationModel.createdBy.value.title).to.equal('John Doe');
    });

    it('Should not return an empty array if value is not set', ()=> {
      User.prototype.fieldViewModel = {
        identifier: 'createdBy'
      };
      User.prototype.validationModel = {
        createdBy: {}
      };
      //validation model property is wrapped in a instance model property
      expect(new User().validationModel.createdBy).to.eql({});
    });
  })
});
