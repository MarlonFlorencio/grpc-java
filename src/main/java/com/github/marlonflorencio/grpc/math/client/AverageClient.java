package com.github.marlonflorencio.grpc.math.client;

import com.github.marlonflorencio.grpc.util.TimeUtil;
import com.github.marlonflorencio.proto.math.DoubleResponse;
import com.github.marlonflorencio.proto.math.MathServiceGrpc;
import com.github.marlonflorencio.proto.math.LongRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AverageClient {

    public static void main(String[] args) {
        new AverageClient().run();
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

        MathServiceGrpc.MathServiceStub asyncClient = MathServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongRequest> requestObserver = asyncClient.average(new StreamObserver<>() {
            @Override
            public void onNext(DoubleResponse value) {
                System.out.println("Received a response from the server1: " + value.getResult() );
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
                System.out.println("Server has completed sending us something");
                latch.countDown();
            }
        });

        Instant start = Instant.now() ;

        for (int i = 0; i < 20_000; i++) {
            requestObserver.onNext(LongRequest.newBuilder().setValue(i).build());
        }

        requestObserver.onCompleted();

        TimeUtil.timeElapsed(start);

        try {
            latch.await(2L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
