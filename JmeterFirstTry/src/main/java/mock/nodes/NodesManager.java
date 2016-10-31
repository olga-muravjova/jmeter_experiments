package mock.nodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by phd on 31.10.16.
 */
public class NodesManager {
    private final Map<Integer, Node> nodes;

    public NodesManager() {
        nodes = new HashMap<>();
        nodes.put(8090, new Node(this, 8090));
        nodes.put(8091, new Node(this, 8091));
        nodes.put(8092, new Node(this, 8092));
        nodes.put(8093, new Node(this, 8093));
    }

    public void startNodes() {
        nodes.values().forEach(Node::start);
    }

    public Node getNode(int port) {
        return nodes.get(port);
    }

    public void stopNodes() {
        nodes.values().forEach(Node::stop);
    }

    public List<Integer> getOtherNodePorts(int port) {
        return  nodes.keySet().stream().filter(p -> p != port).collect(Collectors.toList());
    }
}
