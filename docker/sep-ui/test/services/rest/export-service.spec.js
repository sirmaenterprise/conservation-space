import { ExportService } from 'services/rest/export-service';

describe('ExportService', () => {
  let restClient = {};
  let exportService = new ExportService(restClient);

  it('exportPDF should perform post with proper arguments', () => {
    restClient.post = sinon.spy();
    let objectId = 'emf:123456';
    exportService.exportPDF(objectId, null);
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/export');
    expect(restClient.post.getCall(0).args[1]).to.equal('/#/idoc/emf:123456?mode=print');
  });

  it('getExportedFile should perform get with proper arguments', () => {
    restClient.get = sinon.spy();
    exportService.getExportedFile('fileName');
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/export/fileName');
    expect(restClient.get.getCall(0).args[1]).to.eql({ headers: { 'Accept': 'application/pdf' }, responseType: 'arraybuffer' });
  });

  it('exportXlsx should perform post with proper arguments', () => {
    restClient.post = sinon.spy();
    let data = {};
    exportService.exportXlsx(data);
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/instances/undefined/actions/export-xlsx');
  });
});