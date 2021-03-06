package com.hmoura.grpc.greet.client;

import com.proto.greet.GreetEveryoneRequest;
import com.proto.greet.GreetEveryoneResponse;
import com.proto.greet.GreetManyTimesRequest;
import com.proto.greet.GreetRequest;
import com.proto.greet.GreetResponse;
import com.proto.greet.GreetServiceGrpc;
import com.proto.greet.GreetWithDeadlineRequest;
import com.proto.greet.Greeting;
import com.proto.greet.LongGreetRequest;
import com.proto.greet.LongGreetResponse;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    public static void main(String[] args) throws SSLException {
        System.out.println("Hello I'm a gRPC client");

        GreetingClient main = new GreetingClient();
        main.run();
    }

    private void run() throws SSLException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext().build();

        //With server authentication SSL/TLS; custom CA root certificates;
        final var securedChannel = NettyChannelBuilder.forAddress("localhost", 50051)
                .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.crt")).build())
                .build();
        //doUnaryCall(channel);
        //doServerStreamingCall(channel);
        //doClientStreamingCall(channel);
        //doBiDirectionalStreamingCall(channel);
        //doUnaryCallWithDeadline(channel);

        //Calling server using a secured channel
        doUnaryCall(securedChannel);

        System.out.println("Shutting down channel");
        channel.shutdown();

    }

    private void doUnaryCallWithDeadline(ManagedChannel channel) {
        final var blockingStub = GreetServiceGrpc.newBlockingStub(channel);

        //first call (500 ms deadline)
        try {
            System.out.println("Sending a request with a deadline of 3000 ms");
            final var response = blockingStub.withDeadline(Deadline.after(3000, TimeUnit.MILLISECONDS)).greetWithDeadline(GreetWithDeadlineRequest.newBuilder()
                    .setGreeting(Greeting.newBuilder().setFirstName("Hivison").getDefaultInstanceForType())
                    .build());
            System.out.println(response);
        } catch (StatusRuntimeException ex) {
            if (ex.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("deadline has been exceeded, we don't want the response");
            } else {
                ex.printStackTrace();
            }
        }

        //second call (100 ms deadline)
        try {
            System.out.println("Sending a request with a deadline of 100 ms");
            final var response = blockingStub.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS)).greetWithDeadline(GreetWithDeadlineRequest.newBuilder()
                    .setGreeting(Greeting.newBuilder().setFirstName("Hivison").getDefaultInstanceForType())
                    .build());
            System.out.println(response);
        } catch (StatusRuntimeException ex) {
            if (ex.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("deadline has been exceeded, we don't want the response");
            } else {
                ex.printStackTrace();
            }
        }
    }

    private void doBiDirectionalStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetEveryoneRequest> requestObserver = asyncClient.greetEveryone(new StreamObserver<>() {
            @Override
            public void onNext(GreetEveryoneResponse value) {
                System.out.println("Response from server: " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending data");
                latch.countDown();
            }
        });

        Arrays.asList("Hivison", "Stephan", "John", "Marc", "Patricia").forEach(
                name -> {
                    System.out.println("Sending: " + name);
                    requestObserver.onNext(GreetEveryoneRequest.newBuilder()
                            .setGreeting(Greeting.newBuilder()
                                    .setFirstName(name)
                                    .setLastName(""))
                            .build());

                    //This thread sleep is just of show the async works
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
        //Create a client (stub)
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongGreetRequest> requestObserver = asyncClient.longGreet(new StreamObserver<>() {
            @Override
            public void onNext(LongGreetResponse value) {
                //We get a response from the server
                System.out.println("Received a response from the server");
                System.out.println(value.getResult());
                //onNext will be called only once
            }

            @Override
            public void onError(Throwable t) {
                // We get an error from the server
            }

            @Override
            public void onCompleted() {
                //The server os done sending data
                //onCompleted will be called right after onNext()
                System.out.println("Server has completed sending us something");
                latch.countDown();
            }
        });

        //Streaming message #1
        System.out.println("Sending message 1");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Hivison")
                        .setLastName("Moura")
                        .build())
                .build());

        //Streaming message #2
        System.out.println("Sending message 2");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Stephane")
                        .setLastName("Mark")
                        .build())
                .build());

        //Streaming message #3
        System.out.println("Sending message 3");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Davidu")
                        .setLastName("S??oltach")
                        .build())
                .build());

        // we tell the server that the client is done sending data
        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void doServerStreamingCall(ManagedChannel channel) {
        //Created a greet service client (blocking -> synchronous)
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        GreetManyTimesRequest greetManyTimesRequest =
                GreetManyTimesRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder()
                                .setFirstName("Hivison")
                                .setLastName("Moura"))
                        .build();
        greetClient.greetManyTimes(greetManyTimesRequest).forEachRemaining(
                greetManyTimesResponse -> System.out.println(greetManyTimesResponse.getResult()));
    }

    private void doUnaryCall(ManagedChannel channel) {

        //Created a greet service client (blocking -> synchronous)
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        //Created a protocol buffer greeting message
        Greeting greeting = Greeting.newBuilder().setFirstName("Hivison").setLastName("Moura").build();

        //Do the same for a GreetRequest
        GreetRequest greetRequest = GreetRequest.newBuilder().setGreeting(greeting).build();

        //Call the RPC and get back a GreetResponse (protocol buffers)
        GreetResponse greetResponse = greetClient.greet(greetRequest);

        System.out.println(greetResponse.getResult());
    }
}
