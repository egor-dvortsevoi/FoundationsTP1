package guiTools;


public class NameRecognizer {
	/**
	 * <p> Title: FSM-translated NameRecognizer. </p>
	 * 
	 * <p> Description: A demonstration of the mechanical translation of Finite State Machine 
	 * diagram into an executable Java program using the Name Recognizer. The code 
	 * detailed design is based on a while loop with a select list.</p>
	 * 
	 * <p> A name (first, middle, last, preferred first) must satisfy:
	 *     - 3-16 characters
	 *     - Only alphabetic characters (A-Z, a-z) </p>
	 * 
	 */

	/**********************************************************************************************
	 * 
	 * Result attributes to be used for GUI applications where a detailed error message and a 
	 * pointer to the character of the error will enhance the user experience.
	 * 
	 */

	public static String nameErrorMessage = "";			// The error message text
	public static String nameInput = "";				// The input being processed
	public static int nameIndexofError = -1;			// The index of error location
	private static int state = 0;						// The current state value
	private static int nextState = 0;					// The next state value
	private static boolean finalState = false;			// Is this state a final state?
	private static String inputLine = "";				// The input line
	private static char currentChar;					// The current character in the line
	private static int currentCharNdx;					// The index of the current character
	private static boolean running;						// The flag that specifies if the FSM is 
														// running
	private static int nameSize = 0;					// Track name length

	// Private method to display debugging data
	private static void displayDebuggingInfo() {
		if (currentCharNdx >= inputLine.length())
			System.out.println(((state > 99) ? " " : (state > 9) ? "  " : "   ") + state + 
					((finalState) ? "       F   " : "           ") + "None");
		else
			System.out.println(((state > 99) ? " " : (state > 9) ? "  " : "   ") + state + 
				((finalState) ? "       F   " : "           ") + "  " + currentChar + " " + 
				((nextState > 99) ? "" : (nextState > 9) || (nextState == -1) ? "   " : "    ") + 
				nextState + "     " + nameSize);
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
	 * @param fieldName	The name of the field being validated (e.g. "First Name", "Last Name")
	 * @return			An output string that is empty if everything is okay or it is a String
	 * 						with a helpful description of the error
	 */
	public static String checkForValidName(String input, String fieldName) {
		// Check to ensure that there is input to process
		if (input.length() <= 0) {
			nameIndexofError = 0;
			return fieldName + " must not be empty.\n";
		}

		// The local variables used to perform the Finite State Machine simulation
		state = 0;
		inputLine = input;
		currentCharNdx = 0;
		currentChar = input.charAt(0);

		nameInput = input;
		running = true;
		nextState = -1;
		System.out.println("\nCurrent Final Input  Next\nState   State Char  State  Size");

		// Initialize semantic action variables
		nameSize = 0;

		// The Finite State Machine continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state
		while (running) {
			switch (state) {
			case 0:
				// State 0: Accept alphabetic characters (A-Z, a-z)
				if ((currentChar >= 'A' && currentChar <= 'Z') ||
						(currentChar >= 'a' && currentChar <= 'z')) {
					nextState = 1;
					nameSize++;
				} else {
					running = false;
				}
				break;

			case 1:
				// State 1: Continue accepting alphabetic characters
				if ((currentChar >= 'A' && currentChar <= 'Z') ||
						(currentChar >= 'a' && currentChar <= 'z')) {
					nextState = 1;
					nameSize++;
				} else {
					running = false;
				}

				// If the size is larger than 16, the loop must stop
				if (nameSize > 16)
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

		nameIndexofError = currentCharNdx;
		nameErrorMessage = "";

		// When the FSM halts, determine if the situation is an error or not
		switch (state) {
		case 0:
			// State 0 is not a final state
			nameIndexofError = currentCharNdx;
			nameErrorMessage = fieldName + " must start with an alphabetic character (A-Z, a-z).\n";
			return nameErrorMessage;

		case 1:
			// State 1 is a final state. Check length requirements.
			if (nameSize < 3) {
				nameErrorMessage = fieldName + " must have at least 3 characters.\n";
				return nameErrorMessage;
			}
			if (nameSize > 16) {
				nameErrorMessage = fieldName + " must have no more than 16 characters.\n";
				return nameErrorMessage;
			}
			if (currentCharNdx < input.length()) {
				nameErrorMessage = fieldName + " may only contain alphabetic characters (A-Z, a-z).\n";
				return nameErrorMessage;
			}

			// All checks passed - name is valid
			nameIndexofError = -1;
			nameErrorMessage = "";
			return nameErrorMessage;

		default:
			return "";
		}
	}
}
