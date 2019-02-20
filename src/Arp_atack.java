import java.awt.Adjustable;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Arp_atack extends JFrame {
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;

	private static JList<String> listIP = new JList<String>();
	private static JList<String> listState = new JList<String>();
	private static JList<String> listHostName = new JList<String>();

	private static DefaultListModel<String> ListModelState = new DefaultListModel<String>();

	// tablica zawierajaca wszystkie dzialajace watki. Index odpowiada indeksowi tablicy IP
	private static List<Thread> threads = new ArrayList<Thread>();

	private Network net = new Network(); // klasa do uzyskania adresow IP, MAC

	public static void main(String[] args) throws Exception {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Arp_atack frame = new Arp_atack();

					// inicjalizacja list
					frame.initList();
					
					//frame.setUndecorated(true);
					
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// inicjalizacja list
	private void initList() {
		DefaultListModel<String> ListModelIP = new DefaultListModel<String>();
		DefaultListModel<String> tempHostName = new DefaultListModel<String>(); // tymczasowa nazwa
		List<String> IPs = net.getIPs();

		// rozpoczecie watku pobierajacego nazwy hostow
		ListThread t = new ListThread(this, IPs);
		Thread thread = new Thread(t);
		thread.start();

		// dodaje kazdy adres IP do wyswietlenia
		int i;
		for (i = 0; i < IPs.size(); i++) {

			// adresy IP
			ListModelIP.addElement(IPs.get(i));

			// state
			ListModelState.addElement("ON");

			// hostname
			tempHostName.addElement("szukam...");

			// watki
			threads.add(null);
		}

		// dodanie do list
		listIP.setModel(ListModelIP);
		listState.setModel(ListModelState);
		listHostName.setModel(tempHostName);
	}

	// konstruktor
	public Arp_atack() {
		setTitle("ARP Spoof");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 455, 230);
		contentPane = new JPanel();
		contentPane.setBackground(SystemColor.controlDkShadow);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		
		listIP.setBackground(Color.DARK_GRAY);
		listIP.setForeground(SystemColor.menu);

		// ---listy-----------------------------------
		listIP.setBounds(10, 28, 100, 150);
		listIP.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listIP.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		contentPane.add(listIP);
		listState.setBackground(Color.DARK_GRAY);
		listState.setForeground(SystemColor.menu);

		listState.setBounds(279, 28, 40, 150);
		listState.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listState.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		listState.setCellRenderer(new DefaultListCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public int getHorizontalAlignment() {
				return CENTER;
			}
		});
		contentPane.add(listState);
		listHostName.setBackground(Color.DARK_GRAY);
		listHostName.setForeground(SystemColor.menu);

		listHostName.setBounds(115, 28, 159, 150);
		listHostName.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listHostName.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		listHostName.setCellRenderer(new DefaultListCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public int getHorizontalAlignment() {
				return CENTER;
			}
		});
		contentPane.add(listHostName);

		// ---label-----------------------------------
		JLabel lblAdresIp = new JLabel("Adres IP");
		lblAdresIp.setForeground(SystemColor.inactiveCaptionBorder);
		lblAdresIp.setHorizontalAlignment(SwingConstants.CENTER);
		lblAdresIp.setBounds(10, 11, 100, 14);
		contentPane.add(lblAdresIp);

		JLabel lblNazwa = new JLabel("Nazwa");
		lblNazwa.setForeground(SystemColor.inactiveCaptionBorder);
		lblNazwa.setHorizontalAlignment(SwingConstants.CENTER);
		lblNazwa.setBounds(115, 11, 159, 14);
		contentPane.add(lblNazwa);

		JLabel lblStatus = new JLabel("Status");
		lblStatus.setForeground(SystemColor.inactiveCaptionBorder);
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
		lblStatus.setBounds(279, 11, 40, 14);
		contentPane.add(lblStatus);

		// ---suwak-------------------------------------

		AdjustmentListener adjustmentListener = new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent adjustmentEvent) {
				System.out.println("Adjusted: " + adjustmentEvent.getValue());
			}
		};

		JScrollBar scrollBar = new JScrollBar(Adjustable.VERTICAL);
		scrollBar.setBounds(324, 28, 11, 150);
		scrollBar.addAdjustmentListener(adjustmentListener);
		contentPane.add(scrollBar);

		// listIP.setAlignmentX(25);

		listIP.setAutoscrolls(true);
		listIP.setVisibleRowCount((int) listIP.getPreferredScrollableViewportSize().getHeight());
		// listIP.set

		// ustawienie tla wyboru
		// listIP.setSelectionBackground(listIP.getBackground());

		// ---buttony-----------------------------------
		JButton btnRefresh = new JButton("Od\u015Bwie\u017C");
		btnRefresh.setBackground(SystemColor.inactiveCaptionText);
		btnRefresh.setForeground(SystemColor.menu);
		btnRefresh.setBounds(340, 81, 100, 25);
		contentPane.add(btnRefresh);
		btnRefresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});

		JToggleButton btnBlokuj = new JToggleButton("Blokuj");
		btnBlokuj.setBackground(SystemColor.inactiveCaptionText);
		btnBlokuj.setForeground(SystemColor.menu);
		btnBlokuj.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnBlokuj.setBounds(340, 28, 100, 50);
		contentPane.add(btnBlokuj);

		btnBlokuj.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (listState.getSelectedValue().equals("ON")) {
					startBlockingThread();

					ListModelState.setElementAt("OFF", listState.getSelectedIndex());
					System.out.println("Zaczynam blokowac " + listIP.getSelectedValue());
				}

				else if (listState.getSelectedValue().equals("OFF")) {
					stopBlockingThread(listIP.getSelectedIndex());

					ListModelState.setElementAt("ON", listState.getSelectedIndex());
					System.out.println("Odblokowalem " + listIP.getSelectedValue());
				}
			}
		});

		// ---listenery do list----------------------
		listIP.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				listHostName.setSelectedIndex(listIP.getSelectedIndex());
				listState.setSelectedIndex(listIP.getSelectedIndex());

				if(listIP.getSelectedIndex() > 0)
				btnBlokuj.setSelected(checkState(listIP.getSelectedIndex()));
			}
		});
		listHostName.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				listIP.setSelectedIndex(listHostName.getSelectedIndex());
			}
		});
		listState.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				listIP.setSelectedIndex(listState.getSelectedIndex());
			}
		});
	}

	// metoda zatrzymujaca i usuwajaca watek
	@SuppressWarnings("deprecation")
	private void stopBlockingThread(int index) {
		Thread thread = threads.get(index);
		thread.stop();
		thread.interrupt();

		threads.set(index, null);
	}

	// metoda startujaca watek
	private void startBlockingThread() {
		BlockingThread t = new BlockingThread(listIP.getSelectedValue(), net);
		Thread thread = new Thread(t);
		thread.start();

		threads.set(listIP.getSelectedIndex(), thread);
	}

	// sprawdza czy w state jest ON czy OFF i na tej podstawie ustala dostepnosc guzika
	public boolean checkState(int index) {
		Thread temp = null;
		try {
			temp = threads.get(index);

		} catch (Exception e) {
			System.out.println("Exception-Arp_atack");
			e.printStackTrace();
		}

		if (temp != null)
			// if (temp.equals("OFF"))
			return true;
		else
			return false;
	}

	public void refresh() {
		// wyczyszczenie listy statusow
		ListModelState.clear();
		
		net = new Network();

		// wyczyszczenie listy i zatrzymanie watkow
		int i;
		for (i = 0; i < threads.size(); i++) {
			if (threads.get(i) != null)
				stopBlockingThread(i);
		}

		initList();

		System.out.println("odswiezono adresy IP");
	}

	public JList<String> getListIP() {
		return listIP;
	}

	public JList<String> getListState() {
		return listState;
	}

	public JList<String> getListHostName() {
		return listHostName;
	}

	public Network getNetwork() {
		return net;
	}
}

/*
 * odwiezac czale network towrzac go od nowa (moze to this.net zamiast net w refersh?)
 * konczenie watkow nie poprzez stop(), a zmiane parametru do ktorego maja dostep, aby same zauwazyly ze maja sie zakonczyc
 * 
 * scrollbar do listy
 * musze przeszukac siec recznie bo dopoki nie wysle czegokolwiek do kazdego to arp bedzie niepelny. Np nie znajduje tabletu bo nic z nim nie wymienialem danych.
 * trzeba przejsc po calym zakresie sieci i wysylac pingi albo moze cos szybszego?
 * ogarnac blokowanie tabletow i telefonow
 * 
 */
