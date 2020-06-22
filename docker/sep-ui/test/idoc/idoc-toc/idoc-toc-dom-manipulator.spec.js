import {ToCDomManipulator} from 'idoc/idoc-toc/idoc-toc-dom-manipulator';

describe('Tests for the idoc table of contents : dom-operations', ()=> {

  let eventbusMock = {
    instance: {
      publish: sinon.spy()
    }
  };

  before(function () {
    fixture.setBase('test/idoc/idoc-toc');
  });

  beforeEach(function () {
    this.template = fixture.load('idoc-toc-dom-manipulator.html');
  });

  afterEach(function () {
    fixture.cleanup();
  });

  it('Test if the tree is built from the dom correctly', ()=> {
    let config = {source: '#buildFromDomTest'};
    let result = '';
    let virtualTree = {
      createSection: function (sectionID, parentID) {
        if (parentID) {
          result = result + (parentID + ' > ' + sectionID + ' ');
        }
      }
    };

    let domManipulator = new ToCDomManipulator(config, virtualTree);
    domManipulator.buildFromDom();
    expect(result).to.equal('a > b b > c a > b1 ');
  });

  it('Test if checkHeadingsID() gives id to heading without one', ()=> {
    let config = {source: '#checkHeadingsIDTest'};
    let virtualTree = {};
    let domManipulator = new ToCDomManipulator(config, virtualTree);
    domManipulator.checkHeadingsID();
    let heading = $('.withoutID', config.source);
    expect(heading.attr('id')).to.be.a('string');
  });

  it('Test if updateHeading(heading,difference) updates correctly the heading', ()=> {
    let config = {source: '#checkHeadingsIDTest'};
    let virtualTree = {};
    let heading = $('#h1');

    let domManipulator = new ToCDomManipulator(config, virtualTree);
    domManipulator.updateHeading(heading, 1);

    heading = $('#h1');
    expect(heading.prop('tagName')).to.equal('H2');
  });

  it('Test if recalculateHeadings(sectionHeading,sectionContent,newSectionTag) recalculate the headings', ()=> {
    let config = {source: '#recalculateHeadingsTest'};
    let virtualTree = {};
    let heading = $('.recalc', config.source);
    let headingContent = $('.headingContent', config.source);
    let domManipulator = new ToCDomManipulator(config, virtualTree);
    domManipulator.recalculateHeadings(heading, headingContent, 'h2');

    heading = $('.recalc', config.source);
    expect(heading.prop('tagName')).to.equal('H2');
    headingContent = $('.headingContent', config.source).each((i, element)=> {
      expect(element.tagName).to.equal('H' + (i + 3));
    });
  });

  it('Test canMove(sectionHeading,sectionContent,newSectionTag) with invalid data', ()=> {
    let config = {source: '#canMoveTest'};
    let virtualTree = {};
    let domManipulator = new ToCDomManipulator(config, virtualTree);

    let sectionHeading = $('.a', config.source);
    let newSectionTag = 'h3';
    let sectionContent = $('h3,h6');
    let result = domManipulator.canMove(sectionHeading, sectionContent, newSectionTag);

    expect(result).to.be.false;
  });

  it('Test canMove(sectionHeading,sectionContent,newSectionTag) with valid data', ()=> {
    let config = {source: '#canMoveTest'};
    let virtualTree = {};
    let domManipulator = new ToCDomManipulator(config, virtualTree);

    let sectionHeading = $('.c', config.source);
    let newSectionTag = 'h1';
    let sectionContent = $('');
    let result = domManipulator.canMove(sectionHeading, sectionContent, newSectionTag);

    expect(result).to.be.true;
  });

  it('Test moveSection(movedElement) with section at last position', ()=> {
    let config = {source: '#moveSectionTestArea'};
    let virtualTree = {
      moveSection: ()=> {
      }
    };
    let domManipulator = new ToCDomManipulator(config, virtualTree);
    let movedSection = $('.dropped_at_last_position', '#moveSectionTestView');
    let result = domManipulator.moveSection(movedSection);
    let heading = $('#' + movedSection.children('a').attr('data-ref-id'));

    expect(result).to.be.true;
    expect(heading.prev().prop('id')).to.equal('move-A1');
  });

  it('Test moveSection(movedElement) with dropped section containing sub-section at first position', ()=> {
    let config = {source: '#moveSectionTestArea'};
    let virtualTree = {
      moveSection: ()=> {
      }
    };
    let domManipulator = new ToCDomManipulator(config, virtualTree);
    let movedSection = $('.dropped_at_first_position', '#moveSectionTestView');
    let result = domManipulator.moveSection(movedSection);
    let heading = $('#' + movedSection.children('a').attr('data-ref-id'));

    expect(result).to.be.true;
    expect(heading.next().prop('id')).to.equal('move-A1');
    expect(heading.next().next().prop('id')).to.equal('move-B');
  });

  it('Test moveSection(movedElement) with dropped section with no previous or next siblings', ()=> {
    let config = {source: '#moveSectionTestArea'};
    let virtualTree = {
      moveSection: ()=> {
      }
    };
    let domManipulator = new ToCDomManipulator(config, virtualTree);
    let movedSection = $('.dropped_without_prev_or_next', '#moveSectionTestView');
    let result = domManipulator.moveSection(movedSection);
    let heading = $('#' + movedSection.children('a').attr('data-ref-id'));

    expect(result).to.be.true;
    expect(heading.prev().prop('id')).to.equal('move-A1');
  });

  it('Test moveSection(movedElement) with incorrectly dropped section', ()=> {
    let config = {source: '#moveSectionTestArea'};
    let virtualTree = {
      moveSection: ()=> {
      }
    };
    let domManipulator = new ToCDomManipulator(config, virtualTree);
    let movedSection = $('.dropped_incorrectly', '#moveSectionTestView');
    let result = domManipulator.moveSection(movedSection);
    expect(result).to.be.false;
  });

  it('Test createCollapseContainer() if it creates div for storing collapsed sections', ()=> {
    let config = {source: '', collapsedContainerID: 'createCollapseContainerTest'};
    let virtualTree = {};
    let domManipulator = new ToCDomManipulator(config, virtualTree);
    domManipulator.createCollapseContainer();

    let result = $('#createCollapseContainerTest');
    expect(result.length).to.be.above.zero;
  });

  describe('Test buildDocument()', () => {
    it('should build a copy of the document data if clone is true', ()=> {
      let config = {source: '#buildDocumentAreaCloneTest'};
      let virtualTree = {};
      let domManipulator = new ToCDomManipulator(config, virtualTree);
      domManipulator.createCollapseContainer();
      let collapsedContainerID = '#collapse-container-buildDocumentAreaCloneTest';
      $(collapsedContainerID).append('<div data-ref-id="buildDocumentClone-D"><h5 id="buildDocumentClone-E"></h5></div>');
      $(collapsedContainerID).append('<div data-ref-id="buildDocumentClone-C"><h4 id="buildDocumentClone-D"></h4></div>');
      $(collapsedContainerID).append('<div data-ref-id="buildDocumentClone-B"><h3 id="buildDocumentClone-C"></h3></div>');
      $(collapsedContainerID).append('<div data-ref-id="buildDocumentClone-A"><h2 id="buildDocumentClone-B"></h2></div>');

      let document = domManipulator.buildDocument(true);

      expect($.trim(document.html())).to.equal('<h1 id="buildDocumentClone-A"></h1><h2 id="buildDocumentClone-B"></h2><h3 id="buildDocumentClone-C"></h3><h4 id="buildDocumentClone-D"></h4><h5 id="buildDocumentClone-E"></h5>');
      // Check that original element remains intact
      expect($.trim($(config.source).html())).to.equal('<h1 id="buildDocumentClone-A"></h1>');
      // Check that collapsed container is not changed (children are not removed) while building content
      expect($(collapsedContainerID).html()).to.equal('<div data-ref-id="buildDocumentClone-D"><h5 id="buildDocumentClone-E"></h5></div><div data-ref-id="buildDocumentClone-C"><h4 id="buildDocumentClone-D"></h4></div><div data-ref-id="buildDocumentClone-B"><h3 id="buildDocumentClone-C"></h3></div><div data-ref-id="buildDocumentClone-A"><h2 id="buildDocumentClone-B"></h2></div>');
    });

    it('should modify document data directly if clone is false', ()=> {
      let config = {source: '#buildDocumentAreaTest'};
      let virtualTree = {};
      let domManipulator = new ToCDomManipulator(config, virtualTree);
      domManipulator.createCollapseContainer();
      let collapsedContainerID = '#collapse-container-buildDocumentAreaTest';
      $(collapsedContainerID).append('<div data-ref-id="buildDocument-D"><h5 id="buildDocument-E"></h5></div>');
      $(collapsedContainerID).append('<div data-ref-id="buildDocument-C"><h4 id="buildDocument-D"></h4></div>');
      $(collapsedContainerID).append('<div data-ref-id="buildDocument-B"><h3 id="buildDocument-C"></h3></div>');
      $(collapsedContainerID).append('<div data-ref-id="buildDocument-A"><h2 id="buildDocument-B"></h2></div>');

      let document = domManipulator.buildDocument(false);

      expect($.trim(document.html())).to.equal('<h1 id="buildDocument-A"></h1><h2 id="buildDocument-B"></h2><h3 id="buildDocument-C"></h3><h4 id="buildDocument-D"></h4><h5 id="buildDocument-E"></h5>');
      // Check that original element is changed
      expect($.trim($(config.source).html())).to.equal('<h1 id="buildDocument-A"></h1><h2 id="buildDocument-B"></h2><h3 id="buildDocument-C"></h3><h4 id="buildDocument-D"></h4><h5 id="buildDocument-E"></h5>');
      // Check that collapsed container is changed (children are removed)
      expect($(collapsedContainerID).html()).to.equal('<div data-ref-id="buildDocument-D"></div><div data-ref-id="buildDocument-C"></div><div data-ref-id="buildDocument-B"></div><div data-ref-id="buildDocument-A"></div>');
    });
  });

  it('Test collapseExpandSection(section) to collapse section', (done)=> {
    let config = {
      source: '#collapseExpandSectionTestCollapse',
      eventbus: {
        instance: {
          publish: () => {
          }
        },
        channel: ''
      }
    };
    let virtualTree = {};
    let domManipulator = new ToCDomManipulator(config, virtualTree);
    domManipulator.createCollapseContainer();
    domManipulator.collapseExpandSection($('#collapse-A', config.source), () => {
      let collapsedContainerID = '#collapse-container-collapseExpandSectionTestCollapse';
      expect($(collapsedContainerID).find('#collapse-B').length).to.be.above(0);
      done();
    });
  });

  it('Test collapseExpandSection(section) to expand section', (done)=> {
    let config = {
      source: '#collapseExpandSectionTestExpand',
      eventbus: {
        instance: {
          publish: () => {
          }
        },
        channel: ''
      }
    };
    let virtualTree = {};
    let domManipulator = new ToCDomManipulator(config, virtualTree);
    domManipulator.createCollapseContainer();
    let collapsedContainerID = '#collapse-container-collapseExpandSectionTestExpand';
    $(collapsedContainerID).append('<div data-ref-id="expand-A"><h2 id="expand-B" style="display: none;"></h2></div>');
    domManipulator.collapseExpandSection($('#expand-A', config.source), () => {
      expect($('#expand-B', config.source).prev().prop('id')).to.equal('expand-A');
      done();
    });
  });

  it('Test if deleteCollapsedSection(parentID) deletes the collapsed section', ()=> {
    let config = {
      source: '#deleteCollapsedSectionTest',
      eventbus: eventbusMock
    };
    let virtualTree = {};
    let domManipulator = new ToCDomManipulator(config, virtualTree);
    domManipulator.createCollapseContainer();
    let collapsedContainerID = '#collapse-container-deleteCollapsedSection';
    $(collapsedContainerID).append('<div data-ref-id="deleteCollapsed-A"><h2 id="deleteCollapsed-B" style="display: none;"></h2></div>');
    domManipulator.deleteCollapsedSection('deleteCollapsed-A');

    expect($(collapsedContainerID).children().length).to.be.not.above(0);
  });

  it('Test if deleteCollapsedContainer() deletes the container', ()=> {
    let config = {source: '#deleteCollapsedContainer'};
    let virtualTree = {};
    let domManipulator = new ToCDomManipulator(config, virtualTree);
    let collapsedContainerID = '#collapse-container-deleteCollapsedContainer';
    domManipulator.createCollapseContainer();
    domManipulator.removeCollapseContainer();

    expect($(collapsedContainerID).length).to.not.be.above(0);
  });
});