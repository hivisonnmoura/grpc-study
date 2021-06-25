package com.hmoura.grpc.calculator.client;

import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.ComputeAverageRequest;
import com.proto.calculator.ComputeAverageResponse;
import com.proto.calculator.FindMaximumRequest;
import com.proto.calculator.FindMaximumResponse;
import com.proto.calculator.PrimeNumberDecompositionRequest;
import com.proto.calculator.SquareRootRequest;
import com.proto.calculator.SumRequest;
import com.proto.calculator.SumResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class CalculatorClient {

    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC Calculator Client");

        CalculatorClient main = new CalculatorClient();
        main.run();

    }

    private void run() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052).usePlaintext().build();

        //doUnaryCall(channel);
        //doServerStreamingCall(channel);
        //doClientStreamingCall(channel);
        //doBiDirectionalStreamingCall(channel);
        doSquareErrorCall(channel);


        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void doSquareErrorCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub blockingStub = CalculatorServiceGrpc.newBlockingStub(channel);

        try {
            blockingStub.squareRoot(
                    SquareRootRequest.newBuilder()
                            .setNumber(-1).build());
        } catch (StatusRuntimeException ex) {
            System.out.println("Got an exception for square root!");
            ex.printStackTrace();
        }

    }

    private void doBiDirectionalStreamingCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);


        StreamObserver<FindMaximumRequest> requestObserver = asyncClient.findMaximum(new StreamObserver<>() {
            @Override
            public void onNext(FindMaximumResponse value) {
                System.out.println("Response from server: "+value.getResponse());
            }

            @Override
            public void onError(Throwable t) { latch.countDown(); }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending data");
                latch.countDown();

            }
        });

        Arrays.asList(1,5,3,6,2,20).forEach(
                maxValue -> {
                    System.out.println("Sending: "+ maxValue);
                    requestObserver.onNext(FindMaximumRequest.newBuilder()
                    .setValue(maxValue)
                    .build());
                }
        );


        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doClientStreamingCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<ComputeAverageRequest> requestStreamObserver = asyncClient.computeAverage(new StreamObserver<>() {
            @Override
            public void onNext(ComputeAverageResponse value) {
                System.out.println("Received a response from the server");
                System.out.println(value.getAverage());
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

        IntStream.rangeClosed(1, 10_000).forEach( i -> {
            System.out.println("Sending message "+ i);
            requestStreamObserver.onNext(ComputeAverageRequest.newBuilder()
                    .setValue(i).build());
        });

        requestStreamObserver.onCompleted();
        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void doServerStreamingCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        PrimeNumberDecompositionRequest request =
                PrimeNumberDecompositionRequest.newBuilder()
                        .setNumber(123123123).build();
        calculatorClient.primeNumberDecomposition(request).forEachRemaining(
                primeResponse -> System.out.println(primeResponse.getPrimeFactor())
        );
    }

    private void doUnaryCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        SumRequest request = SumRequest.newBuilder().setFirstValue(3).setSecondValue(10).build();

        //Call the RPC and get back a CalculatorResponse (protocol buffers)
        SumResponse response = calculatorClient.sum(request);
        //print the result
        System.out.println(request.getFirstValue() + " + " + request.getSecondValue() + " = " + response.getResult());
    }
}
