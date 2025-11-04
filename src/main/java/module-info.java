module org.example.ugv_rc {
  requires javafx.controls;
  requires javafx.fxml;
  requires lombok;
  requires org.apache.httpcomponents.core5.httpcore5;
  requires org.apache.httpcomponents.client5.httpclient5;
  requires com.fasterxml.jackson.databind;
  requires org.slf4j;
  requires com.fasterxml.jackson.core;
  requires jinput;

  opens org.example.ugv_rc to javafx.fxml;
  exports org.example.ugv_rc;
  exports org.example.ugv_rc.clients;
}