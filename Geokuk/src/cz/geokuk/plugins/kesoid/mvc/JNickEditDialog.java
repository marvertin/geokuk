package cz.geokuk.plugins.kesoid.mvc;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import cz.geokuk.framework.JMyDialog0;

public class JNickEditDialog extends JMyDialog0 {

  private static final long serialVersionUID = 5215923043342722378L;

  JTextField jNick;
  JButton jUlozit;

  private KesoidModel kesoidModel;

  public JNickEditDialog() {
    setTitle("Nick na geocaching.com (přihlašovací jméno");
    init();
  }

  @Override
  protected void initComponents() {
    Box box = Box.createVerticalBox();
    jNick = new JTextField();
    jNick.setColumns(30);
    jNick.setAlignmentX(CENTER_ALIGNMENT);
    jUlozit = new JButton("Uložit");
    jUlozit.setAlignmentX(CENTER_ALIGNMENT);
    box.add(jNick);
    box.add(jUlozit, BorderLayout.SOUTH);
    JLabel upozorneni = new JLabel("Po změně nicku nutno program znovu spustit.");
    upozorneni.setAlignmentX(CENTER_ALIGNMENT);
    box.add(upozorneni);
    add(box);

    jUlozit.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        // TODO Předělat něco s nickem, už nevím co
        kesoidModel.setGccomNick(jNick.getText());
        dispose();
      }
    });
  }

  public void onEvent(GccomNickChangedEvent event) {
    jNick.setText(event.getGccomNick());
  }

  public void inject(KesoidModel kesoidModel) {
    this.kesoidModel = kesoidModel;
  }

  @Override
  protected String getTemaNapovedyDialogu() {
    return "InformaceOSobe";
  }

}
