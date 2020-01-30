package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.framework.Event0;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper=false)
public class KesFilterChangeEvent extends Event0<KesFiltrModel> {
	private final KesFilterDefinition kesFilterDefinition;
}
