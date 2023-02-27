package pl.umk.mat.zesp01.pz2022.researcher.config

import com.mongodb.client.MongoClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.testcontainers.containers.MongoDBContainer

@Configuration
@Profile("integration")
class MongoTestConfiguration {

    private val MONGO_DB_DOCKER_IMAGE_NAME = "mongo:6.0"
    private val MONGO_PORT = 27017

    @Bean(initMethod = "start", destroyMethod = "stop")
    fun mongoContainer() = MongoDBContainer(MONGO_DB_DOCKER_IMAGE_NAME).withExposedPorts(MONGO_PORT)

    @Bean
    fun mongoClient(mongoDBContainer: MongoDBContainer) = MongoClients.create(mongoDBContainer.replicaSetUrl)

}