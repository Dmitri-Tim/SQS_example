//snippet-sourcedescription:[SendMessages.java demonstrates how to send messages to an Amazon Simple Queue Service (Amazon SQS) queue.]
//snippet-keyword:[AWS SDK for Java v2]
//snippet-service:[Amazon Simple Queue Service]

/*
   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
   SPDX-License-Identifier: Apache-2.0
*/

package com.example.sqs;

// snippet-start:[sqs.java2.send_recieve_messages.import]
import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;
// snippet-end:[sqs.java2.send_recieve_messages.import]

/**
 * Before running this Java V2 code example, set up your development environment, including your credentials.
 *
 * For more information, see the following documentation topic:
 *
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html
 */
public class SendMessages {

    public static void main(String[] args) {

        final String usage = "\n" +
            "Usage: " +
            "   <queueName> <message>\n\n" +
            "Where:\n" +
            "   queueName - The name of the queue.\n\n" +
            "   message - The message to send.\n\n";

//        if (args.length != 2) {
//            System.out.println(usage);
//            System.exit(1);
//        }

        String queueName = "appCreatedTrxFee.fifo";
        String message = "12345678";
        SqsClient sqsClient = SqsClient.builder()
            .region(Region.EU_NORTH_1)
            .credentialsProvider(ProfileCredentialsProvider.create())
            .build();
		for (int i = 0; i < 20; i++) {
			sendMessage(sqsClient, queueName, "messageB" + i);
		}

        sqsClient.close();
    }

    // snippet-start:[sqs.java2.send_recieve_messages.main]
    public static void sendMessage(SqsClient sqsClient, String queueName, String message) {

        try {
			Map<QueueAttributeName, String> attributes = Map.of(QueueAttributeName.FIFO_QUEUE, "true", QueueAttributeName.CONTENT_BASED_DEDUPLICATION, "true");
            CreateQueueRequest request = CreateQueueRequest.builder()
                .queueName(queueName)
				.attributes(attributes)
                .build();
            sqsClient.createQueue(request);

            GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();

            String queueUrl = sqsClient.getQueueUrl(getQueueRequest).queueUrl();
            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
				.messageGroupId("localCron")
                .messageBody(message)
                .delaySeconds(0)
                .build();

            sqsClient.sendMessage(sendMsgRequest);

        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
    // snippet-end:[sqs.java2.send_recieve_messages.main]
}

