/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cz.geokuk.plugins.kesoidpopisky;

/*
 * TextFieldDemo.java requires one additional file:
 * content.txt
 */


import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.util.gui.fontchoser.JFontChooser;

public class JPopiskyDialog extends JMyDialog0 implements AfterEventReceiverRegistrationInit {

  private static final long serialVersionUID = 7087453419069194768L;

  private JSpinner xspinner;
  private JSpinner yspinner;
  private JColorChooser foregroundChooser;
  private JFontChooser fontChooser;
  private JColorChooser backgroudChooser;
  private JTextField jKesPatternEdit;
  private JTextField jWaymarkPatternEdit;
  private JTextField jCgpPatternEdit;
  private JTextField jSimplewaypontPatternEdit;

  private final JLabel jKesPatternLabel = new JLabel("Keš:");
  private final JLabel jWaymarkPatternLabel = new JLabel("Waymark:");
  private final JLabel jCgpPatternLabel = new JLabel("Czech geodetic point:");
  private final JLabel jSimplewaypontPatternLabel = new JLabel("Simple waypoint:");

  private PopiskyModel popiskyModel;

  public JPopiskyDialog() {
    setTitle("Nastavení paramtrů popisek keší na mapě");
    init();
  }

  public void onEvent(PopiskyPreferencesChangeEvent event) {
    PopiskySettings pose = event.pose;
    foregroundChooser.getSelectionModel().setSelectedColor(pose.foreground);
    xspinner.setValue(pose.posuX);
    yspinner.setValue(pose.posuY);
    backgroudChooser.getSelectionModel().setSelectedColor(pose.background);
    fontChooser.setFont(pose.font);
    setPattern(jKesPatternEdit, pose.getPatterns().getKesPattern());
    setPattern(jWaymarkPatternEdit, pose.getPatterns().getWaymarkPattern());
    setPattern(jCgpPatternEdit, pose.getPatterns().getCgpPattern());
    setPattern(jSimplewaypontPatternEdit, pose.getPatterns().getSimplewaypointPattern());
  }

  private void setPattern(JTextField jField, String pattern) {
    if (! jField.getText().equals(pattern)) {
      jField.setText(pattern);
    }

  }

  /** This method is called from within the constructor to
   * initialize the form.
   */
  @Override
  protected void initComponents() {
    xspinner = new JSpinner();
    yspinner = new JSpinner();
    foregroundChooser = new JColorChooser(Color.BLACK);
    backgroudChooser = new JColorChooser(Color.WHITE);
    fontChooser = new JFontChooser();
    jKesPatternEdit = new JTextField();
    jWaymarkPatternEdit = new JTextField();
    jCgpPatternEdit = new JTextField();
    jSimplewaypontPatternEdit = new JTextField();

    xspinner.setToolTipText("Posun popisku vůči ikoně v horizontálním směru");
    yspinner.setToolTipText("Posun popisku vůči ikoně ve vertkálním směru");
    foregroundChooser.setToolTipText("Barva písma včetně průhlednosti");
    backgroudChooser.setToolTipText("Barva podkladu včetně průhlednosti");
    fontChooser.setToolTipText("Font popisků na mapě");

    foregroundChooser.setBorder(BorderFactory.createTitledBorder("Písmo"));
    backgroudChooser.setBorder(BorderFactory.createTitledBorder("Podklad"));
    Box box = Box.createHorizontalBox();

    box.add(foregroundChooser);
    box.add(xspinner);
    box.add(yspinner);
    //  	box.add(Box.createVerticalStrut(10));
    box.add(fontChooser);
    box.add(backgroudChooser);

    JPanel pan = new JPanel(new BorderLayout());
    pan.add(box);
    JPanel patternPan = new JPanel();
    grlay(patternPan);
    pan.add(patternPan, BorderLayout.NORTH);

    add(pan);
    pack();
  }


  private void grlay(JPanel panel) {
    GroupLayout layout = new GroupLayout(panel);
    panel.setBorder(BorderFactory.createTitledBorder("Vzorky popisků: " + SestavovacPopisku.getNahrazovaceDisplay()));
    panel.setLayout(layout);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);

    //    panel.add(jKesPatternEdit);
    //    panel.add(jWaymarkPatternEdit);
    //    panel.add(jCgpPatternEdit);
    //    panel.add(jSimplewaypontPatternEdit);

    layout.setHorizontalGroup(layout.createSequentialGroup()       //hroup
        .addGroup(layout.createParallelGroup()  //h1
            .addComponent(jKesPatternLabel)
            .addComponent(jWaymarkPatternLabel)
            .addComponent(jCgpPatternLabel)
            .addComponent(jSimplewaypontPatternLabel)
        )
        .addGroup(layout.createParallelGroup()  //h1
            .addComponent(jKesPatternEdit)
            .addComponent(jWaymarkPatternEdit)
            .addComponent(jCgpPatternEdit)
            .addComponent(jSimplewaypontPatternEdit)
        )
    );
    layout.setVerticalGroup(layout.createSequentialGroup()       //hroup
        .addGroup(layout.createParallelGroup()  //h1
            .addComponent(jKesPatternLabel)
            .addComponent(jKesPatternEdit)
        )
        .addGroup(layout.createParallelGroup()  //h1
            .addComponent(jWaymarkPatternLabel)
            .addComponent(jWaymarkPatternEdit)
        )
        .addGroup(layout.createParallelGroup()  //h1
            .addComponent(jCgpPatternLabel)
            .addComponent(jCgpPatternEdit)
        )
        .addGroup(layout.createParallelGroup()  //h1
            .addComponent(jSimplewaypontPatternLabel)
            .addComponent(jSimplewaypontPatternEdit)
        )
    );
  }

  //  private JPanel obalRameckem(JComponent com, String title) {
  //  }
  /**
   * 
   */
  private void registerEvents() {
    ChangeListener chlistener = new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        PopiskySettings pose = popiskyModel.getData();
        pose.foreground = foregroundChooser.getSelectionModel().getSelectedColor();
        pose.posuX = (Integer) xspinner.getValue();
        pose.posuY = (Integer) yspinner.getValue();
        pose.background = backgroudChooser.getSelectionModel().getSelectedColor();
        pose.font = fontChooser.getFont();
        System.out.println("POPIPOP: " +pose);
        popiskyModel.setData(pose);
      }
    };

    jKesPatternEdit.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent e) {
        zmena();
      }
      @Override
      public void insertUpdate(DocumentEvent e) {
        zmena();
      }
      @Override
      public void changedUpdate(DocumentEvent e) {
        zmena();
      }
      private void zmena() {
        PopiskySettings data = popiskyModel.getData();
        data.patterns.setKesPattern(jKesPatternEdit.getText());
        popiskyModel.setData(data);
      }
    });

    jWaymarkPatternEdit.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent e) {
        zmena();
      }
      @Override
      public void insertUpdate(DocumentEvent e) {
        zmena();
      }
      @Override
      public void changedUpdate(DocumentEvent e) {
        zmena();
      }
      private void zmena() {
        PopiskySettings data = popiskyModel.getData();
        data.patterns.setWaymarkPattern(jWaymarkPatternEdit.getText());
        popiskyModel.setData(data);
      }
    });

    jCgpPatternEdit.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent e) {
        zmena();
      }
      @Override
      public void insertUpdate(DocumentEvent e) {
        zmena();
      }
      @Override
      public void changedUpdate(DocumentEvent e) {
        zmena();
      }
      private void zmena() {
        PopiskySettings data = popiskyModel.getData();
        data.patterns.setCgpPattern(jCgpPatternEdit.getText());
        popiskyModel.setData(data);
      }
    });

    jSimplewaypontPatternEdit.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent e) {
        zmena();
      }
      @Override
      public void insertUpdate(DocumentEvent e) {
        zmena();
      }
      @Override
      public void changedUpdate(DocumentEvent e) {
        zmena();
      }
      private void zmena() {
        PopiskySettings data = popiskyModel.getData();
        data.patterns.setSimplewaypointPattern(jSimplewaypontPatternEdit.getText());
        popiskyModel.setData(data);
      }
    });

    foregroundChooser.getSelectionModel().addChangeListener(chlistener);
    xspinner.addChangeListener(chlistener);
    yspinner.addChangeListener(chlistener);
    backgroudChooser.getSelectionModel().addChangeListener(chlistener);
    fontChooser.getSelectionModel().addChangeListener(chlistener);
  }

  public void inject (PopiskyModel popiskyModel) {
    this.popiskyModel = popiskyModel;
  }

  /* (non-Javadoc)
   * @see cz.geokuk.framework.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
   */
  @Override
  public void initAfterEventReceiverRegistration() {
    registerEvents();
  }

  @Override
  protected String getTemaNapovedyDialogu() {
    return "PopiskyKesoidu";
  }

}

