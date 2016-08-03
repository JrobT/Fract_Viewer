package coursework1;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 * <h>New frame to hold a new Julia Set</h>
 * 	Set up JFrame. Save method explained.
 * 
 * @author Jack Trute jt10g15
 *
 */
@SuppressWarnings("serial")
public class JuliaSetDisplay extends JFrame {

	Complex userSelectedPoint;
	SetPanel juliaPanel;
	JComboBox<String> saves;
	ArrayList<Complex> selectedNum;

	JuliaSetDisplay(String title, Complex userSelectedPoint, int iter, boolean trapped,
			JComboBox<String> saves, ArrayList<Complex> selectedNum, double r, double g, 
			double b) {
		super(title);
		juliaPanel = new SetPanel(userSelectedPoint,iter,r,g,b);
		juliaPanel.trapButton = trapped;
		this.saves = saves;
		this.selectedNum = selectedNum;
		this.userSelectedPoint = userSelectedPoint;
		this.init();
		juliaPanel.calcSet();
	}

	void init() {
		JPanel savePanel = new JPanel();
		JPanel container = new JPanel();
		JButton save = new JButton("Save To Favorites");

		this.setContentPane(savePanel);
		this.setLayout( new FlowLayout() );
		
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JuliaSetDisplay.this.save();
			}
		});

		this.setContentPane(juliaPanel);
		this.setLayout( new FlowLayout() );

		this.setContentPane(container);
		this.setLayout( new BorderLayout() );
		savePanel.add(save);
		container.add(juliaPanel, BorderLayout.CENTER);
		container.add(savePanel, BorderLayout.SOUTH);
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible(true);
		this.setSize(juliaPanel.getJulSize(),juliaPanel.getJulSize()+50);
	}
	
	/**
	 * 	Method adds the set to the runtime program, before
	 * 	saving the runtime content to the file. 
	 */
	public void save() {
		saves.addItem("Set " + (saves.getItemCount()+1));
		selectedNum.add(userSelectedPoint);
		
		try {
			OutputStream outputStream = new FileOutputStream("JuliaFile.txt");
			BufferedWriter outputStreamWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
			
			for (int i=0;i<saves.getItemCount();i++)
			{
				outputStreamWriter.write(saves.getItemAt(i) + "," + selectedNum.get(i).getReal() + "," + selectedNum.get(i).getImg());
				outputStreamWriter.newLine();
			}
			
			outputStreamWriter.close();
		}
		catch (IOException e) {
			System.err.println("Could not save the Julia Set to the file: " + e);
		}
	}

}