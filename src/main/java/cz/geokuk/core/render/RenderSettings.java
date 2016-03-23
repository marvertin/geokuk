/**
 *
 */
package cz.geokuk.core.render;

import cz.geokuk.framework.Copyable;
import cz.geokuk.framework.Preferenceble;

/**
 * @author veverka
 *
 */
@Preferenceble
public class RenderSettings implements Copyable<RenderSettings> {

	private int			renderedMoumer			= 12;
	private EImageType	imageType				= EImageType.png;
	private EWhatRender	whatRender				= EWhatRender.GOOGLE_EARTH;

	private Patterned	pureFileName;										// pure jméno souboru
	private Patterned	kmzFolder;											// jméno foldru v KMZ souboru

	private String		kmzFolderDescription;
	private int			kmzMaxDlazdiceX			= 800;						// maximální velikost dlaždice v pixlech
	private int			kmzMaxDlazdiceY			= 800;						// maximální velikost dlaždice v pixlech
	private int			kmzDrawOrder			= 0;						// pořadí rendrování

	private int			kalibrBodu				= 2;						// pro OI případně i jiné systémy
	private boolean		srovnatDoSeveru			= true;
	private int			papiroveMeritko			= 50000;					// 50000 znamená 1:50000
	private boolean		kalibracniZnackyProTisk	= false;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RenderSettings [renderedMoumer=" + renderedMoumer + ", imageType=" + imageType + ", whatRender=" + whatRender + ", pureFileName=" + pureFileName + ", kmzFolder=" + kmzFolder
				+ ", kmzFolderDescription=" + kmzFolderDescription + ", kmzMaxDlazdiceX=" + kmzMaxDlazdiceX + ", kmzMaxDlazdiceY=" + kmzMaxDlazdiceY + ", kmzDrawOrder=" + kmzDrawOrder
				+ ", kalibrBodu=" + kalibrBodu + ", srovnatDoSeveru=" + srovnatDoSeveru + ", papiroveMeritko=" + papiroveMeritko + ", kalibracniZnackyProTisk=" + kalibracniZnackyProTisk + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (imageType == null ? 0 : imageType.hashCode());
		result = prime * result + kalibrBodu;
		result = prime * result + (kalibracniZnackyProTisk ? 1231 : 1237);
		result = prime * result + kmzDrawOrder;
		result = prime * result + (kmzFolder == null ? 0 : kmzFolder.hashCode());
		result = prime * result + (kmzFolderDescription == null ? 0 : kmzFolderDescription.hashCode());
		result = prime * result + kmzMaxDlazdiceX;
		result = prime * result + kmzMaxDlazdiceY;
		result = prime * result + papiroveMeritko;
		result = prime * result + (pureFileName == null ? 0 : pureFileName.hashCode());
		result = prime * result + renderedMoumer;
		result = prime * result + (srovnatDoSeveru ? 1231 : 1237);
		result = prime * result + (whatRender == null ? 0 : whatRender.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final RenderSettings other = (RenderSettings) obj;
		if (imageType != other.imageType) {
			return false;
		}
		if (kalibrBodu != other.kalibrBodu) {
			return false;
		}
		if (kalibracniZnackyProTisk != other.kalibracniZnackyProTisk) {
			return false;
		}
		if (kmzDrawOrder != other.kmzDrawOrder) {
			return false;
		}
		if (kmzFolder == null) {
			if (other.kmzFolder != null) {
				return false;
			}
		} else if (!kmzFolder.equals(other.kmzFolder)) {
			return false;
		}
		if (kmzFolderDescription == null) {
			if (other.kmzFolderDescription != null) {
				return false;
			}
		} else if (!kmzFolderDescription.equals(other.kmzFolderDescription)) {
			return false;
		}
		if (kmzMaxDlazdiceX != other.kmzMaxDlazdiceX) {
			return false;
		}
		if (kmzMaxDlazdiceY != other.kmzMaxDlazdiceY) {
			return false;
		}
		if (papiroveMeritko != other.papiroveMeritko) {
			return false;
		}
		if (pureFileName == null) {
			if (other.pureFileName != null) {
				return false;
			}
		} else if (!pureFileName.equals(other.pureFileName)) {
			return false;
		}
		if (renderedMoumer != other.renderedMoumer) {
			return false;
		}
		if (srovnatDoSeveru != other.srovnatDoSeveru) {
			return false;
		}
		if (whatRender != other.whatRender) {
			return false;
		}
		return true;
	}

	/**
	 * @param renderedMoumer
	 *            the renderedMoumer to set
	 */
	public void setRenderedMoumer(final int renderedMoumer) {
		this.renderedMoumer = renderedMoumer;
	}

	/**
	 * @param imageType
	 *            the imageType to set
	 */
	public void setImageType(final EImageType imageType) {
		this.imageType = imageType;
	}

	/**
	 * @param whatRender
	 *            the whatRender to set
	 */
	public void setWhatRender(final EWhatRender whatRender) {
		this.whatRender = whatRender;
	}

	/**
	 * @return the pureFileName
	 */
	public Patterned getPureFileName() {
		return pureFileName;
	}

	/**
	 * @param pureFileName
	 *            the pureFileName to set
	 */
	public void setPureFileName(final Patterned pureFileName) {
		this.pureFileName = pureFileName;
	}

	/**
	 * @return the kmzFolder
	 */
	public Patterned getKmzFolder() {
		return kmzFolder;
	}

	/**
	 * @param kmzFolder
	 *            the kmzFolder to set
	 */
	public void setKmzFolder(final Patterned kmzFolder) {
		this.kmzFolder = kmzFolder;
	}

	/**
	 * @return the kmzFolderDescription
	 */
	public String getKmzFolderDescription() {
		return kmzFolderDescription;
	}

	/**
	 * @param kmzFolderDescription
	 *            the kmzFolderDescription to set
	 */
	public void setKmzFolderDescription(final String kmzFolderDescription) {
		this.kmzFolderDescription = kmzFolderDescription;
	}

	/**
	 * @return the kmzDrawOrder
	 */
	public int getKmzDrawOrder() {
		return kmzDrawOrder;
	}

	/**
	 * @param kmzDrawOrder
	 *            the kmzDrawOrder to set
	 */
	public void setKmzDrawOrder(final int kmzDrawOrder) {
		this.kmzDrawOrder = kmzDrawOrder;
	}

	/**
	 * @return the kalibrBodu
	 */
	public int getKalibrBodu() {
		return kalibrBodu;
	}

	/**
	 * @param kalibrBodu
	 *            the kalibrBodu to set
	 */
	public void setKalibrBodu(final int kalibrBodu) {
		this.kalibrBodu = kalibrBodu;
	}

	/**
	 * @return the srovnatDoSeveru
	 */
	public boolean isSrovnatDoSeveru() {
		return srovnatDoSeveru;
	}

	/**
	 * @param srovnatDoSeveru
	 *            the srovnatDoSeveru to set
	 */
	public void setSrovnatDoSeveru(final boolean srovnatDoSeveru) {
		this.srovnatDoSeveru = srovnatDoSeveru;
	}

	/**
	 * @return the papiroveMeritko
	 */
	public int getPapiroveMeritko() {
		return papiroveMeritko;
	}

	/**
	 * @param papiroveMeritko
	 *            the papiroveMeritko to set
	 */
	public void setPapiroveMeritko(final int papiroveMeritko) {
		this.papiroveMeritko = papiroveMeritko;
	}

	/**
	 * @return the kalibracniZnackyProTisk
	 */
	public boolean isKalibracniZnackyProTisk() {
		return kalibracniZnackyProTisk;
	}

	/**
	 * @param kalibracniZnackyProTisk
	 *            the kalibracniZnackyProTisk to set
	 */
	public void setKalibracniZnackyProTisk(final boolean kalibracniZnackyProTisk) {
		this.kalibracniZnackyProTisk = kalibracniZnackyProTisk;
	}

	/**
	 * @return the renderedMoumer
	 */
	public int getRenderedMoumer() {
		return renderedMoumer;
	}

	/**
	 * @return the imageType
	 */
	public EImageType getImageType() {
		return imageType;
	}

	/**
	 * @return the whatRender
	 */
	public EWhatRender getWhatRender() {
		return whatRender;
	}

	@Override
	public RenderSettings copy() {
		try {
			final RenderSettings klon = (RenderSettings) super.clone();
			klon.pureFileName = klon.pureFileName.copy();
			klon.kmzFolder = klon.kmzFolder.copy();
			return klon;
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Preferenceble
	public static class Patterned implements Copyable<Patterned> {
		private String	patternNumberCilovy;	// číslo patteru ciloveho, kdyz je hotov geocoding
		private String	patternNumberPredbezny;	// cislo patternu predbezneho, kdyz jedte neni hotov geocoding
		private String	text	= "";			// neukládá se do preferencí, musí uživatel zadat nebo se generuje

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Patterned [patternNumberCilovy=" + patternNumberCilovy + ", patternNumberPredbezny=" + patternNumberPredbezny + ", text=" + text + "]";
		}

		/**
		 * @return the patternNumberCilovy
		 */
		public String getPatternNumberCilovy() {
			return patternNumberCilovy;
		}

		/**
		 * @param patternNumberCilovy
		 *            the patternNumberCilovy to set
		 */
		public void setPatternNumberCilovy(final String patternNumberCilovy) {
			this.patternNumberCilovy = patternNumberCilovy;
		}

		/**
		 * @return the patternNumberPredbezny
		 */
		public String getPatternNumberPredbezny() {
			return patternNumberPredbezny;
		}

		/**
		 * @param patternNumberPredbezny
		 *            the patternNumberPredbezny to set
		 */
		public void setPatternNumberPredbezny(final String patternNumberPredbezny) {
			this.patternNumberPredbezny = patternNumberPredbezny;
		}

		/**
		 * @return the text
		 */
		public String getText() {
			return text;
		}

		/**
		 * @param text
		 *            the text to set
		 */
		public void setText(final String text) {
			this.text = text;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (patternNumberCilovy == null ? 0 : patternNumberCilovy.hashCode());
			result = prime * result + (patternNumberPredbezny == null ? 0 : patternNumberPredbezny.hashCode());
			result = prime * result + (text == null ? 0 : text.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Patterned other = (Patterned) obj;
			if (patternNumberCilovy == null) {
				if (other.patternNumberCilovy != null) {
					return false;
				}
			} else if (!patternNumberCilovy.equals(other.patternNumberCilovy)) {
				return false;
			}
			if (patternNumberPredbezny == null) {
				if (other.patternNumberPredbezny != null) {
					return false;
				}
			} else if (!patternNumberPredbezny.equals(other.patternNumberPredbezny)) {
				return false;
			}
			if (text == null) {
				if (other.text != null) {
					return false;
				}
			} else if (!text.equals(other.text)) {
				return false;
			}
			return true;
		}

		@Override
		public Patterned copy() {
			try {
				final Patterned klon = (Patterned) super.clone();
				return klon;
			} catch (final CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}

	}

	/**
	 * @return the kmzMaxDlazdiceX
	 */
	public int getKmzMaxDlazdiceX() {
		return kmzMaxDlazdiceX;
	}

	/**
	 * @return the kmzMaxDlazdiceY
	 */
	public int getKmzMaxDlazdiceY() {
		return kmzMaxDlazdiceY;
	}

	/**
	 * @param kmzMaxDlazdiceX
	 *            the kmzMaxDlazdiceX to set
	 */
	public void setKmzMaxDlazdiceX(final int kmzMaxDlazdiceX) {
		this.kmzMaxDlazdiceX = kmzMaxDlazdiceX;
	}

	/**
	 * @param kmzMaxDlazdiceY
	 *            the kmzMaxDlazdiceY to set
	 */
	public void setKmzMaxDlazdiceY(final int kmzMaxDlazdiceY) {
		this.kmzMaxDlazdiceY = kmzMaxDlazdiceY;
	}
}
