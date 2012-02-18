package com.pack.chickenes;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class iNES {
	private byte[] rawHeader = null;
	public byte[] CHR = null;
	public byte[] PRG = null;
	public char mapper()
	{
		return (char)((rawHeader[6] >>> 4) & 0xf | (rawHeader[7] & 0xf0));
	}
	public int prgSize()
	{
		return rawHeader[4];
	}
	public int chrSize()
	{
		return rawHeader[5];
	}
	public iNES(String filename)
	{
		FileInputStream file = null;
		byte[] iNESmagic = new byte[]{'N','E','S', 0x1A};
		rawHeader = new byte[16];
		try {
			file = new FileInputStream(filename);
			file.read(rawHeader, 0, 16);
			boolean isNES = Arrays.equals(iNESmagic, Arrays.copyOf(rawHeader,4));
			if(isNES)
			{
				System.out.println("NES file found");
			}else{
			}
			int prg_size = prgSize() * 0x4000;
			int chr_size = chrSize() * 0x2000;
			PRG = new byte[prg_size];
			file.read(PRG, 0, prg_size);
			CHR = new byte[chr_size];
			file.read(CHR,0,chr_size);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void print(){
		System.out.println("Mapper: " + Integer.toHexString((int)mapper()));
		System.out.println("PRG size: " + prgSize());
		System.out.println("CHR size: " + chrSize());
		System.out.println("first byte: " + Integer.toHexString((byte)PRG[0]));
		System.out.println("first byte: " + Integer.toHexString((byte)CHR[0]));
	}
}