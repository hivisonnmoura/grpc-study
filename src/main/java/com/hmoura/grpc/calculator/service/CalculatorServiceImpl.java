package com.hmoura.grpc.calculator.service;

import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.PrimeNumberDecompositionRequest;
import com.proto.calculator.PrimeNumberDecompositionResponse;
import com.proto.calculator.SumRequest;
import com.proto.calculator.SumResponse;
import io.grpc.stub.StreamObserver;

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

}
