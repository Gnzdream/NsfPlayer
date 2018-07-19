package com.zdream.famitracker.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import com.zdream.famitracker.document.StChanNote;

public class FamitrackerLogger {
	
	public static FamitrackerLogger instance = new FamitrackerLogger();
	
	boolean muteToDo = true;
	boolean muteNote = true;
	boolean muteWriteAddress = true;
	
	HashSet<String> muteAddressSet = new HashSet<>();
	
	File file;
	FileWriter writer;
	
	public void createFile(String path) throws IOException {
		file = new File(path);
		writer = new FileWriter(file);
	}
	
	void writeFile(String s) {
		if (writer != null) {
			try {
				writer.write(s);
				writer.write('\n');
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void closeFile() throws IOException {
		if (writer == null)
			return;
		
		writer.flush();
		writer.close();
	}

	private FamitrackerLogger() { }
	
	public void ready() {
		
	}
	
	public void logNote(StChanNote note, int channel, int frame, int row) {
		if (muteNote)
			return;
		
//		if (channel != 3)
//			return;
		
		StringBuilder b = new StringBuilder();
		b.append("note").append(':').append('[').append(channel).append(']').append(' ');
		b.append(frame).append(':').append(row).append(' ');
		b.append(note);
		
		String l = b.toString();
		System.out.println(l);
		writeFile(l);
	}
	
	public void addMuteAddressName(String name) {
		muteAddressSet.add(name);
	}
	
	public void logWriteAddress(String name, int address, int value) {
		if (muteWriteAddress)
			return;
		
		if (muteAddressSet.contains(name)) {
			return;
		}
		
		value = value & 0xFF;
		StringBuilder b = new StringBuilder();
		b.append("write").append(':').append(name).append(' ');
		b.append(Integer.toHexString(address)).append(" -> ").append(value);
		b.append('(').append(Integer.toHexString(value)).append(',')
				.append(Integer.toBinaryString(value)).append(')');
		
		String l = b.toString();
		System.out.println(l);
		writeFile(l);
	}
	
	public void logToDo(String msg) {
		if (muteToDo)
			return;
		
		String l = msg + " | " + Thread.currentThread().getStackTrace()[2];
		System.out.println(l);
		writeFile(l);
	}

}
