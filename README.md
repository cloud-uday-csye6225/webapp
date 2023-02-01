# Cloud Web Application
This Project is built using Spring boot, Java, MySQL.

## Requirements
For building and running the application you need:
 - JDK 19
 - maven 3.8.6
 - mysql 8.0.32
 - BCrypt 0.3
 - JUnit 4
 - Mockito

## Steps to Build, Run & Test 
 
  - Clone the repository it using ssh.
  - Run the below two queries to set up database. 
  ``` 
  CREATE DATABASE cbdh;
  USE cbdh;
   ```
  - mvn clean install && mvn spring-boot:run
  -  PORT :  http://localhost:8080
  - For Testing -   mvn -Dtest=CloudAppApplicationTests test

  - or by using any IDE, you can build & run the project in any IDE.

## Github Actions
Added branch protection by preventing merge if any workflow fails.
Added unit test cases to the workflow to make sure.

## API Endpoints Curls

### Create
```
curl --location --request POST 'http://localhost:8080/v1/user' \
--header 'Content-Type: application/json' \
--header 'Cookie: JSESSIONID=60495B2988F7210B7555D0445DC94525' \
--data-raw '{
  "first_name": "hjasc dsv",
  "last_name": "Doe",
  "password": "b",
  "username": "b@gmail.com"
}'

```

### Fetch
```
curl --location --request GET 'http://localhost:8080/v1/user/4' \
--header 'Authorization: Basic YUBnbWFpbC5jb206YWFh' \
--header 'Cookie: JSESSIONID=60495B2988F7210B7555D0445DC94525'
```

### Update
```
curl --location --request PUT 'http://localhost:8080/v1/user/4' \
--header 'Authorization: Basic YUBnbWFpbC5jb206YWFh' \
--header 'Content-Type: application/json' \
--header 'Cookie: JSESSIONID=60495B2988F7210B7555D0445DC94525' \
--data-raw '{
  "first_name": "hi fsdv",
  "last_name": "aaaa",
  "password" : "aaa"
}'
```

### Healthz
```
curl --location --request GET 'http://localhost:8080/healthz' \
--header 'Cookie: JSESSIONID=60495B2988F7210B7555D0445DC94525'
```

### Personal Details
 - Name : Uday Kiran Kolluru
 - NUID : 002738927