pipeline {
  agent any

  environment {
     PRIVATE_KEY = credentials('rsa-private-key')
     PUBLIC_KEY = credentials('rsa-public-key')

     ACCESS_PRIVATE_KEY = 'file:${PRIVATE_KEY}'
     ACCESS_PUBLIC_KEY = 'file:${PUBLIC_KEY}'
     REFRESH_PRIVATE_KEY = 'file:${PRIVATE_KEY}'
     REFRESH_PUBLIC_KEY = 'file:${PUBLIC_KEY}'
  }

  stages {
    stage('test') {
      steps {
        sh 'sh ./gradlew test'
      }
    }
  }
}