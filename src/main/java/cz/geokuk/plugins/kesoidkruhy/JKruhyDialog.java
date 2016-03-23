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

package cz.geokuk.plugins.kesoidkruhy;

/*
 * TextFieldDemo.java requires one additional file:
 * content.txt
 */


import java.awt.Color;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.JMyDialog0;

public class JKruhyDialog extends JMyDialog0 implements AfterEventReceiverRegistrationInit {

	private static final long serialVersionUID = 7087453419069194768L;

	private JLabel velikostLabel;
	private JSlider velikostSlider;
	private JColorChooser alfaColorChooser;
	private JCheckBox jJednotkoveKruhy;

	private KruhyModel kruhyModel;

	public JKruhyDialog() {
		setTitle("Nastavení parametrů zvýrazňovaích kruhů");
		init();
	}

	/**
	 *
	 */
	private void registerEvents() {
		ChangeListener chlist = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				//Board.mainFrame.parametryZvyraznovaceKruhuSeZmenily();
				KruhySettings kruhy = new KruhySettings();
				Color barva = alfaColorChooser.getSelectionModel().getSelectedColor();
				System.out.println("KRUHY1: " + barva + barva.getAlpha());
				kruhy.setBarva(barva);
				System.out.println("KRUHY2: " + barva + barva.getAlpha());
				kruhy.setVelikost(velikostSlider.getValue());
				kruhy.setJednotkovaVelikost(jJednotkoveKruhy.isSelected());
				System.out.println("KRUHY3: " + barva + barva.getAlpha());
				kruhyModel.setData(kruhy);
			}
		};
		velikostSlider.getModel().addChangeListener(chlist);
		alfaColorChooser.getSelectionModel().addChangeListener(chlist);
		jJednotkoveKruhy.getModel().addChangeListener(chlist);

	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 */
	@Override
	protected void initComponents() {
		velikostLabel = new JLabel("Velikost kruhů");
		velikostSlider = new JSlider();
		velikostSlider.setToolTipText("Nastavení velikosti výrazňovacího kruhu.");
		alfaColorChooser = new JColorChooser(Color.WHITE);
		alfaColorChooser.setToolTipText("Nastavení barvy a průhlednosti zvýrazňovaího kruhu");
		jJednotkoveKruhy = new JCheckBox();
		jJednotkoveKruhy.setText("Jednotkové kruhy");
		jJednotkoveKruhy.setToolTipText("Nastavení jednotkovosti kruhů");

		Box box = Box.createVerticalBox();
		add(box);

		box.add(jJednotkoveKruhy);
		box.add(velikostLabel);
		box.add(velikostSlider);
		box.add(Box.createVerticalStrut(10));
		box.add(alfaColorChooser);
	}

	public void inject (KruhyModel kruhyModel) {
		this.kruhyModel = kruhyModel;
	}

	/* (non-Javadoc)
	 * @see cz.geokuk.framework.AfterInjectInit#initAfterInject()
	 */
	public void onEvent(KruhyPreferencesChangeEvent event) {
		jJednotkoveKruhy.getModel().setSelected(event.kruhy.isJednotkovaVelikost());
		velikostSlider.getModel().setValue(event.kruhy.getVelikost());
		Color barva = event.kruhy.getBarva();
		alfaColorChooser.getSelectionModel().setSelectedColor(barva);
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
		return "ZvyraznovaciKruhy";
	}

}

