package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.framework.Event0;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper=false)
public class KesFilterDefinitionChangedEvent extends Event0<KesFilterModel> {
	private final KesFilterDefinition kesFilterDefinition;
}
