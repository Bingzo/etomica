/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.virial.cluster2.graph;

/**
 * This interface generalizes algorithms that traverse the nodes of a graph. The
 * traversal allows a visitor to visit each of the nodes as they are traversed.
 * If the graph is not connected, only one connected component is traversed.
 */
public interface GraphTraversal {

  public static int TRAVERSAL_EDGES_ERROR = -8;
  public static int TRAVERSAL_NODES_ERROR = -16;
  public static int TRAVERSAL_ROOT_ERROR = -32;
  public static int VISITED_NONE = -1;
  public static int START_COMPONENT = -1;
  public static int VISITED_COMPONENT = -2;
  public static int START_BICOMPONENT = -4;
  public static int VISITED_BICOMPONENT = -8;
  public static int VISITED_ALL = -16;
  public static int ARTICULATION_POINT = -32;

  /**
   * Traverses all components of the graph, starting at an arbitrary node for
   * each component.
   */
  public void traverseAll(Nodes nodes, Edges edges, NodesVisitor visitor);

  /**
   * Traverses a single component of the graph starting at the designated node.
   *
   * @return true if all nodes in the graph were seen during traversal
   */
  public boolean traverseComponent(int nodeID, Nodes nodes, Edges edges,
                                   NodesVisitor visitor);
}