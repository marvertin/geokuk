package cz.geokuk.plugins.kesoid.kind;

import java.util.Optional;

import cz.geokuk.plugins.kesoid.EWptStatus;
import cz.geokuk.plugins.kesoid.EWptVztah;
import lombok.*;

@Getter @Setter @ToString
@NoArgsConstructor
public abstract class Wpt0 extends Wpt00 {

	private Optional<String> author = Optional.empty();
	private Optional<String> creatinDate = Optional.empty();

	public EWptStatus status = EWptStatus.ACTIVE;
	public EWptVztah vztah = EWptVztah.NORMAL;
}
