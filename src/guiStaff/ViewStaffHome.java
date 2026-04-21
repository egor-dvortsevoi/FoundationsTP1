package guiStaff;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
import entityClasses.User;


/*******
 * <p> Title: ViewStaffHome Class. </p>
 * 
 * <p> Description: The Java/FX-based Staff Home Page.  The page is a stub for some role needed for
 * the application.  The widgets on this page are likely the minimum number and kind for other role
 * pages that may be needed.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00        2025-04-20 Initial version
 *  
 */

public class ViewStaffHome {
    
    /*-*******************************************************************************************

    Attributes
    
     */
    
    // These are the application values required by the user interface
    
    private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
    private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;


    // These are the widget attributes for the GUI. There are 3 areas for this GUI.
    
    // GUI Area 1: It informs the user about the purpose of this page, whose account is being used,
    // and a button to allow this user to update the account settings
    protected static Label label_PageTitle = new Label();
    protected static Label label_UserDetails = new Label();
    protected static Button button_UpdateThisUser = new Button("Account Update");
        
    // This is a separator and it is used to partition the GUI for various tasks
    protected static Line line_Separator1 = new Line(20, 95, width-20, 95);

    // GUI Area 2: This is a stub, so there are no widgets here.  For an actual role page, this area
    // would contain the widgets needed for the user to play the assigned role.

    // === Staff Request System Buttons (ADDED) ===
    protected static Button button_CreateRequest = new Button("Create New Request");
    protected static Button button_ViewMyRequests = new Button("View My Requests");
    protected static Button button_ReopenRequest = new Button("Reopen Selected Request");
    
    
    
    // This is a separator and it is used to partition the GUI for various tasks
    protected static Line line_Separator4 = new Line(20, 525, width-20,525);
    
    // GUI Area 3: This is last of the GUI areas.  It is used for quitting the application and for
    // logging out.
    protected static Button button_Logout = new Button("Logout");
    protected static Button button_Quit = new Button("Quit");
    protected static Button button_SwitchRole = new Button("Switch Role");

    // This is the end of the GUI objects for the page.
    
    // These attributes are used to configure the page and populate it with this user's information
    private static ViewStaffHome theView;       

    // Reference for the in-memory database so this package has access
    private static Database theDatabase = applicationMain.FoundationsMain.database;

    // ⭐ ADDED: Public static DB reference for controllers/models
    public static Database db;   // ADDED

    protected static Stage theStage;            
    protected static Pane theRootPane;          
    protected static User theUser;              
                
    private static Scene thestaffHomeScene;     
    protected static final int theRole = 3;     

    /*-*******************************************************************************************

    Constructors
    
     */

    /**********
     * <p> Method: displayStaffHome(Stage ps, User user) </p>
     */
    public static void displayStaffHome(Stage ps, User user) {
        
        // Establish the references to the GUI and the current user
        theStage = ps;
        theUser = user;

        // ⭐ ADDED: Initialize the global DB reference
        db = theDatabase;   // ADDED
        
        // If not yet established, populate the static aspects of the GUI
        if (theView == null) theView = new ViewStaffHome();     
        
        // Populate the dynamic aspects of the GUI with the data from the user and the current
        // state of the system.
        theDatabase.getUserAccountDetails(user.getUserName());
        applicationMain.FoundationsMain.activeHomePage = theRole;
        
        // Refresh the user's roles from the database so the Switch Role button is accurate
        theUser.setAdminRole(theDatabase.getCurrentAdminRole());
        theUser.setStudentUser(theDatabase.getCurrentNewStudent());
        theUser.setStaffUser(theDatabase.getCurrentNewStaff());
        
        label_UserDetails.setText("User: " + theUser.getUserName());

        button_SwitchRole.setVisible(theUser.getNumRoles() > 1);

        theStage.setTitle("CSE 360 Foundations: Staff Home Page");
        theStage.setScene(thestaffHomeScene);
        theStage.show();
    }
    
    /**********
     * <p> Method: ViewstaffHome() </p>
     */
    private ViewStaffHome() {
        
        theRootPane = new Pane();
        thestaffHomeScene = new Scene(theRootPane, width, height);
        
        label_PageTitle.setText("Staff Home Page");
        setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

        label_UserDetails.setText("User: " + theUser.getUserName());
        setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);
        
        setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 45);
        button_UpdateThisUser.setOnAction((_) -> {ControllerStaffHome.performUpdate(); });

        // === Staff Request System Buttons (ADDED) ===
        setupButtonUI(button_CreateRequest, "Dialog", 16, 250, Pos.CENTER, 20, 150);
        button_CreateRequest.setOnAction((_) -> {
            ControllerStaffHome.performCreateRequest();
        });

        setupButtonUI(button_ViewMyRequests, "Dialog", 16, 250, Pos.CENTER, 20, 200);
        button_ViewMyRequests.setOnAction((_) -> {
            ControllerStaffHome.performViewMyRequests();
        });

        setupButtonUI(button_ReopenRequest, "Dialog", 16, 250, Pos.CENTER, 20, 250);
        button_ReopenRequest.setOnAction((_) -> {
            ControllerStaffHome.performReopenRequest();
        });

        // GUI Area 3
        setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 540);
        button_Logout.setOnAction((_) -> {ControllerStaffHome.performLogout(); });
        
        setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 540);
        button_Quit.setOnAction((_) -> {ControllerStaffHome.performQuit(); });

        setupButtonUI(button_SwitchRole, "Dialog", 18, 180, Pos.CENTER, 580, 540);
        button_SwitchRole.setOnAction((_) -> {ControllerStaffHome.performSwitchRole(); });

        theRootPane.getChildren().addAll(
            label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,

            // === Staff Request System Buttons (ADDED) ===
            button_CreateRequest,
            button_ViewMyRequests,
            button_ReopenRequest,

            line_Separator4, button_Logout, button_Quit, button_SwitchRole
        );
    }
    
    
    /*-********************************************************************************************
    Helper methods
     */
    
    private static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y){
        l.setFont(Font.font(ff, f));
        l.setMinWidth(w);
        l.setAlignment(p);
        l.setLayoutX(x);
        l.setLayoutY(y);        
    }
    
    private static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y){
        b.setFont(Font.font(ff, f));
        b.setMinWidth(w);
        b.setAlignment(p);
        b.setLayoutX(x);
        b.setLayoutY(y);        
    }
}
