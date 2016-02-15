/**
 * 
 */
package cz.geokuk.plugins.kesoid;

/**
 * Spoločný předek kešoidu a waypointu.
 */
public abstract class Weikoid0 {

  // Propojení do kruhu všech waypointů jednoho kešoidu.
  protected Weikoid0 next = this;
}
