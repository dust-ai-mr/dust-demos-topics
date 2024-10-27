package com.mentalresonance.dust.demos.topics.actors.pipe

import com.mentalresonance.dust.core.actors.*
import com.mentalresonance.dust.core.msgs.StopMsg
import com.mentalresonance.dust.html.msgs.HtmlDocumentMsg
import com.mentalresonance.dust.nlp.chatgpt.ChatGptRequestResponseMsg
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Identify occurrences of the named entity classes in the document. LLMs have some difficulty with this
 * often throwing in things which do not match teh entity type, so we take the first response and send
 * it back with a challenge to check it and fix it if necessary. This isn't 100% but it helps.
 */
@CompileStatic
@Slf4j
class EntitiesExtractionServiceActor extends Actor {

	ActorRef originalSender, chatGPTRef
	HtmlDocumentMsg originalMsg
	List<String> entities

	static Props props(List<String> entities, ActorRef chatGPTRef) {
		Props.create(EntitiesExtractionServiceActor, entities, chatGPTRef)
	}

	EntitiesExtractionServiceActor(List<String> entities, ActorRef chatGPTRef) {
		this.entities = entities
		this.chatGPTRef = chatGPTRef
	}

	@Override
	void preStart() {
		dieIn(10*60*1000)
	}


	@Override
	ActorBehavior createBehavior() {
		(message) -> {
			switch(message)
			{
				case HtmlDocumentMsg:
					String mainText

					originalMsg = (HtmlDocumentMsg)message
					originalSender = sender
					mainText = originalMsg.getWholeText()

					if (mainText) {
						String text = "${originalMsg.title} --- $mainText"
						chatGPTRef.tell(
							new ChatGptRequestResponseMsg(
								"""Following is a list of entity categories: ${entities.join(', ')}.
								 For each category give me a numerical list of mentions in the text. 
								 Precede each list with its category followed by ':'.
								 Do not create new categories. Reply in plain text, not markdown.
								 If the entity mentioned is a company use its formal name.
								 ${text}"""),
							self
						)
					}
					else
						context.stop(self)
					break

				case ChatGptRequestResponseMsg:
				ChatGptRequestResponseMsg msg = (ChatGptRequestResponseMsg)message
					fromEntitiesList(msg.getUtterance())?.each {
						if (it.value != [])
							grandParent.tell([originalMsg.source, it.key, it.value] as Serializable, parent)
					}
					stopSelf()
					break
			}
		}
	}

	/**
     * The returned utterance is of the form:
     *
	Countries:
	1. Ethiopia
	2. Nigeria

	Cities:
	1. Addis Ababa
	2. Atsbi
	3. Mekelle

	 * @param utterance the returned utterance from ChatGPT
	 * @return map of entity class -> list of named entities in that class

     */
	static Map<String, List<String>> fromEntitiesList(String utterance) {
		Map<String, List<String>> entities = [:]
		String caseEntity

		try {
			utterance
				?.split('\n')
				?.collect { it.trim() }
				?.findAll { it }
				?.forEach {
					if (! it.startsWith('*')) {
						if (it.contains(':')) {
							caseEntity = it.toLowerCase()[0..<it.indexOf(':')]
							entities[caseEntity] = []
						}
						else if (it =~ "[0-9]+\\.") {
							int dot = 1 + it.indexOf('.')
							String rest = it[dot..-1]?.trim()

							if (rest && ! rest.startsWith('*')) {
								entities[caseEntity] << rest
							}
						}
					}
				}
		} catch(Exception e) {
			e.printStackTrace()
			log.error "fromEntitiesList ${e.message}"
		}
		entities.collectEntries { [it.key, it.value.unique()] }
	}
}
