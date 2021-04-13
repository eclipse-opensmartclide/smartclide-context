FROM gitlab.atb-bremen.de:5555/atb/alpine-openjdk-8-jre:0.0.12


ADD indexes /indexes
ADD resources /resources
ADD original-context-monitoring-1.1.6-shaded.jar context-monitoring-1.1.6-shaded.jar

EXPOSE 1000-9000

CMD java -Dserver.port=8090 -jar context-monitoring-1.1.6-shaded.jar
