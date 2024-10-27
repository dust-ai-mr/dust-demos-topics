package com.mentalresonance.dust.demos.topics.actors

import com.mentalresonance.dust.core.actors.Actor
import com.mentalresonance.dust.core.actors.ActorRef
import com.mentalresonance.dust.core.actors.Props
import com.mentalresonance.dust.core.actors.lib.LogActor
import com.mentalresonance.dust.core.actors.lib.PipelineActor
import com.mentalresonance.dust.core.actors.lib.ServiceManagerActor
import com.mentalresonance.dust.demos.topics.actors.pipe.ContentFilterServiceActor
import com.mentalresonance.dust.demos.topics.actors.pipe.EntitiesExtractionServiceActor
import com.mentalresonance.dust.demos.topics.actors.pipe.FeedHubActor
import com.mentalresonance.dust.demos.topics.actors.pipe.RssLocatorActor
import com.mentalresonance.dust.nlp.chatgpt.ChatGptAPIServiceActor
import groovy.util.logging.Slf4j

@Slf4j
class RootActor extends Actor {

	String topic
	List<String> entities
	String key = System.getenv('ChatGPTKey')

	static Props props(String topic, List<String> entities) {
		Props.create(RootActor, topic, entities)
	}

	RootActor(String topic, List<String> entities) {
		this.topic = topic
		this.entities = entities
	}

	@Override
	void preStart() {
		ActorRef chatGPTRef = actorOf(ServiceManagerActor.props(
				ChatGptAPIServiceActor.props(null, key),
				4
			),
			'chat-gpt'
		)

		actorOf( PipelineActor.props([
			RssLocatorActor.props(topic, chatGPTRef),
			FeedHubActor.props(),
			ServiceManagerActor.props(ContentFilterServiceActor.props(topic, chatGPTRef), 4),
			ServiceManagerActor.props(EntitiesExtractionServiceActor.props(entities, chatGPTRef), 4),
			LogActor.props()
			], [
		    	'rss-locator',
				'feeds',
				'topic-filter',
				'entities-extraction',
				'logger'
			]
		), 'pipeline')

		log.info "Started ${self.path}"
	}
}
