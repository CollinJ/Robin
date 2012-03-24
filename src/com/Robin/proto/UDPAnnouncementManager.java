package com.Robin.proto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;

import android.app.Activity;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class UDPAnnouncementManager extends Thread {
	private static final String MULTICAST_ADDR = "224.1.1.1";
	private static final int DISCOVERY_PORT = 2562;

	private Activity mParent;
	private Button sendUDP;
	MulticastSocket socket;
	InetAddress sessAddr;
	MessageManager tcp;

	public UDPAnnouncementManager(MessageManager tcp, RobinActivity parent) {
		mParent = parent;
		this.tcp = tcp;
		sendUDP = (Button) mParent.findViewById(R.id.sendUDP);
		sendUDP.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					sendDiscoveryRequest(socket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				Log.d("UDP", "Announcement Sent");
			}
		});

	}

	@Override
	public void run() {
		try {
			sessAddr = InetAddress.getByName(MULTICAST_ADDR);
			socket = new MulticastSocket(DISCOVERY_PORT);
			socket.joinGroup(sessAddr);
			socket.setReuseAddress(true);

			sendDiscoveryRequest(socket);
			listenForResponses(socket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendDiscoveryRequest(DatagramSocket socket) throws IOException {
		String s = getLocalIpAddress() + "\n";
		DatagramPacket packet = new DatagramPacket(s.getBytes(),
				s.getBytes().length, sessAddr, DISCOVERY_PORT);
		socket.send(packet);
	}

	private void listenForResponses(DatagramSocket socket) throws IOException {
		byte[] buf = new byte[4096];
		try {
			while (true) {
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				Log.d("UDP", "" + packet.getAddress().toString());
				final String s = "UDP connect request from: "
						+ new String(packet.getData(), 0, packet.getLength());
				Log.d("UDP", s);
				mParent.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						((TextView) mParent.findViewById(R.id.messageView))
								.append(s);
					}
				});

				if (packet.getAddress().toString().substring(1)
						.equals(getLocalIpAddress())) {
					Log.d("ERROR", "CONNECTED TO ITSELF");
				} else {
					Message msg = Message.obtain();
					msg.obj = packet.getAddress();
					tcp.mHandler.sendMessage(msg);
				}

			}
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		}
	}

	// public static byte[] intToByteArray(int value) {
	// byte[] b = new byte[4];
	// for (int i = 0; i < 4; i++) {
	// int offset = (b.length - 1 - i) * 8;
	// b[i] = (byte) ((value >>> offset) & 0xFF);
	// }
	// return b;
	// }
	//
	// public static int byteArrayToInt(byte [] b) {
	// return (b[0] << 24)
	// + ((b[1] & 0xFF) << 16)
	// + ((b[2] & 0xFF) << 8)
	// + (b[3] & 0xFF);
	// }

	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("Socket", ex.toString());
		}
		return null;
	}

}
