	Update README.md	2 days ago
 README.md
CSYE 6225 Spring 2018 Starter Repository
Clone the repository in your local machine

Webapp:

Import the webapp folder to an IDE that supports Gradle builds. Eg: IntelliJ Idea
Set up Apache Tomcat Server to run the code. https://www.jetbrains.com/help/idea/defining-application-servers-in-intellij-idea.html
Run the code.
Build:

Travis CI is used to build the gradle project: https://travis-ci.com/ranadeepguha/csye6225-spring2018

JMeter:

Install and open Apache JMeter
Navigate to the jmeter folder
Run the jmx scripts
Infrastructure:

Using CloudFormation:

Navigate to the infrastructure/aws/cloudformation folder
Open your terminal (and navigate to infrastructure/aws/cloudformation)
Enter ./csye6225-aws-cf-create-stack.sh
Navigate to the AWS Console and verify that a VPC, Internet Gateway and a Route Table with the IGW attached to it
To terminate the stack enter ./csye6225-aws-cf-terminate-stack.sh
Using AWS CLI:

Navigate to the infrastructure/aws/scripts folder
Open your terminal (and navigate to infrastructure/aws/scripts)
Enter ./csye6225-aws-networking-setup.sh
Navigate to the AWS Console and verify that a VPC, Internet Gateway and a Route Table with the IGW attached to it
To terminate the stack enter ./csye6225-aws-networking-teardown
