package tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import guiNewAccount.ModelNewAccount;

public class ModelNewAccountTest {

    private ModelNewAccount model;

    @BeforeEach
    public void setup() {
        model = new ModelNewAccount();
    }

    @Test
    public void testModelNewAccountExists() {
        assertNotNull(model, "ModelNewAccount should be constructible");
    }

    @Test
    public void testModelHasNoBehavior() {
        // Since the class has no methods, this test documents the design flaw.
        // This is intentional for HW3: we are demonstrating Insecure Design.
        assertEquals(0, ModelNewAccount.class.getDeclaredMethods().length,
                "ModelNewAccount should have no declared methods (design flaw)");
    }

    @Test
    public void testModelHasNoFields() {
        // Again, documenting the design flaw.
        assertEquals(0, ModelNewAccount.class.getDeclaredFields().length,
                "ModelNewAccount should have no fields (design flaw)");
    }
}
