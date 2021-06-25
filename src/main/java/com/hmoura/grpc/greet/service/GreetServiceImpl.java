package com.hmoura.grpc.greet.service;

import com.proto.greet.GreetEveryoneRequest;
import com.proto.greet.GreetEveryoneResponse;
import com.proto.greet.GreetManyTimesRequest;
import com.proto.greet.GreetManyTimesResponse;
import com.proto.greet.GreetRequest;
import com.proto.greet.GreetResponse;
import com.proto.greet.GreetServiceGrpc;
import com.proto.greet.GreetWithDeadlineRequest;
import com.proto.greet.GreetWithDeadlineResponse;
import com.proto.greet.Greeting;
import com.proto.greet.LongGreetRequest;
import com.proto.greet.LongGreetResponse;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

import java.util.stream.IntStream;

public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {

        //Extract the fields we need
        Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();
        String lastName = greeting.getLastName();

        //Creating the response
        String result = "Hello " + firstName + " " + lastName;
        GreetResponse response = GreetResponse.newBuilder()
                .setResult(result)
                .build();

        //Send the response
        responseObserver.onNext(response);

        //Complete the RPC call
        responseObserver.onCompleted();

    }

    @Override
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {

        String firstName = request.getGreeting().getFirstName();
        String lastName = request.getGreeting().getLastName();

        try {
            IntStream.range(0, 10).forEach(index -> {
                String result = "Hello " + firstName + " " + lastName + ", response number: " + index;
                GreetManyTimesResponse response = GreetManyTimesResponse.newBuilder().setResult(result).build();

                responseObserver.onNext(response);
            });
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            responseObserver.onCompleted();
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreetResponse> responseObserver) {

        return new StreamObserver<>() {

            String result = "";

            //Implement how to react when we receive a new message
            @Override
            public void onNext(LongGreetRequest value) {
                //Client sends a message
                result += "Hello " + value.getGreeting().getFirstName() + " " + value.getGreeting().getLastName() + "! ";
            }

            //Implement how to react when we receive an error
            @Override
            public void onError(Throwable t) {
                //Client sends an error
            }

            //Implement how to react when we the client is done
            @Override
            public void onCompleted() {
                //Client is done
                responseObserver.onNext(
                        LongGreetResponse.newBuilder()
                                .setResult(result)
                                .build()
                );
                responseObserver.onCompleted();
                //This is when we want to return a response (responseObserver)
            }
        };

    }

    @Override
    public StreamObserver<GreetEveryoneRequest> greetEveryone(StreamObserver<GreetEveryoneResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(GreetEveryoneRequest value) {
                String response = value.getGreeting().getFirstName() + " " + value.getGreeting().getLastName();
                GreetEveryoneResponse greetEveryoneResponse = GreetEveryoneResponse.newBuilder()
                        .setResult(response)
                        .build();
                responseObserver.onNext(greetEveryoneResponse);
            }

            @Override
            public void onError(Throwable t) {
                //TODO
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();

            }
        };
    }

    @Override
    public void greetWithDeadline(GreetWithDeadlineRequest request, StreamObserver<GreetWithDeadlineResponse> responseObserver) {

        Context current = Context.current();
        try {
            for (int i = 0; i < 3; i++) {
                if(current.isCancelled()){
                    return;
                } else {
                    System.out.println("Sleep for 100 ms");
                    Thread.sleep(100);
                }

            }

            System.out.println("Send Response");
            responseObserver.onNext(
                    GreetWithDeadlineResponse.newBuilder()
                            .setResponse("Hello: " + request.getGreeting().getFirstName())
                            .build()
            );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
