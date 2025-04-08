pipeline {
  agent any

  environment {
    IMAGE_NAME = "openolat"
    LATEST_TAG = "latest"
    BRANCH_NAME = "${env.BRANCH_NAME ?: 'dev'}"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Read Version from olat.properties') {
      steps {
        script {
          def version = sh(script: "grep '^build.version' src/main/resources/serviceconfig/olat.properties | cut -d'=' -f2", returnStdout: true).trim()
          env.VERSION_TAG = version
        }
      }
    }

    stage('Build Docker Image') {
      steps {
        sh "docker build -t ${IMAGE_NAME}:${VERSION_TAG} ."
      }
    }

    stage('Helm Lint & Package') {
      steps {
        sh """
          curl -s https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
          helm lint openolat
          helm package openolat
          mv *.tgz chart.tgz
        """
      }
    }
  }
}