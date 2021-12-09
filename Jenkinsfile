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
                mvn  clean install -Ddefault.min.distinct.threshold=104857600
            """
      }
    }
	stage('SonarQube analysis'){
        environment {
          scannerHome = tool 'SonarQubeScanner'
        }
        steps {
            withSonarQubeEnv('SonarCloud') {
                sh  """ #!/bin/bash                       
                        ${scannerHome}/bin/sonar-scanner
                    """
            }
        }
    }
	stage('Build docker images') {
		    when {
				allOf {
				// Triggered on every tag
					expression{tag "*"}
				   }
            } 
            steps {
                sh "docker build -t refactoring_option_discoverer -f Dockerfile ."                
            }
    }   
    stage('Push Dockerfile to DockerHub') {
		    when {
				allOf {
				// Triggered on every tag
					expression{tag "*"}
				   }
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
