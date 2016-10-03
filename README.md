# JBilling API Samples

## Introduction

This project contains several small examples that demonstrate how to use jBilling's API client library and how to interact with JBilling API.

The API examples shown here are developed and tested against **JBilling Enterprise** version. This project will have many releases. Each release will be tagged with a version number. The version number will match the version number of JBilling Enterprise version that was used to create and test the examples. So, look for your appropriate (tag) version number according to the enterprise version number that you are using.

More detailed explanation and documentation for the API is available in JBilling's **Integration Guide** and **API Reference Guide**. Contact JBilling for a copy.

## Prerequisites

- Java 8
- Maven 2+
- jBilling API Client JAR

## Getting Started

1. Clone this project locally
2. Acquire JBilling API jar from JBilling with correct version
3. Drop the client api jar into **lib** folder
4. Go to **pom.xml** and configure the correct dependency towards the JBilling API Client jar.
5. Compile the project with **mvn compile**

## Configuration

In case you want to try out the examples against a running JBilling application or a cloud instance check the file **resources/jbilling-remote-beans.xml**. It contains Spring beans that enable the application to connect to a remote server. This beans take care of the communication protocol and authentication. Find more information about these beans in the jBilling's **Integration Guide**. 
