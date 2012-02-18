package com.pack.chickenes;

public class JMOS6502 {
	byte[] ram;
	private int pc;
	private char sp = 0xFF;
	private char a, x, y, flags;
	private boolean running = false;
	int irq = 0;
	void JMOS602(){
		sp = 0xFF;
		a = x = y = 0;
		pc = (int)(read_ram(0xFFFC) | (int)(read_ram(0xFFFD) << 8));
		//System.out.println("start" + Integer.toHexString(pc & 0xFFFF));
	}
	byte read_ram(int address){
		return 0;
	}
	void write_ram(int address, char val)
	{
		
	}
	public void run(){
		running = true;
		while(running){
			if(irq != 0){
				
			}
			do_op();
		}
	}
	public void do_op(){
		int op = read_ram(pc);
		switch(op)
		{
			case 0x78:
				System.out.println("SEI");
				pc++;
				break;
			default:
				System.out.println("Unimplemented opcode: " + Integer.toHexString(op&0xFF)
						+ " @ " + Integer.toHexString(pc&0xFFFF) );
				running = false;
				
		
		}
	}
}
