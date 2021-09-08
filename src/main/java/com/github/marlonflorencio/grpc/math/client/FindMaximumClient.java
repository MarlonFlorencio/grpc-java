package com.github.marlonflorencio.grpc.math.client;


import com.github.marlonflorencio.proto.math.LongRequest;
import com.github.marlonflorencio.proto.math.LongResponse;
import com.github.marlonflorencio.proto.math.MathServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FindMaximumClient {

    public static void main(String[] args) {
        new FindMaximumClient().run();
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

        StreamObserver<LongRequest> requestObserver = asyncClient.findMaximum(new StreamObserver<LongResponse>() {
            @Override
            public void onNext(LongResponse value) {
                System.out.println("Response from server: " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending data");
                latch.countDown();
            }
        });

        Arrays.asList(1,4,2,8,5,6,10,9).forEach(
                value -> {
                    System.out.println("Sending: " + value);
                    requestObserver.onNext(LongRequest.newBuilder().setValue(value).build());
                }
        );

        requestObserver.onCompleted();

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
