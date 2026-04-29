result_count : En envoyant le nombre de résultats dans le MDC, vous pouvez créer un graphique dans Kibana montrant l'évolution du volume de fichiers stockés au fil du temps sans même interroger votre base de données.

search_query : Cela vous permet de voir ce que les utilisateurs recherchent le plus. C'est très utile pour l'audit ou pour optimiser vos index de recherche plus tard.

file_db_id : En cas d'erreur sur un fichier spécifique, vous pouvez filtrer tous les logs liés à cet ID précis (action: * AND file_db_id: 125) pour voir tout l'historique : Upload -> Search -> Download -> Delete.



Points clés de cette configuration :
includeMdc à true : C'est le réglage crucial. Sans lui, vos MDC.put("file_type", ...) dans le service seraient ignorés. Ici, ils deviennent des colonnes directes dans Kibana.

Rotation des logs : Le fichier logs/app-json.log est celui que vous devrez pointer dans votre configuration Filebeat ou Logstash. La rotation évite de saturer le disque du serveur.

Performance : L'utilisation de LogstashEncoder est beaucoup plus performante que d'essayer de parser des logs textuels avec des expressions régulières (Grok) côté Logstash.