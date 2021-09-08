package com.github.marlonflorencio.grpc.math.server;


import com.github.marlonflorencio.proto.math.DoubleResponse;
import com.github.marlonflorencio.proto.math.LongRequest;
import com.github.marlonflorencio.proto.math.LongResponse;
import com.github.marlonflorencio.proto.math.MathServiceGrpc;
import com.github.marlonflorencio.proto.math.SumRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class MathServiceImpl extends MathServiceGrpc.MathServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<LongResponse> responseObserver) {

        LongResponse response = LongResponse.newBuilder()
                .setResult(request.getValue1() + request.getValue2())
                .build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

    @Override
    public void prime(LongRequest request, StreamObserver<LongResponse> responseObserver) {

        long k = 2;
        long n = request.getValue();

        while ( n > 1) {
            if (n % k == 0 ) {
                send(responseObserver, k);
                n /= k;
            } else {
                k++;
            }
        }

        responseObserver.onCompleted();
    }

    private void send(StreamObserver<LongResponse> responseObserver, long n) {
        responseObserver.onNext(
                LongResponse.newBuilder().setResult(n).build()
        );
    }


    @Override
    public StreamObserver<LongRequest> average(StreamObserver<DoubleResponse> responseObserver) {

        return new StreamObserver<>() {

            Long sum = 0L;
            int qtd = 0;

            @Override
            public void onNext(LongRequest value) {
                sum += value.getValue();
                qtd ++;
            }

            @Override
            public void onError(Throwable t) {
                // client sends an error
            }

            @Override
            public void onCompleted() {
                // client is done
                responseObserver.onNext(
                        DoubleResponse.newBuilder()
                                .setResult((double) sum / qtd)
                                .build()
                );
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<LongRequest> findMaximum(StreamObserver<LongResponse> responseObserver) {

        return new StreamObserver<>() {

            final LongResponse.Builder max = LongResponse.newBuilder().setResult(0L);

            @Override
            public void onNext(LongRequest value) {
                if (value.getValue() > max.getResult()) {
                    max.setResult(value.getValue());
                    responseObserver.onNext(max.build());
                }
            }

            @Override
            public void onError(Throwable t) {
                // do nothing
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(max.build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void squareRoot(LongRequest request, StreamObserver<DoubleResponse> responseObserver) {

        long number = request.getValue();

        if (number >= 0) {
            double numberRoot = Math.sqrt(number);
            responseObserver.onNext(
                    DoubleResponse.newBuilder().setResult(numberRoot).build()
            );
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("The number being sent is not positive")
                            .augmentDescription("Number sent: " + number)
                            .asRuntimeException()
            );
        }
    }
}
