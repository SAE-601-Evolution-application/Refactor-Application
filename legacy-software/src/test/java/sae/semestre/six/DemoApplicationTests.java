package sae.semestre.six;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
public class DemoApplicationTests {

	@Test
	public void contextLoads() {
		assertTrue(true);
	}

}