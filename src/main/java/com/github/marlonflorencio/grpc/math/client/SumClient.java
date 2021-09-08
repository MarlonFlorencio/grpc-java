package com.github.marlonflorencio.grpc.math.client;

import com.github.marlonflorencio.grpc.util.TimeUtil;
import com.github.marlonflorencio.proto.math.LongResponse;
import com.github.marlonflorencio.proto.math.MathServiceGrpc;
import com.github.marlonflorencio.proto.math.SumRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.time.Instant;

public class SumClient {

    public static void main(String[] args) {
        new SumClient().run();
    }

    private void run() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        for (int i = 0; i < 1000; i++) {
            doUnaryCall(channel, i);
        }

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void doUnaryCall(ManagedChannel channel, int i) {
        // created a greet service client (blocking - synchronous)
        MathServiceGrpc.MathServiceBlockingStub client = MathServiceGrpc.newBlockingStub(channel);

        // do the same for a GreetRequest
        SumRequest request = SumRequest.newBuilder()
                .setValue1(i)
                .setValue2(3)
                .build();

        // call the RPC and get back a GreetResponse (protocol buffers)
        Instant start = Instant.now();

        LongResponse response = client.sum(request);

        TimeUtil.timeElapsed(start);

        System.out.println(response.getResult());
    }

}
