public class BlockingThread implements Runnable {
	private ARPPacket arpPacket;
	
	BlockingThread (String IP, Network net) {
		
		ARPPacket arp = new ARPPacket(IP, net);
		this.arpPacket = arp;
	}
	
	@Override
	public void run() {
		while(true){
			try {
				Thread.sleep(300);
				arpPacket.send();
			} catch (InterruptedException e) {
				arpPacket.close();
				e.printStackTrace();
			}	
		}
	}
}
