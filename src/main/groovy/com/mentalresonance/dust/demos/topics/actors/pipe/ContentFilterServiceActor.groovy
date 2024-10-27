package com.mentalresonance.dust.demos.topics.actors.pipe

import com.mentalresonance.dust.core.actors.Actor
import com.mentalresonance.dust.core.actors.ActorBehavior
import com.mentalresonance.dust.core.actors.ActorRef
import com.mentalresonance.dust.core.actors.Props
import com.mentalresonance.dust.html.msgs.HtmlDocumentMsg
import com.mentalresonance.dust.nlp.chatgpt.ChatGptRequestResponseMsg
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class ContentFilterServiceActor extends Actor {

	String filter
	ActorRef originalSender, chatGPTRef
	HtmlDocumentMsg originalMsg

	static Props props(String filter, ActorRef chatGPTRef) {
		Props.create(ContentFilterServiceActor, filter, chatGPTRef)
	}

	ContentFilterServiceActor(String filter, ActorRef chatGPTRef) {
		this.filter = filter
		this.chatGPTRef =  chatGPTRef
	}

	@Override
	ActorBehavior createBehavior() {
		(message) -> {
			switch(message) {
				case HtmlDocumentMsg:
					originalMsg = (HtmlDocumentMsg)message
					String request =  "Does '${originalMsg.title}' refer to $filter " +
									  "Answer simply yes or no."
					chatGPTRef.tell(new ChatGptRequestResponseMsg(request), self)
					break

				case ChatGptRequestResponseMsg:
					ChatGptRequestResponseMsg msg = (ChatGptRequestResponseMsg)message
					String response = msg.getUtterance()?.toLowerCase()

					if (response?.toLowerCase()?.trim()?.startsWith('yes')) {
						log.trace "**** ${originalMsg.uuid} ${originalMsg.title} PASSES filter $filter"
						/*
						 * The actual pipe member is my parent (the service manager) and my grand parent
						 * is the pipe, so send response back to the pipe as though from my parent
						 */
						grandParent.tell(originalMsg, parent)
					}
					else
						log.trace "**** ${originalMsg.uuid} ${originalMsg.title} FAILS filter $filter"
					stopSelf()
					break
			}
		}
	}
}
