package cz.geokuk.plugins.mapy.kachle;


import java.awt.Image;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Při ukládání jsou data vždy v paměťové keši a drženy tímto
 * objektem, aby nezanikly. Tím, že zanikne tento objekt,
 * mohou být paměťová data uvolněna z keše.
 * @author tatinek
 *
 */
class DiskSaveRequest {
	private Ka0 klic;   // identifikuje dlaždici, pro kterou se data zapisují
	
	@SuppressWarnings("unused") 	// drží image jen proto, aby ho ;; garbage collector
  private Image img;   
	
	private DiskSaveSpi ukladac;  // ví jak zapsat do streamu data
	
	public DiskSaveRequest(Ka0 klic, Image img, byte[] data) {
	  this(klic, img, new DiskSaveByteArray(data));
  }

	public DiskSaveRequest(Ka0 klic, Image img, DiskSaveSpi dss) {
	  super();
	  this.klic = klic;
	  this.img = img;
	  this.ukladac = dss;
  }

	
	public Ka0 getKlic() {
  	return klic;
  }

	public void save(OutputStream ostm) throws IOException {
		ukladac.save(ostm);
	}

	public DiskSaveSpi getUkladac() {
  	return ukladac;
  }
}
