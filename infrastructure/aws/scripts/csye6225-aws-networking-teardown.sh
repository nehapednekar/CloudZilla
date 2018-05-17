#!/bin/bash

echo "Gathering Stack Name from the command line!!"

Stack_Name=$1
VPC_Name="${Stack_Name}-csye6225-vpc"
InternetGateway_Name="${Stack_Name}-csye6225-InternetGateway"
RouteTable_Name="${Stack_Name}-csye6225-public-route-table"


echo "Provided Stack name is: ${Stack_Name}"
echo "Desired VPC name is: ${VPC_Name}"
echo "Desired Internet Gateway name is: $InternetGateway_Name"
echo "Desired Route Table name is: $RouteTable_Name"


echo "Deleteting internet gateway first !!"

export internetGatewayId=$(aws ec2 describe-internet-gateways --query 'InternetGateways[*].[InternetGatewayId, Tags[0].Value]' --output text|grep $InternetGateway_Name|awk '{print $1}')

export routeTableId=$(aws ec2 describe-route-tables --query 'RouteTables[*].[RouteTableId, Tags[0].Value]' --output text| grep $RouteTable_Name | awk '{print $1}')

export VpcId=$(aws ec2 describe-vpcs --query 'Vpcs[*].[VpcId, Tags[0].Value]' --output text | grep $VPC_Name | awk '{print $1}')

aws ec2 detach-internet-gateway --internet-gateway-id $internetGatewayId --vpc-id $VpcId

aws ec2 delete-internet-gateway --internet-gateway-id $internetGatewayId

if [ $? -eq 0 ]; then

    echo "Internet gateway deleted"

else 

    echo "Unable to delete"

fi

aws ec2 delete-route-table --route-table-id $routeTableId

if [ $? -eq 0 ]; then
    echo "Route table deleted"

else 

    echo "unable to delete route table"

fi
echo "deleting VPC"


aws ec2 delete-vpc --vpc-id $VpcId


if [ $? -eq 0 ]; then

    echo "vpc deleted"

else 

    echo "unable to delete"

fi