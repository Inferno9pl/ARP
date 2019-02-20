import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//klasa sluzaca do uzyskania adresow IP i ich nazw hostow
public class Network {
	private String localHostName;
	private String localHostAddress;

	private String gatewayIP;
	private String gatewayMAC;
	private String myMAC;
	
	private List<String> IPs = new ArrayList<String>(); // wyseleckjonowane IP

	Network() {
		// zdobycie adresu i nazwy hosta
		try {
			this.localHostAddress = InetAddress.getLocalHost().getHostAddress();
			this.localHostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//przypisanie do zmiennych adresu ip bramy, mojego i bramy maca oraz znalezienie innych adresow IP
		setGatewayIP();
		setMyMAC();
		setIPs();

	}

	// zwraca znaleziony adres bramy domyslnej
	private void setGatewayIP() {
		Scanner s;
		gatewayIP = null;
		String[] temp = null;
		boolean nextIsGatewayAddr = false;

		try {
			s = new Scanner(Runtime.getRuntime().exec("ipconfig").getInputStream());

			while (s.hasNext()) {
				// dzieli to co ma na linie
				temp = (s.nextLine().toString()).split("\\n");

				// w kazdej linii szuka slow Brama albo Gateway
				int j = 0;
				for (j = 0; j < temp.length; j++) {

					// szuka slow
					if (temp[j].matches("(.*)Brama(.*)|(.*)Gateway(.*)") || nextIsGatewayAddr) {
						
						//jesli to adres IP4 to dzieli od razu
						//jesli znajdzie to tworzy nowa tabele dzielac ta dobra linie na wyrazy
						String[] temp2 = temp[j].split("\\s+");

						// szuka adresu wsrod tych wyrazow
						int k = 0;
						for (k = 0; k < temp2.length; k++) {

							// wyszukanie IP
							if (temp2[k].matches("[0-9]+.[0-9]+.[0-9]+.[0-9]+"))
								gatewayIP = temp2[k];
						}
						
						nextIsGatewayAddr = false;
						
						//jesli to adres IP6 to zaznacza, ze kolejny bedzie IP4
						//czyli nie znalazl adresu IP4 bo byl zly format
						if(gatewayIP == null) {
							nextIsGatewayAddr = true;
						}
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// uzyskanie wszystkich IP i przy okazji adresu MAC bramy
	public void setIPs() {
		Scanner s;
		String[] temp = null; // tymczasowe wyrazy z jednego wiersza
		List<String> allStrings = new ArrayList<String>(); // wszystkie IP

		boolean nextIsGatewayMAC = false; // okresla czy kolejny wyraz bedzie poszukiwanym MAC

		// wyczyszczenie zmiennej
		IPs.clear();

		try {
			s = new Scanner(Runtime.getRuntime().exec("arp -a").getInputStream());

			while (s.hasNext()) {
				temp = (s.nextLine().toString()).split("\\n|\\s+");

				// dodanie do tablicy lacznej
				if (temp.length > 0) {
					int j = 0;
					for (j = 0; j < temp.length; j++) {

						// poszukiwanie MAC
						if (nextIsGatewayMAC) {
							//uzyskuje adres, ale nalezy go pozbawic pauz
							String gatewayMACpauses = temp[j];
							
							//pozbawiam pauz
							gatewayMAC = gatewayMACpauses.replace("-", "");
							
							nextIsGatewayMAC = false;
						}

						// wyszukanie IP
						if (temp[j].matches("[0-9]+.[0-9]+.[0-9]+.[0-9]+")) {
							allStrings.add(temp[j]);

							if (temp[j].equals(gatewayIP)) {
								nextIsGatewayMAC = true;
							}
						}
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// selekcja adresow IP
		int i = 0;
		String[] quarters;
		for (i = 0; i < allStrings.size(); i++) {

			quarters = allStrings.get(i).split("\\W");

			int y0 = Integer.parseInt(quarters[0]);
			int y3 = Integer.parseInt(quarters[3]);

			// eliminacja zlych zakresow adresow, klasy C, broadcastow, mojego adresu
			if ((y0 < 224) && (y3 != 255) && !(allStrings.get(i).equals(localHostAddress))) {
				IPs.add(allStrings.get(i));
			}
		}
	}
	
	// uzyskanie mojego adresu mac
	private void setMyMAC() {
		NetworkInterface nwi;
		try {
			nwi = NetworkInterface.getByInetAddress(InetAddress.getByName(localHostName));
			byte mac[] = nwi.getHardwareAddress();

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				// zwraca w poprawnej formie z pauzami
				// sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));

				// zwraca bez pauz
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "" : ""));
			}

			myMAC = sb.toString();

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<String> getIPs() {
		return IPs;
	}

	public String getGatewayIP() {
		return gatewayIP;
	}

	public String getGatewayMAC() {
		return gatewayMAC;
	}

	public String getMyMAC() {
		return myMAC;
	}	
}
