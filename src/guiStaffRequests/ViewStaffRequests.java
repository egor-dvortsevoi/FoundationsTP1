package guiStaffRequests;

import guiStaff.ControllerStaffHome;
import guiStaff.ViewStaffHome;
import entityClasses.Request;
import entityClasses.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.List;

/*******
 * <p> Title: ViewStaffRequests Class. </p>
 * 
 * <p> Description: Displays all requests created by the staff user.
 * Allows reopening a closed request. </p>
 * 
 * <p> Follows the MVC structure used throughout the Foundations system. </p>
 * 
 * <p> Copyright:
 * Lynn Robert Carter © 2025 </p>
 */

public class ViewStaffRequests {

    /*-*******************************************************************************************
     * Attributes
     *********************************************************************************************/

    private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
    private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

    protected static Stage theStage;
    protected static User theUser;
    protected static Pane theRootPane;
    private static Scene theScene;

    protected static Label label_PageTitle = new Label("My Requests");
    protected static Line line_Separator = new Line(20, 95, width - 20, 95);

    protected static TableView<Request> table = new TableView<>();

    protected static Button button_Reopen = new Button("Reopen Selected Request");
    protected static Button button_Return = new Button("Return to Staff Home");


    /*-*******************************************************************************************
     * Display Method
     *********************************************************************************************/

    /**********
     * <p> Method: displayStaffRequests </p>
     * 
     * <p> Description: Displays the staff member’s request list. </p>
     */
    public static void displayStaffRequests(Stage ps, User user) {

        theStage = ps;
        theUser = user;

        theRootPane = new Pane();
        theScene = new Scene(theRootPane, width, height);

        setupLabel(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

        setupTable();

        setupButton(button_Reopen, "Dialog", 18, 250, Pos.CENTER, 20, 500);
        button_Reopen.setOnAction((_) -> {
            Request selected = table.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getStatus().equals("CLOSED")) {
                ViewStaffCreateRequest.displayCreateRequest(theStage, theUser);
            }
        });

        setupButton(button_Return, "Dialog", 18, 250, Pos.CENTER, 300, 500);
        button_Return.setOnAction((_) -> {
            ViewStaffHome.displayStaffHome(theStage, theUser);
        });

        theRootPane.getChildren().addAll(
            label_PageTitle, line_Separator,
            table,
            button_Reopen, button_Return
        );

        theStage.setTitle("My Requests");
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

    private static void setupButton(Button b, String ff, double f, double w, Pos p, double x, double y) {
        b.setFont(Font.font(ff, f));
        b.setMinWidth(w);
        b.setAlignment(p);
        b.setLayoutX(x);
        b.setLayoutY(y);
    }

    private static void setupTable() {

        table.setLayoutX(20);
        table.setLayoutY(120);
        table.setMinWidth(width - 40);
        table.setMinHeight(350);

        TableColumn<Request, Integer> colID = new TableColumn<>("ID");
        colID.setCellValueFactory(new PropertyValueFactory<>("requestID"));

        TableColumn<Request, String> colTitle = new TableColumn<>("Title");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Request, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Request, String> colTimestamp = new TableColumn<>("Timestamp");
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        table.getColumns().addAll(colID, colTitle, colStatus, colTimestamp);

        List<Request> list = ControllerStaffHome.getMyRequests();
        ObservableList<Request> data = FXCollections.observableArrayList(list);
        table.setItems(data);
    }
}
