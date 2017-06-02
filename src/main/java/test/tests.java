package test;

import httpServer.booter;
import interfaceApplication.Message;

public class tests {
	public static void main(String[] args) {
//		String messageContent = "{\"messageContent\":\"asdsadas\",\"fatherid\":\"58f5d7478aa72f18a09b6c52\"}";
//		// System.out.println(new
//		// Message().UpdateMessage("58f5d8fd8aa72f0610dbe25a", messageContent));
//		// System.out.println(new Message().connArc("58f5d8fd8aa72f0610dbe25a",
//		// "5"));
//		// System.out.println(new Message().PageMessage(1, 10));
//		System.out.println(new Message().AddMessage(messageContent));
		
		booter booter = new booter();
		System.out.println("GrapeMessage!");
		try {
			System.setProperty("AppName", "GrapeMessage");
			booter.start(6004);
		} catch (Exception e) {

		}
	}
}
