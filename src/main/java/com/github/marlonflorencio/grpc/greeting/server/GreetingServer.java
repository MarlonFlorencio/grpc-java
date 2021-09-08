package com.github.marlonflorencio.grpc.greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.File;
import java.io.IOException;

public class GreetingServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello gRPC");

        // plaintext server
//        Server server = ServerBuilder.forPort(50051)
//                .addService(new GreetServiceImpl())
//                .build();

        // secure server
        Server server = ServerBuilder.forPort(50051)
                .addService(new GreetServiceImpl())
                .useTransportSecurity(
                        new File("tls/server.crt"),
                        new File("tls/server.pem")
                )
                .build();


        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received Shutdown Request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));

        server.awaitTermination();
    }

}
