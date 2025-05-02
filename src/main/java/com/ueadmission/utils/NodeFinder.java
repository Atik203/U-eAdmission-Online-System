package com.ueadmission.utils;

import java.util.logging.Logger;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Utility class for finding nodes in a scene
 */
public class NodeFinder {
    private static final Logger LOGGER = Logger.getLogger(NodeFinder.class.getName());
    
    /**
     * Find a node in the scene by ID and class type
     * @param scene The scene to search in
     * @param id The ID of the node
     * @param type The class type of the node
     * @return The node if found, null otherwise
     */
    public static <T extends Node> T findNodeById(Scene scene, String id, Class<T> type) {
        if (scene == null) return null;
        
        try {
            Node node = scene.lookup("#" + id);
            if (node != null && type.isInstance(node)) {
                return type.cast(node);
            }
        } catch (Exception e) {
            LOGGER.warning("Error finding node by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Find a node in the scene by CSS class and node type
     * @param scene The scene to search in
     * @param cssClass The CSS class to look for
     * @param type The class type of the node
     * @return The first matching node, or null if not found
     */
    public static <T extends Node> T findNodeByClass(Scene scene, String cssClass, Class<T> type) {
        if (scene == null || scene.getRoot() == null) return null;
        
        try {
            for (Node node : scene.getRoot().lookupAll("." + cssClass)) {
                if (type.isInstance(node)) {
                    return type.cast(node);
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error finding node by class: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Find the first child of a parent that is of a specific type
     * @param parent The parent to search in
     * @param type The type of child to find
     * @return The first matching child, or null if not found
     */
    public static <T extends Node> T findFirstChildOfType(Parent parent, Class<T> type) {
        if (parent == null) return null;
        
        try {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (type.isInstance(child)) {
                    return type.cast(child);
                } else if (child instanceof Parent) {
                    T result = findFirstChildOfType((Parent) child, type);
                    if (result != null) {
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error finding child by type: " + e.getMessage());
        }
        
        return null;
    }
}