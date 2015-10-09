package icqi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IServer {
	private DatagramSocket ds = null;
	private Map<String, SocketAddress> clients = new HashMap<String, SocketAddress>();
	private Map<String, String> names = new HashMap<String, String>();
	private Order order;
	private int sport = 4040;
	private String path;

	public static void main(String[] args) {
		new IServer().service();
	}

	private void service() {

		try {
			ds = new DatagramSocket(sport);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		while (true) {
			try {
				byte[] buffer = new byte[2048];
				DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
				ds.receive(dp);
				String s = new String(dp.getData()).trim();
				String uuid = getuuid(s);
				if (!clients.containsKey(uuid)) {
					SocketAddress csa = dp.getSocketAddress();
					clients.put(uuid, csa);
				}
				Thread resolver = new Thread(new Resolver(new String(
						dp.getData()).trim()));
				resolver.setDaemon(true);
				resolver.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private class Resolver implements Runnable {
		private String rs = null;

		public Resolver(String s) {
			this.rs = s;
		}

		@Override
		public void run() {
			resolve(rs);
		}

		private void resolve(String s) {
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

		private void handle_message(String m) {
			String uuid = getuuid(m);
			String m2 = m.substring(uuid.length() + 1);
			String uuid2 = getuuid(m2);
			if (uuid2 != null) {
				String message = names.get(uuid) + ":"
						+ m2.substring(uuid.length() + 1);
				noticeone(order.MESSAGE.toString() + message, uuid2);
				noticeone(order.MESSAGE.toString() + message, uuid);
			} else {
				String message = names.get(uuid) + ":" + m2.substring(1);
				noticeall(order.MESSAGE.toString() + message);
			}
		}

		private void handle_offline(String m) {
			String u = getuuid(m);
			String id = names.get(u);
			Iterator<String> it = names.keySet().iterator();
			String bs = order.OFFLINE.toString() + u;
			names.remove(u);
			clients.remove(u);
			noticeall(bs);
			noticeall(order.MESSAGE.toString() + id + "已下线");
		}

		private void handle_rename(String m) {
			String uuid = getuuid(m);
			String name = names.get(uuid);
			if (names.containsKey(uuid)) {
				names.remove(uuid);
				names.put(uuid, m.substring(uuid.length() + 1));
			}
			noticeall(order.MESSAGE.toString() + name + "已改名为"
					+ names.get(uuid));
			noticeall(order.RENAME.toString() + uuid + "&" + names.get(uuid));

		}

		private void handle_login(String m) {
			String u = getuuid(m);
			String id = getid(m);
			names.put(u, id);
			Iterator<String> it = names.keySet().iterator();
			while (it.hasNext()) {
				String s = (String) it.next();
				if (!s.equals(u)) {
					noticeone(order.LOGIN.toString() + s + "&ID=" + names.get(s),u);
					noticeone(order.MESSAGE.toString() + names.get(s) + "已登录",u);
				} else
					noticeone(order.LOGIN.toString(), u);
			}
			noticeallexcept(order.LOGIN.toString() + u + "&ID=" + id, u);
			noticeall(order.MESSAGE.toString() + id + "已登录");
			if (path == null)
				path = System.getProperty("java.class.path")
						+ System.getProperty("file.separator") + "serverdata";
			File filedir = new File(path);
			if(!filedir.exists()) filedir.mkdirs();
			File[] files=filedir.listFiles();
			for (File file : files) {
				noticeone(order.FILE_UPLOAD.toString() + file.getName(), u);
			}
		}

		private void handle_download(String m) {
			String uuid = getuuid(m);
			String filename = m
					.substring(uuid.length() + 1, m.lastIndexOf("&"));
			int port = Integer.parseInt(m.substring(m.lastIndexOf("&") + 1));
			File filedir = new File(path);
			File file = new File(filedir, filename);
			if (file.exists()) {
				ServerSocket ss;
				try {
					ss = new ServerSocket(port);
					Socket s = null;
					byte[] buffer = new byte[2048];
					int ps = (int) (file.length() / buffer.length);
					noticeone(order.FILE_DOWNLOAD.toString() + "&" + port + "&"
							+ ps, uuid);
					s = ss.accept();
					BufferedInputStream bis = new BufferedInputStream(
							new FileInputStream(file));
					OutputStream os = s.getOutputStream();
					int len;
					while ((len = bis.read(buffer)) != -1) {
						os.write(buffer, 0, len);
					}
					bis.close();
					os.flush();
					s.shutdownOutput();
					s.close();
					ss.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				noticeone(order.MESSAGE.toString() + file.getName() + "不存在",
						uuid);
			}
		}

		private void handle_upload(String m) {
			String uuid = getuuid(m);
			String m2 = m.substring(uuid.length() + 1);
			String uuid2 = getuuid(m2);
			String fname = null;
			int port;
			int ps;
			if (uuid2 != null) {
				String m3=m2.substring(0, m2.lastIndexOf("&"));
				fname = m2.substring(uuid.length() + 1, m3.lastIndexOf("&"));
				port=Integer.parseInt(m3.substring(m3.lastIndexOf("&") + 1));
			} else {
				String m3=m2.substring(0, m2.lastIndexOf("&"));
				fname = m2.substring(1, m3.lastIndexOf("&"));
				port=Integer.parseInt(m3.substring(m3.lastIndexOf("&") + 1));
			}
			File filedir = new File(path);
			File file = new File(filedir, fname);
			while (file.exists()) {
				Random r = new Random();
				int j = fname.lastIndexOf(".");
				file = new File(filedir, fname.substring(0, j) + "_"
						+ r.nextInt(100) + fname.substring(j));
			}
			try {
				ServerSocket ss = new ServerSocket(port);
				Socket s = ss.accept();
				if (!filedir.exists())
					filedir.mkdirs();

				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(file));
				InputStream is = s.getInputStream();
				byte[] buffer = new byte[2048];
				int len;
				while ((len = is.read(buffer)) != -1) {
					bos.write(buffer, 0, len);
				}
				bos.flush();
				bos.close();
				s.shutdownInput();
				s.close();
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (uuid2 != null) {
				noticeone(order.MESSAGE.toString() + names.get(uuid)
						+ "给你发送了文件：" + file.getName(), uuid2);
				noticeone(order.FILE_UPLOAD.toString() + file.getName(), uuid2);
				noticeone(order.MESSAGE.toString() + "您成功发送文件：" + file.getName(),uuid);
				noticeone(order.FILE_UPLOAD.toString() + file.getName(), uuid);
			} else {
				noticeall(order.MESSAGE.toString() + names.get(uuid) + ": 文件："
						+ file.getName());
				noticeall(order.FILE_UPLOAD.toString() + file.getName());
			}
		}

		private String getid(String s) {
			Pattern p = Pattern.compile("ID=\\w+");
			Matcher m = p.matcher(s);
			while (m.find()) {
				return m.group(0).substring(3);
			}
			return null;
		}

		private String cuthead(String s, Enum<Order> t) {
			return s.substring(t.toString().length());
		}

	}

	private String getuuid(String s) {
		Pattern p = Pattern.compile("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}");
		Matcher m = p.matcher(s);
		while (m.find()) {
			return m.group(0);
		}
		return null;
	}

	private void noticeall(String s) {
		Iterator<String> it = clients.keySet().iterator();
		while (it.hasNext()) {
			String c = (String) it.next();
			byte[] me = s.getBytes();
			try {
				DatagramPacket dp = new DatagramPacket(me, me.length,
						clients.get(c));
				ds.send(dp);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void noticeallexcept(String s, String uuid) {
		Iterator<String> it = clients.keySet().iterator();
		while (it.hasNext()) {
			String c = (String) it.next();
			if (!c.equals(uuid)) {
				byte[] me = s.getBytes();
				try {
					DatagramPacket dp = new DatagramPacket(me, me.length,
							clients.get(c));
					ds.send(dp);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void noticeone(String s, String uuid) {
		byte[] me = s.getBytes();
		try {
			DatagramPacket dp = new DatagramPacket(me, me.length,
					clients.get(uuid));
			ds.send(dp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Thread.currentThread().sleep(60l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
