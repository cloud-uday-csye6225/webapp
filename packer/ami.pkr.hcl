variable "aws_access_key" {
  type    = string
  default = env("AWS_DEV_ACCESS_KEY")
}

variable "aws_region" {
  type    = string
  default = env("AWS_REGION")
}

variable "aws_secret_key" {
  type    = string
  default = env("AWS_DEV_SECRET_KEY")
}

variable "demo_account_id" {
  type    = list(string)
  default = ["986054102018", "162585530017"]
}

variable "source_ami" {
  type    = string
  default = "ami-0dfcb1ef8550277af"
}

variable "ssh_username" {
  type    = string
  default = "ec2-user"
}

variable "subnet_id" {
  type    = string
  default = env("AWS_DEV_SUBNET_ID")
}

# "timestamp" template function replacement
locals { timestamp = regex_replace(timestamp(), "[- TZ:]", "") }

source "amazon-ebs" "aws-ebs" {
  access_key      = "${var.aws_access_key}"
  ami_description = "AWS Linux AMI for CSYE 6225"
  ami_name        = "csye6225_${local.timestamp}"
  ami_users       = "${var.demo_account_id}"
  instance_type   = "t2.micro"
  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/xvda"
    volume_size           = 8
    volume_type           = "gp2"
  }
  region       = "${var.aws_region}"
  secret_key   = "${var.aws_secret_key}"
  source_ami   = "${var.source_ami}"
  ssh_username = "${var.ssh_username}"
  subnet_id    = "${var.subnet_id}"
}

build {
  sources = ["source.amazon-ebs.aws-ebs"]

  provisioner "file" {
    destination = "/home/ec2-user/"
    source      = "./../target/cloud-app-0.0.1-SNAPSHOT.jar"
  }

  provisioner "file" {
    destination = "/tmp/"
    sources     = ["webservice.service", "config.json"]
  }

  provisioner "shell" {
    script = "script.sh"
  }

}
