package com.github.marlonflorencio.grpc.math.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class MathServer {

    public static void main(String[] args) throws IOException, InterruptedException {

        Server server = ServerBuilder.forPort(50052)
                .addService(new MathServiceImpl())
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
