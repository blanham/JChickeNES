package com.pack.chickenes;

public class NES extends JMOS6502 {
	
	iNES cartridge = null;
	public NES(String path)
	{

		cartridge = new iNES(path);
		cartridge.print();
		super.JMOS602();
		System.arraycopy(cartridge.PRG, 0, ram, 0x8000, cartridge.prgSize()*0x4000);
		System.arraycopy(cartridge.PRG, 0, ram, 0xC000, cartridge.prgSize()*0x4000);
		init();
		//ram = new byte[0x400];
		//do_op();
	}
	public void run(){
		super.run();
	}
	byte read_ram(int address)
	{
		address &= 0xFFFF;
		switch(address >>> 12){
			case 4:
				return 0;
			default:
				return ram[address];
		}
	}
	void write_ram(int address, byte val)
	{
		address &= 0xFFFF;
		ram[address] = val;
		if (0x8000 <= address){
			System.out.println("invalid write");
		}
		
	}
}
