pipeline {
  agent { label 'agentA && docker && helm' }

  parameters {
    string(name: 'REGISTRY_HOST',  defaultValue: 'zot', description: 'Registry Host (e.g. zot, ghcr.io)')
    string(name: 'REGISTRY_PORT',  defaultValue: '5000', description: 'Registry Port (e.g. 5000, leave empty if not needed)')
    string(name: 'DOCKERFILE_NAME', defaultValue: 'Dockerfile', description: 'Dockerfile to use')
    string(name: 'CHART_DIR',       defaultValue: 'openolat', description: 'Helm chart directory')
    string(name: 'IMAGE_NAME',      defaultValue: 'openolat', description: 'Name of the Docker image and Helm package')
    string(name: 'CREDENTIALS_ID',  defaultValue: 'zot-local', description: 'Jenkins credentials ID for registry login')
  }

  environment {
    LATEST_TAG = "latest"
    BRANCH_NAME = "${env.BRANCH_NAME ?: 'dev'}"
    REGISTRY_URL = "${params.REGISTRY_PORT ? "${params.REGISTRY_HOST}:${params.REGISTRY_PORT}" : "${params.REGISTRY_HOST}"}"
    OCI_REGISTRY_URL = "oci://${REGISTRY_URL}/${params.IMAGE_NAME}"
  }

  stages {

    stage('Checkout Source') {
      steps {
        checkout scm
      }
    }

    stage('Extract Version') {
      steps {
        script {
          def version = sh(script: "./jenkins/get-version.sh", returnStdout: true).trim()
          env.VERSION_TAG = version
        }
      }
    }

    stage('Build Docker Image') {
      steps {
        sh "./jenkins/build-image.sh ${params.DOCKERFILE_NAME} ${params.IMAGE_NAME} ${env.VERSION_TAG}"
      }
    }

    stage('Push Docker Image to Registry') {
      steps {
        withCredentials([usernamePassword(credentialsId: "${params.CREDENTIALS_ID}", usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PASS')]) {
          sh """
            ./jenkins/docker-push.sh ${params.IMAGE_NAME} ${env.VERSION_TAG} ${env.BRANCH_NAME} ${REGISTRY_URL} \$REGISTRY_USER \$REGISTRY_PASS
          """
        }
      }
    }

    stage('Helm Package & Push') {
      steps {
        sh "./jenkins/helm-package-push.sh ${params.CHART_DIR} ${OCI_REGISTRY_URL}"
      }
    }

  }

  post {
    success {
      echo "✅ Build and push completed successfully."
    }
    failure {
      echo "❌ Build failed. Check the stage logs for errors."
    }
  }
}