package com.hmoura.grpc.calculator.client;

import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.ComputeAverageRequest;
import com.proto.calculator.ComputeAverageResponse;
import com.proto.calculator.PrimeNumberDecompositionRequest;
import com.proto.calculator.SumRequest;
import com.proto.calculator.SumResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
        doClientStreamingCall(channel);


        System.out.println("Shutting down channel");
        channel.shutdown();
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

        System.out.println("Sending message 1");
        requestStreamObserver.onNext(ComputeAverageRequest.newBuilder()
        .setValue(1).build());

        System.out.println("Sending message 2");
        requestStreamObserver.onNext(ComputeAverageRequest.newBuilder()
                .setValue(2).build());

        System.out.println("Sending message 3");
        requestStreamObserver.onNext(ComputeAverageRequest.newBuilder()
                .setValue(3).build());

        System.out.println("Sending message 4");
        requestStreamObserver.onNext(ComputeAverageRequest.newBuilder()
                .setValue(4).build());

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
