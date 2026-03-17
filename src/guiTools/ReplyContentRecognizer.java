package guiTools;

/**
 * Finite-state-machine based recognizer for validating reply content input.
 */
public class ReplyContentRecognizer {
	/** Error text for the most recent validation. */
	public static String replyContentErrorMessage = "";
	/** Input value from the most recent validation. */
	public static String replyContentInput = "";
	/** Index of the detected error in the most recent validation. */
	public static int replyContentIndexofError = -1;

	private static int state = 0;
	private static int nextState = 0;
	private static String inputLine = "";
	private static int currentCharNdx;
	private static boolean running;
	private static int contentSize = 0;

	private ReplyContentRecognizer() {
	}

	private static void moveToNextCharacter() {
		currentCharNdx++;
		if (currentCharNdx < inputLine.length()) {
		} else {
			running = false;
		}
	}

	/**
	 * Validates reply content format.
	 *
	 * Rules:
	 * - required
	 * - 2 to 1500 chars after trimming
	 *
	 * @param input reply content input
	 * @return empty string if valid, otherwise an error message
	 */
	public static String checkForValidReplyContent(String input) {
		if (input == null || input.trim().isEmpty()) {
			replyContentIndexofError = 0;
			replyContentErrorMessage = "Reply content is required.";
			return replyContentErrorMessage;
		}

		state = 0;
		inputLine = input.trim();
		currentCharNdx = 0;
		replyContentInput = inputLine;
		running = true;
		nextState = -1;
		contentSize = 0;

		while (running) {
			switch (state) {
			case 0:
				nextState = 1;
				contentSize++;
				if (contentSize > 1500) {
					running = false;
				}
				break;

			case 1:
				nextState = 1;
				contentSize++;
				if (contentSize > 1500) {
					running = false;
				}
				break;
			default:
				running = false;
				break;
			}

			if (running) {
				moveToNextCharacter();
				state = nextState;
				nextState = -1;
			}
		}

		replyContentIndexofError = currentCharNdx;
		replyContentErrorMessage = "";

		if (contentSize < 2 || contentSize > 1500) {
			replyContentErrorMessage = "Reply content must be between 2 and 1500 characters.";
			return replyContentErrorMessage;
		}

		if (currentCharNdx < inputLine.length()) {
			replyContentErrorMessage = "Reply content must be between 2 and 1500 characters.";
			return replyContentErrorMessage;
		}

		replyContentIndexofError = -1;
		replyContentErrorMessage = "";
		return replyContentErrorMessage;
	}
}
