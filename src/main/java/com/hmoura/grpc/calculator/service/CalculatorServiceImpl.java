package com.hmoura.grpc.calculator.service;

import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.ComputeAverageRequest;
import com.proto.calculator.ComputeAverageResponse;
import com.proto.calculator.FindMaximumRequest;
import com.proto.calculator.FindMaximumResponse;
import com.proto.calculator.PrimeNumberDecompositionRequest;
import com.proto.calculator.PrimeNumberDecompositionResponse;
import com.proto.calculator.SumRequest;
import com.proto.calculator.SumResponse;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {

        //Perform the sum operation
        long result = Integer.sum(request.getFirstValue(),
                request.getSecondValue());

        //Creating the response
        SumResponse response = SumResponse.newBuilder().setResult(result).build();

        //Send the response
        responseObserver.onNext(response);

        //Complete the RPC call
        responseObserver.onCompleted();
    }

    @Override
    public void primeNumberDecomposition(PrimeNumberDecompositionRequest request, StreamObserver<PrimeNumberDecompositionResponse> responseObserver) {
        factors(request.getNumber()).forEach(factor ->
                responseObserver.onNext(PrimeNumberDecompositionResponse.newBuilder().setPrimeFactor(factor).build()));

        responseObserver.onCompleted();
    }

    private IntStream factors(int num) {
        return IntStream.range(2, num)
                .filter(x -> num % x == 0)
                .mapToObj(x -> IntStream.concat(IntStream.of(x), factors(num / x)))
                .findFirst()
                .orElse(IntStream.of(num));
    }


    @Override
    public StreamObserver<ComputeAverageRequest> computeAverage(StreamObserver<ComputeAverageResponse> responseObserver) {
        return new StreamObserver<>() {
            final List<Integer> listOfValues = new ArrayList<>();

            @Override
            public void onNext(ComputeAverageRequest value) {
                listOfValues.add(value.getValue());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println(t.getLocalizedMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(
                        ComputeAverageResponse.newBuilder()
                                .setAverage(
                                        listOfValues.stream()
                                                .mapToDouble(d -> d)
                                                .average()
                                                .orElseThrow()
                                )
                                .build()
                );
                responseObserver.onCompleted();
                System.out.println("Completed request");
            }
        };
    }

    @Override
    public StreamObserver<FindMaximumRequest> findMaximum(StreamObserver<FindMaximumResponse> responseObserver) {
        return new StreamObserver<>() {

            int  currentMaximum = 0;

            @Override
            public void onNext(FindMaximumRequest value) {
                int currentNumber = value.getValue();

                if(currentNumber > currentMaximum) {
                    currentMaximum = currentNumber;
                    responseObserver.onNext(
                            FindMaximumResponse.newBuilder()
                                    .setResponse(currentNumber)
                                    .build()
                    );
                } else {
                    //Nothing
                }

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
