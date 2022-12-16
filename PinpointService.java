//snippet-sourcedescription:[PinpointService.java demonstrates how to send an email message.]
//snippet-keyword:[AWS SDK for Java v2]
//snippet-keyword:[Code Sample]
//snippet-keyword:[Amazon Pinpoint]
//snippet-sourcetype:[full-example]
//snippet-sourcedate:[05/18/2022]

/*
   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
   SPDX-License-Identifier: Apache-2.0
*/

package com.luxottica.tabmessaging.service.impl.aws;

//snippet-start:[pinpoint.java2.send_email.import]

import com.luxottica.tabmessaging.client.AWSClient;
import com.luxottica.tabmessaging.service.impl.ValidatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.pinpoint.model.AddressConfiguration;
import software.amazon.awssdk.services.pinpoint.model.ChannelType;
import software.amazon.awssdk.services.pinpoint.model.DirectMessageConfiguration;
import software.amazon.awssdk.services.pinpoint.model.EmailMessage;
import software.amazon.awssdk.services.pinpoint.model.MessageRequest;
import software.amazon.awssdk.services.pinpoint.model.MessageResponse;
import software.amazon.awssdk.services.pinpoint.model.PinpointException;
import software.amazon.awssdk.services.pinpoint.model.SMSMessage;
import software.amazon.awssdk.services.pinpoint.model.SendMessagesRequest;
import software.amazon.awssdk.services.pinpoint.model.SendMessagesResponse;
import software.amazon.awssdk.services.pinpoint.model.SimpleEmail;
import software.amazon.awssdk.services.pinpoint.model.SimpleEmailPart;

import java.util.HashMap;
import java.util.Map;
//snippet-end:[pinpoint.java2.send_email.import]

/**
 * Before running this Java V2 code example, set up your development environment, including your credentials.
 *
 * For more information, see the following documentation topic:
 *
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html
 */
@Service
public class PinpointService {


    private final Logger log = LoggerFactory.getLogger(getClass());

    // The character encoding the you want to use for the subject line and
    // message body of the email.
    private static final String CHARSET_BODY = "UTF-8";

    private static final ValidatorService.PhoneFormat PHONE_FORMAT = ValidatorService.PhoneFormat.E164;

    private final ValidatorService validatorService;

    private AWSClient awsClient;

    // The type of SMS message that you want to send. If you plan to send
    // time-sensitive content, specify TRANSACTIONAL. If you plan to send
    // marketing-related content, specify PROMOTIONAL.
    private static final String MESSAGE_TYPE = "TRANSACTIONAL";

    // The registered keyword associated with the originating short code.
    private static final String REGISTERED_KEYWORD = "myKeyword";

    // The sender ID to use when sending the message. Support for sender ID
    // varies by country or region. For more information, see
    // https://docs.aws.amazon.com/pinpoint/latest/userguide/channels-sms-countries.html
    private static final String SENDER_ID = "MySenderID";


    // The body of the email for recipients whose email clients support HTML content.

    public PinpointService(ValidatorService validatorService, AWSClient awsClient) {
        this.validatorService = validatorService;
        this.awsClient = awsClient;
    }

    public void sendEmail(String subject, String htmlBody, String senderAddress, String toAddress) {

        try {
            Map<String,AddressConfiguration> addressMap = new HashMap<>();
            AddressConfiguration configuration = AddressConfiguration.builder()
                    .channelType(ChannelType.EMAIL)
                    .build();

            addressMap.put(toAddress, configuration);
            SimpleEmailPart emailPart = SimpleEmailPart.builder()
                    .data(htmlBody)
                    .charset(CHARSET_BODY)
                    .build() ;

            SimpleEmailPart subjectPart = SimpleEmailPart.builder()
                    .data(subject)
                    .charset(CHARSET_BODY)
                    .build() ;

            SimpleEmail simpleEmail = SimpleEmail.builder()
                    .htmlPart(emailPart)
                    .subject(subjectPart)
                    .build();

            EmailMessage emailMessage = EmailMessage.builder()
                    .body(htmlBody)
                    .fromAddress(senderAddress)
                    .simpleEmail(simpleEmail)
                    .build();

            DirectMessageConfiguration directMessageConfiguration = DirectMessageConfiguration.builder()
                    .emailMessage(emailMessage)
                    .build();

            MessageRequest messageRequest = MessageRequest.builder()
                    .addresses(addressMap)
                    .messageConfiguration(directMessageConfiguration)
                    .build();

            SendMessagesRequest messagesRequest = SendMessagesRequest.builder()
                    .applicationId(awsClient.appId)
                    .messageRequest(messageRequest)
                    .build();

            awsClient.getPinpointClient().sendMessages(messagesRequest);

        } catch (PinpointException e) {
            log.error("Failed PinpSystem.out.printlnointService.sendEmail - Error : {}", e.getMessage());
        }
    }
    //snippet-end:[pinpoint.java2.send_email.main]

    public void sendSMSMessage(String message, String originationNumber, String destinationNumber) {

        log.info("PinPointService - Message: {} \n OriginationNumber: {} \n DestinationNumber: {}",
                message, originationNumber, destinationNumber);

        Map<String, AddressConfiguration> addressMap = new HashMap<String, AddressConfiguration>();

        AddressConfiguration addConfig = AddressConfiguration.builder()
                .channelType(ChannelType.SMS)
                .build();

        addressMap.put(validatorService.phoneFormatter(destinationNumber, PHONE_FORMAT), addConfig);

        SMSMessage smsMessage = SMSMessage.builder()
                .body(message)
                .messageType(MESSAGE_TYPE)
                .originationNumber(validatorService.phoneFormatter(originationNumber, PHONE_FORMAT))
                .senderId(SENDER_ID)
                .keyword(REGISTERED_KEYWORD)
                .build();

        log.info("PinPointService - SMSMessage object - Body: {} \n MessageType: {} \n OriginationNumber: {} \n" +
                        "SenderId: {} \n Keyword: {}",
                smsMessage.body(),
                smsMessage.messageType(),
                smsMessage.originationNumber(),
                smsMessage.senderId(),
                smsMessage.keyword());

        // Create a DirectMessageConfiguration object
        DirectMessageConfiguration direct = DirectMessageConfiguration.builder()
                .smsMessage(smsMessage)
                .build();

        MessageRequest msgReq = MessageRequest.builder()
                .addresses(addressMap)
                .messageConfiguration(direct)
                .build();
        msgReq.addresses().forEach((k, v) -> log.info("PinPointService - MessageRequest object- Address: {} {}",k, v));

        // create a  SendMessagesRequest object
        SendMessagesRequest request = SendMessagesRequest.builder()
                .applicationId(awsClient.appId)
                .messageRequest(msgReq)
                .build();
        log.info("PinPointService - Before call AWS.");
        log.info("PinPointService - SendMessagesRequest object - ApplicationId: {}", request.applicationId());
        SendMessagesResponse response= awsClient.getPinpointClient().sendMessages(request);
        log.info("PinPointService - After call AWS");
        response.messageResponse().result().forEach((k,v) -> log.info("PinPointService - SendMessagesResponse object - " +
                "Result: {} {}", k, v));
        log.info("PinPointService - SendMessagesResponse object - ApplicationId: {}\n" + "\n RequestId: {}",
                response.messageResponse().applicationId(),response.messageResponse().requestId());
        response.messageResponse().endpointResult().forEach((k,v) -> log.info("PinPointService - SendMessagesResponse " +
                "object - EndpointResult: {} {}", k, v));

        MessageResponse msg1 = response.messageResponse();
        Map map1 = msg1.result();

        //Write out the result of sendMessage
        map1.forEach((k, v) -> log.debug("{} : {}",k, v));
    }

}
