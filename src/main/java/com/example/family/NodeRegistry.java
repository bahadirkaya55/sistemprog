package com.example.family;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Basit bir node kayıt defteri iskeleti.
 */
public class NodeRegistry {

    private final Map<String, String> nodes = new HashMap<>();

    public void registerNode(String nodeId, String address) {
        nodes.put(nodeId, address);
    }

    public String getNodeAddress(String nodeId) {
        return nodes.get(nodeId);
    }

    public Map<String, String> getAllNodes() {
        return Collections.unmodifiableMap(nodes);
    }
}


