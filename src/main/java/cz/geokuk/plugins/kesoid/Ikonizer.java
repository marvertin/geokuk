package cz.geokuk.plugins.kesoid;


import cz.geokuk.img.ImageLoader;
import cz.geokuk.img.MissingIcon;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Ikonizer {

	//private static final Icon[] smery = {smer("N"), smer("NE"), smer("E"), smer("SE"), smer("S"), smer("SW"), smer("W"), smer("NW")};


	//  private static Icon smer(String smer) {
	//  	return ImageLoader.seekResIcon("x16/smery/" + smer + ".gif");
	//  }

	public Icon seekIcon(String path) {
		Image bi = ImageLoader.locateResImage(path);
		if (bi != null) return new ImageIcon(bi);
		return new MissingIcon();
	}

	public static Icon findSmerIcon(double smer) {
		BufferedImage sm = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) sm.getGraphics();
		g.translate(8, 8);
		g.rotate(smer * Math.PI / 180);
		g.translate(-8, -8);
		BufferedImage severni = ImageLoader.locateResImage("x16/smery/N.gif");
		g.drawImage(severni, 0, 0, null);
		return new ImageIcon(sm);
		//  	int index = ((int)smer + 45) % 360 / 45;
		//  	return smery[index];
	}
}
