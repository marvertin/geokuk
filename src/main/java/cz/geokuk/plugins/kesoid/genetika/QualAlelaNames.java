package cz.geokuk.plugins.kesoid.genetika;

import java.util.*;

import lombok.Data;

/**
 * Jména kvalifikovaných alel. Pojmenováváme pro typovou kotnrolu.
 * @author veverka
 *
 */
@Data
public class QualAlelaNames {
	public static QualAlelaNames EMPTY= new QualAlelaNames(Collections.emptySet());

	private final Set<String> qualNames;


	public QualAlelaNames(final Set<String> qualNames) {
		this.qualNames = Collections.unmodifiableSet(qualNames);
	}

	public QualAlelaNames(final String ... qualNames) {
		this(new HashSet<>(Arrays.asList(qualNames)));
	}
}
