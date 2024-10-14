package com.sk.customer.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

@Slf4j
public class SimpleLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {


     @Override
     public String getName() {
          return this.getClass().getSimpleName();
     }

     @Override
     public int getOrder() {
          return 0;
     }

     @Override
     public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
          log.debug("BEFORE: {}", advisedRequest);
          AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
          log.debug("AFTER: {}", advisedResponse);
          return advisedResponse;
     }

     @Override
     public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
          log.debug("BEFORE: {}", advisedRequest);
          Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
          return new MessageAggregator().aggregateAdvisedResponse(advisedResponses,
                  advisedResponse -> log.debug("AFTER: {}", advisedResponse));
     }
}