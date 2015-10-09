package icqi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

public class IClient {

	private JFrame frmIcqi;
	private JPanel panel;
	private JPanel panel_3;
	private JEditorPane t_input;
	private JTextField t_SIP;
	private JTextField t_SPort;
	private JTextField t_ID;
	private JTextField t_upload;
	private JTextField t_CIP;
	private JTextArea t_message;
	private JComboBox<String> c_online;
	private JComboBox<String> c_file;
	private JLabel l_message;
	private JLabel l_tip;
	private JProgressBar p_upload;
	private JFileChooser jfc;
	private InetAddress cia;
	private InetAddress sia;
	private String uuid;
	private DatagramSocket socket_udp;
	private String sip = "127.0.0.1";
	private int sport = 4040;
	private Order order;
	private String path;
	private File fdir;
	private File fhdir;
	private File fhis;
	private File fdown;
	private FileOutputStream fosh;
	private OutputStreamWriter foswh;
	private BufferedWriter fbrh;
	private Boolean login = false;
	private Map<String, String> names = new HashMap<String, String>();
	private int refresh;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					IClient window = new IClient();
					window.frmIcqi.setVisible(true);
				} catch (Exception e) {
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public IClient() {
		initialize();
		try {
			cia = InetAddress.getLocalHost();
			sia = Inet4Address.getByName(sip);
		} catch (UnknownHostException e) {
			l_tip.setText("未知网络地址");
			l_tip.setVisible(true);
		}
		t_CIP.setText(cia.getHostAddress());
		t_SIP.setText(sip);
		t_SPort.setText(String.valueOf(sport));
		t_ID.setText(randomname());
		uuid = UUID.randomUUID().toString();
		initpath();
		initudp();
		new Thread(new Listener()).start();
		new Thread(new Loginning()).start();

	}

	private String randomname() {
		StringBuffer sb = new StringBuffer();
		char[] set = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
				'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
				'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
				'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
				'V', 'W', 'X', 'Y', 'Z' };
		for (int i = 0; i < 6; i++) {
			Random r = new Random();
			sb.append(set[r.nextInt(set.length - 1)]);
		}
		return sb.toString();
	}

	private void initpath() {
		path = System.getProperty("java.class.path")
				+ System.getProperty("file.separator") + "data";
		fdir = new File(path);
		SimpleDateFormat s = (SimpleDateFormat) SimpleDateFormat
				.getDateInstance(DateFormat.MEDIUM);
		fhdir = new File(path + System.getProperty("file.separator")
				+ "history");
		fhis = new File(fhdir, t_ID.getText() + "-" + s.format(new Date())
				+ ".txt");
		fdown = new File(path + System.getProperty("file.separator")
				+ "download");
		if (!fdir.exists())
			fdir.mkdirs();
		if (!fhdir.exists())
			fhdir.mkdirs();
		if (!fdown.exists())
			fdown.mkdirs();
		try {
			fosh = new FileOutputStream(fhis);
			foswh = new OutputStreamWriter(fosh, "utf-8");
			fbrh = new BufferedWriter(foswh);
		} catch (FileNotFoundException e) {
			l_tip.setText("磁盘无法访问");
			l_tip.setVisible(true);
		} catch (UnsupportedEncodingException e) {
		}
	}

	private void initudp() {
		try {
			socket_udp = new DatagramSocket();
		} catch (IOException e) {
			l_tip.setText("网络未连接");
			l_tip.setVisible(true);
		}
	}

	private class Loginning implements Runnable {

		@Override
		public void run() {
			while (true) {
				byte[] blogin = (order.LOGIN.toString() + uuid + "&ID=" + t_ID
						.getText()).getBytes();
				DatagramPacket dp = new DatagramPacket(blogin, blogin.length,
						sia, sport);
				try {
					socket_udp.send(dp);
				} catch (IOException e) {
					new Thread(new Tip("发送失败")).start();
				}
				try {
					Thread.currentThread().sleep(1000l);
				} catch (InterruptedException e) {
					continue;
				}
				if (login == true) {
					new Thread(new Tip("网络已连接")).start();
					c_online.setSelectedItem("All");
					break;
				}
				new Thread(new Tip("网络未连接")).start();
			}
		}

	}

	private class Listener implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					byte[] buffer = new byte[2048];
					DatagramPacket dp = new DatagramPacket(buffer,
							buffer.length);
					socket_udp.receive(dp);
					Thread resovler = new Thread(new Resovler(dp));
					resovler.setDaemon(true);
					resovler.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private class Resovler implements Runnable {
		DatagramPacket rdp;

		public Resovler(DatagramPacket dp) {
			this.rdp = dp;
		}

		@Override
		public void run() {
			resolve(rdp);
		}

		private void resolve(DatagramPacket dp) {
			String s = new String(dp.getData()).trim();
			handlemapping(s);
		}

		private Enum<Order> selectorder(String s) {
			if (s.startsWith(order.MESSAGE.toString()))
				return order.MESSAGE;
			if (s.startsWith(order.LOGIN.toString()))
				return order.LOGIN;
			if (s.startsWith(order.OFFLINE.toString()))
				return order.OFFLINE;
			if (s.startsWith(order.FILE_UPLOAD.toString()))
				return order.FILE_UPLOAD;
			if (s.startsWith(order.FILE_DOWNLOAD.toString()))
				return order.FILE_DOWNLOAD;
			if (s.startsWith(order.RENAME.toString()))
				return order.RENAME;
			return null;
		}

		private void handlemapping(String s) {
			int o = selectorder(s).ordinal();
			Enum<Order> t = selectorder(s);
			String m = cuthead(s, t);
			switch (o) {
			case 0:
				handle_login(m);
				break;
			case 1:
				handle_offline(m);
				break;
			case 2:
				handle_upload(m);
				break;
			case 3:
				handle_download(m);
				break;
			case 4:
				handle_message(m);
				break;
			case 5:
				handle_rename(m);
				break;

			default:
				break;
			}
		}

		private String cuthead(String s, Enum<Order> t) {
			return s.substring(t.toString().length());
		}

		private void handle_message(String m) {
			appendtext(t_message, m);
		}

		private void handle_download(String m) {
			int ps = Integer.parseInt(m.substring(m.lastIndexOf("&") + 1));
			int port = Integer.parseInt(m.substring(m.indexOf("&") + 1,
					m.lastIndexOf("&")));
			p_upload.setMaximum(ps);
			p_upload.setVisible(true);
			try {
				Socket socket_tcp = new Socket(sia, port);
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(jfc.getSelectedFile()));
				InputStream is = socket_tcp.getInputStream();
				byte[] buffer = new byte[2048];
				int len;
				int count = 0;
				while ((len = is.read(buffer)) != -1) {
					bos.write(buffer, 0, len);
					if (count < ps)
						p_upload.setValue(++count);
				}
				p_upload.setVisible(false);
				new Thread(new Tip("下载完成")).start();
				appendtext(t_message, "tips: 文件："
						+ jfc.getSelectedFile().getName() + " 下载成功");
				bos.flush();
				bos.close();
				socket_tcp.shutdownOutput();
				is.close();
				socket_tcp.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void handle_upload(String m) {
			c_file.addItem(m);
			panel.updateUI();
		}

		synchronized private void handle_offline(String m) {
			c_online.removeItem(names.get(m));
			names.remove(m);
			panel.updateUI();
		}

		synchronized private void handle_login(String m) {
			if (login == false) {
				login = true;
				c_online.addItem("All");
				c_online.setSelectedItem("All");
			}
			String[] set = m.split("&");
			if (!m.equals("") && !names.containsKey(set[0])) {
				names.put(set[0], set[1].substring(3));
				c_online.addItem(set[1].substring(3));
			}
		}

		synchronized private void handle_rename(String m) {
			String uid = getuuid(m);
			if (names.containsKey(uid)) {
				names.remove(uid);
				c_online.removeItem(names.get(uid));
				names.put(uid, m.substring(uid.length() + 1));
				c_online.addItem(m.substring(uid.length() + 1));
			}
			panel.updateUI();
		}

		private String getuuid(String s) {
			Pattern p = Pattern.compile("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}");
			Matcher m = p.matcher(s);
			while (m.find()) {
				return m.group(0);
			}
			return null;
		}

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmIcqi = new JFrame();
		frmIcqi.setIconImage(Toolkit.getDefaultToolkit().getImage(
				System.getProperty("java.class.path") + "\\icqi.png"));
		frmIcqi.setTitle("ICQI");
		frmIcqi.setResizable(false);
		frmIcqi.setBounds(100, 100, 791, 445);
		frmIcqi.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frmIcqi.getContentPane().setLayout(null);
		frmIcqi.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				try {
					byte[] off = (order.OFFLINE.toString() + uuid).getBytes();
					DatagramPacket dp = new DatagramPacket(off, off.length,
							sia, sport);
					socket_udp.send(dp);
					fbrh.flush();
					finalize();
					System.exit(0);
				} catch (Throwable e1) {
				}
				super.windowClosing(e);
			}

		});

		panel = new JPanel();
		panel.setBounds(0, 0, 800, 270);
		frmIcqi.getContentPane().add(panel);
		panel.setLayout(null);

		t_message = new JTextArea();
		t_message.setEditable(false);
		t_message.setWrapStyleWord(true);
		t_message.setLineWrap(true);
		t_message.setBounds(20, 23, 674, 247);
		panel.add(t_message);

		c_online = new JComboBox<String>();
		c_online.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (c_online.getSelectedItem() != null
						&& c_online.getSelectedItem().equals(t_ID.getText()))
					c_online.setSelectedItem("All");
				l_message.setText("Message(" + c_online.getSelectedItem() + ")");
			}
		});
		c_online.setBounds(704, 46, 67, 21);
		panel.add(c_online);

		JLabel label = new JLabel("\u5728\u7EBF\u597D\u53CB");
		label.setBounds(704, 21, 54, 15);
		panel.add(label);

		l_message = new JLabel("Message");
		l_message.setBounds(20, 0, 191, 25);
		panel.add(l_message);

		t_CIP = new JTextField();
		t_CIP.setEditable(false);
		t_CIP.setBounds(578, 1, 116, 23);
		panel.add(t_CIP);
		t_CIP.setColumns(10);

		JLabel l_CIP = new JLabel("你的IP地址");
		l_CIP.setBounds(513, 2, 67, 20);
		panel.add(l_CIP);

		JLabel label_1 = new JLabel("\u4E0B\u8F7D\u6587\u4EF6");
		label_1.setBounds(704, 77, 54, 15);
		panel.add(label_1);

		c_file = new JComboBox<String>();
		c_file.setBounds(704, 102, 67, 21);
		panel.add(c_file);

		JButton b_download = new JButton("\u4E0B\u8F7D");
		b_download.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jfc == null)
					jfc = new JFileChooser();
				if (c_file.getSelectedItem() != null) {
					File file = new File(fdown, (String) c_file
							.getSelectedItem());
					jfc.setSelectedFile(file);
					int re = jfc.showSaveDialog(panel);
					if (re == jfc.APPROVE_OPTION) {
						if (jfc.getSelectedFile().exists()) {
							int re2 = JOptionPane.showConfirmDialog(null,
									"是否覆盖当前文件？", "保存",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE);
							if (re2 == JOptionPane.YES_OPTION)
								requestdownload();
						} else
							requestdownload();
					}
				}
			}

			private void requestdownload() {
				Random r = new Random();
				int port;
				while (true) {
					port = r.nextInt(30000);
					if (port > 10000) {
						break;
					}
				}
				udpsend(order.FILE_DOWNLOAD + uuid + "&"
						+ (String) c_file.getSelectedItem() + "&" + port);
			}
		});
		b_download.setBounds(704, 133, 67, 21);
		panel.add(b_download);

		JButton b_history = new JButton("\u8BB0\u5F55");
		b_history.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Runtime rt = Runtime.getRuntime();
				try {
					rt.exec("notepad.exe " + fhis.getAbsolutePath());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		b_history.setBounds(704, 179, 67, 21);
		panel.add(b_history);

		JButton b_rename = new JButton("\u6539\u540D");
		b_rename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!t_ID.isEditable())
					t_ID.setEditable(true);
				else {
					if (validateID()) {
						t_ID.setText(t_ID.getText().trim());
						rename();
						t_ID.setEditable(false);
					}
				}
			}

			private void rename() {
				udpsend(order.RENAME + uuid + "&" + t_ID.getText());
				SimpleDateFormat s = (SimpleDateFormat) SimpleDateFormat
						.getDateInstance(DateFormat.MEDIUM);
				fhis.renameTo(new File(fhdir, t_ID.getText() + "-"
						+ s.format(new Date()) + ".txt"));
			}

			private boolean validateID() {
				if (!t_ID.getText().trim().matches("\\w+")) {
					new Thread(new Tip("ID格式不正确")).start();
					return false;
				} else if (t_ID.getText().equals(names.get(uuid))) {
					if (t_ID.isEditable())
						t_ID.setEditable(false);
					return false;
				} else if (names.containsValue(t_ID.getText())) {
					appendtext(t_message, "tips:该名字已存在");
					return false;
				}
				return true;
			}
		});
		b_rename.setBounds(704, 224, 67, 23);
		panel.add(b_rename);

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(0, 269, 775, 138);
		frmIcqi.getContentPane().add(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel l_SIP = new JLabel("IP");
		panel_2.add(l_SIP);

		t_SIP = new JTextField();
		t_SIP.setEditable(false);
		panel_2.add(t_SIP);
		t_SIP.setColumns(10);

		JLabel l_SPort = new JLabel("Port");
		panel_2.add(l_SPort);

		t_SPort = new JTextField();
		t_SPort.setEditable(false);
		panel_2.add(t_SPort);
		t_SPort.setColumns(10);

		JLabel l_ID = new JLabel("ID");
		panel_2.add(l_ID);

		t_ID = new JTextField();
		t_ID.setEditable(false);
		panel_2.add(t_ID);
		t_ID.setColumns(10);

		JButton b_upload = new JButton("\u53D1\u9001\u6587\u4EF6");
		b_upload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jfc = new JFileChooser();
				jfc.setDialogTitle("选择上传文件");
				jfc.showOpenDialog(panel);
				if (jfc.getSelectedFile() != null)
					t_upload.setText(jfc.getSelectedFile().getName());
			}
		});
		panel_2.add(b_upload);

		t_upload = new JTextField();
		panel_2.add(t_upload);
		t_upload.setColumns(10);

		panel_3 = new JPanel();
		panel_1.add(panel_3, BorderLayout.CENTER);
		panel_3.setLayout(null);

		t_input = new JEditorPane();
		t_input.setBounds(22, 0, 672, 84);
		panel_3.add(t_input);

		p_upload = new JProgressBar();
		p_upload.setMaximum(100);
		p_upload.setMinimum(0);
		p_upload.setBounds(450, 90, 146, 14);
		p_upload.setValue(0);
		p_upload.setStringPainted(true);
		p_upload.setBackground(Color.WHITE);
		p_upload.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (p_upload.getMaximum() != 0) {
					int p = 100 * p_upload.getValue() / p_upload.getMaximum();
					l_tip.setText("已完成进度：" + p + "%");
					panel_3.updateUI();
				}
			}
		});

		panel_3.add(p_upload);

		l_tip = new JLabel("tip");
		l_tip.setVisible(false);
		l_tip.setBounds(10, 82, 131, 22);
		panel_3.add(l_tip);

		JButton b_UDP = new JButton("send");
		b_UDP.setBounds(694, -1, 81, 85);
		panel_3.add(b_UDP);
		b_UDP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (getreciever() != null) {
					if (getreciever().equals(uuid)) {
						appendtext(t_message, "不能只对自己发信息");
					} else if (!t_input.getText().equals("")) {
						String re = getreciever();
						udpsend(order.MESSAGE.toString() + uuid + "&" + re
								+ "&" + t_input.getText());
						t_input.setText("");
					}
				}
				if (jfc != null && jfc.getSelectedFile() != null
						&& !getreciever().equals(uuid)) {
					String re = getreciever();
					sendfile(re);
				}
			}

			private void sendfile(String re) {
				Random r = new Random();
				int port;
				while (true) {
					port = r.nextInt(65534);
					if (port > 10000) {
						break;
					}
				}
				File file = jfc.getSelectedFile();
				byte[] buffer = new byte[2048];
				int ps = (int) (file.length() / buffer.length);
				udpsend(order.FILE_UPLOAD.toString() + uuid + "&" + re + "&"
						+ jfc.getSelectedFile().getName() + "&" + port + "&"
						+ ps);
				Thread t = Thread.currentThread();
				try {
					t.sleep(100l);
				} catch (InterruptedException e1) {
				}
				try {
					Socket socket_tcp = new Socket(sia, port);
					BufferedInputStream bis = new BufferedInputStream(
							new FileInputStream(file));
					OutputStream os = socket_tcp.getOutputStream();
					p_upload.setMaximum(ps);
					p_upload.setVisible(true);
					int len;
					int count = 0;
					while ((len = bis.read(buffer)) != -1) {
						os.write(buffer, 0, len);
						if (count < ps)
							p_upload.setValue(++count);
					}
					p_upload.setVisible(false);
					new Thread(new Tip("上传完成")).start();
					bis.close();
					os.flush();
					socket_tcp.shutdownOutput();
					os.close();
					socket_tcp.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
				jfc.setSelectedFile(null);
				t_upload.setText("");
			}

		});
		p_upload.setVisible(false);
	}

	private String getreciever() {
		if (c_online.getSelectedItem() == null)
			return null;
		if (c_online.getSelectedItem().equals("All"))
			return "";
		Iterator<String> it = names.keySet().iterator();
		String key = null;
		while (it.hasNext()) {
			key = (String) it.next();
			if (names.get(key).equals(c_online.getSelectedItem())) {
				break;
			}
		}
		return key;
	}

	private void appendtext(JTextComponent t, String s) {
		t.setText(t.getText().equals("") ? t.getText().concat(s) : t.getText()
				.concat("\n" + s));
		if (t == t_message) {
			refresh++;
			if (refresh % 14 == 0) {
				String his = t_message.getText();
				try {
					char[] hcs = his.toCharArray();
					for (char c : hcs) {
						if (c == '\n')
							fbrh.newLine();
						fbrh.write(c);
					}
					fbrh.flush();
					t_message.setText("历史消息已保存到聊天记录");
				} catch (IOException e) {
					t_message.setText("历史消息保存到聊天记录失败");
				}
			}
		}
	}

	private void udpsend(String text) {

		byte[] bs = text.getBytes();
		DatagramPacket dp = new DatagramPacket(bs, bs.length, sia, sport);
		try {
			socket_udp.send(dp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private class Tip implements Runnable{
		String tip=null;
		
		public Tip(String t) {
			tip=t;
		}
		
		@Override
		public void run() {
			l_tip.setText(tip);
			l_tip.setVisible(true);
			try {
				Thread.currentThread().sleep(3000l);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			l_tip.setText("");
			l_tip.setVisible(false);
		}
	}
}
