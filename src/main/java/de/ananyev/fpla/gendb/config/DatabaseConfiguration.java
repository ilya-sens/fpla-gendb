package de.ananyev.fpla.gendb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by Ilya Ananyev on 30.12.16.
 */
@Configuration
@EnableJpaRepositories("de.ananyev.fpla.gendb.repository")
@EnableTransactionManagement
public class DatabaseConfiguration {
}
