pipeline {
  agent any
  options { timestamps() }

  environment {
    AWS_REGION = 'us-east-1'
    ACCOUNT_ID = '248928952946'
    ECR_REPO   = 'userprofile'
    IMAGE_TAG  = "${env.GIT_COMMIT?.take(7) ?: env.BUILD_NUMBER}"
    ECR_URI    = "${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO}"
    BIN        = '/var/jenkins_home/bin'     // persists via EFS
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build & Test') {
      steps {
        retry(2) {
          sh '''
            bash -lc '
              set -euo pipefail
              export GRADLE_USER_HOME="${WORKSPACE}/.gradle"
              chmod +x ./gradlew || true
              ./gradlew clean test --no-daemon --max-workers=1
            '
          '''
        }
      }
      post {
        always {
          junit testResults: 'build/test-results/test/*.xml',
                allowEmptyResults: true,
                keepLongStdio: true
        }
      }
    }

    stage('Assemble Jar') {
      steps {
        sh '''
          bash -lc '
            set -euo pipefail
            ./gradlew bootJar --no-daemon
          '
        '''
      }
    }

    stage('Bootstrap tools (aws+docker CLI, git, jq)') {
      steps {
        sh '''
          bash -lc '
            set -euo pipefail
            mkdir -p "${BIN}"
            if ! "${BIN}/docker" --version >/dev/null 2>&1 && ! command -v docker >/dev/null 2>&1; then
              VER="26.1.3"
              curl -fsSL "https://download.docker.com/linux/static/stable/x86_64/docker-${VER}.tgz" -o /tmp/docker.tgz
              tar -xzf /tmp/docker.tgz -C /tmp
              install -m0755 /tmp/docker/docker "${BIN}/docker"
            fi
            if ! "${BIN}/aws" --version >/dev/null 2>&1 && ! command -v aws >/dev/null 2>&1; then
              curl -fsSL "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o /tmp/awscliv2.zip
              apt-get update -y || true; apt-get install -y unzip ca-certificates || true
              unzip -q -o /tmp/awscliv2.zip -d /tmp
              /tmp/aws/install -i /var/jenkins_home/.aws-cli -b "${BIN}"
            fi
            apt-get update -y || true
            apt-get install -y git jq || true
            export PATH="${BIN}:$PATH"
            aws --version; docker --version; git --version; jq --version
          '
        '''
      }
    }

    stage('Docker build & push to ECR') {
      steps {
        sh '''
          bash -lc '
            set -euo pipefail
            export PATH="${BIN}:$PATH"
            aws ecr describe-repositories --repository-names "${ECR_REPO}" --region "${AWS_REGION}" \
              || aws ecr create-repository --repository-name "${ECR_REPO}" --region "${AWS_REGION}"
            aws ecr get-login-password --region "${AWS_REGION}" \
              | docker login --username AWS --password-stdin "${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
            docker build --platform=linux/amd64 -t "${ECR_REPO}:${IMAGE_TAG}" .
            docker tag "${ECR_REPO}:${IMAGE_TAG}" "${ECR_URI}:${IMAGE_TAG}"
            docker tag "${ECR_REPO}:${IMAGE_TAG}" "${ECR_URI}:latest"
            docker push "${ECR_URI}:${IMAGE_TAG}"
            docker push "${ECR_URI}:latest"
          '
        '''
      }
    }

    stage('Deploy to ECS (register new task-def revision)') {
      steps {
        sh '''
          bash -lc '
            set -euo pipefail
            export PATH="${BIN}:$PATH"
            FAMILY="userprofile-task"; CLUSTER="userprofile-cluster"; SERVICE="userprofile-service"
            TD=$(aws ecs describe-task-definition --task-definition "$FAMILY" --region "$AWS_REGION" \
                 --query "taskDefinition" --output json)
            NEW=$(echo "$TD" | jq --arg IMG "${ECR_URI}:${IMAGE_TAG}" \
              '\''del(.taskDefinitionArn, .revision, .status, .requiresAttributes, .compatibilities, .registeredAt, .registeredBy)
                | (.containerDefinitions[] | select(.name=="user-app")).image = $IMG'\'')
            echo "$NEW" > /tmp/td.json
            REV_ARN=$(aws ecs register-task-definition --cli-input-json file:///tmp/td.json --region "$AWS_REGION" \
                      --query "taskDefinition.taskDefinitionArn" --output text)
            aws ecs update-service --cluster "$CLUSTER" --service "$SERVICE" \
              --task-definition "$REV_ARN" --region "$AWS_REGION"
          '
        '''
      }
    }

    stage('Smoke test (auto-discover host)') {
      steps {
        sh '''
          bash -lc '
            set -euo pipefail
            export PATH="${BIN}:$PATH"
            CLUSTER="userprofile-cluster"; SERVICE="userprofile-service"; PATH_SUFFIX="/actuator/health"
            TASK_ARN=$(aws ecs list-tasks --cluster "$CLUSTER" --service-name "$SERVICE" \
                       --desired-status RUNNING --region "$AWS_REGION" \
                       --query "taskArns[0]" --output text)
            [ -z "$TASK_ARN" ] || [ "$TASK_ARN" = "None" ] && { echo "No RUNNING task"; exit 1; }
            DESC=$(aws ecs describe-tasks --cluster "$CLUSTER" --tasks "$TASK_ARN" --region "$AWS_REGION")
            CI_ARN=$(echo "$DESC" | jq -r ".tasks[0].containerInstanceArn")
            HOST_PORT=$(echo "$DESC" | jq -r "[.tasks[0].containers[0].networkBindings[]?.hostPort] | first // 8080")
            EC2_ID=$(aws ecs describe-container-instances --cluster "$CLUSTER" --container-instances "$CI_ARN" \
                     --region "$AWS_REGION" --query "containerInstances[0].ec2InstanceId" --output text)
            PUBLIC_IP=$(aws ec2 describe-instances --instance-ids "$EC2_ID" --region "$AWS_REGION" \
                       --query "Reservations[0].Instances[0].PublicIpAddress" --output text)
            [ -z "$PUBLIC_IP" ] || [ "$PUBLIC_IP" = "None" ] && { echo "No public IP"; exit 1; }
            echo "Smoke-testing http://$PUBLIC_IP:$HOST_PORT$PATH_SUFFIX"
            for i in {1..12}; do curl -fsS "http://$PUBLIC_IP:$HOST_PORT$PATH_SUFFIX" && exit 0 || sleep 5; done
            echo "Smoke test failed"; exit 1
          '
        '''
      }
    }
  }
}