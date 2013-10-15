================
= Build Project
================
mvn clean package

======================
= Load BAG Mutaties
======================
Usage: nl.ordina.bag.etl.LoadMutaties
Description: Downloads mail messages from bag mailbox. Parses the link to the BAGMutatiesFile from mail message and download the BAGMutatiesFile. Parses, validates and imports BAGMutatiesFiles into table bag_mutaties_file. Then while available:
	- takes next BAGMutatiesFile from table bag_mutaties_file with status = 0
	- imports BAGMutatiesFile into table bag_mutatie
	- exports BAGMutaties from table bag_mutatie to bag tables

> java -cp bag-etl-mail-1.0.0-jar-with-dependencies.jar nl.ordina.bag.etl.mail.LoadMutaties
