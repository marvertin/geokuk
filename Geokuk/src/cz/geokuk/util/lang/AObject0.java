package cz.geokuk.util.lang;

import java.io.Serializable;

/**
 * Title:        Evidence exemplářů a dodávek
 * Description:  V první fázi zde bude implementace přidání dodávky a jejích exemplářů
 * Copyright:    Copyright (c) 2001
 * Company:      TurboConsult s.r.o.
 * @author
 * @version 1.0
 */

public abstract class AObject0 implements Serializable, IAtomString 
{
  /**
   * 
   */
  private static final long serialVersionUID = 1096188445300191688L;

  /**
   * Určuje, zda je hodnota atomického typu z jistého pohledu validní.
   * @return Předek vrací true, potomek musí případně přepsat.
   */
  public boolean isValid() {
    return true;
  }

}
