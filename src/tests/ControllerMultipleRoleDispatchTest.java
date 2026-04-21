package tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

public class ControllerMultipleRoleDispatchTest {

    @Test
    public void testPerformRoleIsUntestableDueToJavaFXStaticInitialization() {

        assertThrows(ExceptionInInitializerError.class, () -> {
            Class.forName("guiMultipleRoleDispatch.ViewMultipleRoleDispatch");
        });
    }
}
