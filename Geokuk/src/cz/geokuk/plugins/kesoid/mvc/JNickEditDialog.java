package cz.geokuk.plugins.kesoid.mvc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cz.geokuk.framework.Dlg;
import cz.geokuk.framework.JMyDialog0;

public class JNickEditDialog extends JMyDialog0 {

  private static final long serialVersionUID = 5215923043342722378L;

  JLabel jNickNameLabel = new JLabel("Nick: ");
  JLabel jNickIdLabel = new JLabel("Id: ");
  JTextField jNickName;
  JTextField jNickId;
  JButton jUlozit;

  private KesoidModel kesoidModel;

  public JNickEditDialog() {
    setTitle("Nick na geocaching.com (přihlašovací jméno");
    init();
  }

  @Override
  protected void initComponents() {
    //Box box = Box.createVerticalBox();
    jNickName = new JTextField();
    jNickName.setColumns(30);
    jNickName.setAlignmentX(CENTER_ALIGNMENT);
    jNickId = new JTextField();
    jNickId.setColumns(30);
    jNickId.setAlignmentX(CENTER_ALIGNMENT);
    jUlozit = new JButton("Uložit");
    jUlozit.setAlignmentX(CENTER_ALIGNMENT);
    //    box.add(jNickName);
    //    box.add(jNickId);
    //    box.add(jUlozit, BorderLayout.SOUTH);
    JLabel upozorneni = new JLabel("Po změně nicku nutno program znovu spustit.");
    upozorneni.setAlignmentX(CENTER_ALIGNMENT);
    //    box.add(upozorneni);
    JPanel panel = new JPanel();
    add(panel);

    grlay(panel);

    jUlozit.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        int gccomNIckId;
        try {
          gccomNIckId = Integer.parseInt(jNickId.getText());
        } catch (NumberFormatException e1) {
          Dlg.error("Owner ID \"" + jNickId.getText() + "\" musí číslem býti!");
          return;
        }
        kesoidModel.setGccomNick(new GccomNick(jNickName.getText(),gccomNIckId));
        dispose();
      }
    });
  }

  private void grlay(JPanel panel) {
    GroupLayout layout = new GroupLayout(panel);
    //panel.setBorder(BorderFactory.createTitledBorder("Nastvit nickVzorky popisků: " + SestavovacPopisku.getNahrazovaceDisplay()));
    panel.setLayout(layout);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);

    //    panel.add(jKesPatternEdit);
    //    panel.add(jWaymarkPatternEdit);
    //    panel.add(jCgpPatternEdit);
    //    panel.add(jSimplewaypontPatternEdit);

    layout.setHorizontalGroup(
        layout.createParallelGroup(Alignment.CENTER)
        .addGroup(layout.createSequentialGroup()       //hroup
            .addGroup(layout.createParallelGroup()  //h1
                .addComponent(jNickNameLabel)
                .addComponent(jNickIdLabel)
                )
                .addGroup(layout.createParallelGroup()  //h1
                    .addComponent(jNickName)
                    .addComponent(jNickId)
                    )
            )

            .addComponent(jUlozit)
        );
    layout.setVerticalGroup(layout.createSequentialGroup()       //hroup
        .addGroup(layout.createParallelGroup()  //h1
            .addComponent(jNickNameLabel)
            .addComponent(jNickName)
            )
            .addGroup(layout.createParallelGroup()  //h1
                .addComponent(jNickIdLabel)
                .addComponent(jNickId)
                )
                .addGroup(layout.createParallelGroup()  //h1
                    .addComponent(jUlozit)
                    )
        );
  }

  public void onEvent(GccomNickChangedEvent event) {
    jNickName.setText(event.getGccomNick().name);
    jNickId.setText(event.getGccomNick().id + "");
  }

  public void inject(KesoidModel kesoidModel) {
    this.kesoidModel = kesoidModel;
  }

  @Override
  protected String getTemaNapovedyDialogu() {
    return "InformaceOSobe";
  }

}
