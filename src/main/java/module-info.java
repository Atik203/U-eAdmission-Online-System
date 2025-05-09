module com.u.eadmission.ueadmission {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires atlantafx.base;
    requires MaterialFX;
    requires java.desktop;
    requires java.sql;
    requires java.prefs;
    requires annotations;
    requires java.logging;
    requires transitive javafx.base;
    requires transitive javafx.graphics;

    opens com.ueadmission to javafx.fxml;
    opens com.ueadmission.about to javafx.fxml;
    opens com.ueadmission.admission to javafx.fxml;
    opens com.ueadmission.auth to javafx.fxml;
    opens com.ueadmission.student to javafx.fxml;
    opens com.ueadmission.admin to javafx.fxml;
    opens com.ueadmission.components to javafx.fxml;
    opens com.ueadmission.contact to javafx.fxml;
    opens com.ueadmission.profile to javafx.fxml;
    opens com.ueadmission.navigation to javafx.fxml;
    opens com.ueadmission.application to javafx.fxml;
    opens com.ueadmission.examPortal to javafx.fxml;
    opens com.ueadmission.exam to javafx.fxml;
    opens com.ueadmission.mockTest to javafx.fxml;
    
    exports com.ueadmission;
    exports com.ueadmission.about;
    exports com.ueadmission.admission;
    exports com.ueadmission.auth;
    exports com.ueadmission.student;
    exports com.ueadmission.admin;
    exports com.ueadmission.components;
    exports com.ueadmission.contact;
    exports com.ueadmission.profile;
    exports com.ueadmission.navigation;
    exports com.ueadmission.application;
    exports com.ueadmission.examPortal;
    exports com.ueadmission.exam;
    exports com.ueadmission.mockTest;
}