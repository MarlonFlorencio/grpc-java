package com.github.marlonflorencio.grpc.math.client;

import com.github.marlonflorencio.grpc.util.TimeUtil;
import com.github.marlonflorencio.proto.math.MathServiceGrpc;
import com.github.marlonflorencio.proto.math.LongResponse;
import com.github.marlonflorencio.proto.math.SumRequest;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MathAsyncClient {

    public static void main(String[] args) {
        new MathAsyncClient().run();
    }

    private void run() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        Instant start = Instant.now();
        Executor listeningExecutor = Executors.newFixedThreadPool(1);

        for (int i = 0; i < 10000; i++) {
            doUnaryCall(channel, i, listeningExecutor);
        }

        TimeUtil.timeElapsed(start);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void doUnaryCall(ManagedChannel channel, int i, Executor listeningExecutor) {
        // created a greet service client (blocking - synchronous)
        MathServiceGrpc.MathServiceFutureStub client = MathServiceGrpc.newFutureStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        // do the same for a GreetRequest
        SumRequest request = SumRequest.newBuilder()
                .setValue1(i)
                .setValue2(i)
                .build();

        ListenableFuture<LongResponse> response = client.sum(request);

        Futures.addCallback(response, new FutureCallback<>() {
            @Override
            public void onSuccess(LongResponse result) {
                System.out.println(result.getResult());
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, listeningExecutor);

        try {
            latch.await(20L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
