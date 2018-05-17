#!/bin/bash

echo "Gathering Stack Name from the command line!!"

Stack_Name=$1

aws cloudformation delete-stack --stack-name $Stack_Name

aws cloudformation wait stack-delete-complete --stack-name $Stack_Name

echo "Stack deletion complete!"
