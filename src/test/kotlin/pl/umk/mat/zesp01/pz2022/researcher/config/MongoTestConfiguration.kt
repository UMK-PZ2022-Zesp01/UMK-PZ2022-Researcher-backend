package pl.umk.mat.zesp01.pz2022.researcher.config

import com.mongodb.client.MongoClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.testcontainers.containers.MongoDBContainer

@Configuration
@Profile("integration")
class MongoTestConfiguration {

    private val mongoDBDockerImageName = "mongo:6.0"
    private val mongoDBPort = 27017

    @Bean(initMethod = "start", destroyMethod = "stop")
    fun mongoContainer(): MongoDBContainer = MongoDBContainer(mongoDBDockerImageName).withExposedPorts(mongoDBPort)

    @Bean
    fun mongoClient(mongoDBContainer: MongoDBContainer) = MongoClients.create(mongoDBContainer.replicaSetUrl)

}