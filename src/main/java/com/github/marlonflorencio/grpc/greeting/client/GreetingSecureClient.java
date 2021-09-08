package com.github.marlonflorencio.grpc.greeting.client;

import com.github.marlonflorencio.grpc.util.TimeUtil;
import com.github.marlonflorencio.proto.greet.GreetEveryoneRequest;
import com.github.marlonflorencio.proto.greet.GreetEveryoneResponse;
import com.github.marlonflorencio.proto.greet.GreetManyTimesRequest;
import com.github.marlonflorencio.proto.greet.GreetRequest;
import com.github.marlonflorencio.proto.greet.GreetResponse;
import com.github.marlonflorencio.proto.greet.GreetServiceGrpc;
import com.github.marlonflorencio.proto.greet.GreetWithDeadlineRequest;
import com.github.marlonflorencio.proto.greet.GreetWithDeadlineResponse;
import com.github.marlonflorencio.proto.greet.Greeting;
import com.github.marlonflorencio.proto.greet.LongGreetRequest;
import com.github.marlonflorencio.proto.greet.LongGreetResponse;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.net.ssl.SSLException;
import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingSecureClient {

    public static void main(String[] args) throws SSLException {
        System.out.println("Hello I'm a gRPC client");

        GreetingSecureClient main = new GreetingSecureClient();
        main.run();
    }

    private void run() throws SSLException {

        ManagedChannel secureChannel = NettyChannelBuilder.forAddress("localhost", 50051)
                .sslContext(GrpcSslContexts.forClient().trustManager(new File("tls/ca.crt")).build())
                .build();

        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(secureChannel);

        for (int i = 0; i < 1000; i++) {
            doUnaryCall(greetClient);
        }

        System.out.println("Shutting down channel");
        secureChannel.shutdown();
    }

    private void doUnaryCall(GreetServiceGrpc.GreetServiceBlockingStub greetClient) {

        Greeting greeting = Greeting.newBuilder()
                .setFirstName("John")
                .setLastName("Doe")
                .build();

        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        Instant start = Instant.now();

        GreetResponse greetResponse = greetClient.greet(greetRequest);

        TimeUtil.timeElapsedNano(start);

        System.out.println(greetResponse.getResult());
    }
}
