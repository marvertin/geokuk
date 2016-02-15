package cz.geokuk.core.program;

import java.util.List;

import javax.swing.JFrame;

import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coord.SlideListProvider;
import cz.geokuk.framework.Factory;

public class MainFrameHolder implements SlideListProvider {

  private JMainFrame mainFrame;
  private Factory factory;

  public void setMainFrame(JMainFrame mainFrame) {
    this.mainFrame = mainFrame;
    factory.init(this.mainFrame);
  }

  public JFrame getMainFrame() {
    return mainFrame;
  }

  public void inject(Factory factory) {
    this.factory = factory;
  }

  /* (non-Javadoc)
   * @see cz.geokuk.core.coord.SlideListProvider#getSlides()
   */
  @Override
  public List<JSingleSlide0> getSlides() {
    return mainFrame.getSlides();
  }
}
