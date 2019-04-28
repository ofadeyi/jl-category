package com.example.jlcategory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JlCategoryApplicationTests {
    @LocalServerPort
    int serverPort;

    @Test
    public void contextLoads() {
        // Given
        WebTestClient client = WebTestClient
                .bindToRouterFunction(RootRoutes.routes(Stream.of()))
                .build();
        //Expect
        client.get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isOk();
    }


}
