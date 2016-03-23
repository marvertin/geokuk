package cz.geokuk.core.napoveda;

class ZpravaUzivateli {
	final int msgnum;
	final String text;

	public ZpravaUzivateli(int msgnum, String text) {
		this.msgnum = msgnum;
		this.text = text;
	}

	@Override
	public String toString() {
		return "ZpravaUzivateli [msgnum=" + msgnum + ", text=" + text + "]";
	}
}