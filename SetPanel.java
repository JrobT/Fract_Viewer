package coursework1;

import java.awt.*;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

/**
 * <h>Displays the main set on the panel that is applied to the view frame</h>
 * 	<p>The set displayed can be either Mandelbrot or Burning ship. This
 * 	is indicted by the <code>kindOfSet</code> variable which is set to either
 * 	ManSet or Burning for the respective set.</p>
 * 
 * @author Jack Trute jt10g15
 */
@SuppressWarnings("serial")
class SetPanel extends JPanel {
	final private int numThreads = 10; 
	static boolean status=false;
	final private int julSize = 300;
	final private int size = 1000;	// default frame size
	private String kindOfSet;	// mandelbrot or burning ship
	final private int zoomAlphaVal = 128;	// make the zoom panel semi-transparent
	int rectx, recty, rectw, recth;	// resizing the zoom box
	protected JPanel boxZoom;
	double trapSize;
	double trapDist;
	boolean trapButton = true;
	boolean trapped;	// orbit trapping
	double startR;
	double endR;
	double startI;
	double endI;	// my default axis
    int maxIter;
    int width;
    int height;
    Color[][] drawing;	// set up visual space of set
    Complex userSelectedPoint;
    double cRed;
    double cGreen;
    double cBlue;
	
	public int getJulSize() {
		return julSize;
	}

	public int getFrameSize() {
		return size;
	}
	
	public String getKindOfSet() {
		return kindOfSet;	
	}
	
	public void setKindOfSet(String setType) {
		kindOfSet = setType;
	}
	
	public Color getDrawing(int x, int y) {
		return drawing[x][y];
	}

	SetPanel() {
		startR = -2.0;
		endR = 2.0;
		startI = -1.6;
		endI = 1.6;	//	set initial axis
		cRed = cGreen = cBlue = 0;	//	set initial colouring
		setKindOfSet("ManSet");
		this.trapSize = 0.05;
		maxIter = 100;
		width = size;
    	height = size;
		drawing = new Color[width][height];
    	boxZoom = new JPanel(null);
    	boxZoom.setBackground( new Color(255,255,255,zoomAlphaVal) );
    	boxZoom.setBounds(0,0,0,0);
    	this.add(boxZoom);
    	this.cRed = 0;
    	this.cBlue = 0;
    	this.cGreen = 0;
    }
	
	SetPanel(Complex userSelectedPoint, int maxIter, double r, 
			double g, double b) {
		startR = -2.0;
		endR = 2.0;
		startI = -1.6;
		endI = 1.6;
		cRed = cGreen = cBlue = 0;
		setKindOfSet("Julia");
		this.trapSize = 0.05;
		this.maxIter = maxIter;
		width = getJulSize();
    	height = getJulSize();
		drawing = new Color[width][height];
		boxZoom = new JPanel(null);
    	boxZoom.setBackground( new Color(255,255,255,zoomAlphaVal) );
    	boxZoom.setBounds(0,0,0,0);
    	this.add(boxZoom);
    	this.cRed = r;
    	this.cBlue = b;
    	this.cGreen = g;
		this.userSelectedPoint = userSelectedPoint;
    }
    
    /**
     * 	Receives pixal (x,y) values and relates
     * 	the pixal size to the axis' size
     * 
     * @param x	x value of pixal
     * @param y	y value of pixal
     * @return the complex number
     */
    Complex makeItComplex(double x, double y){
    	Complex c = new Complex(x,y);
    	c.setReal(startR + ((x/width) * (endR - startR)));
    	c.setImg(startI + ((y/height) * (endI - startI)));
    	return c;
    }

    /**
     * 	Check for burning ship or mandelbrot;
     * 	burning ship requires an extra line.
     * 	The method loops through the pixals
     * 	and calculates if the pixal is in the 
     * 	set.
     * 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    void calcSet() {
		class RenderSwingWorker extends SwingWorker<Boolean, Void> {
			private int part_width;
			
			@Override
			protected void done() {
				repaint();
			}

        	@Override
        	protected Boolean doInBackground() throws Exception {
        		status = true;
        		
        		System.out.println("Active threads: " + Thread.activeCount());
        		
        		class MyThread extends Thread {
        			private int w, sw;
        			Color[][] colourBuffer;
        			
        			MyThread(int w, int sw, Color[][] buffer) {
        				this.w = w;
        				this.sw = sw;
        				this.colourBuffer = buffer;
        			}
        			
        			@Override
        			public void run() {
        				double log_zn, nu;
        				double new_iter=0;
        	        	Color colour1, colour2, colour;
        				
        				System.out.println(this.getId() + " started");

        				try {

        					for (int y = 0; y < height; y++)
        						for (int x = sw; x < w; x++)
        						{

        							Complex num = makeItComplex(x,y);
        							int iter = 0;

        							while (!diverges(iter, num))	
        							{
        								if (getKindOfSet() == "Burning")
        									num = new Complex(Math.abs(num.getReal()), Math.abs(num.getImg()));
        								num = num.square();
        								if (getKindOfSet() == "Julia")
        									num = num.add(userSelectedPoint);
        								else
        									num = num.add(makeItComplex(x,y));
        								iter = iter + 1;
        								
        								if (trapButton)
        									if (Math.abs(num.getReal())<trapSize)
        									{
        										trapDist=Math.abs(num.getReal());
        										iter = (int) (trapDist/trapSize*50);
        										break;
        									}
        									else if (Math.abs(num.getImg())<trapSize)
        									{
        										trapDist=Math.abs(num.getImg());
        										iter = (int) (trapDist/trapSize*50);
        										break;
        									}
        							}

        							if ( iter < maxIter ) 
        							{
        								log_zn = Math.log10( num.modulusSquared() ) / 2d;
        								nu = Math.log10( log_zn / Math.log10(2) ) / Math.log10(2);
        								new_iter = iter + 1 - nu;
        								colour1 = getGUIColor(new_iter);
            							colour2 = getGUIColor(new_iter+1);
            							colour = linear_interpolate(colour1, colour2, iter % 1);
        							}
        							else {
        								colour = Color.BLACK;
        							}

        							colourBuffer[x][y] = colour;
        						}
        				}	catch (Exception e) {	System.err.println("Colouring obtained the following error: " + e);	}
        			}
        		}
        		
        		Color[][] buffer = new Color[width][height];

        		MyThread[] threads = new MyThread[numThreads];
        		part_width = width / numThreads;

        		for (int i=0; i<numThreads; ++i)
        		{
        			threads[i] = new MyThread(part_width*(i+1), part_width*i, buffer);
        			threads[i].start();
        		}

        		for (int i=0; i<numThreads; ++i)
        			threads[i].join();
        		
        		System.out.println("All Thread Workers are Finished\n");
        		
        		drawing = buffer;

        		status=false;
        		return true;
        	}

		}

		final SwingWorker<Boolean, Void> sw = new RenderSwingWorker();
		if (!status) 
			sw.execute();
    }
    
    /**
     * Display the Set
     * 
     * @param g
     */
    void draw(Graphics g) {
    	for (int y = 0; y < height; y++)
	    	for (int x = 0; x < width; x++)
	    	{
	    		g.setColor(drawing[x][y]);
	    		g.fillRect(x, y, 1, 1);
	    	}
    }

    /**
     * Blends two colours to apply the colour smoothness algorithm
     * 
     * @param colour1 the actual colour
     * @param colour2 the next colour
     * @param iter the number of iteration we were on
     * @return
     */
    @SuppressWarnings("static-access")
	Color linear_interpolate(Color colour1, Color colour2, float iter) {
    	float p = 1-iter;
    	float[] hsbCol1 = colour1.RGBtoHSB(colour1.getRed(), colour1.getGreen(),
    			colour1.getBlue(), null);
    	float[] hsbCol2 = colour2.RGBtoHSB(colour2.getRed(), colour2.getGreen(),
    			colour2.getBlue(), null);
    	// hsb colouring
    	float h = (float) (hsbCol1[0]*p + hsbCol2[0]*iter);
		float s = (float) (hsbCol1[1]*p + hsbCol2[1]*iter);
		float b = (float) (hsbCol1[2]*p + hsbCol2[2]*iter);
		
		Color colour = new Color(Color.HSBtoRGB(h, s, b));
		
		return colour;
    }
    
    /**
     * 	Checks if the complex number pixal diverges from
     * 	the set or not
     * 
     * @param iter the number of iterations
     * @param num the complex number of the pixal
     * @return true/false if the point diverges
     */
    boolean diverges(int iter, Complex num) {
    	return (!((int) num.modulusSquared() < (4*4) && iter < maxIter));
    }
    
	public void paintComponent(Graphics g) {
        draw(g);
    }
    
    Color getGUIColor(double c){
    	float r,g,b,t;	// red green blue and relative iterations
    	
    	t = (float) c / (float) maxIter;
    	t *= Math.PI*2; // radians
    	
    	r = ((float) Math.sin((double) t+((cRed/3d)*Math.PI)));
    	g = ((float) Math.sin((double) t+((cGreen/3d)*Math.PI)));
    	b = ((float) Math.sin((double) t+((cBlue/3d)*Math.PI)));
    	
    	// checks between 0 and 1
    	r = r > 1.0f ? 1.0f : (r < 0.0f ? 0.0f : r);
    	g = g > 1.0f ? 1.0f : (g < 0.0f ? 0.0f : g);
    	b = b > 1.0f ? 1.0f : (b < 0.0f ? 0.0f : b);
    	
    	return new Color(r,g,b);
    	//return new Color((c*3)%255,0,0); //first colour scheme
    }
  
}