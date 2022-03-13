import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import enigma.core.Enigma;

public class Game {
	private CircularQueue expression;
	private Stack stack;
	private Stack tempstack;
	private CircularQueue input;
	private int symbolCount = 0;
	private char[][] screen;// transfer to game
	private int time;
	private int score = 0;
	private int scorefactor = 0;
	private static enigma.console.Console enigmaScreen = Enigma.getConsole("Ekran"); // Enigma.getConsole("Ekran",100,30,20)
	private KeyListener keyPress;
	private int keypr; // key pressed?
	private int rkey; // key (for press/release)
	private int cursorx;
	private int cursory;
	int stackPrintingLastLocation = 26;// stack deep location
	int lastLocation = 0;
	boolean gameOver = false;

	public Game(int inputTime) throws InterruptedException {
		expression = new CircularQueue(40); // string array or queue
		stack = new Stack(40);
		tempstack = new Stack(40);
		time = inputTime;
		input = new CircularQueue(8); // OPERATIONS
		screen = new char[10][10]; // the game screen array
		for (int i = 0; i < screen.length; i++)
			for (int j = 0; j < screen[i].length; j++)
				screen[i][j] = '.';
		keyPress = new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if (keypr == 0) {
					keypr = 1;
					rkey = e.getKeyCode();
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		};
		enigmaScreen.getTextWindow().addKeyListener(keyPress);
		cursorx = 2;
		cursory = 2;
		do {
			fillScreen();
			printScreen("Free");
			freeMode();
		} while (!gameOver);
		enigmaScreen.getTextWindow().setCursorPosition(2, 15);
		System.out.print("The game is over!");
	}

	public void fillScreen() {
		while (symbolCount < 40) {
			if (input.size() != 0) {
				int x = (int) (Math.random() * 10);
				int y = (int) (Math.random() * 10);
				if (screen[y][x] == '.') {
					screen[y][x] = (char) input.dequeue();
					symbolCount++;
				}
			} else
				fillInput();
		}
		fillInput();
	}

	public void fillInput() {
		int loopSize = 8 - input.size();
		for (int i = 0; i < loopSize; i++) {
			int rand = (int) (Math.random() * 13) + 1;
			if (rand < 10)
				input.enqueue((char) (rand + 48));
			else if (rand == 10)
				input.enqueue('*');
			else if (rand == 11)
				input.enqueue('+');
			else if (rand == 12)
				input.enqueue('/');
			else if (rand == 13)
				input.enqueue('-');
		}
	}

	public void printScreen(String modeName) {
		enigmaScreen.getTextWindow().setCursorPosition(0, 0);
		System.out.print("  1234567890");
		enigmaScreen.getTextWindow().setCursorPosition(0, 1);
		System.out.println(" ############");
		for (int i = 0; i < screen.length; i++) {
			if (i != 9)
				enigmaScreen.getTextWindow().output(i + 1 + "#");
			else
				enigmaScreen.getTextWindow().output("0#");
			for (int j = 0; j < screen[i].length; j++)
				System.out.print(screen[i][j]);
			enigmaScreen.getTextWindow().output("#");
			System.out.println();
		}
		enigmaScreen.getTextWindow().output(" ############");
		enigmaScreen.getTextWindow().setCursorPosition(20, 0);
		System.out.print("Input");
		enigmaScreen.getTextWindow().setCursorPosition(20, 1);
		System.out.print("<<<<<<<<<");
		enigmaScreen.getTextWindow().setCursorPosition(20, 2);
		for (int i = 0; i < 8; i++) {
			System.out.print(input.peek());
			input.enqueue(input.dequeue());
		}
		enigmaScreen.getTextWindow().setCursorPosition(20, 3);
		System.out.print("<<<<<<<<<");
		enigmaScreen.getTextWindow().setCursorPosition(32, 0);
		System.out.print("Time  :    ");
		enigmaScreen.getTextWindow().setCursorPosition(32, 0);
		System.out.print("Time  : " + time);
		enigmaScreen.getTextWindow().setCursorPosition(32, 1);
		System.out.print("Score : ");
		enigmaScreen.getTextWindow().setCursorPosition(32, 2);
		System.out.print("Mode  : " + modeName + "          ");
		enigmaScreen.getTextWindow().setCursorPosition(32, 3);
		System.out.print("Expr : ");
		int cursorShift = 39;
		enigmaScreen.getTextWindow().setCursorPosition(39, 3);
		System.out.print("                                   ");// Stack line cleaner
		for (int i = 0; i < expression.size(); i++) {
			enigmaScreen.getTextWindow().setCursorPosition(cursorShift, 3);
			String elementLength = (String) (expression.peek()); // important case is this line. numbers can be bigger
			System.out.print(expression.peek());
			cursorShift += elementLength.length() + 1;// important case is this line. numbers can be bigger
			expression.enqueue(expression.dequeue());
		}
		enigmaScreen.getTextWindow().setCursorPosition(32, 4);
		System.out.print("Score : " + score);
		enigmaScreen.getTextWindow().setCursorPosition(32, 5);
		System.out.print("Scorefactor(n) : " + scorefactor);
		if (modeName.equalsIgnoreCase("evaluation")) {
			enigmaScreen.getTextWindow().setCursorPosition(32, 6);
			System.out.print(">>Press 'space' to see calculation");
			if (!stack.isEmpty()) {
				for (int i = 0; i < 9; i++) {
					enigmaScreen.getTextWindow().setCursorPosition(50, 18 + i);
					System.out.print("|         ");
					enigmaScreen.getTextWindow().setCursorPosition(61, 18 + i);
					System.out.print("|");
				}
				enigmaScreen.getTextWindow().setCursorPosition(50, 27);
				System.out.print("+----------+");
				while (!stack.isEmpty())
					tempstack.push(stack.pop());
				while (!tempstack.isEmpty()) {
					enigmaScreen.getTextWindow().setCursorPosition(51, stackPrintingLastLocation);
					System.out.print(tempstack.peek());
					stack.push(tempstack.pop());
					stackPrintingLastLocation--;
				}
				lastLocation = stackPrintingLastLocation; // Kept last location to delete before evaluation
				stackPrintingLastLocation = 26; // Reseted original location
			}
		}
	}

	private void freeMode() throws InterruptedException {
		enigmaScreen.getTextWindow().setCursorType(1);
		printScreen("Free");
		while (true) {
			enigmaScreen.getTextWindow().setCursorPosition(cursorx, cursory);
			if (time == 0) {
				gameOver = true;
				break;
			}
			if (keypr == 1) {
				if (cursorx == 1)
					cursorx = 2; // left limit
				if (cursorx == 12)
					cursorx = 11; // right limit
				if (cursory == 1)
					cursory = 2; // up limit
				if (cursory == 12)
					cursory = 11; // down limit
				if (rkey == KeyEvent.VK_LEFT)
					cursorx--;
				if (rkey == KeyEvent.VK_RIGHT)
					cursorx++;
				if (rkey == KeyEvent.VK_UP)
					cursory--;
				if (rkey == KeyEvent.VK_DOWN)
					cursory++;
				char key = (char) rkey;
				if (key == 'T') {
					if (time == 0)
						break;
					if (!expression.isFull())
						takeMode();
					break;
				}
				keypr = 0;
			}
			enigmaScreen.getTextWindow().setCursorPosition(cursorx, cursory);
		}
	}

	private void takeMode() throws InterruptedException {
		enigmaScreen.getTextWindow().setCursorPosition(cursorx, cursory);
		int counter = 0;
		while (true) {
			if (time == 0) {
				gameOver = true;
				break;
			}
			if (counter % 50 == 0) {
				time--;
				printScreen("Take");
			}
			counter++;
			if (keypr == 1) {
				if (rkey == KeyEvent.VK_A || rkey == KeyEvent.VK_LEFT) {
					String str = "";
					Boolean flag = true;
					cursorx--;
					if (cursorx == 1)
						cursorx = 2;// if cursor is in _boundry, then stop
					// if operator,then push
					if (screen[cursory - 2][cursorx - 2] == '/' || screen[cursory - 2][cursorx - 2] == '*'
							|| screen[cursory - 2][cursorx - 2] == '+' || screen[cursory - 2][cursorx - 2] == '-') {
						expression.enqueue((Character.toString(screen[cursory - 2][cursorx - 2])));
						screen[cursory - 2][cursorx - 2] = '.';
						symbolCount--;
						flag = false;
					}
					if (!flag) {
						printScreen("Take");
						flag = true;
					}
					// taken numbers (all next to next numbers)
					while (screen[cursory - 2][cursorx - 2] != '.' && screen[cursory - 2][cursorx - 2] != '/'
							&& screen[cursory - 2][cursorx - 2] != '*' && screen[cursory - 2][cursorx - 2] != '+'
							&& screen[cursory - 2][cursorx - 2] != '-') {// if char is a number
						str = str.concat(Character.toString(screen[cursory - 2][cursorx - 2]));
						screen[cursory - 2][cursorx - 2] = '.';
						symbolCount--;
						flag = false;
						cursorx--;
						if (cursorx == 1)
							cursorx = 2;
					}
					if (!flag) {
						expression.enqueue(str);
						printScreen("Take");
						str = "";
						flag = true;
						cursorx++;
					}
				}
				if (rkey == KeyEvent.VK_D || rkey == KeyEvent.VK_RIGHT) {
					String str = "";
					Boolean flag = true;
					cursorx++;
					if (cursorx == 12)
						cursorx = 11;// if cursor is in _boundry, then stop
					// if operator,then push
					if (screen[cursory - 2][cursorx - 2] == '/' || screen[cursory - 2][cursorx - 2] == '*'
							|| screen[cursory - 2][cursorx - 2] == '+' || screen[cursory - 2][cursorx - 2] == '-') {
						expression.enqueue(Character.toString(screen[cursory - 2][cursorx - 2]));
						screen[cursory - 2][cursorx - 2] = '.';
						symbolCount--;
						flag = false;
					}
					if (!flag) {
						printScreen("Take");
						flag = true;
					}
					// taken numbers (all next to next numbers)
					while (screen[cursory - 2][cursorx - 2] != '.' && screen[cursory - 2][cursorx - 2] != '/'
							&& screen[cursory - 2][cursorx - 2] != '*' && screen[cursory - 2][cursorx - 2] != '+'
							&& screen[cursory - 2][cursorx - 2] != '-') {// if char is a number
						str = str.concat(Character.toString(screen[cursory - 2][cursorx - 2]));
						screen[cursory - 2][cursorx - 2] = '.';
						symbolCount--;
						flag = false;
						cursorx++;
						if (cursorx == 12)
							cursorx = 11;
					}
					if (!flag) {
						expression.enqueue(str);
						printScreen("Take");
						str = "";
						flag = true;
						cursorx--;
					}
				}
				if (rkey == KeyEvent.VK_W || rkey == KeyEvent.VK_UP) {
					String str = "";
					Boolean flag = true;
					cursory--;
					if (cursory == 1)
						cursory = 2;// if cursor is in _boundry, then stop
					// if operator,then push
					if (screen[cursory - 2][cursorx - 2] == '/' || screen[cursory - 2][cursorx - 2] == '*'
							|| screen[cursory - 2][cursorx - 2] == '+' || screen[cursory - 2][cursorx - 2] == '-') {
						expression.enqueue(Character.toString(screen[cursory - 2][cursorx - 2]));
						screen[cursory - 2][cursorx - 2] = '.';
						symbolCount--;
						flag = false;
					}
					if (!flag) {
						printScreen("Take");
						flag = true;
					}
					// taken numbers (all next to next numbers)
					while (screen[cursory - 2][cursorx - 2] != '.' && screen[cursory - 2][cursorx - 2] != '/'
							&& screen[cursory - 2][cursorx - 2] != '*' && screen[cursory - 2][cursorx - 2] != '+'
							&& screen[cursory - 2][cursorx - 2] != '-') {// if char is a number
						str = str.concat(Character.toString(screen[cursory - 2][cursorx - 2]));
						screen[cursory - 2][cursorx - 2] = '.';
						symbolCount--;
						flag = false;
						cursory--;
						if (cursory == 1)
							cursory = 2;
					}
					if (!flag) {
						expression.enqueue(str);
						printScreen("Take");
						str = "";
						flag = true;
						cursory++;
					}
				}
				if (rkey == KeyEvent.VK_S || rkey == KeyEvent.VK_DOWN) {
					String str = "";
					Boolean flag = true;
					cursory++;
					if (cursory == 12)
						cursory = 11;// if cursor is in _boundry, then stop
					// if operator,then push
					if (screen[cursory - 2][cursorx - 2] == '/' || screen[cursory - 2][cursorx - 2] == '*'
							|| screen[cursory - 2][cursorx - 2] == '+' || screen[cursory - 2][cursorx - 2] == '-') {
						expression.enqueue(Character.toString(screen[cursory - 2][cursorx - 2]));
						screen[cursory - 2][cursorx - 2] = '.';
						symbolCount--;
						flag = false;
					}
					if (!flag) {
						printScreen("Take");
						flag = true;
					}
					// taken numbers (all next to next numbers)
					while (screen[cursory - 2][cursorx - 2] != '.' && screen[cursory - 2][cursorx - 2] != '/'
							&& screen[cursory - 2][cursorx - 2] != '*' && screen[cursory - 2][cursorx - 2] != '+'
							&& screen[cursory - 2][cursorx - 2] != '-') {// if char is a number
						str = str.concat(Character.toString(screen[cursory - 2][cursorx - 2]));
						screen[cursory - 2][cursorx - 2] = '.';
						symbolCount--;
						flag = false;
						cursory++;
						if (cursory == 12)
							cursory = 11;
					}
					if (!flag) {
						expression.enqueue(str);
						printScreen("Take");
						str = "";
						flag = true;
						cursory--;
					}
				}
				if ((char) rkey == 'F') {
					if (!expression.isEmpty()) {
						keypr = 0;
						ExpressionScoreCalculation();
						evaluationMode();
						break;
					}
				}
				keypr = 0;
			}
			Thread.sleep(20); // 20_ms
			enigmaScreen.getTextWindow().setCursorPosition(cursorx, cursory);
		}
	}

	private void ExpressionScoreCalculation() {
		scorefactor = 0;
		int exprSize = expression.size();
		boolean isFirstOperator = false, isSecondOperator;
		int exprLength = expression.peek().toString().length();
		if (expression.peek().toString().equals("+") || expression.peek().toString().equals("-")
				|| expression.peek().toString().equals("/") || expression.peek().toString().equals("*")) {
			isFirstOperator = true;// First element type control
		}
		if (exprLength > 1) {
			scorefactor += exprLength * 2;
			expression.enqueue(expression.dequeue());
		} else {
			scorefactor += 1;
			expression.enqueue(expression.dequeue());
		}
		for (int i = 0; i < exprSize - 1; i++) {// The rest of elements
			if (expression.peek().toString().equals("+") || expression.peek().toString().equals("-")
					|| expression.peek().toString().equals("/") || expression.peek().toString().equals("*"))
				isSecondOperator = true;// Second element type control
			else
				isSecondOperator = false;
			exprLength = expression.peek().toString().length();
			if (exprLength > 1) {
				scorefactor += exprLength * 2;
				expression.enqueue(expression.dequeue());
			} else {
				if (isFirstOperator == isSecondOperator) { // same adds 1 point
					scorefactor += 1;
					expression.enqueue(expression.dequeue());
				} else { // different adds 2 points
					scorefactor += 2;
					expression.enqueue(expression.dequeue());
				}
			}
			isFirstOperator = isSecondOperator;
		}
		score += scorefactor * scorefactor;
	}

	private void evaluationMode() {
		printScreen("Evaluation");
		boolean isProper = true;
		int op_count = 0;
		int num_count = 0;
		double first_num;
		double second_num;
		int q_size = expression.size();
		int value = 0;
		enigmaScreen.getTextWindow().setCursorPosition(32, 16);
		System.out.print("Original Expr:                                            ");
		int length = 0;
		for (int i = 0; i < q_size; i++) {
			enigmaScreen.getTextWindow().setCursorPosition(47 + length, 16);
			System.out.print(expression.peek());
			length = length + expression.peek().toString().length() + 1;
			if (expression.peek().toString().equals("+") || expression.peek().toString().equals("-")
					|| expression.peek().toString().equals("/") || expression.peek().toString().equals("*")) {
				op_count++;
				expression.enqueue(expression.dequeue());
			} else {
				num_count++;
				expression.enqueue(expression.dequeue());
			}
		}
		if (op_count != num_count - 1) { // Valid expression control
			score = score - 20;
			enigmaScreen.getTextWindow().setCursorPosition(15, 7);
			System.out.println("Postfix expression is not proper => YOU GET -20 PENALTY POINTS");
			isProper = false;
			printScreen("Evaluation");
		}
		while (isProper && !expression.isEmpty()) {
			enigmaScreen.getTextWindow().setCursorPosition(1, 1);
			if (keypr == 1 && rkey == KeyEvent.VK_SPACE) {
				if (expression.peek().toString().equals("+") || expression.peek().toString().equals("-")
						|| expression.peek().toString().equals("/") || expression.peek().toString().equals("*")) {
					// if expr_ is operations
					if (stack.size() >= 2) {
						second_num = Double.parseDouble(stack.pop().toString());
						enigmaScreen.getTextWindow().setCursorPosition(51, lastLocation + 1);
						System.out.print("          "); // cleaning 2 numbers which are evaluating
						first_num = Double.parseDouble(stack.pop().toString());
						enigmaScreen.getTextWindow().setCursorPosition(51, lastLocation + 2);
						System.out.print("          "); // cleaning 2 numbers which are evaluating
						if (expression.peek().toString().equals("+")) {
							value = (int) Math.round(first_num + second_num);
						} else if (expression.peek().toString().equals("-")) {
							value = (int) Math.round(first_num - second_num);
						} else if (expression.peek().toString().equals("/")) {
							if (second_num == 0)
								second_num = 1;
							value = (int) Math.round(first_num / second_num);
						} else if (expression.peek().toString().equals("*")) {
							value = (int) Math.round(first_num * second_num);
						}
						stack.push(Integer.toString(value));
						printScreen("Evaluation");
						expression.dequeue();
					} else {
						score = score - 20;
						enigmaScreen.getTextWindow().setCursorPosition(15, 7);
						System.out.println("Postfix expression is not proper => YOU GET -20 PENALTY POINTS");
						printScreen("Evaluation");
						isProper = false;
						break;
					}
				} else {
					stack.push(expression.dequeue().toString());
					printScreen("Evaluation");
				}
				keypr = 0; // necessary
			}
		}
		while(!expression.isEmpty())
			expression.dequeue();
		while(!stack.isEmpty())
			stack.pop();
	}
}