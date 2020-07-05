pipeline {
  options { disableConcurrentBuilds() }
  agent { label 'docker-slave' }
  stages {
    stage ('Pull repo code from github') {
      steps {
        checkout scm
      }
    }
    stage ('Build semantic-reasoner') {
      when { 
          not { 
                triggeredBy 'UpstreamCause' 
          }
      }
      steps {
        build 'semantic-reasoner/master'
      }
    }
	stage ('Build refactoring-option-discoverer') {
      steps {
        sh  """ #!/bin/bash
                mvn clean install
            """
        archiveArtifacts artifacts: '**/*.war, **/*.jar', onlyIfSuccessful: true
      }
    }
	stage('Build docker images') {
            steps {
                sh "cd docker build -t refactoring_option_discoverer -f Dockerfile ."                
            }
    }   
    stage('Push Dockerfile to DockerHub') {
            when {
               branch "master"
            }
            steps {
                withDockerRegistry(credentialsId: 'jenkins-sodalite.docker_token', url: '') {
                    sh  """#!/bin/bash                       
                            docker tag refactoring_option_discoverer sodaliteh2020/refactoring_option_discoverer:${BUILD_NUMBER}
                            docker tag refactoring_option_discoverer sodaliteh2020/refactoring_option_discoverer
                            docker push sodaliteh2020/refactoring_option_discoverer:${BUILD_NUMBER}
                            docker push sodaliteh2020/refactoring_option_discoverer
                        """
                }
            }
    }
  }
  post {
    failure {
        slackSend (color: '#FF0000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
    }
    fixed {
        slackSend (color: '#6d3be3', message: "FIXED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})") 
    }
  }
}
