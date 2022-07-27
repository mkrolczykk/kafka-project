### **_Integration tests_** for project modules <br /> <br />

Module structure:
```
├── modules-tests
│   ├── src
│   │   └── main
│   │       ├── java
│   │       │   └── com
│   │       │       └── github
│   │       │           └── mkrolczyk12
│   │       │               └── kafka
│   │       │                   └── metrics
│   │       │                       ├── E2EApiMockServer.java       # API mock server client
│   │       │                       ├── E2EMetricsBuilder.java      # tests metrics builder
│   │       │                       ├── E2EMetricsIT.java           # e2e tests
│   │       │                       └── E2EProperties.java          # tests properties service
│   │       ├── resources
│   │       │   ├── config.test.properties      # tests properties (Do not remove any property!)
│   │       │   └── docker-compose.yml          # mock docker environment for ksql e2e tests
```