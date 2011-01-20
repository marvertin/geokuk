package cz.geokuk.plugins.kesoidobsazenost;

import java.awt.Color;

import cz.geokuk.framework.Copyable;
import cz.geokuk.framework.Preferenceble;

@Preferenceble
public class ObsazenostSettings implements Copyable<ObsazenostSettings> {

  private Color color = Color.BLACK;

  

  public Color getColor() {
    return color;
  }



  public void setColor(Color color) {
    this.color = color;
  }



  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((color == null) ? 0 : color.hashCode());
    return result;
  }



  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ObsazenostSettings other = (ObsazenostSettings) obj;
    if (color == null) {
      if (other.color != null)
        return false;
    } else if (!color.equals(other.color))
      return false;
    return true;
  }



  @Override
  public String toString() {
    return "ObsazenostSettings [color=" + color + "alfa=" + color.getAlpha() + "]";
  }



  @Override
  public ObsazenostSettings copy() {
    try {
      return (ObsazenostSettings) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
    

}
