package com.github.marlonflorencio.grpc.math.client;

import com.github.marlonflorencio.proto.math.LongRequest;
import com.github.marlonflorencio.proto.math.MathServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class PrimeClient {

    public static void main(String[] args) {
        new PrimeClient().run();
    }

    private void run() {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        execute(channel);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void execute(ManagedChannel channel) {

        MathServiceGrpc.MathServiceBlockingStub client = MathServiceGrpc.newBlockingStub(channel);

        LongRequest request = LongRequest.newBuilder().setValue(4342423322l).build();

        client.prime(request).forEachRemaining(response -> System.out.println(response.getResult()));

    }

}
