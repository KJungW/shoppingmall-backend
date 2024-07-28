#!/bin/bash

PROFILE_NAME="test-profile"
AWS_ACCESS_KEY_ID="testAccessKey"
AWS_SECRET_ACCESS_KEY="testSecretKey"
AWS_DEFAULT_REGION="ap-northeast-2"
OUTPUT_FORMAT="json"

mkdir -p ~/.aws

if grep -q "\[${PROFILE_NAME}\]" ~/.aws/credentials; then
    echo "프로필 ${PROFILE_NAME}이(가) 이미 존재합니다."
else
    echo "[${PROFILE_NAME}]" >> ~/.aws/credentials
    echo "aws_access_key_id = ${AWS_ACCESS_KEY_ID}" >> ~/.aws/credentials
    echo "aws_secret_access_key = ${AWS_SECRET_ACCESS_KEY}" >> ~/.aws/credentials
fi

if grep -q "\[profile ${PROFILE_NAME}\]" ~/.aws/config; then
    echo "프로필 ${PROFILE_NAME}의 config가 이미 존재합니다."
else
    echo "[profile ${PROFILE_NAME}]" >> ~/.aws/config
    echo "region = ${AWS_DEFAULT_REGION}" >> ~/.aws/config
    echo "output = ${OUTPUT_FORMAT}" >> ~/.aws/config
fi

echo "프로필 ${PROFILE_NAME}이(가) 설정되었습니다."

export AWS_PROFILE=${PROFILE_NAME}
echo "프로필 ${PROFILE_NAME}이(가) 설정되었습니다."
echo "현재 AWS_PROFILE 환경 변수 값: ${AWS_PROFILE}"

aws s3 mb s3://test-bucket --endpoint-url http://localhost:4566
aws s3 ls --endpoint-url=http://localhost:4566/ --recursive --human-readable