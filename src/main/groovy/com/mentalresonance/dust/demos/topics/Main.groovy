package com.mentalresonance.dust.demos.topics

import com.mentalresonance.dust.core.actors.ActorSystem
import com.mentalresonance.dust.demos.topics.actors.RootActor
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

@Slf4j
class Main {
	static void main(String[] args) {
		log.info "Hello News Reader!"

		ActorSystem system = new ActorSystem('news-reader')
		URL resource = system.class.classLoader.getResource("reader.json")
		Map config = new JsonSlurper().parse(new File(resource.toURI())) as Map
		/*
		 * Start an Actor who will manage everything else
		 * We end when he dies
		 */
		system.context.actorOf(
			RootActor.props((String)config.topic, (List<String>)config.entities), 'root'
		).waitForDeath()
	}
}

