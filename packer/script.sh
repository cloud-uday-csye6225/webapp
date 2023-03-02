#!/bin/bash
sudo yum update
sudo yum upgrade
sudo amazon-linux-extras install -y nginx1
echo Start Java Installation
sudo yum install java-17-amazon-corretto -y
echo "export JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto.x86_64" >>~/.bashrc
echo "export PATH=$PATH:$JAVA_HOME/bin" >>~/.bashrc
echo Java Location
java --version
sudo yum install maven -y
echo completed Java Installation
sudo yum install -y tomcat - y
sudo systemctl start tomcat
sudo systemctl enable tomcat
# sudo amazon-linux-extras install -y epel
# sudo yum install -y https://dev.mysql.com/get/mysql80-community-release-el7-5.noarch.rpm
# sudo yum install -y mysql-community-server
# sudo systemctl start mysqld
# sudo systemctl enable mysqld
# passwords=$(sudo grep 'temporary password' /var/log/mysqld.log | awk {'print $13'})
# mysql -uroot -p$passwords --connect-expired-password -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root@1234ABCDEF';"
# mysql -u root -proot@1234ABCDEF -e "create database cbdh;"
sudo chmod 770 /home/ec2-user/cloud-app-0.0.1-SNAPSHOT.jar
sudo cp /tmp/webservice.service /etc/systemd/system
sudo chmod 770 /etc/systemd/system/webservice.service
sudo systemctl start webservice.service
sudo systemctl enable webservice.service
sudo systemctl restart webservice.service
sudo systemctl status webservice.service
# echo '****** Copied webservice! *******'

