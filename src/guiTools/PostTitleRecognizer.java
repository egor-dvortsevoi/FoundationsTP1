package guiTools;

/**
 * Finite-state-machine based recognizer for validating post title input.
 */
public class PostTitleRecognizer {
	/** Error text for the most recent validation. */
	public static String postTitleErrorMessage = "";
	/** Input value from the most recent validation. */
	public static String postTitleInput = "";
	/** Index of the detected error in the most recent validation. */
	public static int postTitleIndexofError = -1;

	private static int state = 0;
	private static int nextState = 0;
	private static String inputLine = "";
	private static int currentCharNdx;
	private static boolean running;
	private static int titleSize = 0;

	private PostTitleRecognizer() {
	}

	private static void moveToNextCharacter() {
		currentCharNdx++;
		if (currentCharNdx < inputLine.length()) {
		} else {
			running = false;
		}
	}

	/**
	 * Validates post title format.
	 *
	 * Rules:
	 * - required
	 * - 5 to 120 chars after trimming
	 *
	 * @param input title input
	 * @return empty string if valid, otherwise an error message
	 */
	public static String checkForValidPostTitle(String input) {
		if (input == null || input.trim().isEmpty()) {
			postTitleIndexofError = 0;
			postTitleErrorMessage = "Post title is required.";
			return postTitleErrorMessage;
		}

		state = 0;
		inputLine = input.trim();
		currentCharNdx = 0;
		postTitleInput = inputLine;
		running = true;
		nextState = -1;
		titleSize = 0;

		while (running) {
			switch (state) {
			case 0:
				nextState = 1;
				titleSize++;
				if (titleSize > 120) {
					running = false;
				}
				break;

			case 1:
				nextState = 1;
				titleSize++;
				if (titleSize > 120) {
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

		postTitleIndexofError = currentCharNdx;
		postTitleErrorMessage = "";

		if (titleSize < 5 || titleSize > 120) {
			postTitleErrorMessage = "Post title must be between 5 and 120 characters.";
			return postTitleErrorMessage;
		}

		if (currentCharNdx < inputLine.length()) {
			postTitleErrorMessage = "Post title must be between 5 and 120 characters.";
			return postTitleErrorMessage;
		}

		postTitleIndexofError = -1;
		postTitleErrorMessage = "";
		return postTitleErrorMessage;
	}
}
