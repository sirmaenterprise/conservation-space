import {ToCVirtualTreeView} from 'idoc/idoc-toc/idoc-toc-virtual-tree-view';
import {ToCVirtualTree} from 'idoc/idoc-toc/idoc-toc-virtual-tree';
import $ from 'jquery';

describe('Tests for the idoc table of contents : view of the virtual tree', () => {

  before(function () {
    fixture.setBase('test/idoc/idoc-toc')
  });

  beforeEach(function () {
    this.template = fixture.load('idoc-toc-virtual-tree-view.html');
  });

  afterEach(function () {
    fixture.cleanup()
  });

  it('Test cacheHeadingSectionProperties(sectionView) if memoizes the highestContentHeading and the headingNumber itself', ()=> {
    let virtualTree = {};
    let namespace = '#createCacheTest';
    let config = {navigation: '', source: '#createCacheTestEditor'};
    let view = new ToCVirtualTreeView(config, virtualTree);
    let sectionView = $("#createCacheTestItem", namespace);
    let result = view.cacheHeadingSectionProperties(sectionView);
    expect(result.highestContentHeadingNumber).to.equal(5);
    expect(result.headingNumber).to.equal(1);
  });

  it('Test isValidMove(target,cache) with correct data', ()=> {
    let virtualTree = {};
    let config = {navigation: '#isValidMoveTestView', source: '#isValidMoveTestArea'};
    let view = new ToCVirtualTreeView(config, virtualTree);
    let cache = {headingNumber: 4, highestContentHeadingNumber: 4};
    let target = $('#validTarget', config.navigation);
    let result = view.isValidMove(target, cache);

    expect(result).to.be.true;
  });

  it('Test isValidMove(target,cache) with invalid data', ()=> {
    let virtualTree = {};
    let config = {navigation: '#isValidMoveTestView', source: '#isValidMoveTestArea'};
    let view = new ToCVirtualTreeView(config, virtualTree);
    let cache = {headingNumber: 6, highestContentHeadingNumber: 6};
    let target = $('#invalidTarget', config.navigation);
    let result = view.isValidMove(target, cache);

    expect(result).to.be.false;
  });

  it('Test if build() constructs proper view', ()=> {
    let virtualTree = new ToCVirtualTree();
    let childrenA = ['build-A1'];
    virtualTree.tree.set('build-A', childrenA);
    virtualTree.tree.set('build-B', []);
    virtualTree.tree.set('build-A1',[]);
    virtualTree.parents.set('build-A1', 'build-A');
    virtualTree.parents.set('build-A', 'build-A');
    virtualTree.parents.set('build-B', 'build-B');

    let config = {navigation: '#buildTestView', source: '#buildTestArea'};
    let view = new ToCVirtualTreeView(config, virtualTree);
    view.build();

    let viewElement = $(config.navigation);

    let mainHeadings = viewElement.children('ul').children('li');
    expect(mainHeadings.length).to.equal(2);

    let sectionA = mainHeadings.first();
    let sectionAChildrenID = sectionA.children('ul').children('li').children('a').data('ref-id');
    expect(sectionAChildrenID).to.equal('build-A1');

    let sectionB = mainHeadings.last();
    let sectionBChildrenCount = sectionB.children('ul').children('li').length;
    expect(sectionBChildrenCount).to.equal(0);

  });

  it('Test if clearView() clears the navigation root',()=>{
    let virtualTree = {};
    let config = {navigation: '#clearViewTest'};
    let view = new ToCVirtualTreeView(config, virtualTree);
    view.clearView();

    expect($(config.navigation).children().length).to.equal.zero;
  });

});
