import {ComposeEmailAction} from 'idoc/actions/compose-email-action';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseStub} from '../../promise-stub';
import {stub} from 'test/test-utils';

describe('ComposeEmailAction', () => {
  let dialogServiceSpy = {create: sinon.spy()};
  beforeEach(() => {
    dialogServiceSpy.create.reset();
  });
  it('should call dialog service in object context with proper configuration', () => {
    let mockContext = {idocContext: {}};
    let composeEmailAction = new ComposeEmailAction(dialogServiceSpy, PromiseStub);
    composeEmailAction.execute({}, mockContext);

    let mailboxConfig = dialogServiceSpy.create.args[0][1];
    let dialogConfig = dialogServiceSpy.create.args[0][2];
    expect(mailboxConfig).to.eql({context: mockContext.idocContext, mailboxViewType: 'compose'});
    expect(mailboxConfig.context).to.eql(mockContext.idocContext);
    expect(mailboxConfig.mailboxViewType).to.equal('compose');
    expect(dialogConfig.header).to.equal('idoc.compose.email.header');
    expect(dialogConfig.showClose).to.be.true;
    expect(dialogConfig.backdrop).to.equal('static');
    expect(dialogConfig.largeModal).to.be.true;
  });

  it('should call instance service to get email address field when outside of object context', () => {
    let mockContext = {currentObject: {id: 'test-object-id'}};
    let instanceServiceStub = stub(InstanceRestService);
    instanceServiceStub.load.withArgs('test-object-id').returns(PromiseStub.resolve({
      data: {properties: {emailAddress: 'test-object@domain.com', title: 'instance title'}}
    }));
    let composeEmailAction = new ComposeEmailAction(dialogServiceSpy, PromiseStub, instanceServiceStub);
    composeEmailAction.execute({}, mockContext);

    let mailboxConfig = dialogServiceSpy.create.args[0][1];
    let dialogConfig = dialogServiceSpy.create.args[0][2];
    expect(mailboxConfig.mailboxViewType).to.equal('compose');
    mailboxConfig.context.getCurrentObject().then((response) => {
      expect(response.id).to.equal('test-object-id');
      expect(response.getModels()).to.eql({validationModel: {emailAddress: {value: 'test-object@domain.com'}, title: {value: 'instance title'}}});
    });
  });
});