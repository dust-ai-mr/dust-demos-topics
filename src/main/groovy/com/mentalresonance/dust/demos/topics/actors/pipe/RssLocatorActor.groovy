package com.mentalresonance.dust.demos.topics.actors.pipe

import com.mentalresonance.dust.core.actors.Actor
import com.mentalresonance.dust.core.actors.ActorBehavior
import com.mentalresonance.dust.core.actors.ActorRef
import com.mentalresonance.dust.core.actors.Props
import com.mentalresonance.dust.core.msgs.StartMsg
import com.mentalresonance.dust.http.service.HttpRequestResponseMsg
import com.mentalresonance.dust.http.trait.HttpClientActor
import com.mentalresonance.dust.nlp.chatgpt.ChatGptRequestResponseMsg
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * We ask GPT for RSS urls which it can sometimes find. It may hallucinate or the feed may have gone away
 * so we have to check it.
 */
@Slf4j
@CompileStatic
class RssLocatorActor extends Actor implements HttpClientActor {

	VerifyFeedsMsg verifyFeedsMsg
	ActorRef chatGPTRef
	String topic

	static Props props(String topic, ActorRef chatGPTRef) {
		Props.create(RssLocatorActor, topic, chatGPTRef)
	}

	RssLocatorActor(String topic, ActorRef chatGPTRef) {
		this.topic = topic
		this.chatGPTRef = chatGPTRef
	}

	@Override
	void preStart() {
		tellSelf(new StartMsg())
	}

	@Override
	ActorBehavior createBehavior() {
		(message) -> {
			switch(message) {
				case StartMsg:
					chatGPTRef.tell(new ChatGptRequestResponseMsg(
						"""Consider the following topic and give me a numerical list consisting *only* of urls for 
						   RSS feeds that might contain information about the topic: '$topic'.

					       Try hard to find many real RSS urls. Each entry should consist only of the URL - nothing else. 
					       Include no descriptive text."""
						),
						self
					)
					break

				case ChatGptRequestResponseMsg:
					ChatGptRequestResponseMsg msg = (ChatGptRequestResponseMsg)message
					List<String> urls = listFromUtterance(msg.utterance)
					self.tell(new VerifyFeedsMsg(urls), self)
					break

				case VerifyFeedsMsg:
					verifyFeedsMsg = (VerifyFeedsMsg)message
					if (! verifyFeedsMsg.urls.isEmpty()) {
						try {
							request(verifyFeedsMsg.urls.removeFirst())
						} catch (Exception e) {
							log.warn e.message
						}
					}
					break

				/*
				 * Verify the feed. Does the page exist and is it a feed ?? ChatGPT fails in both directions
				 */
				case HttpRequestResponseMsg:
					HttpRequestResponseMsg msg = (HttpRequestResponseMsg)message

					if (null == msg.exception && msg.response.successful) {
						try {
							new SyndFeedInput().build(new XmlReader(msg.response.body().byteStream()))
							parent.tell(msg.request.url().toString(), self)
						} catch (Exception e) {
							log.warn "URL: ${msg.request.url().toString()} exists but is not an RSS feed!"
						}
					} else
						log.warn "URL: ${msg.request.url().toString()} does not exist!"
					self.tell(verifyFeedsMsg, self)
					break


				default: super.createBehavior().onMessage(message as Serializable)
			}
		}
	}

	static List<String> listFromUtterance(String utterance) {
		List<String> list = []

		utterance.split('\n').each {
			if (it =~ "[0-9]+\\.") {
				int dot = 1 + it.indexOf('.')
				list << it[dot..-1]?.trim()
			}
		}
		list
	}

	static class VerifyFeedsMsg implements Serializable {
		List<String> urls

		VerifyFeedsMsg(List<String> urls) {
			this.urls = urls
		}
	}
}
