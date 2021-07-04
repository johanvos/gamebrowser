module com.gluonhq.gamebrowser {
    requires javafx.controls;
    exports com.gluonhq.gamebrowser;
    requires com.almasb.fxgl.all;
    uses com.almasb.fxgl.app.GameApplication;
    requires com.gluonhq.attach.storage;
}
