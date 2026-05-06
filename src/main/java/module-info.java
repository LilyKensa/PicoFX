module dev.huey.picofx {
  requires static lombok;

  requires java.desktop;

  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.media;

  opens dev.huey.picofx to javafx.fxml;
  
  exports dev.huey.picofx;
}
