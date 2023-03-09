//snippet-sourcedescription:[LongPolling.java demonstrates how to enable long polling on an Amazon Simple Queue Service (Amazon SQS) queue.]
//snippet-keyword:[AWS SDK for Java v2]
//snippet-service:[Amazon Simple Queue Service]
/*
   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
   SPDX-License-Identifier: Apache-2.0
*/

// snippet-start:[sqs.java2.long_polling.complete]
package com.example.sqs;

// snippet-start:[sqs.java2.long_polling.import]

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;
// snippet-end:[sqs.java2.long_polling.import]

/*
 While the regular short polling returns immediately,
 long polling doesn't return a response until a message arrives
 in the message queue, or the long poll times out.
 */

/**
 * Before running this Java V2 code example, set up your development environment, including your credentials.
 *
 * For more information, see the following documentation topic:
 *
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html
 */
public class LongPolling {

    private static final String QueueName = "testQueueLongVisibilty5";

    public static void main(String[] args) {

        consumeQueue(1);
    }

	public static void consumeQueue(int runnerNum) {
		SqsClient sqsClient = SqsClient.builder()
				.region(Region.EU_NORTH_1)
				.credentialsProvider(ProfileCredentialsProvider.create())
				.build();

		setLongPoll(sqsClient, runnerNum) ;
		sqsClient.close();
	}

    // snippet-start:[sqs.java2.long_polling.main]
    public static void setLongPoll(SqsClient sqsClient, int runnerNum) {

        // Enable long polling when creating a queue.
        HashMap<QueueAttributeName, String> attributes = new HashMap<QueueAttributeName, String>();
        attributes.put(QueueAttributeName.RECEIVE_MESSAGE_WAIT_TIME_SECONDS, "20");

        CreateQueueRequest createRequest = CreateQueueRequest.builder()
            .queueName(QueueName)
//            .attributes(attributes)
            .build();

        try {
//            sqsClient.createQueue(createRequest);
            GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                .queueName(QueueName)
                .build();

            String queueUrl = sqsClient.getQueueUrl(getQueueRequest).queueUrl();
            SetQueueAttributesRequest setAttrsRequest = SetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributes(attributes)
                .build();

            sqsClient.setQueueAttributes(setAttrsRequest);


            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
				// Enable long polling on a message receipt.
                .waitTimeSeconds(20)
                .build();


			Random random = new Random();
			List<Message> messages;
			boolean stopRecieving = false;
			do {
				ReceiveMessageResponse response = sqsClient.receiveMessage(receiveRequest);

				messages = response.messages();

				// Print out the messages
				for (Message m : messages) {
					System.out.println(m.body());
					try {
//						if (m.body().endsWith("last batch")) {
//							stopRecieving = true;
//						}
						if (runnerNum == 3) {
							throw new RuntimeException("ERROR! Consumer problem");
						}
						if (runnerNum != 4 && m.body().endsWith(String.valueOf(random.nextInt(10)))) {
							throw new RuntimeException("ERROR! Random error");
						}

						Thread.sleep(random.nextInt(3000));

						DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
								.queueUrl(queueUrl)
								.receiptHandle(m.receiptHandle())
								.build();

						sqsClient.deleteMessage(deleteMessageRequest);
					} catch (RuntimeException|InterruptedException e) {
						System.out.println(e.getMessage());
					}
				}



			} while(!stopRecieving);
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
    // snippet-end:[sqs.java2.long_polling.main]
}

// snippet-end:[sqs.java2.long_polling.complete]