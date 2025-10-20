module org.example.ugv_rc {
  requires javafx.controls;
  requires javafx.fxml;

  requires org.controlsfx.controls;

  opens org.example.ugv_rc to javafx.fxml;
  exports org.example.ugv_rc;
}