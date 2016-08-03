package coursework1;

/**
 * <h1>class to represent a complex number</h1>
 * 	A complex number object represented with real and imaginary 
 * 	parts. The accessor method takes the value of the real part 
 * 	of the number and then the imaginary part, both as doubles.
 * 
 * @author Jack Trute jt10g15
 */
public class Complex {
	
	private double real, img;
	
	/**
	 * @return the real part of the number
	 */
	public double getReal() {
		return real;
	}
	/**
	 * @return the imaginary part of the number
	 */
	public double getImg() {
		return img;
	}
	/**
	 * @param real the real to set
	 */
	public void setReal(double real) {
		this.real = real;
	}
	/**
	 * @param img the img to set
	 */
	public void setImg(double img) {
		this.img = img;
	}
	
	Complex(double real, double img) {
		this.setReal(real);
		this.setImg(img);
	}
	/**
	 * square of the complex number is achieved by multiplying each part
	 * of the number by itself
	 * 
	 * @param c the complex number
	 * @return the number squared as a Complex object
	 */
	public Complex square() {
		return new Complex((getReal()*getReal())-(getImg()*getImg()),
				(2*getImg()*getReal()));
	}
	/**
	 * find the modulus of the complex number used pythagoras'
	 * theorem and multiply by itself
	 * 
	 * @param c the complex number
	 * @return the square of the modulus as a double
	 */
	public double modulusSquared() {
		double mod = (getReal()*getReal())+(getImg()*getImg());
		return mod;
	}
	/**
	 * add up the parts of each complex number to create a new Complex
	 * 
	 * @param d the complex number to add
	 * @return the new complex number
	 */
	public Complex add(Complex d) {
		return new Complex(getReal()+d.getReal(),getImg()+d.getImg());
	}
	
	public String asString() {
		return getReal()+"+"+getImg()+"i";
	}
	
}
