# Application Load Balancer
resource "aws_lb" "main" {
  name               = "${var.project_name}-${var.environment}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = aws_subnet.public[*].id

  enable_deletion_protection = false
  enable_http2               = true

  tags = {
    Name = "${var.project_name}-${var.environment}-alb"
  }
}

# Target Group (EC2 인스턴스 기반)
resource "aws_lb_target_group" "blue" {
  name        = "${var.project_name}-${var.environment}-tg"
  port        = var.ecs_container_port
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "instance"

  health_check {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    path                = var.ecs_health_check_path
    matcher             = "200"
  }

  deregistration_delay = 30

  lifecycle {
    create_before_destroy = true
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-blue-tg"
  }
}

# ALB Listener - HTTP (redirect to HTTPS)
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type = "redirect"

    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

# ALB Listener - HTTPS
resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.main.arn
  port              = "443"
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  certificate_arn   = aws_acm_certificate.main.arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.blue.arn
  }

  depends_on = [aws_acm_certificate_validation.main]
}

# ALB Listener Rule - Invite Link Fallback
resource "aws_lb_listener_rule" "invite_redirect" {
  listener_arn = aws_lb_listener.https.arn
  priority     = 10

  action {
    type = "redirect"

    redirect {
      protocol    = "HTTPS"
      host        = "keepiluv.framer.website"
      port        = "443"
      path        = "/"
      query       = ""
      status_code = "HTTP_302"
    }
  }

  condition {
    path_pattern {
      values = ["/invite", "/invite/*"]
    }
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-invite-redirect"
  }
}

# ALB Listener Rule - Apple App Site Association
resource "aws_lb_listener_rule" "apple_app_site_association" {
  listener_arn = aws_lb_listener.https.arn
  priority     = 5

  action {
    type = "fixed-response"

    fixed_response {
      content_type = "application/json"
      status_code  = "200"
      message_body = jsonencode({
        applinks = {
          apps = []
          details = [{
            appID = "VZC79KP79S.org.yapp.twix"
            paths = ["/invite", "/invite/*"]
          }]
        }
      })
    }
  }

  condition {
    path_pattern {
      values = ["/.well-known/apple-app-site-association"]
    }
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-aasa"
  }
}