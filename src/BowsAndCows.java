import java.util.Random;
import java.util.Scanner;

public class BowsAndCows {
	private static int guess;

	public static void main(String args[]) {
		guess = 0;
		if (isValid()) {
			System.out.println(guess);
			play();
		}
	}

	private static void play() {
		Scanner sc = new Scanner(System.in);
		while (true) {
			System.out.println("Enter your guess: ");
			int myGuess = Integer.parseInt(sc.next());
			if (fourBulls(myGuess)) {
				System.out.println("Congratulations!!!");
				break;
			}
		}
		sc.close();
	}

	private static boolean fourBulls(int myGuess) {
		String my_guess_temp = new String("" + myGuess);
		String guess_temp = new String("" + guess);
		int cows = 0, bulls = 0;
		for (int i = 0; i < 4; i++) {
			if (my_guess_temp.contains("" + guess_temp.charAt(i))
					&& my_guess_temp.indexOf("" + guess_temp.charAt(i)) == guess_temp
							.indexOf("" + guess_temp.charAt(i))) {
				++bulls;
			} else if (my_guess_temp.contains("" + guess_temp.charAt(i))
					&& my_guess_temp.indexOf("" + guess_temp.charAt(i)) != guess_temp
							.indexOf("" + guess_temp.charAt(i))) {
				++cows;
			}
		}
		System.out.println(bulls + " Bulls and " + cows + " Cows...");
		if (bulls == 4) {
			return true;
		}
		return false;
	}

	private static boolean isValid() {
		while (true) {
			guess = new Random().nextInt(9999);
			if (guess > 1000 && guess < 9999) {
				if (isDistinct()) {
					break;
				}
			}
		}
		
		return true;
	}

	private static boolean isDistinct() {
		String guessString = new String("" + guess);
		
		// System.out.println("$$$" + guessString);
		for (int i = 0; i < 4; i++) {
			for (int j = i + 1; j < 4; j++) {
				if (guessString.charAt(i) == guessString.charAt(j) && i != j) {
					// System.out.println(guessString.charAt(i));
					// System.out.println(guessString.charAt(j));
					// System.out.println(false);
					return false;
				}
			}
		}
		return true;
	}
}
