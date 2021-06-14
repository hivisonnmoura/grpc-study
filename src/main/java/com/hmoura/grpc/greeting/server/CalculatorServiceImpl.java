package com.hmoura.grpc.greeting.server;

import com.proto.calculator.Calculator;
import com.proto.calculator.CalculatorRequest;
import com.proto.calculator.CalculatorResponse;
import com.proto.calculator.CalculatorServiceGrpc;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void calculator(CalculatorRequest request, StreamObserver<CalculatorResponse> responseObserver) {

        //Extract the values we need
        Calculator calculator = request.getCalculator();
        int firstValue = calculator.getFirstValue();
        int secondValue = calculator.getSecondValue();

        //Perform the sum operation
        long result = firstValue + secondValue;

        //Creating the response
        CalculatorResponse response = CalculatorResponse.newBuilder().setResult(result).build();

        //Send the response
        responseObserver.onNext(response);

        //Complete the RPC call
        responseObserver.onCompleted();
    }
}
