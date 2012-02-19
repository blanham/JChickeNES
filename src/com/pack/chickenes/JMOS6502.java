
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
		out.printf("%-32s", op);
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
	private void printZeroPage(String name){
		print_stats(String.format("%s $%02X = %02x", name, read_ram(pc+1), read_ram(read_ram(pc+1) & 0xFF)),2);

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
	private void popPC(){
		pc = ram[0x100 + ++sp] & 0xff;
		pc |= ram[0x100 + ++sp] << 8;
		pc &=0xFFFF;
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
			case 0x60:
				popPC();
				pc++;
				cycles +=6;
				break;
		}
	}
	private void doFlags(int op){
		switch(op){
		case 0x18: flags &= ~0x1; pc++; cycles += 2; break;
		case 0x38: flags |= 0x1; pc++; cycles += 2; break;
		//case 0x18: flags &= ~0x1; pc++; cycles += 2; break;
		case 0x78: flags |= 0x04; pc++; cycles += 2; break;
		case 0xF8: flags |= 0x08; pc++; cycles += 2; break;
		}
	}
	private void doBranch(boolean condition){
		cycles += 2;
		if(condition){
			int savepc = pc + (char)read_ram(pc + 1);
			pc = savepc;
			cycles += 1;
		}
			
		pc += 2;
	}
	public void logger(int op){
		switch(op){
		//BIT
		case 0x24: printZeroPage("BIT"); break;
		//Branches
		case 0x10: printBranch("BPL"); break;
		case 0x30: printBranch("BMI"); break;
		case 0x50: printBranch("BVC"); break;
		case 0x70: printBranch("BVS"); break;
		case 0x90: printBranch("BCC"); break;
		case 0xB0: printBranch("BCS"); break;
		case 0xD0: printBranch("BNE"); break;
		case 0xF0: printBranch("BEQ"); break;
		//Flags
		case 0x18: printImplied("CLC"); break;
		case 0x38: printImplied("SEC"); break;
		case 0x78: printImplied("SEI"); break;
		case 0xF8: printImplied("SED"); break;

		//JMPs
		case 0x20: printAbsolute("JSR"); break;
		case 0x4C: printAbsolute("JMP"); break;
		case 0x60: printImplied("RTS"); break;
		//LDA
		case 0xA9: printImmediate("LDA"); break;
		//LDX
		case 0xA2: printImmediate("LDX"); break;
		//NOP
		case 0xEA: printImplied("NOP"); break;
		//STX
		case 0x85: printZeroPage("STA"); break;
		//STX
		case 0x86: printZeroPage("STX"); break;
		//default:
			//out.printf("invalid op%x\n", op);
		}
	}
	private void bit(byte value){
		if((a & value) != 0){
			flags &= 0xfd;
		}else{
			flags |= 0x02;
		}
		flags &= 0x3f;
		flags |= value & 0xC0;
	}
	public void do_op(){
		int op = read_ram(pc) & 0xFF;
		logger(op);
		switch(op)
		{
		//BIT
		case 0x24: bit(readZeroPage()); break;
		//Branches
		case 0x10: doBranch((flags & 0x80) == 0); break;
		case 0x30: doBranch((flags & 0x80) != 0); break;
		case 0x50: doBranch((flags & 0x40) == 0); break;
		case 0x70: doBranch((flags & 0x40) != 0); break;
		case 0x90: doBranch((flags & 0x01) == 0); break;
		case 0xB0: doBranch((flags & 0x01) != 0); break;
		case 0xD0: doBranch((flags & 0x02) == 0); break;
		case 0xF0: doBranch((flags & 0x02) != 0); break;
		//Flags
		case 0x18: case 0x38: case 0x58: case 0x78: case 0xB8: case 0xD8: case 0xF8:
			doFlags(op);
			break;
		//Jumps
		case 0x20: case 0x4C: case 0x60:
			doJumps(op);
			break;
		//LDA
		case 0xA9: a = readImmediate(); checkFlags(a); break;
		//LDX
		case 0xA2: x = readImmediate(); checkFlags(x); break;
		//NOP
		case 0xEA: cycles += 2; pc++; break;
		//STA
		case 0x85: writeZeroPage(a); break;
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
		return read_ram(pc-1);
	}
	private byte readZeroPage(){
		pc +=2;
		cycles +=3;
		return read_ram(read_ram(pc-1) & 0xFF);
	}
	private void writeZeroPage(byte value){
		write_ram(read_ram(pc+1) & 0xFF, value);
		cycles += 3;
		pc += 2;
	}
}
