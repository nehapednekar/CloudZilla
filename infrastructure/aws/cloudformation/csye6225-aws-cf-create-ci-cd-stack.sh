#!/bin/bash

echo "Gathering Stack Name from the command line!!"

Stack_Name=$1
CodeDeployEC2S3PolicyName="CodeDeploy-EC2-S3"
TravisUploadToS3PolicyName="Travis-Upload-To-S3"
TravisCodeDeployPolicyName="Travis-Code-Deploy"
cloudwatchpolicy="Cloud-watch-policy"
snspolicy="My-SNS-Policy"
CodeDeployEC2ServiceRoleName="CodeDeployEC2ServiceRole"
CodeDeployServiceRoleName="CodeDeployServiceRole"
CodeDeployS3BucketName="" #S3 Bucket Name for code deploy
TravisUser="Travis"
CodeDeployApplicationName="CodeDeployApplication"
AWSRegion="us-east-1"
AWSAccountID="" #AWS Account ID

echo "Provided Stack name is: ${Stack_Name}"

echo "Starting creation of the stack!!"

aws cloudformation create-stack --stack-name $Stack_Name --capabilities "CAPABILITY_NAMED_IAM" --template-body file://csye6225-cf-ci-cd.json --parameters ParameterKey=snspolicy,ParameterValue=$snspolicy ParameterKey=CodeDeployEC2S3PolicyName,ParameterValue=$CodeDeployEC2S3PolicyName ParameterKey=TravisUploadToS3PolicyName,ParameterValue=$TravisUploadToS3PolicyName ParameterKey=TravisCodeDeployPolicyName,ParameterValue=$TravisCodeDeployPolicyName ParameterKey=CodeDeployEC2ServiceRoleName,ParameterValue=$CodeDeployEC2ServiceRoleName ParameterKey=CodeDeployServiceRoleName,ParameterValue=$CodeDeployServiceRoleName ParameterKey=CodeDeployS3BucketName,ParameterValue=$CodeDeployS3BucketName ParameterKey=TravisUser,ParameterValue=$TravisUser ParameterKey=CodeDeployApplicationName,ParameterValue=$CodeDeployApplicationName ParameterKey=AWSRegion,ParameterValue=$AWSRegion ParameterKey=AWSAccountID,ParameterValue=$AWSAccountID ParameterKey=cloudwatchpolicy,ParameterValue=$cloudwatchpolicy

aws cloudformation wait stack-create-complete --stack-name $Stack_Name

if [ $? -eq 0 ]; then

    echo "Stack created successfully!!"

else

    echo "Unable to create stack. Please input correct name!!"

fi
