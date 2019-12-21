package cz.geokuk.plugins.kesoid.kind;

import java.util.Map;

import cz.geokuk.plugins.kesoidpopisky.PopiskyNahrazovac;


public interface PopiskyDef {

	public String getLabel();

	public String getDefaultPatter();

	public Map<String, PopiskyNahrazovac> getNahrazovace();

	public String geHelpNahrazovace();

}
