package net.nodebox.node;

import net.nodebox.node.canvas.CanvasNetworkType;
import net.nodebox.node.image.ImageNetworkType;
import net.nodebox.node.vector.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manager class for retrieving nodes.
 * <p/>
 * The manager always deals with nodes specified by their qualified name. A qualified name is a "full" name in reverse
 * domain notation, e.g. net.nodebox.node.vector.RectNode.
 */
public class NodeManager {

    /**
     * A "fuzzy" way of specifying a node version.
     * <p/>
     * Examples:
     * <ul>
     * <li>"=2.0" -- only version 2.0, nothing above or below that.</li>
     * <!-- <li>"2.0" -- same as above. Used for XML parsing.</li> -->
     * <li>"&gt;=2.0" -- anything greater or equal then version 2.0</li>
     * </ul>
     */
    public static class VersionSpecifier {

        private String specifier;

        public VersionSpecifier(String specifier) {
            this.specifier = specifier;
        }

        public String getSpecifier() {
            return specifier;
        }

        public boolean matches(NodeType.Version version) {
            // TODO: implement
            return false;
        }

        public boolean matches(int major, int minor) {
            return matches(new NodeType.Version(major, minor));
        }
    }

    /**
     * A list of node types with the same type but different version.
     * <p/>
     * The list is ordered from the newest (highest) version to the oldest (lowest) version.
     */
    public static class VersionedNodeTypeList {
        private List<NodeType> nodeTypes = new ArrayList<NodeType>();

        public void addNodeType(NodeType nodeType) {
            NodeType.Version newVersion = nodeType.getVersion();
            int i = 0;
            for (; i < nodeTypes.size(); i++) {
                NodeType nt = nodeTypes.get(i);
                if (nt.getVersion().smallerThan(newVersion))
                    break;
            }
            nodeTypes.add(i, nodeType);
        }

        public NodeType getLatestVersion() {
            return nodeTypes.get(0);
        }

        public List<NodeType> getNodeTypes() {
            return nodeTypes;
        }

    }

    private HashMap<String, VersionedNodeTypeList> nodeTypeMap = new HashMap<String, VersionedNodeTypeList>();

    private static Logger logger = Logger.getLogger("net.nodebox.node.NodeManager");

    public NodeManager() {
        // Add builtin nodes
        // Canvas nodes
        addNodeType(new CanvasNetworkType(this));
        // Image nodes
        addNodeType(new ImageNetworkType(this));
        // Vector nodes
        addNodeType(new CopyType(this));
        addNodeType(new EllipseType(this));
        addNodeType(new RectType(this));
        addNodeType(new TransformType(this));
        addNodeType(new VectorNetworkType(this));
    }

    public void addNodeType(NodeType n) {
        VersionedNodeTypeList nodeTypeList = nodeTypeMap.get(n.getIdentifier());
        if (nodeTypeList == null) {
            nodeTypeList = new VersionedNodeTypeList();
            nodeTypeMap.put(n.getIdentifier(), nodeTypeList);
        }
        nodeTypeList.addNodeType(n);
    }

    /**
     * Finds and returns the latest version of the node with the given qualified name.
     *
     * @param identifier the identifier in reverse-DNS format (e.g. net.nodebox.node.vector.RectNode)
     * @return a Node object or null if no node with that name was found.
     * @throws NotFoundException if the node could not be found
     */
    public NodeType getNodeType(String identifier) throws NotFoundException {
        VersionedNodeTypeList nodeTypeList = nodeTypeMap.get(identifier);
        if (nodeTypeList == null)
            throw new NotFoundException(this, identifier, "The node manager cannot find node type '" + identifier + "'.");
        return nodeTypeList.getLatestVersion();
    }

    /**
     * Finds and returns the exact specified version of the node type with the given identifier.
     *
     * @param identifier the identifier in reverse-DNS format (e.g. net.nodebox.node.vector.RectNode)
     * @param version    the exact version number you want to retrieve.
     * @return a Node object or null if no node with that name was found.
     * @throws NotFoundException if the node type could not be found
     */
    public NodeType getNodeType(String identifier, NodeType.Version version) throws NotFoundException {
        VersionedNodeTypeList nodeTypeList = nodeTypeMap.get(identifier);
        if (nodeTypeList == null)
            throw new NotFoundException(this, identifier, "The node manager cannot find node type '" + identifier + "'.");
        for (NodeType nt : nodeTypeList.getNodeTypes()) {
            if (nt.getVersion().equals(version))
                return nt;
        }
        throw new NotFoundException(this, identifier, "The node manager cannot find node type '" + identifier + "'.");
    }

    /**
     * Finds and returns the specified version of the node with the given qualified name.
     *
     * @param identifier the identifier in reverse-DNS format (e.g. net.nodebox.node.vector.RectNode)
     * @param specifier  the version specifier you want to retrieve.
     * @return a Node object.
     * @throws NotFoundException if the node type could not be found
     */
    public NodeType getNodeType(String identifier, VersionSpecifier specifier) throws NotFoundException {
        VersionedNodeTypeList nodeTypeList = nodeTypeMap.get(identifier);
        if (nodeTypeList == null)
            throw new NotFoundException(this, identifier, "The node manager cannot find node type '" + identifier + "'.");
        for (NodeType nt : nodeTypeList.getNodeTypes()) {
            if (specifier.matches(nt.getVersion()))
                return nt;
        }
        throw new NotFoundException(this, identifier, "The node manager cannot find node type '" + identifier + "'.");
    }

    /**
     * Gets the latest version of all available node types. Each type only occurs once, and with its latest version.
     *
     * @return a list of node types.
     */
    public List<NodeType> getNodeTypes() {
        List<NodeType> types = new ArrayList<NodeType>();
        for (VersionedNodeTypeList versionedList : nodeTypeMap.values()) {
            types.add(versionedList.getLatestVersion());
        }
        return types;
    }

}