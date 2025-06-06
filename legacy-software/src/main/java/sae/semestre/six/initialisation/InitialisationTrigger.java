package sae.semestre.six.initialisation;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class InitialisationTrigger implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public InitialisationTrigger(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        //if (count != null && count == 0) {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.setSeparator("@@");
            populator.addScript(new ClassPathResource("trigger.sql"));
            populator.execute(dataSource);
        //}
    }
}