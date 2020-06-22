import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import {AuthenticationService} from 'security/authentication-service';

const SERVICE_PATH = '/annotations';
const RECENT_COMMENTS = '/discussions';

@Injectable()
@Inject(RestClient, AuthenticationService)
export class CommentsRestService {

  constructor(restClient, authenticationService) {
    this.restClient = restClient;
    this.authenticationService = authenticationService;
    this.config = {
      headers: {
        'Content-Type': HEADER_V2_JSON
      }
    };
  }

  /**
   * Loads only the comments without their replies.
   * @param instanceId the id of the instance
   * @param tabId the id of the tab of the current instance. This parameter is optional.
   * @returns {*} the comments
   */
  loadComments(instanceId, tabId) {
    return this.authenticationService.getToken().then(token => {
      let config = {
        params: {
          media: 'image',
          limit: '1000',
          id: instanceId,
          tabId,
          APIKey: token
        }
      };

      return this.restClient.get(`${SERVICE_PATH}/search`, config);
    });
  }

  /**
   * Loads the comments and their replies.
   * Necessary for the filtering of the comments.
   * @param id the id of the instance
   */
  loadAllComments(id) {
    return this.authenticationService.getToken().then(token => {
      let config = {
        params: {
          media: 'image',
          limit: '1000',
          id,
          APIKey: token
        }
      };

      return this.restClient.get(`${SERVICE_PATH}/search/all`, config);
    });
  }

  /**
   * Loads the replies for a given comment.
   * @param id the id of the comment
   * @returns {*} the comment and its replies
   */
  loadReplies(id) {
    return this.authenticationService.getToken().then(token => {
      return this.restClient.get(`${SERVICE_PATH}/${encodeURIComponent(id)}`, {
        params: {
          media: 'image',
          limit: '1000',
          APIKey: token
        }
      });
    });
  }

  createComment(data) {
    return this.authenticationService.getToken().then(token => {
      return this.restClient.post(`${SERVICE_PATH}/create`, data, {
        params: {
          APIKey: token
        }
      });
    });
  }

  updateComment(id, data) {
    return this.authenticationService.getToken().then(token => {
      return this.restClient.post(`${SERVICE_PATH}/update/${encodeURIComponent(id)}`, data, {
        params: {
          APIKey: token
        }
      });
    });
  }

  deleteComment(id) {
    return this.authenticationService.getToken().then(token => {
      return this.restClient.delete(`${SERVICE_PATH}/destroy?APIKey=${token}&id=${encodeURIComponent(id)}`);
    });
  }

  loadRecentComments(data) {
    return this.restClient.post(`${RECENT_COMMENTS}`, data, this.config);
  }

  loadCommentsCount(data) {
    return this.restClient.post(`${RECENT_COMMENTS}/count`, data, this.config);
  }
}
