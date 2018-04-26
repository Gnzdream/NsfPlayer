package com.zdream.famitracker;

import com.zdream.famitracker.test.FamitrackerLogger;

public class FamiTrackerDocTest {
	
	public void test() {
		long l = System.currentTimeMillis();
		System.out.println(Runtime.getRuntime().freeMemory());
		
		try {
//			FamitrackerLogger.instance.createFile("D:\\0hasee\\Desktop\\Of_Devious_Machinations_Clockwork_Tower.log");
			FamitrackerLogger.instance.addMuteAddressName("Square");
			FamitrackerLogger.instance.addMuteAddressName("Triangle");
			FamitrackerLogger.instance.addMuteAddressName("Noise");
			FamitrackerLogger.instance.addMuteAddressName("VCR6");
			
			FamiTrackerApp app = FamiTrackerApp.getInstance();
			boolean b = app.open(
					//"D:\\Program\\Rockman\\FamiTracker\\Project\\Shovel Knight\\22_Of_Devious_Machinations_Clockwork_Tower.ftm"
					//"D:\\Program\\Rockman\\FamiTracker\\Project\\Raf的世界\\track04.ftm"
					//"D:\\Program\\Rockman\\FamiTracker\\Project\\Rockman10\\mm10nsf.ftm"
					//"D:\\Program\\Rockman\\FamiTracker\\Project\\Rockman02\\Wily 1.ftm"
					//"D:\\Program\\Rockman\\FamiTracker\\Project\\danooct1 FTMs\\2010\\mega man time tangent early draft.ftm"
					// "D:\\Program\\Rockman\\FamiTracker\\Project\\Rockman other\\MMZ2 - Departure.ftm"
					//"D:\\Program\\Rockman\\FamiTracker\\Project\\Rockman03\\VRC6 - Snake Man.ftm"
					"D:\\Program\\Rockman\\FamiTracker\\Project\\Shovel Knight\\51_Unused_Song.ftm"
					);
			System.out.println(Runtime.getRuntime().freeMemory() + " 用时: " + (System.currentTimeMillis() - l));
			
			if (!b) {
				System.err.println("没有加载成功.");
				return;
			} else {
				System.out.println("成功加载.");
			}
			
			app.play(0);
			
//			FamitrackerLogger.instance.closeFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		FamiTrackerDocTest test = new FamiTrackerDocTest();
		test.test();
	}

}
