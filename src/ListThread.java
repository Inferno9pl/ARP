import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.swing.DefaultListModel;

public class ListThread implements Runnable {

	private Arp_atack arp;
	private List<String> IPs;

	//konstruktor
	public ListThread(Arp_atack arp_atack, List<String> ips) {
		this.arp = arp_atack;
		this.IPs = ips;
	}

	@Override
	public void run() {
		DefaultListModel<String> ListModelHostName = new DefaultListModel<String>();
		
		// dla kazdego adresu IP
		int i;
		for (i = 0; i < IPs.size(); i++) {

			// nazwa hosta
			InetAddress address;
			String temp;
			try {
				address = InetAddress.getByName(IPs.get(i));
				temp = address.getHostName();

				if (!temp.equals(IPs.get(i))) {
					ListModelHostName.addElement(temp);
				} else {
					ListModelHostName.addElement(" ");
				}

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		arp.getListHostName().setModel(ListModelHostName);
	}
}