package com.ueadmission.utils;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Utility class for handling transitions between screens
 * Provides common transition methods to reduce code duplication
 */
public class TransitionUtil {
    // Default durations for animations
    public static final int FADE_IN_DURATION = 300;
    public static final int FADE_OUT_DURATION = 200;
    
    /**
     * Creates and returns a fade-in transition for a node
     * @param node The node to apply the transition to
     * @return The configured fade-in transition
     */
    public static FadeTransition createFadeInTransition(Node node) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_IN_DURATION), node);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        return fadeIn;
    }
    
    /**
     * Creates and returns a fade-out transition for a node
     * @param node The node to apply the transition to
     * @return The configured fade-out transition
     */
    public static FadeTransition createFadeOutTransition(Node node) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(FADE_OUT_DURATION), node);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        return fadeOut;
    }
    
    /**
     * Applies a fade-in transition to a node and plays it
     * @param node The node to apply the transition to
     */
    public static void applyFadeInTransition(Node node) {
        node.setOpacity(0);
        FadeTransition fadeIn = createFadeInTransition(node);
        fadeIn.play();
    }
    
    /**
     * Applies a smooth transition between two stages
     * @param currentStage The stage to close
     * @param newStage The stage to show
     */
    public static void applyStageTransition(Stage currentStage, Stage newStage) {
        // Set new stage opacity to 0 initially
        newStage.setOpacity(0.0);
        newStage.show();
        
        // Force layout to ensure all controls are properly set up
        newStage.getScene().getRoot().applyCss();
        newStage.getScene().getRoot().layout();
        
        // Create fade-in transition for new stage
        FadeTransition fadeIn = createFadeInTransition(newStage.getScene().getRoot());
        
        // Create fade-out transition for current stage
        FadeTransition fadeOut = createFadeOutTransition(currentStage.getScene().getRoot());
        
        // Start the fade out, then hide current stage when done
        fadeOut.setOnFinished(e -> {
            currentStage.hide();
            newStage.setOpacity(1.0);
            fadeIn.play();
            
            // Close old stage when fade-in completes
            fadeIn.setOnFinished(f -> currentStage.close());
        });
        
        // Play fade-out animation
        fadeOut.play();
    }
}