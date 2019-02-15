import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.JMemoryPacket;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.network.Ip4;

public class ARPPacket {
	private static final String hardwareType = "0001";// 2 bytes
	private static final String protocolType = "0800";// IPv4 protocol
	private static final String hardwareAddressLength = "06";// xx-xx-xx-xx-xx-xx 48 bits => 6bytes
	private static final String protocolAdressLength = "04";// yyy.yyy.yyy.yyy 32 bits => 4 bytes
	private static final String operationCode = "0001";// Arp request 0001 ; Arp reply 0002

	private static final String target_MAC = "192C292D0E3E";	//losowy mac
	private static final String zero_MAC = "000000000000";
	
	//testowane poza domem
	//private static final String router_mac = "647c3440ff9a";
	//private static final String router_mac = "ffffffffffff";

	//zdobyte!!
	//private static final String router_IP = "C0A80001"; // router_ip, brama
	//private static final String my_mac = "C01885BF0DB3";
	//private static final String router_mac = "647002A4A292";
	
	private JPacket ethFrame;
	private Pcap pcap;
	
	//zamiana z zapisu z kropkami na szesnastkowy
	private String parseToHex(String IP) {
		String[] temp = IP.split("\\.");
		String hexIP = "";
		String t;
		
		int i;
		for(i=0; i<temp.length; i++) {
			t = Integer.toHexString(Integer.parseInt(temp[i]));
			if (t.length() == 1) t = "0" + t;
			hexIP = hexIP + t;
		}
		return hexIP;
	}

	//do zmiany, dodatkowe 2 paramerty na dres mac routera i moj
	public ARPPacket(String IP_DESTINATION, Network net) {
		
		String ip_gateway = parseToHex(net.getGatewayIP());
		String mac_gateway = net.getGatewayMAC();
		String mac_my = net.getMyMAC();
		
		String ip_dest = parseToHex(IP_DESTINATION);
		
		System.out.println("Blokuje IP:  " + IP_DESTINATION);
		System.out.println("Router IP:   " + net.getGatewayIP());
		System.out.println("Router MAC:  " + mac_gateway);
		System.out.println("Moj MAC:     " + mac_my);
		
		String ARPPacket = hardwareType + protocolType + hardwareAddressLength + protocolAdressLength + operationCode
				+ target_MAC + ip_dest + zero_MAC + ip_gateway;

		String ETHPacket = mac_gateway + mac_my + "0806" + ARPPacket;
		
		//String ARPPacket = hardwareType + protocolType + hardwareAddressLength + protocolAdressLength + operationCode + target_MAC + ip_dest + zero_MAC + ip_gateway;
		//String ETHPacket = router_mac + my_mac + "0806" + ARPPacket;
		
		this.ethFrame = new JMemoryPacket(Ethernet.ID, ETHPacket);
		
		init();
	}

	public JPacket get() {
		return this.ethFrame;
	}

	public void init() {
		List<PcapIf> alldevs = new ArrayList<PcapIf>(); // lista z interfejsami sieciowymi
		StringBuilder errbuf = new StringBuilder(); // bledy

		// lista urzadzen sieciowych
		int r = Pcap.findAllDevs(alldevs, errbuf);
		
		//Pcap.
		if ( alldevs.isEmpty()) {
			System.err.printf("Nie mogê znalezæ listy interfejsów sieciowych, b³¹d: %s", errbuf.toString());
			return;
		}

		
		/* -------------------------------------------- ZMIANA ------------------------------------------------ */
		// trzeba znalezc aktualnie podlaczony interfejs - karte sieciowa
		//System.out.println(alldevs.size());
		//PcapIf device = alldevs.get(1);

		
		System.out.println("\nZnalezione interfejsy sieciowe:");

	    int i = 0;
	    for (PcapIf device : alldevs) {
	        String description =
	            (device.getDescription() != null) ? device.getDescription()
	                : "Brak opisu";
	        System.out.printf("#%d: %s [%s]\n", i++, device.getName(), description);
	    }

	    PcapIf device = alldevs.get(0); // We know we have atleast 1 device
	    
	    System.out.println(device.getFlags());
	    
	    //tutaj musze sprawdzic czy po wybraniu danego interfejsu mam polaczenie z internete, (chociaz ta opcja moze byc zla bo co z tego ze wybralem interfejs skoro i tam mam neta zawsze)

	    
	    
	    System.out.printf("\nWybrano '%s' :\n\n",
	            (device.getDescription() != null) ? device.getDescription()
	                : device.getName());
	    
	    /* -------------------------------------------- ZMIANA ------------------------------------------------ */
		

		// otwarcie interfejsu sieciowego
		int snaplen = 64 * 1024; // Capture all packets, no trucation
		int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
		int timeout = 10 * 1000; // 10 seconds in millis
		this.pcap = Pcap.openLive(device.getName(), snaplen, flags, timeout, errbuf);
		
		
		
		
		//temp------------------------------
		
		PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {

			public void nextPacket(PcapPacket packet, String user) {

				byte[] data = packet.getByteArray(0, packet.size()); // the package data
				byte[] sIP = new byte[4];
				byte[] dIP = new byte[4];

				Ip4 ip = new Ip4();
				if (packet.hasHeader(ip) == false) {
					return; // Not IP packet
				}

				ip.source(sIP);
				ip.destination(dIP);

				/* Use jNetPcap format utilities */
				String sourceIP = org.jnetpcap.packet.format.FormatUtils.ip(sIP);
				String destinationIP = org.jnetpcap.packet.format.FormatUtils.ip(dIP);
				
				System.out.println("srcIP=" + sourceIP + 
						" dstIP=" + destinationIP + 
						" caplen=" + packet.getCaptureHeader().caplen());
			}
		};

		// capture first 10 packages
		pcap.loop(10, jpacketHandler, "jNetPcap");

	//	pcap.close();
		
		//------------------------------
	}

	//wyslanie pakietu ARP
	public void send() {
		if (this.pcap.sendPacket(this.ethFrame) != Pcap.OK) {
			System.err.println(this.pcap.getErr());
		}
	}
	
	//zamkniecie interfejsu
	public void close() {
		this.pcap.close();
	}
	
	
	
	
	
	
}