package com.pack.chickenes;


public class NES extends JMOS6502 {
	
	iNES cartridge = null;
	public NES()
	{

		cartridge = new iNES("/Users/blanham/smario.nes");
		cartridge.print();
		super.JMOS602();
		ram = new byte[0x400];
		//do_op();
	}
	public void run(){
		super.run();
	}
	byte read_ram(int address)
	{
		//System.out.println(Integer.toHexString(address));
		address &= 0xFFFF;
		if(0x8000 <= address){
			return cartridge.PRG[address - 0x8000];
		}
		return (byte)-1;
	}
	void write_ram(int address, char val)
	{
		address &= 0xFFFF;
		if (0x8000 <= address){
			System.out.println("invalid write");
		}
		
	}
}
