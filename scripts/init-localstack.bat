@echo off
echo Initializing LocalStack S3...

set AWS_ACCESS_KEY_ID=test
set AWS_SECRET_ACCESS_KEY=test
set AWS_DEFAULT_REGION=us-east-1

echo Waiting for LocalStack to start...
timeout /t 5 /nobreak > nul

echo Creating bucket 'product-images'...
aws --endpoint-url=http://localhost:4566 s3 mb s3://product-images 2>nul
if %errorlevel% equ 0 (
    echo Bucket created successfully
) else (
    echo Bucket already exists or creation skipped
)

echo Listing buckets...
aws --endpoint-url=http://localhost:4566 s3 ls

echo.
echo LocalStack S3 ready!
echo.
echo You can now start your application with:
echo   mvnw spring-boot:run -Dspring-boot.run.profiles=localstack
