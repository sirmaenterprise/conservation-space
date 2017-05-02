import {PdfViewer} from 'common/pdf-viewer';

describe('PdfViewer', () => {
  it('should construct pdf path with pdf.js configuration arguments', () => {
    let pdfViewer = new PdfViewer('file/path', {
      page: 2,
      zoom: 100
    });
    expect(pdfViewer.src).to.equal('/common/lib/pdfjs/web/viewer.html?file=file%2Fpath#page=2&zoom=100');
  });

  it('should not add pdf.js configuration arguments if viewerConfig is an empty object', () => {
    let pdfViewer = new PdfViewer('file/path', {});
    expect(pdfViewer.src).to.equal('/common/lib/pdfjs/web/viewer.html?file=file%2Fpath');
  });

  it('should not add pdf.js configuration arguments if viewerConfig is undefined', () => {
    let pdfViewer = new PdfViewer('file/path');
    expect(pdfViewer.src).to.equal('/common/lib/pdfjs/web/viewer.html?file=file%2Fpath');
  });
});