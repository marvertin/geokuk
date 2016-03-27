package cz.geokuk.util.index2d;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit tests for {@link Ctverecnik}.
 */
@RunWith(JUnit4.class)
public class CtverecnikTest {

  private static final Sheet<String> FIRST_QUADRANT_ONE = new Sheet<>(25, 25, "FirstQ1");
  private static final Sheet<String> FIRST_QUADRANT_TWO = new Sheet<>(27, 23, "FirstQ2");
  private static final Sheet<String> THIRD_QUADRANT = new Sheet<>(15, 18, "ThirdQ");

  @Mock
  Visitor<String> visitor;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCtverecnik_basicFunctionality() {
    AtomicBoolean duplicityGuard = new AtomicBoolean(false);

    Ctverecnik<String> ctverecnik = new Ctverecnik<>(10, 10, 30, 30);
    ctverecnik.vloz(FIRST_QUADRANT_ONE, duplicityGuard);
    assertThat(duplicityGuard.get()).isFalse();
    ctverecnik.vloz(FIRST_QUADRANT_TWO, duplicityGuard);
    assertThat(duplicityGuard.get()).isFalse();
    ctverecnik.vloz(THIRD_QUADRANT, duplicityGuard);
    assertThat(duplicityGuard.get()).isFalse();

    ctverecnik.visit(new BoundingRect(23, 23, 28, 28), visitor);

    verify(visitor).visit(eq(FIRST_QUADRANT_ONE));
    verify(visitor).visit(eq(FIRST_QUADRANT_TWO));
    verifyNoMoreInteractions(visitor);
  }
}
