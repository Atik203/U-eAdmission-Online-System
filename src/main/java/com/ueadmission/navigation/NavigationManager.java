package com.ueadmission.navigation;

import java.util.logging.Logger;

/**
 * @deprecated This class is deprecated and will be removed in future versions.
 * Use {@link NavigationUtil} instead for all navigation functionality.
 */
@Deprecated
public class NavigationManager {
    private static final Logger LOGGER = Logger.getLogger(NavigationManager.class.getName());
    
    /**
     * @deprecated Use {@link NavigationUtil#navigateToLogin(javafx.event.Event)} instead.
     */
    @Deprecated
    public static void navigateToLogin() {
        LOGGER.warning("NavigationManager is deprecated. Use NavigationUtil.navigateToLogin() instead.");
        NavigationUtil.navigateTo(NavigationUtil.getCurrentStage(), 
                                 "/com.ueadmission/auth/login.fxml", 
                                 "Login - UeAdmission");
    }

    /**
     * @deprecated Use {@link NavigationUtil#navigateToHome(javafx.event.Event)} instead.
     */
    @Deprecated
    public static void navigateToHome() {
        LOGGER.warning("NavigationManager is deprecated. Use NavigationUtil.navigateToHome() instead.");
        NavigationUtil.navigateTo(NavigationUtil.getCurrentStage(), 
                                 "/com.ueadmission/main.fxml", 
                                 "UeAdmission - Home");
    }

    /**
     * @deprecated Use {@link NavigationUtil#navigateToProfile(javafx.event.Event)} instead.
     */
    @Deprecated
    public static void navigateToProfile() {
        LOGGER.warning("NavigationManager is deprecated. Use NavigationUtil.navigateToProfile() instead.");
        NavigationUtil.navigateTo(NavigationUtil.getCurrentStage(), 
                                 "/com.ueadmission/profile/profile.fxml", 
                                 "My Profile - UeAdmission");
    }

    /**
     * @deprecated Use {@link NavigationUtil#navigateToAbout(javafx.event.Event)} instead.
     */
    @Deprecated
    public static void navigateToAbout() {
        LOGGER.warning("NavigationManager is deprecated. Use NavigationUtil.navigateToAbout() instead.");
        NavigationUtil.navigateTo(NavigationUtil.getCurrentStage(), 
                                 "/com.ueadmission/about/about.fxml", 
                                 "About - UeAdmission");
    }

    /**
     * @deprecated Use {@link NavigationUtil#navigateTo(javafx.event.Event, String, String)} instead.
     */
    @Deprecated
    public static void navigate(String fxmlPath, String title) {
        LOGGER.warning("NavigationManager is deprecated. Use NavigationUtil.navigateTo() instead.");
        NavigationUtil.navigateTo(NavigationUtil.getCurrentStage(), fxmlPath, title);
    }

    /**
     * @deprecated Use {@link NavigationUtil#navigateWithTransition(javafx.stage.Stage, String, String)} instead.
     */
    @Deprecated
    public static void navigateWithTransition(String fxmlPath, String title) {
        LOGGER.warning("NavigationManager is deprecated. Use NavigationUtil.navigateWithTransition() instead.");
        NavigationUtil.navigateWithTransition(NavigationUtil.getCurrentStage(), fxmlPath, title);
    }

    /**
     * @deprecated Use {@link NavigationUtil#setMainStage(javafx.stage.Stage)} instead.
     */
    @Deprecated
    public static void setMainStage(javafx.stage.Stage stage) {
        LOGGER.warning("NavigationManager is deprecated. Use NavigationUtil.setMainStage() instead.");
        NavigationUtil.setMainStage(stage);
    }

    /**
     * @deprecated This interface has been moved to {@link NavigationUtil.AuthStateAware}.
     */
    @Deprecated
    public interface AuthStateAware extends NavigationUtil.AuthStateAware {
        // Interface has been moved to NavigationUtil
    }
}
