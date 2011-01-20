package cz.geokuk.util.file;

import java.io.*;



public class TPumpInputStreamToOutputStream extends Thread {

  private InputStream iRdr;
  private OutputStream iWrt;
  private boolean iCloseOutputStream = false;
  private boolean iCloseInputStream = true;
  private int iBufferSize = 1024;
  private Object iNotificationReceiver = this;
  private IOException iException = null; // bude obsahovat výjimku, pokud nějaká bude
  private boolean iIsFinished = false;

  public TPumpInputStreamToOutputStream(InputStream aIstm, OutputStream aOstm) {
    iRdr = aIstm;
    iWrt = aOstm;
  }


  public void run()  {
    try {
      byte[] buf = new byte[iBufferSize];

      for (;;) {
        int len = iRdr.read(buf);
        if (len <= 0) break; // dočteno
        iWrt.write(buf, 0, len);
      }
      iWrt.flush();
      if (iCloseInputStream) iRdr.close();
      if (iCloseOutputStream) iWrt.close();
      iIsFinished = true;
    } catch (IOException ex) {
      synchronized (this) {
        iException = ex;
        iIsFinished = true;
      }
      ex.printStackTrace();
      throw new RuntimeException(ex);
    }

    if (iNotificationReceiver != null) {
      synchronized (iNotificationReceiver) {
        iNotificationReceiver.notifyAll();
      }
    }
  }


  public synchronized IOException getException() {
    return iException;
  }

  public synchronized boolean isFinished() { return iIsFinished; }



  public boolean isCloseOutputStream() {
    return iCloseOutputStream;
  }
  public void setCloseOutputStream(boolean closeOutputStream) {
    this.iCloseOutputStream = closeOutputStream;
  }
  public void setCloseInputStream(boolean closeInputStream) {
    this.iCloseInputStream = closeInputStream;
  }
  public boolean isCloseInputStream() {
    return iCloseInputStream;
  }
  public void setBufferSize(int bufferSize) {
    if (bufferSize < 1) bufferSize = 1;
    this.iBufferSize = bufferSize;
  }
  public int getBufferSize() {
    return iBufferSize;
  }
  public void setNotificationReceiver(Object notificationReceiver) {
    this.iNotificationReceiver = notificationReceiver;
  }
  public Object getNotificationReceiver() {
    return iNotificationReceiver;
  }


}