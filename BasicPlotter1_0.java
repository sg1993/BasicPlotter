import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

import javax.swing.JFrame;

class ETNode {
	int tag;
	char operator;
	char var;
	double operand;
	ETNode left, right;

	ETNode() {

	}
};

public class BasicPlotter extends JFrame implements Runnable, KeyListener {

	private static final long serialVersionUID = 1L;
	char c;

	double p_X = (float) 0, p_Y = (float) 600.0, X_increment;
	
	private static ETNode ETree ;
	private static Stack<ETNode> operatorStack, treeStack;

	private static ETNode postfixString[];
	private static int postfixSize = 0;
	double lastNum;

	HashMap<Character, Boolean> operator;
	HashMap<Character, Integer> priority;

	double n_X, n_Y, magni = 100.0;
	public static String expression;
	Thread t;
	static BasicPlotter b;

	private Scanner scan;

	public static void main(String args[]) {
		b = new BasicPlotter();
	}

	BasicPlotter() {
		operator = new HashMap<Character, Boolean>();
		priority = new HashMap<Character, Integer>();
		operatorStack = new Stack<ETNode>();
		treeStack = new Stack<ETNode>();
		operator.put('^', true);
		operator.put('-', true);
		operator.put('+', true);
		operator.put('*', true);
		operator.put('/', true);
		priority.put('-', 1);
		priority.put('+', 1);
		priority.put('*', 2);
		priority.put('/', 3);
		priority.put('^', 4);
		operator.put('?', true);
		priority.put('?', -1);
		operator.put('(', true);
		priority.put('(', -1);
		operator.put(')', true);
		priority.put(')', -1);
		System.out.println(priority.get('+'));
		scan = new Scanner(System.in);
		System.out.println("Enter the Expression in terms of variable x");
		expression = scan.nextLine();
		
		postfixString = new ETNode[expression.length()];
		ConvertToPostfix(expression);System.out.println("length: " + expression.length());
		while(priority.get(operatorStack.peek().operator) != -1){
			postfixString[postfixSize++] = operatorStack.pop();
		}
		System.out.println("The infix expression is:");
		for(int i=0;i<postfixSize;i++){
			if(postfixString[i].tag == 0)
				System.out.print(postfixString[i].operator + " ");
			else if(postfixString[i].tag == 1)
				System.out.print(postfixString[i].var + " ");
			if(postfixString[i].tag == 2)
				System.out.print(postfixString[i].operand + " ");
		}
		constructExpressionTree();
		ETree = treeStack.pop();
		setSize(1300, 850);
		addKeyListener(this);
		setFocusable(true);
		setVisible(true);
		t = new Thread(this);
		t.start();
		repaint();
	}

	private void constructExpressionTree() {
		for(int i=0;i<postfixSize;i++){
			if(postfixString[i].tag == 1 || postfixString[i].tag == 2){
				treeStack.push(postfixString[i]);
			}
			else{
				ETNode l, r;
				r = treeStack.pop();
				l = treeStack.pop();
				postfixString[i].left = l;
				postfixString[i].right = r;
				treeStack.push(postfixString[i]);
			}
		}
	}

	private void ConvertToPostfix(String expression) {
		int i = 0;
		char cur;
		ETNode p = new ETNode();
		p.tag = 0;
		p.operator = '?';
		operatorStack.push(p);
		while (i < expression.length()) {
			cur = expression.charAt(i);
			if (Character.isLetterOrDigit(cur)) {// current char is not an
													// operator
				if (cur == 'x') {
					ETNode t = new ETNode();
					t.tag = 1;
					t.var = cur;
					t.left = null;
					t.right = null;
					postfixString[postfixSize++] = t;
				} else {// its a digit
					
					i = extractNumber(expression, i);// advance to the next
														// position after the
														// number constant
					ETNode t = new ETNode();
					t.tag = 2;
					t.operand = lastNum;
					t.left = null;
					t.right = null;
					postfixString[postfixSize++] = t;
				}

			}

			else {

				if (cur == '(') {
					ETNode t = new ETNode();
					t.tag = 0;
					t.operator = cur;
					t.left = null;
					t.right = null;
					operatorStack.push(t);
				}

				else if (cur == ')') {
					while ((operatorStack.peek().operator) != '(' ) {
						System.out.println("popping "+ operatorStack.peek().operator);
						postfixString[postfixSize++] = operatorStack.pop();
					}
					operatorStack.pop();// pops the remaining '('
				}

				else {// its an operand
					while (priority.get(operatorStack.peek().operator) > priority
							.get(cur)) {
						postfixString[postfixSize++] = operatorStack.pop();
					}
					ETNode t = new ETNode();
					t.tag = 0;
					t.operator = cur;
					t.left = null;
					t.right = null;
					operatorStack.push(t);
				}
			}
			i++;
		}
	}

	private int extractNumber(String expression2, int i) {
		lastNum = 0.0;
		char c = '@';
		while (i < expression.length()) {
			c = expression.charAt(i);
			if (Character.isDigit(c)) {
				lastNum = (lastNum * 10) + (c - '0');
				i++;
			}
			else
				break;
		}
		double decimal = 0.0;
		if(c == '.'){
			decimal = 0.0;
			int pow = 1;
			i++;
			while (i < expression.length()) {
				c = expression.charAt(i);
				if (Character.isDigit(c)) {
					decimal += ((double)(c-'0'))/Math.pow(10, pow);
					pow++;
					i++;
				}
				else
					break;
			}
		}
		lastNum += decimal;
		return i - 1;
	}

	public void paint(Graphics g) {
		super.paint(g);// clears the screen
		g.drawLine(10, 0, 10, 600);
		g.drawLine(10, 600, 1300, 600);
		double i = 0;
		p_X = 0;
		while (i <= 100000) {
			n_X = p_X + .1;
			n_Y = magni * evaluateExpression(ETree, n_X);
			//n_Y = magni * Math.pow(n_X,n_X);
			g.drawLine((int) (magni * p_X + 10), -(int) p_Y + 600, (int) (magni
					* n_X + 10), -(int) n_Y + 600);
			p_X = n_X;
			p_Y = n_Y;
			i++;
		}
	}

	private double evaluateExpression(ETNode root, double x) {
		if(root.tag == 0){
			if(root.operator == '^'){
				return (Math.pow(evaluateExpression(root.left, x),evaluateExpression(root.right, x)));
			}
			if(root.operator == '+'){
				return (evaluateExpression(root.left, x) + evaluateExpression(root.right, x));
			}
			if(root.operator == '-'){
				return (evaluateExpression(root.left, x) - evaluateExpression(root.right, x));
			}
			if(root.operator == '*'){
				return (evaluateExpression(root.left, x) * evaluateExpression(root.right, x));
			}
			if(root.operator == '/'){
				return (evaluateExpression(root.left, x) / evaluateExpression(root.right, x));
			}
		}
		else if(root.tag == 1){
			return (double)(x);
		}
		else{
			return (double)(root.operand);
		}
		return 0;
	}

	@Override
	public void run() {
		while (true) {

			repaint();
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				//
				e.printStackTrace();
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// System.out.println("fuck yea");
		// TODO Auto-generated method stub
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			magni += 3.0;
			repaint();
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			magni -= 3.0;
			repaint();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

}
