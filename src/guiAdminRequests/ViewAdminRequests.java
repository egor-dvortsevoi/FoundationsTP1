package guiAdminRequests;

import java.util.List;

import entityClasses.Request;
import entityClasses.User;
import guiAdminHome.ViewAdminHome;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/*******
 * <p> Title: ViewAdminRequests Class. </p>
 * 
 * <p> Description: This JavaFX page displays all staff requests to the Admin.
 * It allows the Admin to view all requests in a table and close any selected
 * request. A Return button navigates back to the Admin Home Page.
 * 
 * This class follows the MVC pattern and is a View component. </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author 
 * @version 1.00
 */

public class ViewAdminRequests {

    private static Stage theStage;
    private static Scene theScene;
    private static Pane theRootPane;

    private static User theUser;   // Store the logged-in user

    private static TableView<Request> table_Requests = new TableView<>();

    private static Button button_CloseRequest = new Button("Close Selected Request");
    private static Button button_Return = new Button("Return to Admin Home");

    /**********
     * <p> Method: displayAdminRequests(Stage ps, User user) </p>
     * 
     * <p> Description: Entry point to show the Admin Requests page. </p>
     */
    public static void displayAdminRequests(Stage ps, User user) {

        theStage = ps;
        theUser = user;   // Store user for return navigation

        if (theRootPane == null)
            buildUI();

        refreshTable();

        theStage.setTitle("Admin Request Management");
        theStage.setScene(theScene);
        theStage.show();
    }

    /**********
     * <p> Method: buildUI() </p>
     * 
     * <p> Description: Builds the TableView, buttons, and layout. </p>
     */
    private static void buildUI() {

        theRootPane = new Pane();
        theScene = new Scene(theRootPane, 
                applicationMain.FoundationsMain.WINDOW_WIDTH,
                applicationMain.FoundationsMain.WINDOW_HEIGHT);

        // ===== Table Columns =====
        TableColumn<Request, Integer> col_ID = new TableColumn<>("ID");
        col_ID.setCellValueFactory(new PropertyValueFactory<>("id"));
        col_ID.setPrefWidth(60);

        TableColumn<Request, String> col_User = new TableColumn<>("Staff User");
        col_User.setCellValueFactory(new PropertyValueFactory<>("staffUsername"));
        col_User.setPrefWidth(120);

        TableColumn<Request, String> col_Title = new TableColumn<>("Title");
        col_Title.setCellValueFactory(new PropertyValueFactory<>("title"));
        col_Title.setPrefWidth(180);

        TableColumn<Request, String> col_Status = new TableColumn<>("Status");
        col_Status.setCellValueFactory(new PropertyValueFactory<>("status"));
        col_Status.setPrefWidth(100);

        TableColumn<Request, String> col_Time = new TableColumn<>("Timestamp");
        col_Time.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        col_Time.setPrefWidth(160);

        TableColumn<Request, Integer> col_Parent = new TableColumn<>("Parent ID");
        col_Parent.setCellValueFactory(new PropertyValueFactory<>("parentRequestID"));
        col_Parent.setPrefWidth(90);

        table_Requests.getColumns().addAll(col_ID, col_User, col_Title, col_Status, col_Time, col_Parent);

        table_Requests.setLayoutX(20);
        table_Requests.setLayoutY(20);
        table_Requests.setPrefWidth(700);
        table_Requests.setPrefHeight(450);

        // ===== Close Request Button =====
        setupButton(button_CloseRequest, 200, 500);
        button_CloseRequest.setOnAction((_) -> {
            Request selected = table_Requests.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ControllerAdminRequests.closeRequest(selected.getId());
                refreshTable();
            }
        });

        // ===== Return Button =====
        setupButton(button_Return, 450, 500);
        button_Return.setOnAction((_) -> {
            ViewAdminHome.displayAdminHome(theStage, theUser);
        });

        theRootPane.getChildren().addAll(table_Requests, button_CloseRequest, button_Return);
    }

    /**********
     * <p> Method: refreshTable() </p>
     * 
     * <p> Description: Reloads all requests from the Model. </p>
     */
    protected static void refreshTable() {
        List<Request> list = ControllerAdminRequests.getAllRequests();
        table_Requests.getItems().setAll(list);
    }

    /**********
     * Helper method to style buttons
     */
    private static void setupButton(Button b, double x, double y) {
        b.setFont(Font.font("Dialog", 16));
        b.setMinWidth(200);
        b.setAlignment(Pos.CENTER);
        b.setLayoutX(x);
        b.setLayoutY(y);
    }
}
