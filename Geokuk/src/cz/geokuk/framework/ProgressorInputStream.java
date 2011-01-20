/*
 * @(#)ProgressMonitorInputStream.java	1.20 05/11/17
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */



package cz.geokuk.framework;



import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;



/** 
 * Monitors the progress of reading from some InputStream. This ProgressMonitor
 * is normally invoked in roughly this form:
 * <pre>
 * InputStream in = new BufferedInputStream(
 *                          new ProgressMonitorInputStream(
 *                                  parentComponent,
 *                                  "Reading " + fileName,
 *                                  new FileInputStream(fileName)));
 * </pre><p>
 * This creates a progress monitor to monitor the progress of reading
 * the input stream.  If it's taking a while, a ProgressDialog will
 * be popped up to inform the user.  If the user hits the Cancel button
 * an InterruptedIOException will be thrown on the next read.
 * All the right cleanup is done when the stream is closed.
 *
 *
 * <p>
 *
 * For further documentation and examples see
 * <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/progress.html">How to Monitor Progress</a>,
 * a section in <em>The Java Tutorial.</em>
 *
 * @see ProgressMonitor
 * @see JOptionPane
 * @author James Gosling
 * @version 1.20 11/17/05
 */
public class ProgressorInputStream extends FilterInputStream {
  private final Progressor progressor;
  private int             nread = 0;
  private int             size = 0;


  /**
   * Constructs an object to monitor the progress of an input stream.
   *
   * @param message Descriptive text to be placed in the dialog box
   *                if one is popped up.
   * @param parentComponent The component triggering the operation
   *                        being monitored.
   * @param in The input stream to be monitored.
   */
  public ProgressorInputStream(ProgressModel progressModel,
      String message,
      InputStream in) {
    super(in);
    try {
      size = in.available();
    } catch(IOException ioe) {
      size = 0;
    }
    progressor = progressModel.start(size, message);
  }


  /**
   * Overrides <code>FilterInputStream.read</code> 
   * to update the progress monitor after the read.
   */
  @Override
  public int read() throws IOException {
    int c = in.read();
    if (c >= 0) progressor.setProgress(++nread);
    return c;
  }


  /**
   * Overrides <code>FilterInputStream.read</code> 
   * to update the progress monitor after the read.
   */
  @Override
  public int read(byte b[]) throws IOException {
    int nr = in.read(b);
    if (nr > 0) progressor.setProgress(nread += nr);
    return nr;
  }


  /**
   * Overrides <code>FilterInputStream.read</code> 
   * to update the progress monitor after the read.
   */
  @Override
  public int read(byte b[],
      int off,
      int len) throws IOException {
    int nr = in.read(b, off, len);
    if (nr > 0) progressor.setProgress(nread += nr);
    return nr;
  }


  /**
   * Overrides <code>FilterInputStream.skip</code> 
   * to update the progress monitor after the skip.
   */
  @Override
  public long skip(long n) throws IOException {
    long nr = in.skip(n);
    if (nr > 0) progressor.setProgress(nread += nr);
    return nr;
  }


  /**
   * Overrides <code>FilterInputStream.close</code> 
   * to close the progress monitor as well as the stream.
   */
  @Override
  public void close() throws IOException {
    in.close();
    progressor.finish();
  }


  /**
   * Overrides <code>FilterInputStream.reset</code> 
   * to reset the progress monitor as well as the stream.
   */
  @Override
  public synchronized void reset() throws IOException {
    in.reset();
    nread = size - in.available();
    progressor.setProgress(nread);
  }
}
