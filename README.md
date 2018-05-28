Summary:

A cloud native portfolio application which uses Spring Boot and Amazon Web Services like EC2, S3, Lambda, Simple Notification Service, Simple Email Service, Route 53, DynamoDB, CodeDeploy, Auto Scaling, Load Balancing and RDS etc.


Features:

1. Public portfolio Application which lets user upload images and about me on the website
2. Entire infrastructure is maintained using CloudFormation
3. Springboot application with MySQL Database on RDS for data persistence and S3 for image persistence
4. Enabled Auto-scaling and Load Balancing and integrated with Route 53
5. Implementation of CI/CD Pipeline using Travis, S3 and CodeDeploy
6. Implementation of Forgot Password requests using SNS, Lambda, SES and DynamoDB 
7. Enables user to search another user's details without having to sign in
8. Unit-test scripts using JMeter



How to run the application?

Clone the repository in your local machine

Webapp:

1. Import the webapp folder to an IDE that supports Gradle builds. Eg: IntelliJ Idea
2. Set up Apache Tomcat Server to run the code. https://www.jetbrains.com/help/idea/defining-application-servers-in-intellij-idea.html
3. Run the code.


Build:

Travis CI is used to build the gradle project:
https://travis-ci.com/ranadeepguha/csye6225-spring2018


JMeter:

1. Install and open Apache JMeter
2. Navigate to the jmeter folder
3. Run the jmx scripts



Infrastructure:

Using CloudFormation:
1. Navigate to the infrastructure/aws/cloudformation folder
2. Open your terminal (and navigate to infrastructure/aws/cloudformation)
3. Enter ./csye6225-aws-cf-create-stack.sh <Stack Name>
4. Navigate to the AWS Console and verify that a VPC, Internet Gateway and a Route Table with the IGW attached to it
5. To terminate the stack enter ./csye6225-aws-cf-terminate-stack.sh <Stack Name>


Using AWS CLI:

1. Navigate to the infrastructure/aws/scripts folder
2. Open your terminal (and navigate to infrastructure/aws/scripts)
3. Enter ./csye6225-aws-networking-setup.sh <VPC Name>
4. Navigate to the AWS Console and verify that a VPC, Internet Gateway and a Route Table with the IGW attached to it
5. To terminate the stack enter ./csye6225-aws-networking-teardown <VPC Name>
