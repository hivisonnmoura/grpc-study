package com.hmoura.grpc.greeting.client;

import com.proto.greet.GreetRequest;
import com.proto.greet.GreetResponse;
import com.proto.greet.GreetServiceGrpc;
import com.proto.greet.Greeting;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GreetingClient {
    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC client");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext().build();

        System.out.println("Creating stub");

        //old and dummy
        //DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);
        //DummyServiceGrpc.DummyServiceFutureStub asyncClient = DummyServiceGrpc.DummyServiceFutureStub.newStub(channel);

        //Created a greet service client (blocking -> synchronous)
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        //Created a protocol buffer greeting message
        Greeting greeting = Greeting.newBuilder().setFirstName("Hivison").setLastName("Moura").build();

        //Do the same for a GreetRequest
        GreetRequest greetRequest = GreetRequest.newBuilder().setGreeting(greeting).build();

        //Call the RPC and get back a GreetResponse (protocol buffers)
        GreetResponse greetResponse = greetClient.greet(greetRequest);

        System.out.println(greetResponse.getResult());

        //Do something
        System.out.println("Shutting down channel");
        channel.shutdown();
    }
}
