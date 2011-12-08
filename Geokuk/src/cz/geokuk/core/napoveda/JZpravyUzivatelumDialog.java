package cz.geokuk.core.napoveda;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import cz.geokuk.framework.JMyDialog0;

public class JZpravyUzivatelumDialog extends JMyDialog0 {

  private static final long serialVersionUID = 5215923043342722378L;

  JScrollPane jZpravy;
  JButton jPrecteno;

  private final List<ZpravaUzivateli> zpravyUzivatelum;


  public JZpravyUzivatelumDialog(List<ZpravaUzivateli> zpravyUzivatelum) {
    this.zpravyUzivatelum = zpravyUzivatelum;
    setTitle("Zprávy uživatelům");
    init();
  }

  @Override
  protected void initComponents() {


    JEditorPane editorPane = new JEditorPane();
    editorPane.setEditable(false);
    java.net.URL helpURL;
    try {
      helpURL = new URL("http://geokuk.cz/version.php?msgnad=1&verze=4.5.88");
      //      helpURL = null;
      //      editorPane.setText("xxxxxxxxxxxxxxxxxxxxxx");
      if (helpURL != null) {
        try {
          editorPane.setPage(helpURL);
        } catch (IOException e) {
          System.err.println("Attempted to read a bad URL: " + helpURL);
        }
      } else {
        System.err.println("Couldn't find file: TextSamplerDemoHelp.html");
      }
    } catch (MalformedURLException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    //Put the editor pane in a scroll pane.
    jZpravy = new JScrollPane(editorPane);
    //    jZpravy.setVerticalScrollBarPolicy(
    //        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    jZpravy.setPreferredSize(new Dimension(250, 145));
    jZpravy.setMinimumSize(new Dimension(10, 10));

    //Box box = Box.createVerticalBox();

    naplnZpravy();
    jPrecteno = new JButton("Přečteno");
    jZpravy.setAlignmentX(CENTER_ALIGNMENT);
    jPrecteno.setAlignmentX(CENTER_ALIGNMENT);
    //    box.add(jNickName);
    //    box.add(jNickId);
    //    box.add(jUlozit, BorderLayout.SOUTH);
    //    box.add(upozorneni);
    JPanel panel = new JPanel();
    add(panel);

    grlay(panel);

    jPrecteno.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        //TODO Zapamoatovat si, že zprávy jsou přečteny
        dispose();
      }
    });
  }

  private void naplnZpravy() {
    for (ZpravaUzivateli zpravaUzivateli : zpravyUzivatelum) {
      //Document document = jZpravy.getDocument();
      //document.
      // TODO plnit
      //jZpravy.setText(zpravaUzivateli.text);
    }
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
        .addGroup(layout.createParallelGroup()       //hroup
            .addComponent(jZpravy)
            .addComponent(jPrecteno)
            )
        );
    layout.setVerticalGroup(layout.createSequentialGroup()       //hroup
        .addComponent(jZpravy)
        .addComponent(jPrecteno)
        );
  }


  @Override
  protected String getTemaNapovedyDialogu() {
    return "ZpravyUzivatelum";
  }

}
