# Dust Topics
A moderately substantial demo application in Dust - Actors for Java - in which we show how to build 
pipelines to find and process information using Large Language Models.

We create a news reader which, when given a topic, finds valid RSS news feeds that will supply news
articles about that topic. Classes of interesting information (entities) are also supplied by the user,
and will be identified in the articles and logged.

The aim here is not so much to develop a fully-usable application (although surely one could easily
be built on this foundation), but rather to show how easy it is to set up NLP pipelines in Dust where 
various stages of the pipeline have dialogs with ChatGPT. 

A more detailed description can be found <a href="https://github.com/dust-ai-mr/dust-demos-topics/blob/main/docs/topics.pdf">here</a>.

# Building and running
First be sure to have built and locally published the Dust repos this application needs:

<a href="https://github.com/dust-ai-mr/dust-core">dust-core</a> - the heart and soul of Dust. 

<a href="https://github.com/dust-ai-mr/dust-http">dust-http</a> - Actors for creating HttpClients and interacting with
web end points. 

<a href="https://github.com/dust-ai-mr/dust-html">dust-html</a> - Actors and msgs for processing html documents
(as used in dust-http).

<a href="https://github.com/dust-ai-mr/dust-feeds">dust-feeds</a> - Built on the above provides Actors for processing and
managing RSS feeds, crawling web sites and using SearXNG for web search.

<a href="https://github.com/dust-ai-mr/dust-nlp">dust-nlp</a> - Actors to
interact with OpenAI and Ollama LLMs (and a generic Actor for OpenAI-like apis which can be tailored to fit
other providers).

Edit the file reader.json to define your topic and entities. The default is:
```json
{
   "topic" :  "Electric Vehicle Charging",
   "entities":  ["Company", "Technology", "Product", "Location"]
}
```
Run the code supplying your ChatGPT key as an environment variable:
```
ChatGPTKey=your-key-here ./gradlew run
```

And see something like this in the log ...
```
RssLocatorActor - URL: https://cleantechnica.com/feed/ does not exist!
RssLocatorActor - URL: https://www.greencarreports.com/rss/news does not exist!
FeedHubActor - adding RSS feed https://chargedevs.com/feed/
RssLocatorActor - URL: https://www.greencarcongress.com/index.xml exists but is not an RSS feed!
FeedHubActor - adding RSS feed https://www.electrive.com/feed/
RssLocatorActor - URL: https://www.autoblog.com/rss.xml does not exist!
RssLocatorActor - URL: https://www.plugincars.com/feed exists but is not an RSS feed!
FeedHubActor - adding RSS feed https://www.teslarati.com/feed/
[https://www.electrive.com/2024/10/25/mercedes-bmw-get-green-light-for-fast-charging-network-in-china/, company, [Mercedes-Benz, BMW, Ionity, General Motors, Honda, Hyundai, Kia, Stellantis, Toyota, PowerX, Ashok Leyland]]
[https://www.electrive.com/2024/10/25/mercedes-bmw-get-green-light-for-fast-charging-network-in-china/, technology, [Ionchi, Plug&Charge]]
[https://www.electrive.com/2024/10/25/mercedes-bmw-get-green-light-for-fast-charging-network-in-china/, product, [Piaggio EVs, Switch EiV12 electric buses]]
[https://www.electrive.com/2024/10/25/mercedes-bmw-get-green-light-for-fast-charging-network-in-china/, location, [China, European Economic Area, Beijing, Qingdao, Nanjing, North America, North Carolina, Mannheim, Sandy Springs, Japan]]
[https://www.electrive.com/2024/10/24/tesla-appoints-new-head-of-charging-infrastructure-and-reveals-plans-for-charging-park/, company, [Tesla]]
[https://www.electrive.com/2024/10/24/tesla-appoints-new-head-of-charging-infrastructure-and-reveals-plans-for-charging-park/, technology, [Supercharger, Megapack, Powerwall, Solar system, Megapack stationary storage units]]
[https://www.electrive.com/2024/10/24/tesla-appoints-new-head-of-charging-infrastructure-and-reveals-plans-for-charging-park/, product, [Supercharger charging stations, Megapack division, Powerwall home power storage system]]
[https://www.electrive.com/2024/10/24/tesla-appoints-new-head-of-charging-infrastructure-and-reveals-plans-for-charging-park/, location, [California, San Francisco, Los Angeles, Interstate 5, Lost Hills]]
[https://chargedevs.com/features/paired-powers-ev-chargers-let-customers-mix-and-match-solar-storage-and-grid-power/, company, [Paired Power]]
[https://chargedevs.com/features/paired-powers-ev-chargers-let-customers-mix-and-match-solar-storage-and-grid-power/, technology, [EV chargers]]
[https://chargedevs.com/features/paired-powers-ev-chargers-let-customers-mix-and-match-solar-storage-and-grid-power/, product, [PairTree, PairFleet]]
[https://chargedevs.com/features/paired-powers-ev-chargers-let-customers-mix-and-match-solar-storage-and-grid-power/, location, [California]]
[https://electrek.co/2024/10/23/lg-dc-fast-charger-us/, company, [LG Business Solutions USA, LG Electronics]]
[https://electrek.co/2024/10/23/lg-dc-fast-charger-us/, technology, [CCS/NACS, SAE J1772, UL 2594, USB, Power Bank, Over-the-air software updates]]
[https://electrek.co/2024/10/23/lg-dc-fast-charger-us/, product, [LG DC fast charger, LG EVD175SK-PN, Level 2 chargers, Level 3 chargers, Ultra-fast chargers]]
[https://electrek.co/2024/10/23/lg-dc-fast-charger-us/, location, [US, Texas, Fort Worth, Nevada, White River Junction]]
[https://electrek.co/2024/10/23/tesla-unveils-oasis-supercharger-concept-solar-farm-megapacks/, company, [Tesla]]
```
And many more ...


