package guiTools;


public class PasswordRecognizer {
	/**
	 * <p> Title: FSM-translated PasswordRecognizer. </p>
	 * 
	 * <p> Description: A demonstration of the mechanical translation of Finite State Machine 
	 * diagram into an executable Java program using the Password Recognizer. The code 
	 * detailed design is based on a while loop with a select list.</p>
	 * 
	 * <p> The password must satisfy all of the following:
	 *     - At least one lowercase letter (a-z)
	 *     - At least one uppercase letter (A-Z)
	 *     - At least one number (0-9)
	 *     - 8-16 characters total </p>
	 * 
	 */

	/**********************************************************************************************
	 * 
	 * Result attributes to be used for GUI applications where a detailed error message and a 
	 * pointer to the character of the error will enhance the user experience.
	 * 
	 */

	public static String passwordErrorMessage = "";		// The error message text
	public static String passwordInput = "";				// The input being processed
	public static int passwordIndexofError = -1;			// The index of error location
	private static int state = 0;						// The current state value
	private static int nextState = 0;					// The next state value
	private static boolean finalState = false;			// Is this state a final state?
	private static String inputLine = "";				// The input line
	private static char currentChar;					// The current character in the line
	private static int currentCharNdx;					// The index of the current character
	private static boolean running;						// The flag that specifies if the FSM is 
														// running
	private static int passwordSize = 0;				// Track password length

	// Flags to track whether required character types have been seen
	private static boolean hasUpperCase = false;
	private static boolean hasLowerCase = false;
	private static boolean hasDigit = false;

	// Private method to display debugging data
	private static void displayDebuggingInfo() {
		if (currentCharNdx >= inputLine.length())
			System.out.println(((state > 99) ? " " : (state > 9) ? "  " : "   ") + state + 
					((finalState) ? "       F   " : "           ") + "None");
		else
			System.out.println(((state > 99) ? " " : (state > 9) ? "  " : "   ") + state + 
				((finalState) ? "       F   " : "           ") + "  " + currentChar + " " + 
				((nextState > 99) ? "" : (nextState > 9) || (nextState == -1) ? "   " : "    ") + 
				nextState + "     " + passwordSize);
	}

	// Private method to move to the next character within the limits of the input line
	private static void moveToNextCharacter() {
		currentCharNdx++;
		if (currentCharNdx < inputLine.length())
			currentChar = inputLine.charAt(currentCharNdx);
		else {
			currentChar = ' ';
			running = false;
		}
	}

	/**********
	 * This method is a mechanical transformation of a Finite State Machine diagram into a Java
	 * method.
	 * 
	 * @param input		The input string for the Finite State Machine
	 * @return			An output string that is empty if everything is okay or it is a String
	 * 						with a helpful description of the error
	 */
	public static String checkForValidPassword(String input) {
		// Check to ensure that there is input to process
		if (input.length() <= 0) {
			passwordIndexofError = 0;
			return "\n*** ERROR *** The password is empty.";
		}

		// The local variables used to perform the Finite State Machine simulation
		state = 0;
		inputLine = input;
		currentCharNdx = 0;
		currentChar = input.charAt(0);

		passwordInput = input;
		running = true;
		nextState = -1;
		System.out.println("\nCurrent Final Input  Next\nState   State Char  State  Size");

		// Initialize semantic action variables
		passwordSize = 0;
		hasUpperCase = false;
		hasLowerCase = false;
		hasDigit = false;

		// The Finite State Machine continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state
		while (running) {
			switch (state) {
			case 0:
				// State 0: Accept any printable character that is valid for a password
				// We track which character types are present using flags

				if (currentChar >= 'A' && currentChar <= 'Z') {
					hasUpperCase = true;
					nextState = 1;
					passwordSize++;
				} else if (currentChar >= 'a' && currentChar <= 'z') {
					hasLowerCase = true;
					nextState = 1;
					passwordSize++;
				} else if (currentChar >= '0' && currentChar <= '9') {
					hasDigit = true;
					nextState = 1;
					passwordSize++;
				} else if (currentChar == '!' || currentChar == '@' || currentChar == '#' ||
						   currentChar == '$' || currentChar == '%' || currentChar == '^' ||
						   currentChar == '&' || currentChar == '*' || currentChar == '(' ||
						   currentChar == ')' || currentChar == '-' || currentChar == '_' ||
						   currentChar == '=' || currentChar == '+' || currentChar == '[' ||
						   currentChar == ']' || currentChar == '{' || currentChar == '}' ||
						   currentChar == '|' || currentChar == ';' || currentChar == ':' ||
						   currentChar == '\'' || currentChar == '"' || currentChar == ',' ||
						   currentChar == '.' || currentChar == '<' || currentChar == '>' ||
						   currentChar == '/' || currentChar == '?' || currentChar == '`' ||
						   currentChar == '~') {
					nextState = 1;
					passwordSize++;
				} else {
					running = false;
				}

				// If the size is larger than 16, the loop must stop
				if (passwordSize > 16)
					running = false;
				break;

			case 1:
				// State 1: Continue accepting valid password characters

				if (currentChar >= 'A' && currentChar <= 'Z') {
					hasUpperCase = true;
					nextState = 1;
					passwordSize++;
				} else if (currentChar >= 'a' && currentChar <= 'z') {
					hasLowerCase = true;
					nextState = 1;
					passwordSize++;
				} else if (currentChar >= '0' && currentChar <= '9') {
					hasDigit = true;
					nextState = 1;
					passwordSize++;
				} else if (currentChar == '!' || currentChar == '@' || currentChar == '#' ||
						   currentChar == '$' || currentChar == '%' || currentChar == '^' ||
						   currentChar == '&' || currentChar == '*' || currentChar == '(' ||
						   currentChar == ')' || currentChar == '-' || currentChar == '_' ||
						   currentChar == '=' || currentChar == '+' || currentChar == '[' ||
						   currentChar == ']' || currentChar == '{' || currentChar == '}' ||
						   currentChar == '|' || currentChar == ';' || currentChar == ':' ||
						   currentChar == '\'' || currentChar == '"' || currentChar == ',' ||
						   currentChar == '.' || currentChar == '<' || currentChar == '>' ||
						   currentChar == '/' || currentChar == '?' || currentChar == '`' ||
						   currentChar == '~') {
					nextState = 1;
					passwordSize++;
				} else {
					running = false;
				}

				// If the size is larger than 16, the loop must stop
				if (passwordSize > 16)
					running = false;
				break;
			}

			if (running) {
				displayDebuggingInfo();
				moveToNextCharacter();
				state = nextState;
				if (state == 1) finalState = true;
				nextState = -1;
			}
		}
		displayDebuggingInfo();

		System.out.println("The loop has ended.");

		passwordIndexofError = currentCharNdx;
		passwordErrorMessage = "";

		// When the FSM halts, determine if the situation is an error or not
		switch (state) {
		case 0:
			// State 0 is not a final state
			passwordIndexofError = currentCharNdx;
			passwordErrorMessage = "A password must start with a valid character.\n";
			return passwordErrorMessage;

		case 1:
			// State 1 is a final state. Check all requirements.
			if (passwordSize < 8) {
				passwordErrorMessage = "A password must have at least 8 characters.\n";
				return passwordErrorMessage;
			}
			if (passwordSize > 16) {
				passwordErrorMessage = "A password must have no more than 16 characters.\n";
				return passwordErrorMessage;
			}
			if (currentCharNdx < input.length()) {
				passwordErrorMessage = "A password contains an invalid character.\n";
				return passwordErrorMessage;
			}
			if (!hasUpperCase) {
				passwordErrorMessage = "A password must contain at least one uppercase letter (A-Z).\n";
				return passwordErrorMessage;
			}
			if (!hasLowerCase) {
				passwordErrorMessage = "A password must contain at least one lowercase letter (a-z).\n";
				return passwordErrorMessage;
			}
			if (!hasDigit) {
				passwordErrorMessage = "A password must contain at least one number (0-9).\n";
				return passwordErrorMessage;
			}

			// All checks passed - password is valid
			passwordIndexofError = -1;
			passwordErrorMessage = "";
			return passwordErrorMessage;

		default:
			return "";
		}
	}
}
