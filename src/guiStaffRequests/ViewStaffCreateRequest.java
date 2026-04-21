package guiStaffRequests;

import guiStaff.ControllerStaffHome;
import guiStaff.ViewStaffHome;
import entityClasses.User;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/*******
 * <p> Title: ViewStaffCreateRequest Class. </p>
 * 
 * <p> Description: This JavaFX page allows a staff user to create a new
 * request for the Staff Request Ticketing System. </p>
 * 
 * <p> This page follows the same MVC structure used throughout the
 * Foundations application. </p>
 * 
 * <p> Copyright:
 * Lynn Robert Carter © 2025 </p>
 */

public class ViewStaffCreateRequest {

    /*-*******************************************************************************************
     * Attributes
     *********************************************************************************************/

    private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
    private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

    protected static Stage theStage;
    protected static User theUser;
    protected static Pane theRootPane;
    private static Scene theScene;

    // GUI widgets
    protected static Label label_PageTitle = new Label("Create New Request");
    protected static Label label_Title = new Label("Title:");
    protected static Label label_Content = new Label("Content:");

    protected static TextField field_Title = new TextField();
    protected static TextArea field_Content = new TextArea();

    protected static Button button_Submit = new Button("Submit Request");
    protected static Button button_Return = new Button("Return to Staff Home");

    protected static Line line_Separator = new Line(20, 95, width - 20, 95);


    /*-*******************************************************************************************
     * Display Method
     *********************************************************************************************/

    /**********
     * <p> Method: displayCreateRequest </p>
     * 
     * <p> Description: Displays the Create Request page. </p>
     */
    public static void displayCreateRequest(Stage ps, User user) {

        theStage = ps;
        theUser = user;

        theRootPane = new Pane();
        theScene = new Scene(theRootPane, width, height);

        setupLabel(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);
        setupLabel(label_Title, "Arial", 18, 200, Pos.BASELINE_LEFT, 20, 120);
        setupLabel(label_Content, "Arial", 18, 200, Pos.BASELINE_LEFT, 20, 180);

        setupField(field_Title, 20, 150, 400);
        setupTextArea(field_Content, 20, 210, 500, 200);

        setupButton(button_Submit, "Dialog", 18, 200, Pos.CENTER, 20, 430);
        button_Submit.setOnAction((_) -> {
            ControllerStaffHome.submitRequest(
                field_Title.getText(),
                field_Content.getText()
            );
            ViewStaffHome.displayStaffHome(theStage, theUser);
        });

        setupButton(button_Return, "Dialog", 18, 200, Pos.CENTER, 250, 430);
        button_Return.setOnAction((_) -> {
            ViewStaffHome.displayStaffHome(theStage, theUser);
        });

        theRootPane.getChildren().addAll(
            label_PageTitle, line_Separator,
            label_Title, field_Title,
            label_Content, field_Content,
            button_Submit, button_Return
        );

        theStage.setTitle("Create Staff Request");
        theStage.setScene(theScene);
        theStage.show();
    }


    /*-*******************************************************************************************
     * Helper Methods
     *********************************************************************************************/

    private static void setupLabel(Label l, String ff, double f, double w, Pos p, double x, double y) {
        l.setFont(Font.font(ff, f));
        l.setMinWidth(w);
        l.setAlignment(p);
        l.setLayoutX(x);
        l.setLayoutY(y);
    }

    private static void setupField(TextField tf, double x, double y, double w) {
        tf.setLayoutX(x);
        tf.setLayoutY(y);
        tf.setMinWidth(w);
    }

    private static void setupTextArea(TextArea ta, double x, double y, double w, double h) {
        ta.setLayoutX(x);
        ta.setLayoutY(y);
        ta.setMinWidth(w);
        ta.setMinHeight(h);
    }

    private static void setupButton(Button b, String ff, double f, double w, Pos p, double x, double y) {
        b.setFont(Font.font(ff, f));
        b.setMinWidth(w);
        b.setAlignment(p);
        b.setLayoutX(x);
        b.setLayoutY(y);
    }
}
