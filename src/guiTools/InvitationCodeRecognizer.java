package guiTools;

/**
 * Finite-state-machine based recognizer for validating invitation code format.
 */
public class InvitationCodeRecognizer {
	/**
	 * <p> Title: FSM-translated InvitationCodeRecognizer. </p>
	 * 
	 * <p> Description: A demonstration of the mechanical translation of Finite State Machine 
	 * diagram into an executable Java program using the Invitation Code Recognizer. The code 
	 * detailed design is based on a while loop with a select list.</p>
	 * 
	 * <p> The invitation code must satisfy:
	 *     - Exactly 6 characters
	 *     - Only lowercase hexadecimal characters (a-f, 0-9) </p>
	 * 
	 */

	/**********************************************************************************************
	 * 
	 * Result attributes to be used for GUI applications where a detailed error message and a 
	 * pointer to the character of the error will enhance the user experience.
	 * 
	 */

	/** Error text for the most recent validation. */
	public static String invitationCodeErrorMessage = "";		// The error message text
	/** Input value from the most recent validation. */
	public static String invitationCodeInput = "";				// The input being processed
	/** Index of the detected error in the most recent validation. */
	public static int invitationCodeIndexofError = -1;			// The index of error location
	private static int state = 0;						// The current state value
	private static int nextState = 0;					// The next state value
	private static boolean finalState = false;			// Is this state a final state?
	private static String inputLine = "";				// The input line
	private static char currentChar;					// The current character in the line
	private static int currentCharNdx;					// The index of the current character
	private static boolean running;						// The flag that specifies if the FSM is 
														// running
	private static int codeSize = 0;					// Track code length

	/**
	 * Creates an invitation code recognizer.
	 */
	public InvitationCodeRecognizer() {
	}

	// Private method to display debugging data
	private static void displayDebuggingInfo() {
		if (currentCharNdx >= inputLine.length())
			System.out.println(((state > 99) ? " " : (state > 9) ? "  " : "   ") + state + 
					((finalState) ? "       F   " : "           ") + "None");
		else
			System.out.println(((state > 99) ? " " : (state > 9) ? "  " : "   ") + state + 
				((finalState) ? "       F   " : "           ") + "  " + currentChar + " " + 
				((nextState > 99) ? "" : (nextState > 9) || (nextState == -1) ? "   " : "    ") + 
				nextState + "     " + codeSize);
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
	public static String checkForValidInvitationCode(String input) {
		// Check to ensure that there is input to process
		if (input.length() <= 0) {
			invitationCodeIndexofError = 0;
			return "The invitation code is not valid.";
		}

		// The local variables used to perform the Finite State Machine simulation
		state = 0;
		inputLine = input;
		currentCharNdx = 0;
		currentChar = input.charAt(0);

		invitationCodeInput = input;
		running = true;
		nextState = -1;
		System.out.println("\nCurrent Final Input  Next\nState   State Char  State  Size");

		// Initialize semantic action variables
		codeSize = 0;

		// The Finite State Machine continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state
		while (running) {
			switch (state) {
			case 0:
				// State 0: Accept lowercase hex characters (a-f, 0-9)
				if ((currentChar >= 'a' && currentChar <= 'f') ||
						(currentChar >= '0' && currentChar <= '9')) {
					nextState = 1;
					codeSize++;
				} else {
					running = false;
				}
				break;

			case 1:
				// State 1: Continue accepting lowercase hex characters
				if ((currentChar >= 'a' && currentChar <= 'f') ||
						(currentChar >= '0' && currentChar <= '9')) {
					nextState = 1;
					codeSize++;
				} else {
					running = false;
				}

				// If the size is larger than 6, the loop must stop
				if (codeSize > 6)
					running = false;
				break;
			}

			if (running) {
				displayDebuggingInfo();
				moveToNextCharacter();
				state = nextState;
				if (state == 1 && codeSize == 6) finalState = true;
				else finalState = false;
				nextState = -1;
			}
		}
		displayDebuggingInfo();

		System.out.println("The loop has ended.");

		invitationCodeIndexofError = currentCharNdx;
		invitationCodeErrorMessage = "";

		// Errors for invitation codes are hidden from the user - use a generic message
		String genericError = "The invitation code is not valid.";

		// When the FSM halts, determine if the situation is an error or not
		switch (state) {
		case 0:
			// State 0 is not a final state
			invitationCodeIndexofError = currentCharNdx;
			invitationCodeErrorMessage = genericError;
			return invitationCodeErrorMessage;

		case 1:
			// State 1 is a final state only when exactly 6 characters have been consumed
			if (codeSize != 6) {
				invitationCodeErrorMessage = genericError;
				return invitationCodeErrorMessage;
			}
			if (currentCharNdx < input.length()) {
				invitationCodeErrorMessage = genericError;
				return invitationCodeErrorMessage;
			}

			// All checks passed - invitation code format is valid
			invitationCodeIndexofError = -1;
			invitationCodeErrorMessage = "";
			return invitationCodeErrorMessage;

		default:
			return genericError;
		}
	}
}
