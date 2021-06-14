package com.hmoura.grpc.calculator.service;

import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.SumRequest;
import com.proto.calculator.SumResponse;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {

        //Perform the sum operation
        long result = Integer.sum(request.getCalculator().getFirstValue(),
                request.getCalculator().getSecondValue());

        //Creating the response
        SumResponse response = SumResponse.newBuilder().setResult(result).build();

        //Send the response
        responseObserver.onNext(response);

        //Complete the RPC call
        responseObserver.onCompleted();
    }
}
