package com.hmoura.grpc.calculator.service;

import com.proto.calculator.Calculator;
import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.SumRequest;
import com.proto.calculator.SumResponse;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {

        //Extract the values we need
        Calculator calculator = request.getCalculator();
        int firstValue = calculator.getFirstValue();
        int secondValue = calculator.getSecondValue();

        //Perform the sum operation
        long result = firstValue + secondValue;

        //Creating the response
        SumResponse response = SumResponse.newBuilder().setResult(result).build();

        //Send the response
        responseObserver.onNext(response);

        //Complete the RPC call
        responseObserver.onCompleted();
    }
}
