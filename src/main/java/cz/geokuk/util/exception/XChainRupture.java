package cz.geokuk.util.exception;

/**
 * Výjimka přerušující řetězec výjimek
 * @author veverka
 *
 */
public class XChainRupture extends RuntimeException {


	static final long serialVersionUID = -1113131046499029970L;
	private final AExcId iExceptionId;
	private final String iCircumstance;

	public XChainRupture(AExcId aExceptionId, String aCircumstance) {
		iExceptionId = aExceptionId;
		iCircumstance = aCircumstance;
	}

	@Override
	public String getMessage() {
		return "Exception '" + iExceptionId + "' catched and duped at \"" + iCircumstance + "\"";
	}


	/**
	 * Identifikace výjimky, jak je uložena v servrovské repozitoři.
	 * @return
	 */
	public AExcId getExceptionId() {
		return iExceptionId;
	}



}
