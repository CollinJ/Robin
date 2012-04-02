package com.Robin.proto;

import java.nio.ByteBuffer;

import android.util.Log;

public class PendingBuffer {
	public static final int MAX_MESSAGE_SIZE = 600;
	private boolean pending;
	private int remaining = 0;
	private int expectedSize = 0;
	private boolean sizeIsSplit = false;
	public ByteBuffer data;

	public PendingBuffer() {
		this.data = ByteBuffer.allocate(MAX_MESSAGE_SIZE);
		pending = false;
	}

	public void reset() {
		pending = false;
		data.clear();
	}

	public int numToSend() {
		return remaining;
	}

	public byte[] put(byte[] b) {
		int size = b.length;
		if (pending) // Have a half message already in our buffer
		{

			data.put(b);
			if (sizeIsSplit) {
				int oldP = data.position();
				data.position(0);
				expectedSize = data.getInt();
				data.position(oldP);
				sizeIsSplit = false;
				remaining = expectedSize - size + remaining;
			}
			if (remaining == size) {
				remaining = 0;
				pending = false;
				Log.d("Remaining", "" + remaining);
				return PendingBuffer.arraySlice(data.array(), 0,
						expectedSize + 4);
			}
			Log.d("Remaining", "" + remaining);
			return null;

		} else {
			data.clear();
			if (size >= 4) {
				data.put(b);
				int oldP = data.position();
				data.position(0);
				expectedSize = data.getInt();
				data.position(oldP);
				remaining = expectedSize - size + 4;
			} else {
				sizeIsSplit = true;
				data.put(b);
				remaining = 4 - b.length; // Here remaining is used to denote
											// the number of bytes needed to get
											// the expected size
			}
			pending = true;
		}
		Log.d("Remaining", "" + remaining);
		return null;
	}

	public boolean isPending() {
		return pending;
	}

	public void setPending(boolean newState) {
		pending = newState;
	}

	public static byte[] arraySlice(byte[] array, int start, int end) {
		byte[] b = new byte[end - start];
		for (int i = start; i < end; i++) {
			b[i - start] = array[i];
		}
		return b;
	}
}
