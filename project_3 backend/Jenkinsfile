pipeline {
    agent any
     stages {
        stage('build') {
            steps {
                sh 'ls'
                dir ('gbt'){
                sh './gradlew clean build'
                }
             }
        }
        stage('upload') {
            steps {
                sh 'aws s3 cp gbt/build/libs/application.war s3://gbt-elasticbean-back-um/application.war --region us-west-1'
            }
        }
        stage('deploy') {
            steps {
                 sh 'aws elasticbeanstalk create-application-version --region us-west-1 --application-name GBT-Finalspringboot --version-label ${BUILD_TAG} --source-bundle S3Bucket="gbt-elasticbean-back-um",S3Key="application.war"'
                 sh 'aws elasticbeanstalk update-environment --region us-west-1 --environment-name Gbtfinalspringboot-env --version-label ${BUILD_TAG}'
            }
        }
    }
}