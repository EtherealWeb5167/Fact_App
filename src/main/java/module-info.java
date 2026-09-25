module ni.edu.uam.fact_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires org.postgresql.jdbc;
    requires java.sql;

    exports ni.edu.uam.fact_app;
    exports ni.edu.uam.fact_app.application;
    exports ni.edu.uam.fact_app.model;
    exports ni.edu.uam.fact_app.controller;
    exports ni.edu.uam.fact_app.util;
    exports ni.edu.uam.fact_app.dao;

    opens ni.edu.uam.fact_app.controller to javafx.fxml;
    opens ni.edu.uam.fact_app.model to javafx.base;
}
