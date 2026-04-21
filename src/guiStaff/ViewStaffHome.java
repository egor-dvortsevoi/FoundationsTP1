package guiStaff;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import database.Database;
import entityClasses.User;

/*******
 * <p>
 * Title: ViewStaffHome Class.
 * </p>
 * 
 * <p>
 * Description: The Java/FX-based Staff Home Page. The page is a stub for some
 * role needed for the application. The widgets on this page are likely the
 * minimum number and kind for other role pages that may be needed.
 * </p>
 * 
 * <p>
 * Copyright: Lynn Robert Carter © 2025
 * </p>
 * 
 * @author Lynn
 * 
 * @version 1.00 2025-04-20 Initial version
 */

public class ViewStaffHome {

    private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
    private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

    // GUI Area 1
    protected static Label label_PageTitle = new Label();
    protected static Label label_UserDetails = new Label();
    protected static Button button_UpdateThisUser = new Button("Account Update");

    // === Staff Request System ===
    protected static Label label_RequestSection = new Label();
    protected static Button button_CreateRequest = new Button("Create New Request");
    protected static Button button_ViewMyRequests = new Button("View My Requests");
    protected static Button button_ReopenRequest = new Button("Reopen Selected Request");

    // === Private Feedback ===
    protected static Label label_FeedbackSection = new Label();
    protected static Label label_PostId = new Label();
    protected static Label label_Recipient = new Label();
    protected static Label label_Message = new Label();

    protected static TextField text_PostId = new TextField();
    protected static TextField text_RecipientUsername = new TextField();
    protected static TextArea text_FeedbackMessage = new TextArea();

    protected static Button button_SendFeedback = new Button("Send Feedback");

    // === Discussion Evaluation ===
    protected static Label label_AnalyticsSection = new Label();
    protected static Label label_StudentUsername = new Label();
    protected static Label label_AnalyticsResult = new Label();

    protected static TextField text_StudentUsername = new TextField();
    protected static Button button_EvaluateStudent = new Button("Evaluate Student");

    // GUI Area 3
    protected static Button button_Logout = new Button("Logout");
    protected static Button button_Quit = new Button("Quit");
    protected static Button button_SwitchRole = new Button("Switch Role");

    // Backend references
    private static ViewStaffHome theView;
    private static Database theDatabase = applicationMain.FoundationsMain.database;
    public static Database db;

    protected static Stage theStage;
    protected static Pane theRootPane;
    protected static User theUser;

    private static Scene thestaffHomeScene;
    protected static final int theRole = 3;

    public static void displayStaffHome(Stage ps, User user) {

        theStage = ps;
        theUser = user;
        db = theDatabase;

        if (theView == null)
            theView = new ViewStaffHome();

        theDatabase.getUserAccountDetails(user.getUserName());
        applicationMain.FoundationsMain.activeHomePage = theRole;

        theUser.setAdminRole(theDatabase.getCurrentAdminRole());
        theUser.setStudentUser(theDatabase.getCurrentNewStudent());
        theUser.setStaffUser(theDatabase.getCurrentNewStaff());

        label_UserDetails.setText("User: " + theUser.getUserName());
        button_SwitchRole.setVisible(theUser.getNumRoles() > 1);

        theStage.setTitle("CSE 360 Foundations: Staff Home Page");
        theStage.setScene(thestaffHomeScene);
        theStage.show();
    }

    private ViewStaffHome() {

        theRootPane = new Pane();
        thestaffHomeScene = new Scene(theRootPane, width, height);

        // GUI Area 1
        label_PageTitle.setText("Staff Home Page");
        setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

        setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

        setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 45);
        button_UpdateThisUser.setOnAction((_) -> ControllerStaffHome.performUpdate());

        // === Staff Request System ===
        label_RequestSection.setText("Staff Request System");
        setupLabelUI(label_RequestSection, "Arial", 20, 280, Pos.BASELINE_LEFT, 20, 120);

        setupButtonUI(button_CreateRequest, "Dialog", 16, 250, Pos.CENTER, 20, 160);
        button_CreateRequest.setOnAction((_) -> ControllerStaffHome.performCreateRequest());

        setupButtonUI(button_ViewMyRequests, "Dialog", 16, 250, Pos.CENTER, 20, 205);
        button_ViewMyRequests.setOnAction((_) -> ControllerStaffHome.performViewMyRequests());

        setupButtonUI(button_ReopenRequest, "Dialog", 16, 250, Pos.CENTER, 20, 250);
        button_ReopenRequest.setOnAction((_) -> ControllerStaffHome.performReopenRequest());

        // === Private Feedback ===
        label_FeedbackSection.setText("Private Feedback");
        setupLabelUI(label_FeedbackSection, "Arial", 22, 280, Pos.BASELINE_LEFT, 20, 300);

        label_PostId.setText("Post ID");
        setupLabelUI(label_PostId, "Arial", 16, 180, Pos.BASELINE_LEFT, 20, 340);
        setupTextFieldUI(text_PostId, "Arial", 16, 180, 190, 333);

        label_Recipient.setText("Recipient Username");
        setupLabelUI(label_Recipient, "Arial", 16, 220, Pos.BASELINE_LEFT, 20, 380);
        setupTextFieldUI(text_RecipientUsername, "Arial", 16, 180, 190, 373);

        label_Message.setText("Feedback Message");
        setupLabelUI(label_Message, "Arial", 16, 220, Pos.BASELINE_LEFT, 20, 420);

        // Larger feedback box
        text_FeedbackMessage.setFont(Font.font("Arial", 16));
        text_FeedbackMessage.setPrefWidth(350);
        text_FeedbackMessage.setPrefHeight(85);
        text_FeedbackMessage.setLayoutX(20);
        text_FeedbackMessage.setLayoutY(440);
        text_FeedbackMessage.setWrapText(true);

        // Send Feedback button aligned right
        setupButtonUI(button_SendFeedback, "Dialog", 18, 200, Pos.CENTER, 380, 440);
        button_SendFeedback.setOnAction((_) -> ControllerStaffHome.performSendPrivateFeedback());

        // === Discussion Evaluation ===
        label_AnalyticsSection.setText("Discussion Evaluation");
        setupLabelUI(label_AnalyticsSection, "Arial", 22, 320, Pos.BASELINE_LEFT, 430, 120);

        label_StudentUsername.setText("Student Username");
        setupLabelUI(label_StudentUsername, "Arial", 16, 220, Pos.BASELINE_LEFT, 430, 180);
        setupTextFieldUI(text_StudentUsername, "Arial", 16, 240, 430, 220);

        setupButtonUI(button_EvaluateStudent, "Dialog", 18, 220, Pos.CENTER, 430, 290);
        button_EvaluateStudent.setOnAction((_) -> ControllerStaffHome.performEvaluateStudent());

        setupLabelUI(label_AnalyticsResult, "Arial", 16, 300, Pos.BASELINE_LEFT, 430, 350);
        label_AnalyticsResult.setWrapText(true);
        label_AnalyticsResult.setPrefWidth(300);

        // === Bottom Buttons ===
        setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 560);
        button_Logout.setOnAction((_) -> ControllerStaffHome.performLogout());

        setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 560);
        button_Quit.setOnAction((_) -> ControllerStaffHome.performQuit());

        setupButtonUI(button_SwitchRole, "Dialog", 18, 180, Pos.CENTER, 580, 560);
        button_SwitchRole.setOnAction((_) -> ControllerStaffHome.performSwitchRole());

        // Add all widgets
        theRootPane.getChildren().addAll(
                label_PageTitle, label_UserDetails, button_UpdateThisUser,

                label_RequestSection, button_CreateRequest, button_ViewMyRequests, button_ReopenRequest,

                label_FeedbackSection, label_PostId, text_PostId,
                label_Recipient, text_RecipientUsername,
                label_Message, text_FeedbackMessage, button_SendFeedback,

                label_AnalyticsSection, label_StudentUsername, text_StudentUsername,
                button_EvaluateStudent, label_AnalyticsResult,

                button_Logout, button_Quit, button_SwitchRole);
    }

    private static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y) {
        l.setFont(Font.font(ff, f));
        l.setMinWidth(w);
        l.setAlignment(p);
        l.setLayoutX(x);
        l.setLayoutY(y);
    }

    private static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y) {
        b.setFont(Font.font(ff, f));
        b.setMinWidth(w);
        b.setAlignment(p);
        b.setLayoutX(x);
        b.setLayoutY(y);
    }

    private static void setupTextFieldUI(TextField t, String ff, double f, double w, double x, double y) {
        t.setFont(Font.font(ff, f));
        t.setMinWidth(w);
        t.setLayoutX(x);
        t.setLayoutY(y);
    }

    public TextField getPostIdField() {
        return text_PostId;
    }

    public TextField getRecipientField() {
        return text_RecipientUsername;
    }

    public TextArea getMessageField() {
        return text_FeedbackMessage;
    }

    public TextField getStudentField() {
        return text_StudentUsername;
    }

    public Label getAnalyticsResultLabel() {
        return label_AnalyticsResult;
    }

    public static ViewStaffHome getInstance() {
        return theView;
    }
}
