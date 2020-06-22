import {Injectable} from 'app/app';
import {ModelPath} from 'administration/model-management/model/model-path';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';

const NODE_SEPARATOR = '/';
const PATH_SEPARATOR = '=';

/**
 * Service which builds a {@link ModelPath} from a provided source. Currently this service supports
 * building a model path from a provided string or model. The reverse equivalent of these operations
 * are also supported
 *
 * @author Svetlozar Iliev
 */
@Injectable()
export class ModelPathBuilder {

  /**
   * Constructs a {@link ModelPath} from a string. String should comply to
   * the proper format which is required to extract correct and complete
   * path from it.
   *
   * String path is such that for a single node name and value
   * are divided by the PATH_SEPARATOR. Nodes in the string path
   * should be divided by the NODE_SEPARATOR literal.
   *
   * @param string - path to a model represented as a string
   */
  buildPathFromString(string) {
    // first collect nodes from the string path
    let nodes = string.split(NODE_SEPARATOR);
    return this.buildStringPath(nodes);
  }

  /**
   * Constructs a {@link ModelPath} from a provided model. The
   * head of the built node is either a {@link ModelDefinition}
   * or a {@link ModelClass}, while the tail can be at most of
   * any attribute type extending off of {@link ModelAttribute}
   *
   * @param model - the model for which to build the path
   */
  buildPathFromModel(model) {
    // build model path and get the head node
    return this.buildModelPath(model).head();
  }

  /**
   * Constructs a string path from a provided {@link ModelPath}.
   * The string path is built in compliance with required path
   * string format
   *
   * String path is such that for a single node name and value
   * are divided by the PATH_SEPARATOR. Nodes in the string path
   * should be divided by the NODE_SEPARATOR literal.
   *
   * @param path - the path to be converted to its string representation
   */
  buildStringFromPath(path) {
    let stringPath = '';
    let node = path.head();
    while (node) {
      stringPath += this.getAsString(node);
      node = node.getNext();
      node && (stringPath += NODE_SEPARATOR);
    }
    return stringPath;
  }

  buildStringPath(nodes) {
    if (!nodes.length) {
      return;
    }

    let node = nodes[0];
    let pair = node.split(PATH_SEPARATOR);

    let current = new ModelPath(pair[0], pair[1]);
    let next = this.buildStringPath(nodes.slice(1));

    if (next) {
      current.setNext(next);
      next.setPrevious(current);
    }
    return current;
  }

  buildModelPath(model) {
    if (!model) {
      return;
    }

    let nextModel = this.getNextModel(model);
    let modelType = this.getModelType(model);
    let identifier = model.getId();

    let next = this.buildModelPath(nextModel);
    let current = new ModelPath(modelType, identifier);

    if (next) {
      current.setPrevious(next);
      next.setNext(current);
    }
    return current;
  }

  getNextModel(model) {
    return ModelManagementUtility.isBaseType(model) ? null : model.getParent();
  }

  getModelType(model) {
    return ModelManagementUtility.getModelType(model);
  }

  getAsString(node) {
    return node.getName() + PATH_SEPARATOR + encodeURIComponent(node.getValue());
  }
}
