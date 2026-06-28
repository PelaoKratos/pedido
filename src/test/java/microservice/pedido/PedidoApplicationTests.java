package microservice.pedido;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class PedidoApplicationTests {

	@Test
	void contextLoads(ApplicationContext applicationContext) {
		assertThat(applicationContext).isNotNull();
		assertThat(applicationContext.getBean(PedidoApplication.class)).isNotNull();
	}

	@Test
	void mainDelegatesToSpringApplicationRun() {
		String[] args = { "--spring.profiles.active=test" };

		try (MockedStatic<SpringApplication> springApplication = Mockito.mockStatic(SpringApplication.class)) {
			PedidoApplication.main(args);

			springApplication.verify(() -> SpringApplication.run(PedidoApplication.class, args));
		}
	}

	@Test
	void applicationIsAnnotatedAsSpringBootApplication() {
		assertThat(PedidoApplication.class).hasAnnotation(SpringBootApplication.class);
	}
}
