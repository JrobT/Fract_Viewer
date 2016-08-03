package coursework1;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * <h>Class setGUI builds the initial application</h>
 * 	<p>Three separate frames represent the program: the main
 * 	panel for display, the controller, and for the julia
 * 	set.</p>
 * 
 * @author Jack Trute jt10g15
 */
public class SetGUI {
	/**
	 * @param args from command line, not used.
	 */
	public static void main(String args[])	{
		SetGUI gui = new SetGUI();
		gui.init();
	}
	
	public void init()	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				SetDisplay set = new SetDisplay("Fractel Set's Viewer");
				set.init();
			}
		});
	}
	
}

/**
 * <h>Creates a JFrame for the set paint</h>
 * 	<p>Sets up the controller and the main panel. The
 * 	Julia set is added as an inner class. These three
 * 	classes share information with each other.</p>
 * 
 * @author Jack Trute jt10g15
 */
@SuppressWarnings("serial")
class SetDisplay extends JFrame {
	static final int sideBarWidth = 300;
	static final int COL_MAX = 6;
	static final int COL_MIN = 0;
	static final int COL_INIT = 0;	// set up for sliders
	private Complex userSelectedPoint;	// the complex number for the julia set where the user clicked
	private ArrayList<Complex> cValues;	// list of saved julia sets complex number values
	private JComboBox<String> saves;
	private Point startDrag, endDrag;
	private Rectangle rect;	// box zoom
	/**
	 * controller components
	 */
	SetPanel mainPanel; // the set is displayed in here
	JPanel setContainer;
	JPanel rightPanel;	// RHS
	JPanel leftPanel;
	JPanel controlPanel;	// components for controller
	JLabel cValue;
	JLabel realAxis;
	JLabel imgAxis;
	JLabel iterLabel;
	JTextField midR;
	JTextField midI;
	JTextField iterText;
	JButton updateSet;
	JButton buttonPlus;
	JButton buttonMinus;
	JButton swapFormula;
	JButton showJulia;
	JButton orbitTrap;
	JButton reset;
	JSlider red;
	JSlider green;
	JSlider blue;
	
	SetDisplay(String title) { 
		super(title);
		/**
		 * controller components
		 */
		setContainer = new JPanel();
		rightPanel = new JPanel();
		leftPanel = new JPanel();
		controlPanel = new JPanel();
		cValue = new JLabel("Null");
		realAxis = new JLabel("Mid. Real Axis");
		imgAxis = new JLabel("Mid. Imaginary Axis");
		iterLabel = new JLabel("Set Iterations");
		midR = new JTextField(5);
		midI = new JTextField(5);
		iterText = new JTextField(5);
		updateSet = new JButton("Update Set");
		buttonPlus = new JButton(" + ");
		buttonMinus = new JButton(" - ");
		swapFormula = new JButton("Switch Formula");
		showJulia = new JButton("Show Fav.");
		orbitTrap = new JButton("Orbit Trap");
		reset = new JButton("Reset");
		red = new JSlider(JSlider.HORIZONTAL,
				COL_MIN, COL_MAX, COL_INIT);
		green = new JSlider(JSlider.HORIZONTAL,
				COL_MIN, COL_MAX, COL_INIT);
		blue = new JSlider(JSlider.HORIZONTAL,
				COL_MIN, COL_MAX, COL_INIT);
		cValues = new ArrayList<Complex>();
		saves = new JComboBox<String>();
		saves.setSelectedItem(null);
	}
	
	/**
	 * get the saved julia sets
	 */
	public void readJuliaFile() {
		String file = "JuliaFile.txt";
		String[] strLine;
		String line="";
		double real, im;
		
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while((line = bufferedReader.readLine()) != null)	// each line has a set
			{
				strLine = line.split(",");
				saves.addItem(strLine[0]);
				real = Double.parseDouble(strLine[1]);
				im = Double.parseDouble(strLine[2]);
				cValues.add(new Complex(real,im));	// add to the list
			}
			bufferedReader.close();
		}	catch (IOException e) {	System.err.println("There was a problem with reading the julia set file" + e);	}
	}
	
	void init() {
		
		mainPanel = new SetPanel();
		this.readJuliaFile(); // get the saved julias
		
		/**
		 * <h>Inner Class <code>MouseUser</code> allows user interaction
		 * with the set.</h>
		 * 	Drag implemented for box zoom. Moved implemented to update 'c' value.
		 * 	Clicked implemented for the creation of a Julia Set. Pressed & Released
		 * 	implemented for box zoom.</p>
		 *
		 * @author Jack Trute jt10g15
		 */
		class MouseUser implements MouseListener, MouseMotionListener {
			@Override
			public void mouseDragged(MouseEvent e) {
				endDrag	=	new Point(e.getX(),e.getY());
				// workout the size of the box
				mainPanel.rectx = Math.min((int) startDrag.getX(), (int) endDrag.getX());
				mainPanel.recty = Math.min((int) startDrag.getY(), (int) endDrag.getY());
				mainPanel.rectw = Math.abs((int) startDrag.getX() - (int) endDrag.getX());
				mainPanel.recth = Math.abs((int) startDrag.getY() - (int) endDrag.getY());
				// create rectangle to match the box
				rect =	new Rectangle(mainPanel.rectx,	mainPanel.recty,
							mainPanel.rectw,	mainPanel.recth);
				mainPanel.boxZoom.setBounds(rect);
				// paint the box
				mainPanel.boxZoom.repaint();
			}
			@Override
			public void mouseMoved(MouseEvent e) {
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						updateC(e);
					}
				});
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				// creating a new GUI JFrame
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						makeNewJulia(e);
					}
				});
			}
			@Override
			public void mousePressed(MouseEvent e) {
				// start the dragging
				startDrag = new Point(e.getX(),e.getY());
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				// finish dragging
				endDrag=new Point(e.getX(),e.getY());

				// check it is a drag not a click
				if (endDrag.getX() != startDrag.getX() && endDrag.getY() != startDrag.getY())
				{
					//	the start pixal's complex number
					Complex startingC = mainPanel.makeItComplex(startDrag.getX(), startDrag.getY());
					//	find scale factors
					double scaleFactorR = mainPanel.getWidth() / mainPanel.boxZoom.getWidth();
					double scaleFactorI = mainPanel.getHeight() / mainPanel.boxZoom.getHeight();
					//	create the new crosshair point
					Complex newAxis = new Complex((mainPanel.endR - mainPanel.startR)/scaleFactorR,
							(mainPanel.endI - mainPanel.startI)/scaleFactorI);
					//	change the axis ('zoom')
					mainPanel.startR = startingC.getReal();
					mainPanel.endR = startingC.getReal()+newAxis.getReal();
					mainPanel.startI = startingC.getImg();
					mainPanel.endI = startingC.getImg()+newAxis.getImg();
					//	reset rectangle
					rect = new Rectangle(0,0,0,0);
					mainPanel.boxZoom.setBounds(rect);
					//	generate the set
					mainPanel.calcSet();
				}
			}
			
			/**
			 * Updates 'c' value in the <code>JLabel</code>
			 * 
			 * @param e	the mouse event 'move'
			 */
			private void updateC(MouseEvent e) {
				userSelectedPoint = mainPanel.makeItComplex(e.getX(), e.getY());
				if (userSelectedPoint != null)
					cValue.setText(userSelectedPoint.asString());
			}
			private void makeNewJulia(MouseEvent e) {
				// get the point clicked on
				userSelectedPoint = mainPanel.makeItComplex(e.getX(), e.getY());
				// set the JLabel to show the current 'c' value
				cValue.setText(userSelectedPoint.asString());
				// create the frame
				JuliaSetDisplay julSet = new JuliaSetDisplay("Julia Set", userSelectedPoint,
						mainPanel.maxIter, mainPanel.trapButton, saves, cValues, mainPanel.cRed,
						mainPanel.cGreen, mainPanel.cBlue);
				julSet.init();
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
		}
		//	add user mouse interaction
		mainPanel.addMouseListener( new MouseUser() );
		mainPanel.addMouseMotionListener( new MouseUser() );
		//	let the user swap between a Mandelbrot formula and a Burning Ship formula
		swapFormula.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainPanel.getKindOfSet() == "ManSet")
					mainPanel.setKindOfSet("Burning");
				else
					mainPanel.setKindOfSet("ManSet");
				//	render
				mainPanel.calcSet();
			}
		});
		//	show the julia set indicated by the ComboBox
		showJulia.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)	{
				//	creating a new JFrame set up
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						if (saves.getSelectedItem() != null) 
						{
							JuliaSetDisplay julSet = new JuliaSetDisplay("Julia Set", cValues.get(saves.getSelectedIndex()),
									mainPanel.maxIter, mainPanel.trapButton, saves, cValues, mainPanel.cRed, mainPanel.cGreen,
									mainPanel.cBlue);
							julSet.init();
						}
					}
				});
			}
		});
		//	zoom out from the crosshair
		buttonMinus.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)	{
				mainPanel.startR*=2;
				mainPanel.endR*=2;
				mainPanel.startI*=2;
				mainPanel.endI*=2;

				mainPanel.calcSet();
			}
		});
		//	zoom in to the crosshair
		buttonPlus.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)	{
				mainPanel.startR/=2;
				mainPanel.endR/=2;
				mainPanel.startI/=2;
				mainPanel.endI/=2;

				mainPanel.calcSet();
			}
		});
		//	toggle orbit trap
		orbitTrap.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)	{
				mainPanel.trapButton =	!mainPanel.trapButton;
				mainPanel.calcSet();
			}
		});
		//	change the degree of red in RGB colouring
		red.addChangeListener( new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int val;
				JSlider source =	(JSlider)e.getSource();
				
				if (!source.getValueIsAdjusting()) {
					val = (int) source.getValue();
					mainPanel.cRed = val;
				}
			}
		});
		//	change the degree of green in RGB colouring
		green.addChangeListener( new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int val;
				JSlider source =	(JSlider)e.getSource();
				
				if (!source.getValueIsAdjusting()) {
					val = (int) source.getValue();
					mainPanel.cGreen = val;
				}
			}
		});
		//	change the degree of blue in RGB colouring
		blue.addChangeListener( new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int val;
				JSlider source =	(JSlider)e.getSource();
				
				if (!source.getValueIsAdjusting()) {
					val = (int) source.getValue();
					mainPanel.cBlue = val;
				}
			}
		});
		
		/**
		 * <h>Listener for JButton that allows the user to see 
		 * and change the portion displayed</h>
		 * 	<p>Inner Class</p>
		 * 
		 * @author Jack Trute jt10g15
		 */
		class DisplayListener implements ActionListener {

			JTextField xPos,yPos,iterations;

			public DisplayListener(JTextField midR, JTextField midI, JTextField iterations) {
				this.xPos = midR;
				this.yPos = midI;
				this.iterations = iterations;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				// may update GUI components
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						// refer to method description
						adaptSet(xPos,yPos,iterations);
					}
				});
			}
		}
		
		updateSet.addActionListener( new DisplayListener(midR, midI, iterText) );
		//	refresh the mainPanel
		reset.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});

		
		
		// render and show the set
		mainPanel.calcSet();
		// setup the controller side bar
		this.setupControlPanel();
		// add the set to the frame
		this.setContentPane( setContainer );
		this.setLayout( new BorderLayout() );
		setContainer.add( mainPanel, BorderLayout.CENTER );
		// split the set from the controller
		JSplitPane leftHandView = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftPanel, mainPanel);
		leftHandView.setOneTouchExpandable(true);
		leftHandView.setDividerLocation(sideBarWidth);
		JSplitPane mainView = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftHandView, rightPanel);
		mainView.setOneTouchExpandable(true);
		mainView.setDividerLocation(mainPanel.getFrameSize()+sideBarWidth);
		// set and show the JFrame
		this.setContentPane( mainView );
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(mainPanel.getFrameSize()+(2*sideBarWidth),mainPanel.getFrameSize());
		this.setVisible(true);
		this.setLocationRelativeTo(null);

	}
	
	/**
	 * Called by the <code>DisplayListener</code>. Will change the positioning
	 * of tha set and the number of iterations used as a bailout point in its
	 * calculation.
	 * 
	 * @param xPos	the positional change to the Real axis
	 * @param yPos	the positional change to the imaginary axis
	 * @param iterations	the new bailout point
	 */
	private void adaptSet(JTextField xPos, JTextField yPos, JTextField iterations) {
		try {
			mainPanel.startR += Double.parseDouble(xPos.getText());
			mainPanel.endR += Double.parseDouble(xPos.getText());
		} catch (Exception ex) {};

		try {
			mainPanel.startI += Double.parseDouble(yPos.getText());
			mainPanel.endI += Double.parseDouble(yPos.getText());
		} catch (Exception ex) {};

		try {
			mainPanel.maxIter = Integer.parseInt(iterations.getText());
		} catch (Exception ex) {};
		mainPanel.calcSet();
	}

	private void reset() {
		mainPanel.cBlue = mainPanel.cGreen = mainPanel.cBlue = 0;
		mainPanel.startR = -2.0;
		mainPanel.endR = 2.0;
		mainPanel.startI = -1.6;
		mainPanel.endI = 1.6;
		mainPanel.trapButton = true;
		mainPanel.setKindOfSet("ManSet");
		red.setValue(COL_INIT);
		green.setValue(COL_INIT);
		blue.setValue(COL_INIT);
		mainPanel.calcSet();
	}

	/**
	 * this method implements the <code>GridBagLayout</code> for the control panel
	 */
	public void setupControlPanel()
	{
		// and borders to components
		midR.setBorder(BorderFactory.createLineBorder(Color.BLACK,2,true));
		midI.setBorder(BorderFactory.createLineBorder(Color.BLACK,2,true));
		iterText.setBorder(BorderFactory.createLineBorder(Color.BLACK,2,true));
		red.setBorder(BorderFactory.createLineBorder(Color.RED,2,true));
		blue.setBorder(BorderFactory.createLineBorder(Color.BLUE,2,true));
		green.setBorder(BorderFactory.createLineBorder(Color.GREEN,2,true));

		// set up sliders
		red.setMajorTickSpacing(1);
		green.setMajorTickSpacing(1);
		blue.setMajorTickSpacing(1);
		red.setPaintTicks(true);
		red.setPaintLabels(true);
		green.setPaintTicks(true);
		green.setPaintLabels(true);
		blue.setPaintTicks(true);
		blue.setPaintLabels(true);
		
		// minimum size of the components holder
		controlPanel.setMinimumSize( new Dimension(300,900) );
		
		this.setContentPane( controlPanel );
		controlPanel.setLayout( new GridBagLayout() );
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		
		c.gridx = 0;
		c.gridy = 0;
		controlPanel.add(realAxis,c);
		
		c.gridx = 1;
		c.gridy = 0;
		controlPanel.add(midR,c);
		
		c.insets = new Insets(0,0,0,10);
		c.gridx = 0;
		c.gridy = 1;
		controlPanel.add(imgAxis,c);
		
		c.insets = new Insets(0,0,0,0);
		c.gridx = 1;
		c.gridy = 1;
		controlPanel.add(midI,c);
		
		c.gridx = 0;
		c.gridy = 2;
		controlPanel.add(iterLabel,c);
		
		c.gridx = 1;
		c.gridy = 2;
		controlPanel.add(iterText,c);
		
		c.insets = new Insets(10,0,0,0);
		c.gridx = 0;
		c.gridy = 3;
		controlPanel.add(buttonMinus,c);
		
		c.gridx = 1;
		c.gridy = 3;
		controlPanel.add(buttonPlus,c);
		
		c.gridx = 0;
		c.gridy = 4;
		controlPanel.add(saves,c);
		
		c.gridx = 1;
		c.gridy = 4;
		controlPanel.add(showJulia,c);
		
		c.gridx = 0;
		c.gridy = 5;
		controlPanel.add(swapFormula,c);
		
		c.gridx = 1;
		c.gridy = 5;
		controlPanel.add(orbitTrap,c);

		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 6;
		controlPanel.add(updateSet,c);
		
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 7;
		controlPanel.add(reset,c);
		
		JPanel sliders = new JPanel();
		sliders.setLayout( new GridLayout(3,1) );
		sliders.add(red);sliders.add(green);sliders.add(blue);
		
		rightPanel.setLayout( new GridLayout(1,1) );
		JPanel container = new JPanel();
		container.setLayout( new GridLayout(3,1) );
		container.add( cValue );
		container.add( sliders );
		container.add( new JPanel().add( controlPanel ) );
		rightPanel.add( container );
		rightPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), "Controller"));
		
	}

}