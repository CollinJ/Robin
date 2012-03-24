package com.Robin.proto;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MessageManager extends Thread{
	public static final int SERVER_PORT = 5500;
	
	private ArrayList<SocketChannel> socketList = new ArrayList<SocketChannel>();
	public Handler mHandler;
	public Button button;
	RobinActivity mParent;
	SelectorIOManager selectionManager;
	public List<ChangeRequest> changeRequests = new LinkedList<ChangeRequest>();
	Random rand = new Random();
	ByteBuffer pending = ByteBuffer.allocate(8192);

	  // Maps a SocketChannel to a list of ByteBuffer instances
	public Map<SocketChannel, ArrayList<ByteBuffer>> pendingData = new HashMap<SocketChannel, ArrayList<ByteBuffer>>();
	public MessageManager(RobinActivity parent) {
		mParent = parent;
	}
	
	@Override
	public void run() {
		
		button = (Button)mParent.findViewById(R.id.sendTCP);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				UserMessage m = new UserMessage("Test");
				sendIPToAllTCP(m.getByteArray());
//				sendIPToAllTCP(m.getByteArray());
//				sendIPToAllTCP(m.getByteArray());
//				sendIPToAllTCP(m.getByteArray());
//				sendIPToAllTCP(m.getByteArray());
//				sendIPToAllTCP(m.getByteArray());
//				sendIPToAllTCP(m.getByteArray());
			}
		});
		
		
		Looper.prepare();
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				if(msg.obj instanceof InetAddress) // UDPAnnouncementManager sent us a TCP connection request
				{
					boolean connectionExists = false;
					synchronized(socketList){
						for (SocketChannel sc : socketList)
						{
							InetAddress i = (InetAddress)msg.obj;
							if(sc.isOpen())
							{
								if(sc.socket().getInetAddress().equals(i))
								{
									connectionExists = true;
								}
							}
						}
					}
					if(connectionExists == false)
					{
						try {
							Thread.sleep(rand.nextInt(1000));
							synchronized (socketList) {
								socketList.add(initiateConnection((InetAddress)msg.obj));
							}
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
				}
				else if (msg.obj instanceof SocketChannel) // SelectorIOManager accepted a new connection
				{
					synchronized(socketList)
					{
						socketList.add((SocketChannel)msg.obj);
						
					}
				}
				else if (msg.obj instanceof String) // We have received a message from a SocketChannel in SelectorIOManager
				{
					final String s = (String)msg.obj;
					Log.d("RECEIVED", s);
					mParent.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							((TextView)mParent.findViewById(R.id.messageView)).append("TCP Message From " + s + "\n");
						}
					});
				} else if (msg.obj instanceof Integer) {
					int i = ((Integer)msg.obj).intValue();
					removeBadConnections(i);
				}
				else if (msg.obj instanceof byte[]) // We have received a message from a SocketChannel in SelectorIOManager
				{
					byte[] array = (byte[])msg.obj;
					final UserMessage m = new UserMessage(array);
					//TODO: store this in message pool
					Log.d("Message", new String(m.toString()));
					mParent.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							((TextView)mParent.findViewById(R.id.messageView)).append("TCP Message: " + m.toString() + "\n");
						}
					});
				}
				
			}
		};
		
		try {
			selectionManager = new SelectorIOManager(MessageManager.SERVER_PORT, this);
			Thread t = new Thread(selectionManager);
			t.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		Looper.loop();
	}
	
	private void sendIPToAllTCP(byte[] message) {
		synchronized (socketList) {
			for (SocketChannel s : socketList) {
				send(s, message);
				
			}
		}
		
	}
	 private SocketChannel initiateConnection(InetAddress hostAddress) throws IOException {
		    // Create a non-blocking socket channel
		    SocketChannel socketChannel = SocketChannel.open();
		    socketChannel.configureBlocking(false);
		  
		    // Kick off connection establishment
		    socketChannel.connect(new InetSocketAddress(hostAddress, MessageManager.SERVER_PORT));
		  
		    // Queue a channel registration since the caller is not the 
		    // selecting thread. As part of the registration we'll register
		    // an interest in connection events. These are raised when a channel
		    // is ready to complete connection establishment.
		    synchronized(this.changeRequests) {
		      this.changeRequests.add(new ChangeRequest(socketChannel, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
		    }
		    this.selectionManager.selector.wakeup();
		    return socketChannel;
		  }
	
	public void send(SocketChannel socket, byte[] data) {
	    synchronized (this.changeRequests) {
	      // Indicate we want the interest ops set changed
	      this.changeRequests.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
	      
	      // And queue the data we want written
	      synchronized (this.pendingData) {
	        ArrayList<ByteBuffer> queue = (ArrayList<ByteBuffer>) this.pendingData.get(socket);
	        if (queue == null) {
	          queue = new ArrayList<ByteBuffer>();
	          this.pendingData.put(socket, queue);
	        }
	        queue.add(ByteBuffer.wrap(data));
	      }
	    }
	    
	    // Finally, wake up our selecting thread so it can make the required changes
	    this.selectionManager.selector.wakeup();
	  } 
	
	private void removeBadConnections(int minSize) {
		synchronized (socketList) {
			int size = socketList.size();
			int maxRemoves = size-minSize+1;
			int n = 0;
			for(int j=0; j<size-n;j++) {
				if(n == maxRemoves)
					break;
				if(socketList.get(j).isOpen() != true) {
					for(int i=size-n-1; i>=j; i--) {
						SocketChannel sc = socketList.get(i);
						boolean open = sc.isOpen();
						if(open == true)
						{
							socketList.set(j, socketList.get(i));
							n++;
							break;
						}
						n++;
					}
					
				}
			}
			for(int i=0;i<n;i++) {
				socketList.remove(size-i-1);
			}
		}
		
	}

}
