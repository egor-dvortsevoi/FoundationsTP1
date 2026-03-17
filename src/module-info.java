/**
 * Main module descriptor for the HW1 JavaFX application.
 */
module TP1 {
	requires javafx.controls;
	requires java.sql;
	// requires org.junit.jupiter.api; broken right now, but we should add it back for JUnit 5 testing
	
	opens applicationMain to javafx.graphics, javafx.fxml;
}