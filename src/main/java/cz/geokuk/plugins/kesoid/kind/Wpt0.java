package cz.geokuk.plugins.kesoid.kind;

import java.util.Optional;

import lombok.*;

@Getter @Setter @ToString
@NoArgsConstructor
public abstract class Wpt0 extends Wpt00 {

	private Optional<String> author = Optional.empty();
	private Optional<String> creationDate = Optional.empty();



}
