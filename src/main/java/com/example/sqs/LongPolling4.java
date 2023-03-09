//snippet-sourcedescription:[LongPolling.java demonstrates how to enable long polling on an Amazon Simple Queue Service (Amazon SQS) queue.]
//snippet-keyword:[AWS SDK for Java v2]
//snippet-service:[Amazon Simple Queue Service]
/*
   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
   SPDX-License-Identifier: Apache-2.0
*/

// snippet-start:[sqs.java2.long_polling.complete]
package com.example.sqs;
public class LongPolling4 {

    public static void main(String[] args) {

		LongPolling.consumeQueue(4);
    }

}
