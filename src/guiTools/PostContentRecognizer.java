package guiTools;

/**
 * Finite-state-machine based recognizer for validating post content input.
 */
public class PostContentRecognizer {
	/** Error text for the most recent validation. */
	public static String postContentErrorMessage = "";
	/** Input value from the most recent validation. */
	public static String postContentInput = "";
	/** Index of the detected error in the most recent validation. */
	public static int postContentIndexofError = -1;

	private static int state = 0;
	private static int nextState = 0;
	private static String inputLine = "";
	private static int currentCharNdx;
	private static boolean running;
	private static int contentSize = 0;

	private PostContentRecognizer() {
	}

	private static void moveToNextCharacter() {
		currentCharNdx++;
		if (currentCharNdx < inputLine.length()) {
		} else {
			running = false;
		}
	}

	/**
	 * Validates post content format.
	 *
	 * Rules:
	 * - required
	 * - 10 to 2000 chars after trimming
	 *
	 * @param input post content input
	 * @return empty string if valid, otherwise an error message
	 */
	public static String checkForValidPostContent(String input) {
		if (input == null || input.trim().isEmpty()) {
			postContentIndexofError = 0;
			postContentErrorMessage = "Post content is required.";
			return postContentErrorMessage;
		}

		state = 0;
		inputLine = input.trim();
		currentCharNdx = 0;
		postContentInput = inputLine;
		running = true;
		nextState = -1;
		contentSize = 0;

		while (running) {
			switch (state) {
			case 0:
				nextState = 1;
				contentSize++;
				if (contentSize > 2000) {
					running = false;
				}
				break;

			case 1:
				nextState = 1;
				contentSize++;
				if (contentSize > 2000) {
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

		postContentIndexofError = currentCharNdx;
		postContentErrorMessage = "";

		if (contentSize < 10 || contentSize > 2000) {
			postContentErrorMessage = "Post content must be between 10 and 2000 characters.";
			return postContentErrorMessage;
		}

		if (currentCharNdx < inputLine.length()) {
			postContentErrorMessage = "Post content must be between 10 and 2000 characters.";
			return postContentErrorMessage;
		}

		postContentIndexofError = -1;
		postContentErrorMessage = "";
		return postContentErrorMessage;
	}
}
