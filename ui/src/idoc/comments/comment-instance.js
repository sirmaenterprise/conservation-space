import {CommentParser} from './comment-parser';
import {OPEN} from './comment-status';
export class CommentInstance {
  constructor(comment, reply = false) {
    this.comment = comment;
    this.reply = reply;
    this.data = {};
  }

  addData(key, data) {
    this.data[key] = data;
  }

  getData(key) {
    return this.data[key];
  }

  isReply() {
    return this.reply;
  }

  getId() {
    return CommentParser.getId(this.comment);
  }

  setId(id) {
    this.comment['@id'] = id;
  }

  getDescription() {
    return CommentParser.getDescription(this.comment);
  }

  setDescription(description) {
    this.comment.resource[0].chars = description;
  }

  getActions() {
    return CommentParser.getActions(this.comment);
  }

  removeActions() {
    delete this.comment.actions;
    if (this.getReplies()) {
      for (let reply of this.getReplies()) {
        delete reply.comment.actions;
      }
    }
  }

  getAuthorId() {
    return CommentParser.getAuthorId(this.comment);
  }

  getAuthorLabel() {
    return CommentParser.getAuthorLabel(this.comment);
  }

  getAuthorIcon() {
    return CommentParser.getAuthorIcon(this.comment);
  }

  getCreatedDate() {
    return CommentParser.getCreatedDate(this.comment);
  }

  getModifiedDate() {
    return CommentParser.getModifiedDate(this.comment);
  }

  getMotivation() {
    return CommentParser.getMotivation(this.comment);
  }

  getSelector() {
    return CommentParser.getSelector(this.comment);
  }

  getSelectorType() {
    return CommentParser.getSelectorType(this.comment);
  }

  setMotivation(motivation) {
    this.comment['motivation'] = motivation;
  }

  setMentionedUsers(users){
    this.comment['emf:mentionedUsers'] = users;
  }

  getMentionedUsers(){
    return CommentParser.getMentionedUsers(this.comment);
  }

  getComment() {
    return this.comment;
  }

  setSuspendAction() {
    this.setAction('suspendComment');
  }

  setRestartAction() {
    this.setAction('restartComment');
  }

  setAction(actionToApply) {
    this.comment.action = actionToApply;
    if (this.getReplies()) {
      for (let reply of this.getReplies()) {
        reply.comment.action = actionToApply;
      }
    }
  }

  setStatus(status) {
    this.comment['emf:status'] = status;
  }

  getStatus() {
    return CommentParser.getStatus(this.comment);
  }

  isOpen() {
    return this.getStatus() === OPEN;
  }

  getReplies() {
    if (!this.getRepliesAsJSON()) {
      return;
    }
    if (this.replies) {
      return this.replies;
    }
    this.replies = this.getRepliesAsJSON().map(function (reply) {
      return new CommentInstance(reply, true);
    });
    return this.replies;
  }

  getRepliesAsJSON() {
    return this.comment['emf:reply'];
  }

  hash(hashFunction) {
    if (this.getReplies() && this.getReplies().length) {
      let repliesHash = '';
      for (let reply of this.getReplies()) {
        repliesHash = hashFunction(repliesHash + reply.getDescription() + reply.getModifiedDate());
      }
      return this.getId() + this.getModifiedDate() + repliesHash;
    } else {
      return this.getId() + this.getModifiedDate();
    }
  }

  setHeader(header) {
    this.comment['header'] = header;
  }

  getHeader() {
    return CommentParser.getHeader(this.comment);
  }

  getTarget() {
    return CommentParser.getTarget(this.comment);
  }
}