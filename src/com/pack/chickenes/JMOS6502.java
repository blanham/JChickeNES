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
				out.printf("%02X       ", read_ram(pc));
				break;
			case 2:
				out.printf("%02X %02X    ", read_ram(pc), read_ram(pc+1));
				break;
			case 3:
				out.printf("%02X %02X %02X ", read_ram(pc),read_ram(pc+1),read_ram(pc+2));
				break;
		}
		out.printf("%-16s", op);
		out.printf("A:%02X X:%02X Y:%02X P:%02X SP:%02X CYC: %3d\n", a,x,y,flags,sp, tmpCycles);
	}
	private void printAbsolute(String name){
		print_stats(String.format("%s $%02X%02X", name, read_ram(pc+2),read_ram(pc+1)),3);
	}
	private void printBranch(String name){
		print_stats(String.format("%s $%04X", name, (pc + read_ram(pc+1) + 2) & 0xFFFF),2);
	}
	private void printImplied(String name){
		print_stats(String.format("%s", name),1);

	}
	private void printImmediate(String name){
		print_stats(String.format("%s #$%02X", name, read_ram(pc+1)),2);
	}
	private void printZeroPage(String name, byte value){
		print_stats(String.format("%s #$%02X = %02x", name, read_ram(pc+1), value),2);

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
	private void pushPC(){
		pc += 2;
		ram[0x100 + sp--] = (byte)((pc >> 8) & 0xFF);
		ram[0x100 + sp--] = (byte)(pc & 0xFF);
		pc -= 2;
	}
	private void doJumps(int op)
	{
		switch(op)
		{
			case 0x4C:
				cycles += 3;
				pc =   (read_ram(pc+1) & 0xFF) + (read_ram(pc+2) << 8);
				break;
			case 0x20://JSR
				pushPC();
				pc =   (read_ram(pc+1) & 0xFF) + (read_ram(pc+2) << 8);
				cycles += 6;
				break;
		}
	}
	private void doFlags(int op){
		switch(op){
		case 0x18: flags &= ~0x1; pc++; cycles += 2; break;
		case 0x38: flags |= 0x1; pc++; cycles += 2; break;
		}
	}
	private void doBranch(boolean condition){
		cycles += 2;
		if(condition){
			System.out.println("BRANCH");
			int savepc = pc + (char)read_ram(pc + 1);
			pc = savepc;
			cycles += 1;
		}
			
		pc += 2;
	}
	public void logger(int op){
		switch(op){
		//Branches
		case 0x90: printBranch("BCC"); break;
		case 0xB0: printBranch("BCS"); break;
		//Flags
		case 0x18: printImplied("CLC"); break;
		case 0x38: printImplied("SEC"); break;
		//JMPs
		case 0x20: printAbsolute("JSR"); break;
		case 0x4C: printAbsolute("JMP"); break;
		//LDX
		case 0xA2: printImmediate("LDX"); break;
		//NOP
		case 0xEA: printImplied("NOP"); break;
		//STX
		case 0x86: printZeroPage("STX", x); break;

		}
	}
	public void do_op(){
		int op = read_ram(pc) & 0xFF;
		logger(op);
		switch(op)
		{
		//Branches
		case 0x90: doBranch((flags & 0x1) == 0);
		case 0xB0: doBranch((flags & 0x1) != 0);
		//Flags
		case 0x18: case 0x38: doFlags(op);
		//Jumps
		case 0x20: case 0x4C:
			doJumps(op);
			break;
		//LDX
		case 0xA2: x = readImmediate(); checkFlags(x); break;
		//NOP
		case 0xEA: cycles += 2; pc++; break;
		//STX
		case 0x86: writeZeroPage(x); break;
		default:
			//System.out.println("Unimplemented opcode: " + Integer.toHexString(op&0xFF)
				//	+ " @ " + Integer.toHexString(pc&0xFFFF) );
			print_stats("DEAD",3);
			running = false;				
		
		}
	}
	private byte readImmediate(){
		cycles += 2;
		pc+=2;
		return read_ram(pc+1);
	}
	private void writeZeroPage(byte value){
		write_ram(read_ram(pc+1) & 0xFF, value);
		cycles += 3;
		pc += 2;
	}
}
