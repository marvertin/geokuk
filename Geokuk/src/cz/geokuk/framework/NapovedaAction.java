package cz.geokuk.framework;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import cz.geokuk.core.napoveda.NapovedaModel;

public class NapovedaAction extends AbstractAction {
  private static final long serialVersionUID = -4843379055570361691L;
  private NapovedaModel napovedaModel;
  private final String tema;

  public NapovedaAction(String tema) {
    super("Nápověda");
    this.tema = tema;
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F1"));

  }
  @Override
  public void actionPerformed(ActionEvent e) {
    napovedaModel.zobrazNapovedu(tema);
  }

  public void inject(NapovedaModel napovedaModel) {
    this.napovedaModel = napovedaModel;
  }
}