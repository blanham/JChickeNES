/**
 * 
 */
package com.pack.chickenes;

/**
 * @author blanham
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println("JChickeNES v0.00");
		NES nes = new NES("nestest.nes");
		nes.run();
	}

}
