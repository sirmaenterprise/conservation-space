import {Injectable, Inject, NgTimeout} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import _ from 'lodash';
import data from 'sandbox/services/rest/comments-service.data.json!';

@Injectable()
@Inject(PromiseAdapter, NgTimeout)
export class CommentsRestService {

  constructor(promiseAdapter, $timeout) {
    this.promiseAdapter = promiseAdapter;
    this.$timeout = $timeout;
    this.parent = {};
    this.comments = {
      data: []
    };
    this.comments.data.push(this.stubComment('default comment', false));
    this.parent[1] = 1;
    let defaultReply = this.stubComment('default reply', true);

    this.comments.data[0]['emf:reply'].push(defaultReply);
    // the default reply has id=2 and the default comment id=1
    this.parent[parseInt(defaultReply['@id'].split(':')[1])] = 1;
  }

  loadComments() {
    return this.promiseAdapter.promise((resolve) => {
      this.$timeout(() => {
        resolve(this.comments);
        // Small timeout to simulate real response
      }, 500);
    });
  }

  createComment(comment) {
    return this.promiseAdapter.promise((resolve)=> {

      let replyTo = comment['emf:replyTo'];

      if (replyTo) {
        let stubbedComment = this.stubComment(comment.resource[0].chars, true);
        _.defaultsDeep(stubbedComment, comment);
        replyTo = parseInt(replyTo.split(':')[1]);
        this.parent[parseInt(stubbedComment['@id'].split(':')[1])] = replyTo;
        this.comments.data[replyTo - 1]['emf:reply'].push(stubbedComment);
      } else {
        let stubbedComment = this.stubComment(comment.resource[0].chars, false);
        _.defaultsDeep(stubbedComment, comment);
        let id = parseInt(stubbedComment['@id'].split(':')[1]);
        this.parent[id] = id;
        this.comments.data.push(stubbedComment);
      }
      resolve();
    });
  }

  stubComment(text, reply) {
    let comment = _.cloneDeep(data);
    comment['resource'][0]['chars'] = text;
    if (reply) {
      comment['@id'] = 'emf:' + (parseInt(Math.random() * 10 + 10));
    } else {
      comment['@id'] = this.comments.data.length ? 'emf:' + (this.comments.data.length + 1) : 'emf:1';
    }
    comment['emf:modifiedOn'] = new Date();
    comment['emf:reply'] = [];
    comment['emf:status'] = 'OPEN';

    return comment;
  }

  updateComment(id, comment) {
    return this.promiseAdapter.promise((resolve)=> {
      comment['emf:modifiedOn'] = new Date();
      resolve();
    });
  }

  deleteComment(id) {
    return this.promiseAdapter.promise((resolve, reject)=> {
      //it is comment not reply
      //if this.parent[id] is undefined means that the comment is from mirador because its ids are randomly generated
      //and it can't be reply because mirador does not support them
      if (id === this.parent[id] || !this.parent[id]) {
        for (let i = 0; i < this.comments.data.length; i++) {
          // Mirador changes the places of id and fullId
          if (this.comments.data[i]['@id'] === id || this.comments.data[i].fullId === id) {
            this.deletedComment = true;
            this.comments.data.splice(i, 1);
            delete parent[id];
            break;
          }
        }
      }
      if (!this.deletedComment) {
        id = id.split(':')[1];
        var replies = this.comments.data[this.parent[id] - 1]['emf:reply'];
        let spliceIndex;
        for (let i = 0; i < replies.length; i++) {
          if (replies[i]['@id'] === id) {
            spliceIndex = i;
            break;
          }
        }
        this.comments.data[this.parent[id] - 1]['emf:reply'].splice(spliceIndex, 1);
      }
      resolve();
    });
  }

  loadReplies(id) {
    id = id.split(':')[1];
    let comment = this.comments.data[this.parent[id] - 1];
    if (comment.action == 'suspendComment') {
      comment.actions = [{
        disabled: false,
        label: 'Restart',
        serverOperation: 'restartComment',
        userOperation: 'restartComment'
      }];
    } else if (comment.action == 'restartComment' || !comment.action) {
      for (let reply of comment['emf:reply']) {
        reply.actions = [{
          disabled: false,
          label: 'Edit',
          serverOperation: 'editComment',
          userOperation: 'editComment'
        }, {
          disabled: false,
          label: 'Delete',
          serverOperation: 'deleteComment',
          userOperation: 'deleteComment',
          confirmationMessage: 'Are you sure you want to continue with action {{operationName}}?'
        }];
      }
      comment.actions = [{
        disabled: false,
        label: 'Edit',
        serverOperation: 'editComment',
        userOperation: 'editComment'
      }, {
        disabled: false,
        label: 'Reply',
        serverOperation: 'replyComment',
        userOperation: 'replyComment'
      }, {
        disabled: false,
        label: 'Resolve',
        serverOperation: 'suspendComment',
        userOperation: 'suspendComment'
      }, {
        disabled: false,
        label: 'Delete',
        serverOperation: 'deleteComment',
        userOperation: 'deleteComment',
        confirmationMessage: 'Are you sure you want to continue with action {{operationName}}?'
      }];
    }

    let response = {
      data: comment
    };

    return this.promiseAdapter.resolve(response);
  }

  loadRecentComments() {
    let instanceHeaders = {};
    this.comments.data.map((comment)=> {
      instanceHeaders[comment['on']['full']] = '<div> header</div>';
    });
    let response = {
      data: {
        annotations: this.comments.data,
        instanceHeaders: instanceHeaders
      }
    };
    return this.promiseAdapter.resolve(response);
  }

  loadCommentsCount() {
    return this.promiseAdapter.resolve({
      data: {
        count: this.comments.data.length
      }
    });
  }

  loadAllComments() {
    return this.promiseAdapter.resolve(this.comments);
  }

}