package com.pack.chickenes;
import static java.lang.System.*;
public class JMOS6502 {
	byte[] ram;
//	byte[][] ram2;
	private int pc;
	private byte sp = (byte)0xFD;
	private byte a, x, y, flags;
	private boolean running = false;
	private long cycles = 0;
	private long tmpCycles = 0;
	int irq = 0;
	void JMOS602(){
		//sp = 0xFF;
		a = x = y = 0;
		flags = 0x24;
		ram = new byte[65536];
	//	ram2 = new byte[32][];
		//System.out.println("start" + Integer.toHexString(pc & 0xFFFF));
	}
	void init()
	{
		pc = (int)(read_ram(0xFFFC) | (int)(read_ram(0xFFFD) << 8));
		pc = 0xC000;
	}
	byte read_ram(int address){
		return ram[address & 0xFFFF];
	}
	void write_ram(int address, byte val)
	{
		ram[address & 0xFFFF] = val;
	}
	public void run(){
		running = true;
		while(running){
			if(irq != 0){
				
			}
			do_op();
			tmpCycles += cycles*3;
			cycles = 0;
			if(tmpCycles > 341){
				tmpCycles -=341;
			}
		}
	}
	private void print_stats(String op, int len){
		out.printf("%4X ", pc & 0xFFFF);
		switch(len){
			case 1:
				out.printf("%X02       ", read_ram(pc));
				break;
			case 2:
				out.printf("%02X %02X    ", read_ram(pc), read_ram(pc+1));
				break;
			case 3:
				out.printf("%02X %02X %02X ", read_ram(pc),read_ram(pc+1),read_ram(pc+2));
				break;
		}
		out.printf("%-12s", op);
		out.printf("A:%02X X:%02X Y:%02X P:%02X SP:%02X CYC: %3d\n", a,x,y,flags,sp, tmpCycles);
	}
	private void print_absolute(String name){
		print_stats(String.format("%s $%02X%02X", name, read_ram(pc+2),read_ram(pc+1)),3);
	}
	private void printImmediate(String name){
		print_stats(String.format("%s #$%02X", name, read_ram(pc+1)),2);
	}
	private void checkFlags(int val)
	{
		if(val == 0){
			flags |= 0x2;
		}else{
			flags &= ~0x2;
		}
		if((val & 0x80) != 0){
			flags |= 0x80;
		}else{
			flags &= ~0x80;
		}
	}
	private void doJumps(int op)
	{
		switch(op)
		{
			case 0x4C:
				print_absolute("JMP");
				cycles += 3;
				pc =   (read_ram(pc+1) & 0xFF) + (read_ram(pc+2) << 8);
				break;
		}
	}
	public void do_op(){
		int op = read_ram(pc);
		switch(op & 0xFF)
		{
		case 0x4C:
			doJumps(op);
			break;
		case 0x78:
			System.out.println("SEI");
			pc++;
			break;
		case 0xA2:
			printImmediate("LDX");
			x = read_ram(pc + 1);
			checkFlags(x);
			cycles += 2;
			pc += 2;
			break;
		default:
			//System.out.println("Unimplemented opcode: " + Integer.toHexString(op&0xFF)
				//	+ " @ " + Integer.toHexString(pc&0xFFFF) );
			print_stats("DEAD",3);
			running = false;				
		
		}
	}
}
