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
import java.util.Map;
import java.util.Random;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
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
public class LongSending {

    private static final String QueueName = "testQueueLongVisibilty5";

	private static final int QUEUE_SIZE = 300;

	private static final int CONSUMER_COUNT = 4;

    public static void main(String[] args) {

        SqsClient sqsClient = SqsClient.builder()
            .region(Region.EU_NORTH_1)
            .credentialsProvider(ProfileCredentialsProvider.create())
            .build();

        setLongPoll(sqsClient) ;
        sqsClient.close();
    }

    // snippet-start:[sqs.java2.long_polling.main]
    public static void setLongPoll( SqsClient sqsClient) {

        // Enable long polling when creating a queue.
//		Map<QueueAttributeName, String> attributes = Map.of(QueueAttributeName.FIFO_QUEUE, "true", QueueAttributeName.CONTENT_BASED_DEDUPLICATION, "true", QueueAttributeName.RECEIVE_MESSAGE_WAIT_TIME_SECONDS, "20");
		Map<QueueAttributeName, String> attributes = Map.of(QueueAttributeName.RECEIVE_MESSAGE_WAIT_TIME_SECONDS, "20", QueueAttributeName.VISIBILITY_TIMEOUT, "5");

        CreateQueueRequest createRequest = CreateQueueRequest.builder()
            .queueName(QueueName)
            .attributes(attributes)
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

			Random random = new Random();
			for (int i = 0; i < QUEUE_SIZE; i++) {
				SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
						.queueUrl(queueUrl)
						.messageBody("Transaction id: " + i + (isLastBatch(i) ? " last batch" : ""))
//						.messageGroupId("localCron")
						//in case of FIFO get error: Value 5 for parameter DelaySeconds is invalid. Reason: The request include parameter that is not valid for this queue type.
						.delaySeconds(1)
						.build();

				sqsClient.sendMessage(sendMsgRequest);

				System.out.println("Sent message: " + sendMsgRequest.messageBody());
				Thread.sleep(random.nextInt(1000));
			}
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        } catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	static boolean isLastBatch(int i) {
		return QUEUE_SIZE - i <= CONSUMER_COUNT;
	}
    // snippet-end:[sqs.java2.long_polling.main]
}

// snippet-end:[sqs.java2.long_polling.complete]