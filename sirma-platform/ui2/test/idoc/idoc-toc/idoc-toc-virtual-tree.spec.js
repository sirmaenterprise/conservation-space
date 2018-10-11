import {ToCVirtualTree} from 'idoc/idoc-toc/idoc-toc-virtual-tree';

let hasChild = function (children, child) {
  return children.indexOf(child) > -1;
};

describe('Tests for the idoc table of contents : virtual tree', () => {
  it('Test if moveSection(sectionID, to, nextSiblingID) moves a main section and makes it subsection', () => {
    let tocTree = new ToCVirtualTree();
    tocTree.createSection('mock1', null);
    tocTree.createSection('mock2', null);
    tocTree.moveSection('mock1', 'mock2');
    expect(hasChild(tocTree.tree.get('mock2'), 'mock1')).to.be.true;
  });

  it('Test if moveSection(sectionID, to, nextSiblingID) moves a main section to subsection', () => {
    let tocTree = new ToCVirtualTree();
    tocTree.createSection('mock2', 'mock1');
    tocTree.createSection('mock3', null);
    tocTree.moveSection('mock3', 'mock2');
    expect(hasChild(tocTree.tree.get('mock2'), 'mock3')).to.be.true;
  });

  it('Test if moveSection(sectionID, to, nextSiblingID) makes a subsection main', () => {
    let tocTree = new ToCVirtualTree();
    tocTree.createSection('mock2', 'mock1');
    tocTree.createSection('mock3', null);
    tocTree.moveSection('mock3', 'mock2');
    tocTree.moveSection('mock2', null);
    expect(tocTree.parents.get('mock2')).to.be.equal('mock2');
    expect(hasChild(tocTree.tree.get('mock2'), 'mock3')).to.be.true;
  });

  it('Test if moveSection(sectionID, to, nextSiblingID) makes "sectionID" child of "to" moves "sectionID" just before "nextSiblingID"',()=>{
    let tocTree = new ToCVirtualTree();
    tocTree.createSection('mock',null);
    tocTree.createSection('mock1','mock');
    tocTree.createSection('test',null);
    tocTree.moveSection('test','mock','mock1');
    let children = tocTree.tree.get('mock');
    let childIndex = children.indexOf('test');
    expect(children[childIndex+1]).to.equal('mock1');
  });

  it('Test the creation of main section', () => {
    let toCVirtualTree = new ToCVirtualTree();
    let mockSectionID = 'mockSectionID';
    toCVirtualTree.createSection(mockSectionID, null);
    expect(toCVirtualTree.tree.has(mockSectionID)).to.be.true;
  });

  it('Test the creation of sub section', () => {
    let toCVirtualTree = new ToCVirtualTree();
    let mockParentSectionID = 'mockParentID';
    toCVirtualTree.createSection(mockParentSectionID, null);
    let mockSectionID = 'mockSectionID';
    toCVirtualTree.createSection(mockSectionID, mockParentSectionID);
    expect(hasChild(toCVirtualTree.tree.get(mockParentSectionID), mockSectionID)).to.be.true;
  });

  it('Test the creation of sub sub section', () => {
    let toCVirtualTree = new ToCVirtualTree();
    let mockParentSubSection = 'mockParentSubSection';
    let mockSection = 'mockSubSub';
    toCVirtualTree.createSection(mockSection, mockParentSubSection);
    expect(hasChild(toCVirtualTree.tree.get(mockParentSubSection), mockSection)).to.be.true;
  });

  it('Test the deletion of main section', () => {
    let toCVirtualTree = new ToCVirtualTree();
    let mockSectionID = 'mockSectionID';
    toCVirtualTree.createSection(mockSectionID, null);
    let result = toCVirtualTree.deleteSection(mockSectionID);
    expect(toCVirtualTree.tree.has(mockSectionID)).to.be.false;
    expect(result).to.be.true;
  });

  it('Test the deletion of subsection', () => {
    let toCVirtualTree = new ToCVirtualTree();
    let mockSectionID = 'mockSectionID';
    let mockParentID = 'mockParent';
    toCVirtualTree.createSection(mockSectionID, mockParentID);
    let result = toCVirtualTree.deleteSection(mockSectionID);
    expect(hasChild(toCVirtualTree.tree.get(mockParentID), mockSectionID)).to.be.false;
    expect(result).to.be.true;
  });

  it('Test the deletion of subsection which is parent of other sections', () => {
    let toCVirtualTree = new ToCVirtualTree();
    let mockSectionID = 'mockSectionID';
    let mockSubSectionID = 'mockSubSectionID';
    let mockParentID = 'mockParent';
    toCVirtualTree.createSection(mockSectionID, mockParentID);
    toCVirtualTree.createSection(mockSubSectionID, mockSectionID);
    let result = toCVirtualTree.deleteSection(mockSectionID);
    expect(hasChild(toCVirtualTree.tree.get(mockParentID), mockSectionID)).to.be.false;
    expect(toCVirtualTree.tree.has(mockSectionID)).to.be.false;
    expect(result).to.be.true;
  });

  it('Test the deletion of whole subTree', ()=> {
    let tocVirtualTree = new ToCVirtualTree();
    tocVirtualTree.createSection('b', 'a');
    tocVirtualTree.createSection('b1', 'b');
    tocVirtualTree.createSection('c', 'a');
    tocVirtualTree.deleteSubTree('a');
    expect(tocVirtualTree.tree.length).to.be.zero;
  });

  it('Test the tree full traversal', ()=> {
    let virtualTree = new ToCVirtualTree();
    virtualTree.createSection('a', null);
    virtualTree.createSection('a1', 'a');
    virtualTree.createSection('b', null);
    virtualTree.createSection('b1', 'b');
    let result = '';
    let mockDecorator = function () {
      this.decorateRoots = function (roots) {
        return roots;
      };
      this.decorateNode = function (node) {
      };
      this.decorateNodeChild = function (node, child) {
        result += (node + '->' + child + ' ');
      }
    };
    virtualTree.fullTraversal(new mockDecorator());
    expect(result).to.equal('a->a1 b->b1 ');
  });

});
