### Module includes docker config files and containers volumes data. <br /> <br />

To init the whole environment run:
```
$ docker-compose up
```

All ports are exposed outside of docker network. Check 'docker-compose.yml' file for more information. <br /> <br />

To run Kafka commands from outside of docker network use 'docker exec' command. <br /> <br />
Example:
```
$ docker exec -it <broker_name> kafka-console-consumer --bootstrap-server <broker_name>:<port> --topic <topic_name>
```