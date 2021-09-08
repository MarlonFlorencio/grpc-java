package com.github.marlonflorencio.grpc.math.client;

import com.github.marlonflorencio.grpc.util.TimeUtil;
import com.github.marlonflorencio.proto.math.LongRequest;
import com.github.marlonflorencio.proto.math.LongResponse;
import com.github.marlonflorencio.proto.math.MathServiceGrpc;
import com.github.marlonflorencio.proto.math.SumRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.time.Instant;

public class SquareRootClient {

    public static void main(String[] args) {
        new SquareRootClient().run();
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
        // created a greet service client (blocking - synchronous)
        MathServiceGrpc.MathServiceBlockingStub client = MathServiceGrpc.newBlockingStub(channel);

        int number = -1;

        try {
            client.squareRoot(LongRequest.newBuilder()
                    .setValue(number)
                    .build());
        } catch (StatusRuntimeException e) {
            System.out.println("Got an exception for square root!");
            e.printStackTrace();
        }
    }

}
