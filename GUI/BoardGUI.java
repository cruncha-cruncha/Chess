package Chess.GUI;

import Chess.*;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BoardGUI {

	public BoardGUI () { }

	/**
	 * If user elects to specify their own board configuration, this method handles it. First, it creates
	 * a GUI interface for convenience. When the user is done, their configuration checked for correctness.
	 * 
	 * @return a valid board configuration, specified using uppercase and lowercase chars representing
	 * 		   piece codes.
	 */
	public BoardOptions askBoard () {
		AtomicBoolean paused = new AtomicBoolean(true);
		final BoardOptions options = new BoardOptions();

		SwingUtilities.invokeLater(new GUIThread(paused,options));

		while (true) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) { }
			if (!paused.get())
				break;
		}

		return options;
	}

	private static class GUIThread implements Runnable {
		private AtomicBoolean flag;
		private BoardOptions options;

		public GUIThread (AtomicBoolean flag, BoardOptions options) {
			this.flag = flag;
			this.options = options;
		}

	    public void run() {
	        setup(flag,options);
	    }
	}

	private static void setup (AtomicBoolean flag, BoardOptions options) {
		JFrame mainFrame = new JFrame("enter board configuration");

		String[][] data = {{"8","R","N","B","Q","K","B","N","R"},
						   {"7","P","P","P","P","P","P","P","P"},
						   {"6","","","","","","","",""},
						   {"5","","","","","","","",""},
						   {"4","","","","","","","",""},
						   {"3","","","","","","","",""},
						   {"2","p","p","p","p","p","p","p","p"},
						   {"1","r","n","b","q","k","b","n","r"},
						   {"","A","B","C","D","E","F","G","H"}};
		String[] pos = {"","A","B","C","D","E","F","G","H"};

		JPanel boardPanel = new JPanel();
		DefaultTableModel model = new DefaultTableModel(data,pos);
		JTable table = new JTable(model);

		JPanel instructionsPanel = new JPanel();
		JTextArea words = new JTextArea(1,8);
		JButton doneButton = new JButton("Done");

		JPanel togglesPanel = new JPanel();
		JButton resetButton = new JButton("Reset Board");
		JLabel depthLabel = new JLabel("Depth");
		JTextField depthField = new JTextField("4",1);
		JCheckBox firstColourCheck = new JCheckBox("white to move");
		JCheckBox firstPlayerCheck = new JCheckBox("human to move");
		JCheckBox evalFuncCheck = new JCheckBox("use simple eval");
		
		mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		mainFrame.setSize(450,190);
		mainFrame.setLayout(new FlowLayout());

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for (int i = 0; i < 9; i++)
			table.getColumnModel().getColumn(i).setPreferredWidth(10);
		table.putClientProperty("terminateEditOnFocusLost", true);
		table.setGridColor(Color.black);
		
		instructionsPanel.setLayout(new BoxLayout(instructionsPanel, BoxLayout.Y_AXIS));
		words.setEditable(false);
		words.append("white = lowercase \n");
		words.append("black = UPPERCASE \n");
		words.append("pawn = P/p \n");
		words.append("knight = N/n \n");
		words.append("bishop = B/b \n");
		words.append("rook = R/r \n");
		words.append("queen = Q/q \n");
		words.append("king = K/k");
		
		togglesPanel.setLayout(new BoxLayout(togglesPanel, BoxLayout.Y_AXIS));
		firstColourCheck.setSelected(true);
		firstPlayerCheck.setSelected(true);
		evalFuncCheck.setSelected(true);
		
		boardPanel.add(table);
		mainFrame.add(boardPanel);
		instructionsPanel.add(words);
		instructionsPanel.add(doneButton);
		mainFrame.add(instructionsPanel);
		togglesPanel.add(resetButton);
		togglesPanel.add(depthLabel);
		togglesPanel.add(depthField);
		togglesPanel.add(firstColourCheck);
		togglesPanel.add(firstPlayerCheck);
		togglesPanel.add(evalFuncCheck);
		mainFrame.add(togglesPanel);

		resetButton.addActionListener(new ActionListener(){
			private boolean defaultBoard = true;
			public void actionPerformed (ActionEvent e) {
				if (defaultBoard) {
					for (int i = 0; i < 8; i++) {
						for (int j = 1; j < 9; j++) {
							table.setValueAt("",i,j);
						}
					}
				} else {
					for (int i = 0; i < 8; i++) {
						for (int j = 1; j < 9; j++) {
							if (data[i][j].equals("")) {
								table.setValueAt("",i,j);
							} else {
								table.setValueAt(data[i][j],i,j);
							}
						}
					}
				} 
				defaultBoard = !defaultBoard;
			}
		});

		doneButton.addActionListener(new DoneButtonActionListener(
			flag,options,table,depthField,firstColourCheck,firstPlayerCheck,evalFuncCheck,mainFrame));

		mainFrame.setVisible(true);
	}

	private static class DoneButtonActionListener implements ActionListener {
		private AtomicBoolean flag;
		private BoardOptions options;
		private JTable table;
		private JTextField depthField;
		private JCheckBox firstColourCheck, firstPlayerCheck, evalFuncCheck;
		private JFrame mainFrame;
		
		public DoneButtonActionListener(AtomicBoolean flag, BoardOptions options,
			JTable table, JTextField depthField, JCheckBox firstColourCheck,
			JCheckBox firstPlayerCheck, JCheckBox evalFuncCheck, JFrame mainFrame) {

			this.flag = flag;
			this.options = options;
			this.table = table;
			this.depthField = depthField;
			this.firstColourCheck = firstColourCheck;
			this.firstPlayerCheck = firstPlayerCheck;
			this.evalFuncCheck = evalFuncCheck;
			this.mainFrame = mainFrame;
	    }

	    public void actionPerformed(ActionEvent e) {
	    	int depth = 4;
	    	try {
	    		depth = Integer.parseInt(depthField.getText());
	    		if (depth > 20) {
	    			System.out.println("yea, sure. depth = 10");
	    			depth = 10;
	    		}
	    	} catch (java.lang.NumberFormatException except) {
	    		System.out.println("Error: unable to parse depth, using default of 4");
	    	}

	    	options.firstColour = (firstColourCheck.isSelected()) ? Colour.WHITE : Colour.BLACK;
	    	options.humanFirst = firstPlayerCheck.isSelected();
	    	options.simpleEval = evalFuncCheck.isSelected();
	    	options.depth = depth;

	    	for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					String s = (String) table.getValueAt(7-j,i+1);
					if (!s.equals("")) {
						options.board[i][j] = s.charAt(0);
					}
				}
			}

			verifyTable();
			mainFrame.dispose();
	        flag.set(false);
	    }

	    private void verifyTable () {
			int[] count = new int[12];
			String key = "kqrbnpKQRBNP";
			outer:
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					char c = options.board[i][j];
					if (c != 0) {
						int index = key.indexOf(c);
					if ((index == -1 ) || ((c == 'P' || c == 'p') && (j == 0 || j == 7))) {
							// if an unkown character is present, discard the board
							// also, cannot have a pawn on the back ranks
							options.board[0][0] = 'x';
							return;
						} else {
							count[index] += 1;
						}
					}
				}
			}
					
			// must have a king of each color, and no more pieces than allowed
			if (count[0] != 1 || count[6] != 1 || count[1] > 9 || count[7] > 9 || count[2] > 10 || count[8] > 10 ||
				count[3] > 10 || count[9] > 10 || count[4] > 10 || count[10] > 10 || count[5] > 8 || count[11] > 8 ||
			    count[1]+count[2]+count[3]+count[4]+count[5] > 15 || count[7]+count[8]+count[9]+count[10]+count[11] > 15) {
				options.board[0][0] = 'x';
			}
		}
	}
}