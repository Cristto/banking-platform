pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                dir('backend') {
                    sh 'sh ./mvnw clean package -DskipTests'
                }
            }
        }
    }
}