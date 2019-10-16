# Sypht Kotlin Client
This repository is a Kotlin reference client implementation for working with the Sypht API. [![Docs](https://img.shields.io/badge/API%20Docs-site-lightgrey.svg?style=flat-square)](https://docs.sypht.com)

## About Sypht
[Sypht](https://sypht.com) is a SaaS [API]((https://docs.sypht.com/)) which extracts key fields from documents. For
example, you can upload an image or pdf of a bill or invoice and extract the amount due, due date, invoice number
and biller information.

## Getting started
To get started you'll need API credentials, i.e. a `<client_id>` and `<client_secret>`, which can be obtained by registering
for an [account](https://www.sypht.com/signup/developer)

## Prerequisites
- Install Java 8 JDK or upward

```Bash
brew tap AdoptOpenJDK/openjdk
brew cask install adoptopenjdk8
```
- Install [Kotlin](https://kotlinlang.org)

```Bash
brew install kotlin
```

## Installation
Sypht Kotlin Client is available on maven central

### Maven
```Xml
<dependency>
  <groupId>com.sypht</groupId>
  <artifactId>sypht-kotlin-client</artifactId>
  <version>1.0</version>
</dependency>
```

### Gradle
```Gradle
// https://mvnrepository.com/artifact/com.sypht/sypht-kotlin-client
compile group: 'com.sypht', name: 'sypht-kotlin-client', version: '1.0'
```

## Usage
Populate these system environment variables with the credentials generated above:

```Bash
SYPHT_API_KEY="<client_id>:<client_secret>"
```

or

```Bash
OAUTH_CLIENT_ID="<client_id>"
OAUTH_CLIENT_SECRET="<client_secret>"
```
You can also set the Http Request Timeout using the optional property:
```Bash
REQUEST_TIMEOUT="<value_in_seconds>"
```

then invoke the client with a file of your choice:
```Kotlin
val client = SyphtClient()
        println(
                client.result(
                        client.upload(
                                File("receipt.pdf"))))
```
## Testing
Open pom.xml and add the below line inside `<environmentVariables> </environmentVariables>` with the credentials generated above:
```xml
<OAUTH_CLIENT_ID>client_id</OAUTH_CLIENT_ID>
<OAUTH_CLIENT_SECRET>client_secret</OAUTH_CLIENT_SECRET>
<SYPHT_API_KEY>client_id:client_secret</SYPHT_API_KEY>
```
then run 
```Bash
mvn test
```

## License
The software in this repository is available as open source under the terms of the [Apache License](https://github.com/sypht-team/sypht-kotlin-client/blob/master/LICENSE).

## Code of Conduct
Everyone interacting in the project’s codebases, issue trackers, chat rooms and mailing lists is expected to follow the [code of conduct](https://github.com/sypht-team/sypht-kotlin-client/blob/master/CODE_OF_CONDUCT.md).
