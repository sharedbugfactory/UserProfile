pipeline {
  agent any
  environment {
    AWS_REGION = 'us-east-1'
    ECR_URI    = '<ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/userprofile'
  }
  options { timestamps() }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build & Test') {
      steps { sh './gradlew clean test' }
      post { always { junit 'build/test-results/test/*.xml' } }
    }

    stage('Assemble Jar') {
      steps { sh './gradlew bootJar' }
    }

    stage('Setup tools (awscli + docker CLI if missing)') {
      steps {
        sh '''
          set -eux
          if ! command -v aws >/dev/null 2>&1; then
            apt-get update && apt-get install -y awscli
          fi
          if ! command -v docker >/dev/null 2>&1; then
            apt-get update && apt-get install -y docker.io
          fi
          aws --version
          docker --version
        '''
      }
    }

    stage('Docker build & push to ECR') {
      steps {
        sh '''
          set -eux
          # Get the AWS account ID (uses Task Role creds)
          ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
          aws ecr get-login-password --region ${AWS_REGION} \
            | docker login --username AWS --password-stdin ${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

          # Build image from repo root (Dockerfile expects build/libs/*.jar)
          docker build -t userprofile:${BUILD_NUMBER} .

          # Tag and push :BUILD_NUMBER and :latest
          docker tag userprofile:${BUILD_NUMBER} ${ECR_URI}:${BUILD_NUMBER}
          docker tag userprofile:${BUILD_NUMBER} ${ECR_URI}:latest

          docker push ${ECR_URI}:${BUILD_NUMBER}
          docker push ${ECR_URI}:latest
        '''
      }
    }

    stage('Trigger ECS deploy') {
      steps {
        sh '''
          set -eux
          aws ecs update-service \
            --cluster userprofile-cluster \
            --service userprofile-service \
            --force-new-deployment \
            --region ${AWS_REGION}
        '''
      }
    }

    stage('Smoke test') {
      steps {
        sh 'curl -fsS http://localhost/actuator/health || (sleep 5; curl -fsS http://localhost/actuator/health)'
      }
    }
  }
}