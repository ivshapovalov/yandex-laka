package ru.yandex.yandexlavka.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.yandex.yandexlavka.CommonTest;
import ru.yandex.yandexlavka.service.MainService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
public class JSpritVrpConfigTest extends CommonTest {

    @Autowired
    private JSpritConfig jSpritConfig;

    @MockBean
    private MainService mainService;

    @Test
    public void getJSpritConfigWhenYamlFileProvidedMaps() {
        assertThat(jSpritConfig.getTypes())
                .containsOnlyKeys("foot", "bike", "auto");

        assertThat(jSpritConfig.getConstraints())
                .containsOnlyKeys("region", "amount", "weight");

        assertThat(jSpritConfig.getCapacity())
                .containsOnlyKeys("weightIndex", "amountIndex");

        assertThat(jSpritConfig.getDeliveryCost())
                .containsOnlyKeys("first", "notFirst");

        assertThat(jSpritConfig.getDeliveryDistance())
                .containsOnlyKeys("foot", "bike", "auto");
    }

}
