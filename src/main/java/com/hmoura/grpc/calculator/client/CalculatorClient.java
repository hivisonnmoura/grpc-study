package com.hmoura.grpc.calculator.client;

import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.SumRequest;
import com.proto.calculator.SumResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CalculatorClient {

    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC Calculator Client");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052).usePlaintext().build();

        System.out.println("Creating stub");

        //Created a calculator service client (blocking -> synchronous)
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        //Do the same for a CalculatorRequest
        SumRequest request = SumRequest.newBuilder().setFirstValue(3).setSecondValue(10).build();

        //Call the RPC and get back a CalculatorResponse (protocol buffers)
        SumResponse response = calculatorClient.sum(request);
        //print the result
        System.out.println(request.getFirstValue() + " + " + request.getSecondValue() + " = " + response.getResult());

        //Shutdown the channel
        System.out.println("Shutting down channel");
        channel.shutdown();

    }
}
