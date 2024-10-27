package com.mentalresonance.dust.demos.topics.actors.pipe

import com.mentalresonance.dust.core.actors.Actor
import com.mentalresonance.dust.core.actors.ActorBehavior
import com.mentalresonance.dust.core.actors.Props
import com.mentalresonance.dust.core.actors.SupervisionStrategy
import com.mentalresonance.dust.core.msgs.StartMsg
import com.mentalresonance.dust.feeds.rss.TransientRssFeedPipeActor
import com.mentalresonance.dust.html.msgs.HtmlDocumentMsg
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class FeedHubActor extends Actor {

	static Props props() {
		Props.create(FeedHubActor)
	}

	@Override
	void preStart() {
		supervisor = new SupervisionStrategy(SupervisionStrategy.SS_RESTART, SupervisionStrategy.MODE_ONE_FOR_ONE)
		super.preStart()
	}

	@Override
	ActorBehavior createBehavior() {
		(message) -> {
			switch(message) {
				case String:
					String msg = (String) message
					log.info "${self.path} adding RSS feed ${message}"
					actorOf(TransientRssFeedPipeActor.props(msg, 3600*1000L), msg.md5()).tell(new StartMsg(), self)
					break

				case HtmlDocumentMsg:
					parent.tell(message, self)
					break

				default: super.createBehavior().onMessage(message as Serializable)
			}
		}
	}
}
